package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"mime/multipart"
	"net/http"
	"strings"
	"sync"
	"time"
)

const verifiedProbability = 50 // percentage chance of VERIFIED status

const maxFileSize = 10 // per file in MB

const maxPollRequests = 2 // max polling requests per second per client

const timeout = 5 // seconds

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

var httpClient = &http.Client{
	Timeout: timeout * time.Second,
}

// ProcessDocumentResponse Outgoing response (to client)
type ProcessDocumentResponse struct {
	ApplicationId   string  `json:"application_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

// CallbackPayload Outgoing callback payload (to backend)
type CallbackPayload struct {
	ApplicationId    string  `json:"application_id"`
	IdFilename       string  `json:"id_filename"`
	PropertyFilename string  `json:"proof_filename"`
	Status           string  `json:"status"`
	RejectionReason  *string `json:"rejection_reason,omitempty"`
}

// JobInfo internal job storage
type JobInfo struct {
	ApplicationId    string
	IdFilename       string
	PropertyFilename string
	Status           string
}

var (
	jobStore    = make(map[string]JobInfo)
	reasonStore = make(map[string]string)
	mu          sync.Mutex
)

// RateInfo for limiting the number of polling requests per client
type RateInfo struct {
	Count     int
	ResetTime time.Time
}

var rateStore = make(map[string]*RateInfo)

func main() {
	rand.New(rand.NewSource(time.Now().UnixNano()))

	http.HandleFunc("/process-document", startProcessingHandler)
	http.HandleFunc("/process-document/status/", statusHandler)

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func isPdf(data []byte) bool {
	if len(data) < 4 {
		return false
	}
	// check "magic" number
	return bytes.HasPrefix(data, []byte("%PDF"))
}

func startProcessingHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
		return
	}

	// limit total multipart size
	err := r.ParseMultipartForm(maxFileSize << 20)
	if err != nil {
		http.Error(w, "invalid multipart request", http.StatusBadRequest)
		return
	}

	// read id file
	idFile, idHeader, err := r.FormFile("id_file")
	if err != nil {
		reject(w, "Missing ID PDF")
		return
	}
	defer func(idFile multipart.File) {
		if cerr := idFile.Close(); cerr != nil {
			log.Printf("error closing idFile: %v", cerr)
		}
	}(idFile)

	idData, err := io.ReadAll(idFile)
	if err != nil {
		reject(w, "ID PDF unreadable")
		return
	}

	// read property proof file
	proofFile, proofHeader, err := r.FormFile("proof_file")
	if err != nil {
		reject(w, "Missing proof PDF")
		return
	}
	defer func(proofFile multipart.File) {
		if cerr := proofFile.Close(); cerr != nil {
			log.Printf("error closing proofFile: %v", cerr)
		}
	}(proofFile)

	proofData, err := io.ReadAll(proofFile)
	if err != nil {
		reject(w, "Proof PDF unreadable")
		return
	}

	applicationId := r.FormValue("application_id")
	if applicationId == "" {
		reject(w, "Missing application ID")
		return
	}
	// TODO: validate application ID format?

	// validate pdf format
	if !isPdf(idData) {
		reject(w, "ID document is not a valid PDF: "+idHeader.Filename)
		return
	}
	if !isPdf(proofData) {
		reject(w, "Proof document is not a valid PDF: "+proofHeader.Filename)
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

	// start background processing
	go runBackgroundJob(applicationId, idHeader.Filename, proofHeader.Filename)

	resp := ProcessDocumentResponse{
		ApplicationId: applicationId,
		Status:        "PENDING",
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(resp); err != nil {
		log.Printf("error encoding response: %v", err)
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}
}

func runBackgroundJob(applicationId, idFile, proofFile string) {
	// simulate processing duration
	time.Sleep(processDuration[rand.Intn(len(processDuration))])

	status := getRandomStatus()

	var rejectionReason *string

	mu.Lock()
	// update job status
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

	if err := sendCallback(payload); err != nil {
		log.Printf("callback error for application_id=%s: %v", applicationId, err)
	}
}

func sendCallback(p CallbackPayload) error {
	body, err := json.Marshal(p)
	if err != nil {
		return fmt.Errorf("marshal callback payload: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, callbackURL, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("create callback request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("execute callback request: %w", err)
	}
	defer func(Body io.ReadCloser) {
		if cerr := Body.Close(); cerr != nil {
			log.Printf("error closing callback response body: %v", cerr)
		}
	}(resp.Body)

	if resp.StatusCode >= 400 {
		b, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("callback returned status %s: %s", resp.Status, string(b))
	}

	return nil
}

func sendErr(err error, w http.ResponseWriter) {
	log.Printf("internal error: %v", err)
	http.Error(w, "internal server error: "+err.Error(), http.StatusInternalServerError)
}

func reject(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)

	resp := ProcessDocumentResponse{
		Status:          "REJECTED",
		RejectionReason: &msg,
	}

	if err := json.NewEncoder(w).Encode(resp); err != nil {
		log.Printf("error encoding rejection response: %v", err)
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}
}

func statusHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "only GET allowed", http.StatusMethodNotAllowed)
		return
	}

	base := "/process-document/status/"
	if !strings.HasPrefix(r.URL.Path, base) {
		http.NotFound(w, r)
		return
	}

	applicationId := strings.TrimPrefix(r.URL.Path, base)
	if applicationId == "" {
		http.Error(w, "missing application ID", http.StatusBadRequest)
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
		http.Error(w, "unknown application ID", http.StatusNotFound)
		return
	}

	if !checkRateLimit(job.ApplicationId) {
		http.Error(w, "rate limit exceeded for: "+job.ApplicationId, http.StatusTooManyRequests)
		return
	}

	resp := ProcessDocumentResponse{
		ApplicationId:   job.ApplicationId,
		Status:          job.Status,
		RejectionReason: reasonPtr,
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(resp); err != nil {
		sendErr(err, w)
		return
	}
}

func checkRateLimit(applicationId string) bool {
	mu.Lock()
	defer mu.Unlock()

	now := time.Now()
	ri, exists := rateStore[applicationId]

	if !exists || now.After(ri.ResetTime) {
		rateStore[applicationId] = &RateInfo{
			Count:     1,
			ResetTime: now.Add(time.Second), // reset every second
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
