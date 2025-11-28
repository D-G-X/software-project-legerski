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

// ProcessDocumentResponse Outgoing response (to client)
type ProcessDocumentResponse struct {
	ApplicationId   string  `json:"application_id"`
	VerificationId  string  `json:"verification_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

// CallbackPayload Outgoing callback payload (to backend)
type CallbackPayload struct {
	ApplicationId    string  `json:"application_id"`
	VerificationId   string  `json:"verification_id"`
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
	http.HandleFunc("/process-document", startProcessingHandler)
	http.HandleFunc("/process-document/status/", statusHandler)

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func isPdf(data []byte) bool {
	if len(data) < 4 {
		return false
	}
	return bytes.HasPrefix(data, []byte("%PDF"))
}

func startProcessingHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
		return
	}

	err := r.ParseMultipartForm(maxFileSize << 20)
	if err != nil {
		http.Error(w, "invalid multipart request", http.StatusBadRequest)
		return
	}

	// read ID file
	idFile, idHeader, err := r.FormFile("id_file")
	if err != nil {
		reject(w, "Missing ID PDF")
		return
	}
	defer func(idFile multipart.File) {
		err := idFile.Close()
		if err != nil {
			sendErr(err, w)
			return
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
		err := proofFile.Close()
		if err != nil {
			sendErr(err, w)
			return
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
	//TODO: validate application ID format?

	// validate pdf format
	if !isPdf(idData) {
		reject(w, "ID document is not a valid PDF: "+idHeader.Filename)
		return
	}
	if !isPdf(proofData) {
		reject(w, "Proof document is not a valid PDF: "+proofHeader.Filename)
		return
	}

	verificationId := generateVerificationId()

	mu.Lock()
	jobStore[verificationId] = JobInfo{
		ApplicationId:    applicationId,
		IdFilename:       idHeader.Filename,
		PropertyFilename: proofHeader.Filename,
		Status:           "PENDING",
	}
	mu.Unlock()

	go runBackgroundJob(applicationId, verificationId, idHeader.Filename, proofHeader.Filename, w)

	resp := ProcessDocumentResponse{
		ApplicationId:  applicationId,
		VerificationId: verificationId,
		Status:         "PENDING",
	}

	w.Header().Set("Content-Type", "application/json")
	err = json.NewEncoder(w).Encode(resp)
	if err != nil {
		sendErr(err, w)
		return
	}
}

func runBackgroundJob(applicationId, verificationId, idFile, proofFile string, w http.ResponseWriter) {
	// simulate processing duration
	time.Sleep(processDuration[rand.Intn(len(processDuration))])

	mu.Lock()
	status := getRandomStatus()

	jobStore[verificationId] = JobInfo{
		ApplicationId:    applicationId,
		IdFilename:       idFile,
		PropertyFilename: proofFile,
		Status:           status,
	}

	if status == "REJECTED" {
		reasonStore[verificationId] = rejectionReasons[rand.Intn(len(rejectionReasons))]
	}
	mu.Unlock()

	sendCallback(CallbackPayload{
		ApplicationId:    applicationId,
		VerificationId:   verificationId,
		IdFilename:       idFile,
		PropertyFilename: proofFile,
		Status:           status,
		RejectionReason: func() *string {
			if status == "REJECTED" {
				r := reasonStore[verificationId]
				return &r
			}
			return nil
		}(),
	}, w)
}

func sendCallback(p CallbackPayload, w http.ResponseWriter) {
	body, _ := json.Marshal(p)

	req, err := http.NewRequest("POST", callbackURL, strings.NewReader(string(body)))
	if err != nil {
		sendErr(err, w)

		return
	}

	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: timeout * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		sendErr(err, w)
		return
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			sendErr(err, w)
		}
	}(resp.Body)
}

func sendErr(err error, w http.ResponseWriter) {
	http.Error(w, "internal server error: "+err.Error(), http.StatusInternalServerError)
}

func reject(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)

	resp := ProcessDocumentResponse{
		Status:          "REJECTED",
		VerificationId:  "",
		RejectionReason: &msg,
	}

	err := json.NewEncoder(w).Encode(resp)
	if err != nil {
		sendErr(err, w)
		return
	}
}

func statusHandler(w http.ResponseWriter, r *http.Request) {
	base := "/process-document/status/"
	if !strings.HasPrefix(r.URL.Path, base) {
		http.NotFound(w, r)
		return
	}
	verificationId := r.URL.Path[len(base):]

	mu.Lock()
	job, exists := jobStore[verificationId]
	mu.Unlock()

	if !exists {
		http.Error(w, "unknown verification ID", http.StatusNotFound)
		return
	}

	if !checkRateLimit(job.ApplicationId) {
		http.Error(w, "rate limit exceeded for: "+job.ApplicationId, http.StatusTooManyRequests)
		return
	}

	var reasonPtr *string
	mu.Lock()
	if reason, ok := reasonStore[verificationId]; ok && reason != "" {
		reasonPtr = &reason
	}
	mu.Unlock()

	resp := ProcessDocumentResponse{
		VerificationId:  verificationId,
		ApplicationId:   job.ApplicationId,
		Status:          job.Status,
		RejectionReason: reasonPtr,
	}

	w.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(w).Encode(resp)
	if err != nil {
		sendErr(err, w)
		return
	}
	//_ = json.NewEncoder(w).Encode(resp)
}

func checkRateLimit(applicationId string) bool {
	mu.Lock()
	defer mu.Unlock()

	ri, exists := rateStore[applicationId]
	now := time.Now()

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

func generateVerificationId() string {
	return fmt.Sprintf("DOC-%d-%d", time.Now().Unix(), rand.Intn(999999))
}

func getRandomStatus() string {
	if rand.Intn(100) < verifiedProbability {
		return "VERIFIED"
	}
	return "REJECTED"
}
