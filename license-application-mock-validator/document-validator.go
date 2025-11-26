package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"sync"
	"time"
)

// probability (in %) of document being VERIFIED
const verifiedProbability = 90

// min/max processing duration
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

// ProcessDocumentRequest Incoming payload
type ProcessDocumentRequest struct {
	Filename   string `json:"filename"` // only "pdf"
	UploadedAt string `json:"uploaded_at"`
}

// ProcessDocumentResponse Outgoing response
type ProcessDocumentResponse struct {
	Status          string  `json:"status"`
	VerificationId  string  `json:"verification_id"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

var (
	jobStore    = make(map[string]string) // verificationId → status
	reasonStore = make(map[string]string) // verificationId → rejection reason
	mu          sync.Mutex
)

func main() {
	http.HandleFunc("/process-document", startProcessingHandler)
	http.HandleFunc("/process-document/status/", statusHandler)

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

// startProcessingHandler handles the document processing
func startProcessingHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
		return
	}

	var req ProcessDocumentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}

	id := generateVerificationId()

	// store job as PENDING initially
	mu.Lock()
	jobStore[id] = "PENDING"
	mu.Unlock()

	// start asynchronous processing
	go func(verificationID string) {
		time.Sleep(processDuration[rand.Intn(len(processDuration))])

		mu.Lock()
		status := getRandomStatus()
		jobStore[verificationID] = status
		if status == "REJECTED" {
			msg := rejectionReasons[rand.Intn(len(rejectionReasons))]
			reasonStore[verificationID] = msg
		}
		mu.Unlock()
	}(id)

	resp := ProcessDocumentResponse{
		Status:         "PENDING",
		VerificationId: id,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// statusHandler handles the status check of document processing
func statusHandler(w http.ResponseWriter, r *http.Request) {
	id := r.URL.Path[len("/process-document/status/"):]

	mu.Lock()
	status, exists := jobStore[id]
	reasonMsg := reasonStore[id]
	mu.Unlock()

	if !exists {
		http.Error(w, "unknown verification ID", http.StatusNotFound)
		return
	}

	var reasonPtr *string
	if reasonMsg != "" {
		reasonPtr = &reasonMsg
	}

	resp := ProcessDocumentResponse{
		Status:          status,
		VerificationId:  id,
		RejectionReason: reasonPtr,
	}

	w.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(w).Encode(resp)
	if err != nil {
		http.Error(w, "internal error", http.StatusInternalServerError)
		return
	}
}

func generateVerificationId() string {
	return fmt.Sprintf("DOC-%d-%d", time.Now().Unix(), rand.Intn(999999))
}

func getRandomStatus() string {
	r := rand.Intn(100)
	if r < verifiedProbability {
		return "VERIFIED"
	}
	return "REJECTED"
}
