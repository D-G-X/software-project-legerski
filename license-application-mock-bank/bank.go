package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"regexp"
	"strings"
	"time"
)

// min/max processing duration
var processDuration = []time.Duration{
	5 * time.Millisecond,
	30 * time.Millisecond,
}

var validCountryCodes = []string{
	"AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ",
	"BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BW", "BX", "BY", "BZ",
	"CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CW", "CX", "CY", "CZ",
	"DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DW", "DX", "DY", "DZ",
	"EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EW", "EX", "EY", "EZ",
	"FA", "FB", "FC", "FD", "FE", "FF", "FG", "FH", "FI", "FJ", "FK", "FL", "FM", "FN", "FO", "FP", "FQ", "FR", "FS", "FT", "FU", "FV", "FW", "FX", "FY", "FZ",
	"GA", "GB", "GC", "GD", "GE", "GF", "GG", "GH", "GI", "GJ", "GK", "GL", "GM", "GN", "GO", "GP", "GQ", "GR", "GS", "GT", "GU", "GV", "GW", "GX", "GY", "GZ",
	"HA", "HB", "HC", "HD", "HE", "HF", "HG", "HH", "HI", "HJ", "HK", "HL", "HM", "HN", "HO", "HP", "HQ", "HR", "HS", "HT", "HU", "HV", "HW", "HX", "HY", "HZ",
	"IA", "IB", "IC", "ID", "IE", "IF", "IG", "IH", "II", "IJ", "IK", "IL", "IM", "IN", "IO", "IP", "IQ", "IR", "IS", "IT", "IU", "IV", "IW", "IX", "IY", "IZ",
	"JA", "JB", "JC", "JD", "JE", "JF", "JG", "JH", "JI", "JJ", "JK", "JL", "JM", "JN", "JO", "JP", "JQ", "JR", "JS", "JT", "JU", "JV", "JW", "JX", "JY", "JZ",
	"KA", "KB", "KC", "KD", "KE", "KF", "KG", "KH", "KI", "KJ", "KK", "KL", "KM", "KN", "KO", "KP", "KQ", "KR", "KS", "KT", "KU", "KV", "KW", "KX", "KY", "KZ",
	"LA", "LB", "LC", "LD", "LE", "LF", "LG", "LH", "LI", "LJ", "LK", "LL", "LM", "LN", "LO", "LP", "LQ", "LR", "LS", "LT", "LU", "LV", "LW", "LX", "LY", "LZ",
	"MA", "MB", "MC", "MD", "ME", "MF", "MG", "MH", "MI", "MJ", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ",
	"NA", "NB", "NC", "ND", "NE", "NF", "NG", "NH", "NI", "NJ", "NK", "NL", "NM", "NN", "NO", "NP", "NQ", "NR", "NS", "NT", "NU", "NV", "NW", "NX", "NY", "NZ",
	"OA", "OB", "OC", "OD", "OE", "OF", "OG", "OH", "OI", "OJ", "OK", "OL", "OM", "ON", "OO", "OP", "OQ", "OR", "OS", "OT", "OU", "OV", "OW", "OX", "OY", "OZ",
	"PA", "PB", "PC", "PD", "PE", "PF", "PG", "PH", "PI", "PJ", "PK", "PL", "PM", "PN", "PO", "PP", "PQ", "PR", "PS", "PT", "PU", "PV", "PW", "PX", "PY", "PZ",
	"QA", "QB", "QC", "QD", "QE", "QF", "QG", "QH", "QI", "QJ", "QK", "QL", "QM", "QN", "QO", "QP", "QQ", "QR", "QS", "QT", "QU", "QV", "QW", "QX", "QY", "QZ",
	"RA", "RB", "RC", "RD", "RE", "RF", "RG", "RH", "RI", "RJ", "RK", "RL", "RM", "RN", "RO", "RP", "RQ", "RR", "RS", "RT", "RU", "RV", "RW", "RX", "RY", "RZ",
	"SA", "SB", "SC", "SD", "SE", "SF", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SP", "SQ", "SR", "SS", "ST", "SU", "SV", "SW", "SX", "SY", "SZ",
	"TA", "TB", "TC", "TD", "TE", "TF", "TG", "TH", "TI", "TJ", "TK", "TL", "TM", "TN", "TO", "TP", "TQ", "TR", "TS", "TT", "TU", "TV", "TW", "TX", "TY", "TZ",
	"UA", "UB", "UC", "UD", "UE", "UF", "UG", "UH", "UI", "UJ", "UK", "UL", "UM", "UN", "UO", "UP", "UQ", "UR", "US", "UT", "UU", "UV", "UW", "UX", "UY", "UZ",
	"VA", "VB", "VC", "VD", "VE", "VF", "VG", "VH", "VI", "VJ", "VK", "VL", "VM", "VN", "VO", "VP", "VQ", "VR", "VS", "VT", "VU", "VV", "VW", "VX", "VY", "VZ",
	"WA", "WB", "WC", "WD", "WE", "WF", "WG", "WH", "WI", "WJ", "WK", "WL", "WM", "WN", "WO", "WP", "WQ", "WR", "WS", "WT", "WU", "WV", "WW", "WX", "WY", "WZ",
	"XA", "XB", "XC", "XD", "XE", "XF", "XG", "XH", "XI", "XJ", "XK", "XL", "XM", "XN", "XO", "XP", "XQ", "XR", "XS", "XT", "XU", "XV", "XW", "XX", "XY", "XZ",
	"YA", "YB", "YC", "YD", "YE", "YF", "YG", "YH", "YI", "YJ", "YK", "YL", "YM", "YN", "YO", "YP", "YQ", "YR", "YS", "YT", "YU", "YV", "YW", "YX", "YY", "YZ",
	"ZA", "ZB", "ZC", "ZD", "ZE", "ZF", "ZG", "ZH", "ZI", "ZJ", "ZK", "ZL", "ZM", "ZN", "ZO", "ZP", "ZQ", "ZR", "ZS", "ZT", "ZU", "ZV", "ZW", "ZX", "ZY", "ZZ",
}

