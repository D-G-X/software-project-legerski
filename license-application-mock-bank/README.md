# License Application Mock Bank

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Go](https://golang.org/) version 1.22 or higher is required.


### Quick Start

1. Have Docker running.
2. Launch the `docker compose up` command in the `license-application-service` directory to build and start the mock bank service. 

### Usage

The mock bank service listens on port `8082` and exposes the following endpoint:

- `POST /process-payment`: Simulates processing a payment. Expects a JSON payload with the following structure:

```json
{
    "amount": "float",
    "name": "string",
    "iban": "string",
    "bic": "string", // optional
    "payment_date": "string"
}
```

The service responds with a JSON object indicating the success or failure of the payment processing:

```json
{
    "status": "string", // only "SUCCESS"
    "payment_id": "string"
}
```

### Testing the Service

You can test the mock document validator service using `curl` or any API testing tool like Postman or simply the terminal.

Here are two example `curl` commands:

```bash
curl -X POST http://localhost:8082/process-payment \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 123.45,
    "name": "John Doe",
    "iban": "ES01234567890123456789",           
    "payment_date": "2025-05-22T15:10:00Z"
  }'
```

```bash
curl -X POST http://localhost:8082/process-payment \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 678.90,
    "name": "Jane Doe",
    "iban": "DE09876543210987654321",
    "bic": "DEUTDEXX",
    "payment_date": "2025-06-15T09:30:00Z"
  }'
```

This should return a response similar to:

```json
{
  "status":"SUCCESS",
  "payment_id":"SEPA-1764173549-896471"
}
```
