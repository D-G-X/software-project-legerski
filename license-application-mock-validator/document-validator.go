package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"strings"
	"sync"
	"time"
)

// probability of VERIFIED
const verifiedProbability = 90

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

// ProcessDocumentResponse Outgoing response
type ProcessDocumentResponse struct {
	Status          string  `json:"status"`
	VerificationId  string  `json:"verification_id"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

var (
	jobStore    = make(map[string]string)
	reasonStore = make(map[string]string)
	mu          sync.Mutex
)

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
	return strings.HasPrefix(string(data[:4]), "%PDF")
}

func startProcessingHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
		return
	}

	err := r.ParseMultipartForm(20 << 20) // 20 MB
	if err != nil {
		http.Error(w, "invalid multipart request", http.StatusBadRequest)
		return
	}

	uploadedAt := r.FormValue("uploaded_at")

	// read id/passport file
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

	// read address proof file
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

	// validate pdf format
	if !isPdf(idData) {
		reject(w, "ID document is not a valid PDF: "+idHeader.Filename)
		return
	}
	if !isPdf(proofData) {
		reject(w, "Proof document is not a valid PDF: "+proofHeader.Filename)
		return
	}

	// everything valid -> async background process
	id := generateVerificationId()

	mu.Lock()
	jobStore[id] = "PENDING"
	mu.Unlock()

	go func(verificationID string) {
		time.Sleep(processDuration[rand.Intn(len(processDuration))])

		mu.Lock()
		status := getRandomStatus()
		jobStore[verificationID] = status
		if status == "REJECTED" {
			reasonStore[verificationID] = rejectionReasons[rand.Intn(len(rejectionReasons))]
		}
		mu.Unlock()
	}(id)

	resp := ProcessDocumentResponse{
		Status:         "PENDING",
		VerificationId: id,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
	_ = uploadedAt
}

func reject(w http.ResponseWriter, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)

	resp := ProcessDocumentResponse{
		Status:          "REJECTED",
		VerificationId:  "",
		RejectionReason: &msg,
	}

	json.NewEncoder(w).Encode(resp)
}

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
	json.NewEncoder(w).Encode(resp)
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
