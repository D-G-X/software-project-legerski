package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"time"
)

// Incoming payload
type PaymentRequest struct {
	Amount      float64 `json:"amount"`
	Name        string  `json:"name"`
	IBAN        string  `json:"iban"`
	BIC         string  `json:"bic"`
	PaymentDate string  `json:"payment_date"`
}

// Outgoing response
type PaymentResponse struct {
	Status    string `json:"status"`
	PaymentID string `json:"payment_id"`
}

func main() {
	// seed for random payment IDs
	rand.Seed(time.Now().UnixNano())

	http.HandleFunc("/process-payment", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
			return
		}
		// simulate processing time
		time.Sleep(time.Second)

		var req PaymentRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}

		// generate payment ID (simple mock)
		paymentID := generatePaymentId()

		resp := PaymentResponse{
			Status:    "SUCCESS",
			PaymentID: paymentID,
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(resp)
	})

	log.Println("Mock bank service running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func generatePaymentId() string {
	// simple mock payment ID: PAY-<timestamp>-<rand>
	return fmt.Sprintf("PAY-%d-%d", time.Now().Unix(), rand.Intn(999999))
}