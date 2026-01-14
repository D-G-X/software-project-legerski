package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"regexp"
	"strings"
	"sync"
	"time"
)

const successProbability = 99 // percentage chance of SUCCESS status

const callbackURL = "http://host.docker.internal:8080/payment-callback" // <— Ziel für deinen Backend Callback

var processDuration = []time.Duration{
	5 * time.Millisecond,
	30 * time.Millisecond,
}

var rejectionReasons = []string{
	"Insufficient funds",
	"Account closed",
	"Invalid account details",
	"Suspected fraud",
	"Payment exceeds limit",
	"Beneficiary account frozen",
	"Technical error processing payment",
}

var nameRegex = regexp.MustCompile(`^[\p{L}\p{M}'’\-–]+(?: [\p{L}\p{M}'’\-–]+)*$`)
var ibanRegex = regexp.MustCompile(`^[A-Z]{2}[0-9A-Z]{13,33}$`)
var bicRegex = regexp.MustCompile(`^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$`)

// pollLimiter to limit polling frequency per application id
var pollLimiter = struct {
	sync.Mutex
	last map[string]time.Time
}{last: make(map[string]time.Time)}

func allowPoll(appId string, interval time.Duration) bool {
	pollLimiter.Lock()
	defer pollLimiter.Unlock()

	now := time.Now()
	last, exists := pollLimiter.last[appId]

	if exists && now.Sub(last) < interval {
		return false
	}

	pollLimiter.last[appId] = now
	return true
}

// PaymentRequest Incoming payment request
type PaymentRequest struct {
	ApplicationId string  `json:"application_id"`
	Amount        float64 `json:"amount"`
	Name          string  `json:"name"`
	IBAN          string  `json:"iban"`
	BIC           string  `json:"bic"`
}

// PaymentResponse Outgoing payment response
type PaymentResponse struct {
	ApplicationId   string  `json:"application_id"`
	PaymentId       string  `json:"payment_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

// PaymentCallback Outgoing callback payload
type PaymentCallback struct {
	ApplicationId   string  `json:"application_id"`
	PaymentId       string  `json:"payment_id"`
	Status          string  `json:"status"`
	RejectionReason *string `json:"rejection_reason,omitempty"`
}

func main() {
	rand.New(rand.NewSource(time.Now().UnixNano()))

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
		_ = r.Body.Close()

		if !allowPoll(req.ApplicationId, time.Millisecond) {
			http.Error(w, "rate limit exceeded for: "+req.ApplicationId, http.StatusTooManyRequests)
			return
		}

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

		if !bicValid(req.BIC, w) {
			http.Error(w, "BIC format invalid", http.StatusBadRequest)
			return
		}

		paymentId := generatePaymentId()
		status := getRandomStatus()

		var rejectionReason *string
		if status == "REJECTED" {
			reason := rejectionReasons[rand.Intn(len(rejectionReasons))]
			rejectionReason = &reason
		}

		resp := PaymentResponse{
			ApplicationId:   req.ApplicationId,
			PaymentId:       paymentId,
			Status:          status,
			RejectionReason: rejectionReason,
		}

		// Return immediate response
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(resp)

		// Fire callback async
		go sendCallbackAsync(req.ApplicationId, paymentId, status, w)
	})

	log.Println("Mock bank service running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func sendCallbackAsync(appId, paymentId, status string, w http.ResponseWriter) {
	// simulate callback delay (mock bank doing work)
	time.Sleep(time.Duration(200+rand.Intn(500)) * time.Millisecond)

	cb := PaymentCallback{
		ApplicationId: appId,
		PaymentId:     paymentId,
		Status:        status,
	}

	body, err := json.Marshal(cb)
	if err != nil {
		log.Println("callback marshal error:", err)
		return
	}

	req, err := http.NewRequest("POST", callbackURL, bytes.NewBuffer(body))
	if err != nil {
		log.Println("callback request error:", err)
		return
	}
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: 2 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		log.Println("callback send error:", err)
		return
	}

	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {
			http.Error(w, "internal server error", http.StatusInternalServerError)
		}
	}(resp.Body)

	if resp.StatusCode >= 300 {
		respBody, _ := io.ReadAll(resp.Body)
		log.Printf("callback returned %d: %s\n", resp.StatusCode, string(respBody))
	}
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

func bicValid(bic string, w http.ResponseWriter) bool {
	bic = strings.ToUpper(strings.ReplaceAll(bic, " ", ""))

	if bic == "" {
		http.Error(w, "BIC cannot be empty", http.StatusBadRequest)
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
