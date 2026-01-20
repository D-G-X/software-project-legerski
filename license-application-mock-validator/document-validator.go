package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"os"
	"strconv"
	"strings"
	"sync"
	"time"
)

const verifiedProbability = 100 // percentage chance of VERIFIED status
const maxFileSize = 10         // per file in MB
const maxPollRequests = 2      // max polling requests per second per client
const timeout = 5              // seconds

var processDuration = []time.Duration{
	1 * time.Second,
	2 * time.Second,
}

var rejectionReasons = []string{
	"Document is corrupted or unreadable",
	"Document is expired",
	"Missing required information on the document",
	"Document resolution too low for validation",
	"Document appears altered or forged",
	"Personal data does not match the application",
	"Document is incomplete or contains blank pages",
	"Upload contains multiple files but only a single document is allowed",
	"Page orientation or layout prevents automated scanning",
}

const callbackURL = "http://license-application-backend:8080/validation-callback" // backend callback endpoint

// Login credentials for keycloak
const loginURL = "http://license-application-backend:8080/login"
const loginEmail = "documentvalidator@xx.xx"
const loginPassword = "securepassword123"

var httpClient = &http.Client{
	Timeout: timeout * time.Second,
}

type ProcessDocumentResponse struct {
	ApplicationId   int     `json:"application_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

type CallbackPayload struct {
	ApplicationId    int     `json:"application_id"`
	IdFilename       string  `json:"id_filename"`
	PropertyFilename string  `json:"proof_filename"`
	Status           string  `json:"status"`
	RejectionReason  *string `json:"rejection_reason,omitempty"`
}

type JobInfo struct {
	ApplicationId    int
	IdFilename       string
	PropertyFilename string
	Status           string
}

var (
	jobStore    = make(map[int]JobInfo)
	reasonStore = make(map[int]string)
	mu          sync.Mutex
)

type RateInfo struct {
	Count     int
	ResetTime time.Time
}

var rateStore = make(map[int]*RateInfo)

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	AccessToken      string `json:"access_token"`
	RefreshToken     string `json:"refresh_token"`
	ExpiresIn        int    `json:"expires_in"`
	RefreshExpiresIn int    `json:"refresh_expires_in"`
	TokenType        string `json:"token_type"`
}

func main() {
	rand.New(rand.NewSource(time.Now().UnixNano()))

	http.HandleFunc("/process-document", loggingMiddleware(startProcessingHandler))
	http.HandleFunc("/process-document/status/", loggingMiddleware(statusHandler))

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

type statusRecorder struct {
	http.ResponseWriter
	status int
	bytes  int
}

func (r *statusRecorder) WriteHeader(code int) {
	r.status = code
	r.ResponseWriter.WriteHeader(code)
}

func (r *statusRecorder) Write(b []byte) (int, error) {
	if r.status == 0 {
		r.status = http.StatusOK
	}
	n, err := r.ResponseWriter.Write(b)
	r.bytes += n
	return n, err
}

func loggingMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		rec := &statusRecorder{ResponseWriter: w}

		next(rec, r)

		dur := time.Since(start)
		log.Printf("%s %s -> %d (%dB) in %s", r.Method, r.URL.Path, rec.status, rec.bytes, dur)
	}
}

func isPdf(data []byte) bool {
	if len(data) < 4 {
		return false
	}
	return bytes.HasPrefix(data, []byte("%PDF"))
}

func startProcessingHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeError(w, http.StatusMethodNotAllowed, "only POST allowed")
		return
	}

	r.Body = http.MaxBytesReader(w, r.Body, maxFileSize<<20)

	if err := r.ParseMultipartForm(maxFileSize << 20); err != nil {
		writeError(w, http.StatusRequestEntityTooLarge, "uploaded file too large")
		return
	}


	// err := r.ParseMultipartForm(maxFileSize << 20)
	// if err != nil {
	// 	writeError(w, http.StatusBadRequest, "invalid multipart request")
	// 	return
	// }

	idFile, idHeader, err := r.FormFile("id_file")
	if err != nil {
		reject(w, "Missing ID PDF")
		return
	}
	defer idFile.Close()

	idData, err := io.ReadAll(idFile)
	if err != nil {
		reject(w, "ID PDF unreadable")
		return
	}

	proofFile, proofHeader, err := r.FormFile("proof_file")
	if err != nil {
		reject(w, "Missing proof PDF")
		return
	}
	defer proofFile.Close()

	proofData, err := io.ReadAll(proofFile)
	if err != nil {
		reject(w, "Proof PDF unreadable")
		return
	}

	raw := r.FormValue("application_id")
	if raw == "" {
		reject(w, "Missing application ID")
		return
	}

	applicationId, err := strconv.Atoi(raw)
	if err != nil {
		reject(w, "Invalid application ID")
		return
	}

	log.Printf("POST /process-document received: application_id=%d id_file=%s proof_file=%s id_bytes=%d proof_bytes=%d",
		applicationId, idHeader.Filename, proofHeader.Filename, len(idData), len(proofData))

	if !isPdf(idData) {
		reject(w, "ID document is not a valid PDF: "+idHeader.Filename)
		return
	}
	if !isPdf(proofData) {
		reject(w, "Proof document is not a valid PDF: "+proofHeader.Filename)
		return
	}

	token, err := getAccessToken()
	if err != nil {
		log.Printf("failed to obtain access token: %v", err)
		validatorOutOfService(w)
		return
	}

	mu.Lock()
	jobStore[applicationId] = JobInfo{
		ApplicationId:    applicationId,
		IdFilename:       idHeader.Filename,
		PropertyFilename: proofHeader.Filename,
		Status:           "PENDING",
	}
	mu.Unlock()

	go runBackgroundJob(applicationId, idHeader.Filename, proofHeader.Filename, token)

	resp := ProcessDocumentResponse{
		ApplicationId: applicationId,
		Status:        "PENDING",
	}

	writeJSON(w, http.StatusOK, resp)
}

func runBackgroundJob(applicationId int, idFile, proofFile, token string) {
	log.Printf("job started: application_id=%d", applicationId)

	time.Sleep(processDuration[rand.Intn(len(processDuration))])

	status := getRandomStatus()
	var rejectionReason *string

	mu.Lock()
	jobStore[applicationId] = JobInfo{
		ApplicationId:    applicationId,
		IdFilename:       idFile,
		PropertyFilename: proofFile,
		Status:           status,
	}

	if status == "REJECTED" {
		reason := rejectionReasons[rand.Intn(len(rejectionReasons))]
		reasonStore[applicationId] = reason
		reasonCopy := reason
		rejectionReason = &reasonCopy
	}
	mu.Unlock()

	if rejectionReason != nil {
		log.Printf("job finished: application_id=%d status=%s reason=%s", applicationId, status, *rejectionReason)
	} else {
		log.Printf("job finished: application_id=%d status=%s", applicationId, status)
	}

	payload := CallbackPayload{
		ApplicationId:    applicationId,
		IdFilename:       idFile,
		PropertyFilename: proofFile,
		Status:           status,
		RejectionReason:  rejectionReason,
	}

	log.Printf("sending callback -> %s (application_id=%d status=%s)", callbackURL, applicationId, status)

	if err := sendCallback(payload, token); err != nil {
		log.Printf("callback error for application_id=%d: %v", applicationId, err)
		return
	}

	log.Printf("callback delivered (application_id=%d)", applicationId)
}

func getAccessToken() (string, error) {
	log.Printf("login request -> %s (email=%s)", loginURL, loginEmail)

	reqBody := LoginRequest{
		Email:    loginEmail,
		Password: loginPassword,
	}

	bodyBytes, err := json.Marshal(reqBody)
	if err != nil {
		return "", fmt.Errorf("marshal login request: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, loginURL, bytes.NewReader(bodyBytes))
	if err != nil {
		return "", fmt.Errorf("create login request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := httpClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("execute login request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		b, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("login returned status %s: %s", resp.Status, string(b))
	}

	var loginResp LoginResponse
	if err := json.NewDecoder(resp.Body).Decode(&loginResp); err != nil {
		return "", fmt.Errorf("decode login response: %w", err)
	}

	if loginResp.AccessToken == "" {
		return "", fmt.Errorf("login response did not contain access_token")
	}
	log.Printf("login ok (expires_in=%d)", loginResp.ExpiresIn)
	return loginResp.AccessToken, nil
}

func sendCallback(p CallbackPayload, token string) error {
	body, err := json.Marshal(p)
	if err != nil {
		return fmt.Errorf("marshal callback payload: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, callbackURL, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("create callback request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+token)

	resp, err := httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("execute callback request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		b, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("callback returned status %s: %s", resp.Status, string(b))
	}

	log.Printf("callback response: %s", resp.Status)
	return nil
}

// writeError ensures CORS headers are included for error responses
func writeError(w http.ResponseWriter, status int, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	resp := map[string]string{"error": msg}
	json.NewEncoder(w).Encode(resp)
}

func reject(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)
	resp := ProcessDocumentResponse{
		Status:          "REJECTED",
		RejectionReason: &msg,
	}
	json.NewEncoder(w).Encode(resp)
}

func validatorOutOfService(w http.ResponseWriter) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusServiceUnavailable)

	msg := "Validator is currently out of service"
	resp := ProcessDocumentResponse{
		Status:          "REJECTED",
		RejectionReason: &msg,
	}

	json.NewEncoder(w).Encode(resp)
}

func statusHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		writeError(w, http.StatusMethodNotAllowed, "only GET allowed")
		return
	}

	base := "/process-document/status/"
	if !strings.HasPrefix(r.URL.Path, base) {
		writeError(w, http.StatusNotFound, "not found")
		return
	}

	raw := strings.TrimPrefix(r.URL.Path, base)
	applicationId, err := strconv.Atoi(raw)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid application ID")
		return
	}

	mu.Lock()
	job, exists := jobStore[applicationId]
	var reasonPtr *string
	if reason, ok := reasonStore[applicationId]; ok && reason != "" {
		rCopy := reason
		reasonPtr = &rCopy
	}
	mu.Unlock()

	if !exists {
		writeError(w, http.StatusNotFound, "unknown application ID")
		return
	}

	if !checkRateLimit(applicationId) {
		writeError(w, http.StatusTooManyRequests, "rate limit exceeded")
		return
	}

	resp := ProcessDocumentResponse{
		ApplicationId:   job.ApplicationId,
		Status:          job.Status,
		RejectionReason: reasonPtr,
	}

	writeJSON(w, http.StatusOK, resp)
}

func checkRateLimit(applicationId int) bool {
	mu.Lock()
	defer mu.Unlock()

	now := time.Now()
	ri, exists := rateStore[applicationId]

	if !exists || now.After(ri.ResetTime) {
		rateStore[applicationId] = &RateInfo{
			Count:     1,
			ResetTime: now.Add(time.Second),
		}
		return true
	}

	if ri.Count >= maxPollRequests {
		return false
	}

	ri.Count++
	return true
}

func getRandomStatus() string {
	if rand.Intn(100) < verifiedProbability {
		return "VERIFIED"
	}
	return "REJECTED"
}

// writeJSON helper function
func writeJSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(data)
}

func runningInDocker() bool {
	_, err := os.Stat("/.dockerenv")
	return err == nil
}
