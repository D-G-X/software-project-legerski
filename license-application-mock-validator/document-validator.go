package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"
)

// ProcessDocumentRequest Incoming payload
type ProcessDocumentRequest struct {
	Filename   string `json:"filename"` // only "pdf"
	UploadedAt string `json:"uploaded_at"`
}

// ProcessDocumentResponse Outgoing response
type ProcessDocumentResponse struct {
	Status         string `json:"status"`
	VerificationId string `json:"verification_id"`
}

func main() {
	wd, _ := os.Getwd()
	log.Println("Working directory:", wd)
	http.HandleFunc("/process-document", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
			return
		}

		var req ProcessDocumentRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}

		if req.Filename == "" {
			http.Error(w, "filename required", http.StatusBadRequest)
			return
		}

		ext := strings.ToLower(filepath.Ext(req.Filename))
		if ext != ".pdf" {
			http.Error(w, "file must end with .pdf", http.StatusBadRequest)
			return
		}
		// check file exists
		file, err := os.Open(req.Filename)
		if err != nil {
			http.Error(w, "file not found: "+err.Error(), http.StatusBadRequest)
			return
		}
		defer func(file *os.File) {
			err := file.Close()
			if err != nil {
				http.Error(w, "failed to close file", http.StatusInternalServerError)
			}
		}(file)
		// validate pdf header
		header := make([]byte, 4)
		_, err = file.Read(header)
		if err != nil && err != io.EOF {
			http.Error(w, "failed to read file", http.StatusInternalServerError)
			return
		}
		if string(header) != "%PDF" {
			http.Error(w, "file is not a valid pdf", http.StatusBadRequest)
			return
		}

		// simulate processing time
		time.Sleep(time.Duration(rand.Intn(4)+2) * time.Second)

		resp := ProcessDocumentResponse{
			Status:         "SUCCESS",
			VerificationId: generateVerificationId(),
		}

		w.Header().Set("Content-Type", "application/json")
		err = json.NewEncoder(w).Encode(resp)
		if err != nil {
			http.Error(w, "failed to encode response", http.StatusInternalServerError)
			return
		}
	})

	log.Println("Mock document service running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func generateVerificationId() string {
	// simple mock verification ID: DOC-<timestamp>-<rand>
	return fmt.Sprintf("DOC-%d-%d", time.Now().Unix(), rand.Intn(999999))
}
