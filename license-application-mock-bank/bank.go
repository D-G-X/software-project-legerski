package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"regexp"
	"strings"
	"time"
)

const successProbability = 99 // percentage chance of SUCCESS status
// min/max processing duration
var processDuration = []time.Duration{
	5 * time.Millisecond,
	30 * time.Millisecond,
}

var nameRegex = regexp.MustCompile(`^[\p{L}\p{M}'’\-–]+(?: [\p{L}\p{M}'’\-–]+)*$`)
var ibanRegex = regexp.MustCompile(`^[A-Z]{2}[0-9A-Z]{13,33}$`)
var bicRegex = regexp.MustCompile(`^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$`)

// PaymentRequest Incoming payload
type PaymentRequest struct {
	ApplicationId string  `json:"application_id"`
	Amount        float64 `json:"amount"`
	Name          string  `json:"name"`
	IBAN          string  `json:"iban"`
	BIC           *string `json:"bic"` // optional for Spanish IBANs
}

// PaymentResponse Outgoing response
type PaymentResponse struct {
	ApplicationId string `json:"application_id"`
	PaymentId     string `json:"payment_id"`
	Status        string `json:"status"`
}

func main() {
	http.HandleFunc("/process-payment", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "only POST allowed", http.StatusMethodNotAllowed)
			return
		}
		// simulate processing time
		time.Sleep(processDuration[rand.Intn(len(processDuration))])

		var req PaymentRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		defer func(Body io.ReadCloser) {
			err := Body.Close()
			if err != nil {
				http.Error(w, "internal error", http.StatusInternalServerError)
				return
			}
		}(r.Body)

		if req.ApplicationId == "" {
			http.Error(w, "application_id required", http.StatusBadRequest)
			return
		}

		if !amountValid(req.Amount, w) {
			http.Error(w, "amount must be positive", http.StatusBadRequest)
			return
		}

		if !nameValid(req.Name, w) {
			http.Error(w, "name format invalid", http.StatusBadRequest)
			return
		}
		if !ibanValid(req.IBAN, w) {
			http.Error(w, "IBAN format invalid", http.StatusBadRequest)
			return
		}
		if !bicValid(req.BIC, req.IBAN, w) {
			http.Error(w, "BIC format invalid", http.StatusBadRequest)
			return
		}

		resp := PaymentResponse{
			ApplicationId: req.ApplicationId,
			PaymentId:     generatePaymentId(),
			Status:        getRandomStatus(),
		}

		w.Header().Set("Content-Type", "application/json")
		err := json.NewEncoder(w).Encode(resp)
		if err != nil {
			http.Error(w, "internal error", http.StatusInternalServerError)
			return
		}
	})

	log.Println("Mock bank service running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func amountValid(amount float64, w http.ResponseWriter) bool {
	if amount <= 0 {
		http.Error(w, "amount must be positive", http.StatusBadRequest)
		return false
	}
	return true
}

func nameValid(name string, w http.ResponseWriter) bool {
	if strings.TrimSpace(name) == "" {
		http.Error(w, "name cannot be empty", http.StatusBadRequest)
		return false
	}

	if !nameRegex.MatchString(name) {
		http.Error(w, "name format invalid", http.StatusBadRequest)
		return false
	}
	return true
}

func ibanValid(iban string, w http.ResponseWriter) bool {
	iban = strings.ToUpper(strings.ReplaceAll(iban, " ", ""))

	if iban == "" {
		http.Error(w, "IBAN cannot be empty", http.StatusBadRequest)
		return false
	}

	if len(iban) < 15 || len(iban) > 35 {
		http.Error(w, "IBAN length invalid", http.StatusBadRequest)
		return false
	}

	if !ibanRegex.MatchString(iban) {
		http.Error(w, "IBAN format invalid", http.StatusBadRequest)
		return false
	}
	return true
}

func bicValid(bicPtr *string, iban string, w http.ResponseWriter) bool {
	// BIC optional for Spanish IBAN (starts with "ES")
	iban = strings.ToUpper(strings.ReplaceAll(iban, " ", ""))
	bic := ""
	if bicPtr != nil {
		if cleaned := strings.ToUpper(strings.ReplaceAll(*bicPtr, " ", "")); cleaned != "" {
			bic = cleaned
		}
	}

	if strings.HasPrefix(iban, "ES") {
		// allowed only for Spanish IBANs
		return true
	}

	if bic == "" {
		http.Error(w, "BIC required for non-Spanish IBANs", http.StatusBadRequest)
		return false
	}

	if len(bic) != 8 && len(bic) != 11 {
		http.Error(w, "BIC length invalid", http.StatusBadRequest)
		return false
	}

	if !bicRegex.MatchString(bic) {
		http.Error(w, "BIC format invalid", http.StatusBadRequest)
		return false
	}
	return true
}

func generatePaymentId() string {
	// simple mock payment ID: SEPA-<timestamp>-<rand>
	return fmt.Sprintf("SEPA-%d-%d", time.Now().Unix(), rand.Intn(999999))
}

func getRandomStatus() string {
	if rand.Intn(100) < successProbability {
		return "VERIFIED"
	}
	return "REJECTED"
}
