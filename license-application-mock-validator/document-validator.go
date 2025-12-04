package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"time"
)

const verifiedProbability = 90 // percentage chance of VERIFIED status
const maxFileSize = 10          // per file in MB
const maxPollRequests = 2       // max polling requests per second per client
const timeout = 5               // seconds

var processDuration = []time.Duration{
	2 * time.Second,
	5 * time.Second,
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

const callbackURL = "http://host.docker.internal:8080/validation-callback" // backend callback endpoint

// Login-Konfiguration
const loginURL = "http://localhost:8080/login"
const loginEmail = "documentvalidator@xx.xx"
const loginPassword = "securepassword123"

var httpClient = &http.Client{
	Timeout: timeout * time.Second,
}

// ProcessDocumentResponse Outgoing response (to client)
type ProcessDocumentResponse struct {
	ApplicationId   int     `json:"application_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

// CallbackPayload Outgoing callback payload (to backend)
type CallbackPayload struct {
	ApplicationId    int     `json:"application_id"`
	IdFilename       string  `json:"id_filename"`
	PropertyFilename string  `json:"proof_filename"`
	Status           string  `json:"status"`
	RejectionReason  *string `json:"rejection_reason,omitempty"`
}

// JobInfo internal job storage
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

// RateInfo for limiting the number of polling requests per client
type RateInfo struct {
	Count     int
	ResetTime time.Time
}

var rateStore = make(map[int]*RateInfo)

// LoginRequest / LoginResponse für den Login-Call
type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	AccessToken string `json:"access_token"`
	// weitere Felder ignorieren wir
}

func main() {
	rand.New(rand.NewSource(time.Now().UnixNano()))

	// Wrap handlers with CORS middleware
	http.HandleFunc("/process-document", corsMiddleware(startProcessingHandler))
	http.HandleFunc("/process-document/status/", corsMiddleware(statusHandler))

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

// corsMiddleware ensures CORS headers are sent for all responses, including errors
func corsMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Always set CORS headers
		w.Header().Set("Access-Control-Allow-Origin", "http://localhost:3000") // React frontend
		w.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

		// Handle preflight requests
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}

		next(w, r)
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

	err := r.ParseMultipartForm(maxFileSize << 20)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid multipart request")
		return
	}

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

	if !isPdf(idData) {
		reject(w, "ID document is not a valid PDF: "+idHeader.Filename)
		return
	}
	if !isPdf(proofData) {
		reject(w, "Proof document is not a valid PDF: "+proofHeader.Filename)
		return
	}

	// *** NEU: vor dem Anlegen des Jobs versuchen wir, einen Access Token zu holen ***
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

	// Token an den Background-Job übergeben
	go runBackgroundJob(applicationId, idHeader.Filename, proofHeader.Filename, token)

	resp := ProcessDocumentResponse{
		ApplicationId: applicationId,
		Status:        "PENDING",
	}

	writeJSON(w, http.StatusOK, resp)
}

func runBackgroundJob(applicationId int, idFile, proofFile, token string) {
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

	payload := CallbackPayload{
		ApplicationId:    applicationId,
		IdFilename:       idFile,
		PropertyFilename: proofFile,
		Status:           status,
		RejectionReason:  rejectionReason,
	}

	if err := sendCallback(payload, token); err != nil {
		log.Printf("callback error for application_id=%d: %v", applicationId, err)
	}
}

// holt sich einen Access Token vom Backend-Login
func getAccessToken() (string, error) {
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

	return loginResp.AccessToken, nil
}

// sendCallback bekommt jetzt den Token übergeben und loggt sich nicht mehr selbst ein
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

// *** NEU: spezielle Antwort, wenn der Validator nicht verfügbar ist ***
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