var nameRegex = regexp.MustCompile(`^[\p{L}\p{M}'’\-–]+(?: [\p{L}\p{M}'’\-–]+)*$`)
var ibanRegex = regexp.MustCompile(`^[A-Z]{2}[0-9A-Z]{13,33}$`)
var bicRegex = regexp.MustCompile(`^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$`)

// PaymentRequest Incoming payload
type PaymentRequest struct {
	Amount      float64 `json:"amount"`
	Name        string  `json:"name"`
	IBAN        string  `json:"iban"`
	BIC         *string `json:"bic"` // optional for Spanish IBANs
	PaymentDate string  `json:"payment_date"`
}

// PaymentResponse Outgoing response
type PaymentResponse struct {
	Status    string `json:"status"`
	PaymentID string `json:"payment_id"`
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

		if !amountValid(req.Amount, w) {
			return
		}

		if !nameValid(req.Name, w) {
			return
		}
		if !ibanValid(req.IBAN, w) {
			return
		}
		if !bicValid(req.BIC, req.IBAN, w) {
			return
		}
		if !paymentDateValid(req.PaymentDate, w) {
			return
		}

		resp := PaymentResponse{
			Status:    "SUCCESS",
			PaymentID: generatePaymentId(),
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

	if !isValidCountryCode(iban[:2]) {
		http.Error(w, "IBAN country code invalid", http.StatusBadRequest)
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

	if bic == "" && !strings.HasPrefix(iban, "ES") {
		http.Error(w, "BIC required for non-Spanish IBAN", http.StatusBadRequest)
		return false
	}

	if len(bic) != 8 && len(bic) != 11 {
		http.Error(w, "BIC length invalid", http.StatusBadRequest)
		return false
	}

	if !isValidCountryCode(bic[4:6]) {
		http.Error(w, "BIC country code invalid", http.StatusBadRequest)
		return false
	}

	if !bicRegex.MatchString(bic) {
		http.Error(w, "BIC format invalid", http.StatusBadRequest)
		return false
	}

	// pos6
	if bic[6] == '0' || bic[6] == '1' {
		http.Error(w, "BIC position 6 invalid", http.StatusBadRequest)
		return false
	}

	// pos7
	if bic[7] == 'O' {
		http.Error(w, "BIC position 7 invalid", http.StatusBadRequest)
		return false
	}

	if len(bic) == 11 {
		if bic[8] == 'X' && bic[8:11] != "XXX" {
			http.Error(w, "BIC position 8-10 invalid", http.StatusBadRequest)
			return false
		}
	}
	return true
}

func paymentDateValid(date string, w http.ResponseWriter) bool {
	date = strings.TrimSpace(date)

	if date == "" {
		http.Error(w, "payment date cannot be empty", http.StatusBadRequest)
		return false
	}
	return true // TODO: implement date validation
}

func isValidCountryCode(code string) bool {
	if len(code) != 2 {
		return false
	}

	for _, c := range validCountryCodes {
		if c == strings.ToUpper(code) {
			return true
		}
	}
	return false
}

func generatePaymentId() string {
	// simple mock payment ID: SEPA-<timestamp>-<rand>
	return fmt.Sprintf("SEPA-%d-%d", time.Now().Unix(), rand.Intn(999999))
}
