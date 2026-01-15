# License Application Mock Bank

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Go](https://golang.org/) version 1.22 or higher is required.


### Quick Start

The mock bank service is automatically started with the backend docker-compose setup.

To start the backend including the mock services, follow the Readme in the backend directory.

### Usage

#### Client call to the mock bank service

The mock bank service listens internally on `mock-bank:8080` and exposes the following endpoint:

- `POST /process-payment`: Simulates processing a payment. Expects a JSON payload with the following structure:

```json
{
    "application_id": "string",
    "amount": "number",
    "name": "string",
    "iban": "string",
    "bic": "string"
}
```
> Rate limiting may apply to this endpoint.

The service responds with a JSON object with the result of the payment processing (VERIFIED or REJECTED):

```json
{
    "application_id": "string",
    "payment_id": "string",
    "status": "string", // only "VERIFIED" or "REJECTED"
    "rejection_reason": "*string" // only present if status is "REJECTED"
}
```

#### Callback to the License Application Service

The mock bank service also performs a callback to the License Application Service once the payment verification is complete. The callback is sent to the `/payment-callback` endpoint of the License Application Service.

The callback payload has the following structure:

```json
{
    "application_id": "string",
    "payment_id": "string",
    "status": "string", // only "VERIFIED" or "REJECTED"
    "rejection_reason": "*string" // only present if status is "REJECTED"
}
```

Rejection probability, delay and max file size (per file) can be configured in `bank.go`.

### Testing the Service (Docker Exec Only)

You can test the mock bank service using `curl` or any API testing tool like Postman or simply the terminal.

Here’s an example `curl` command:

```bash
curl -X POST http://mock-bank:8080/process-payment \
  -H "Content-Type: application/json" \
  -d '{
    "application_id": "123",
    "amount": 123.45,
    "name": "John Doe",
    "iban": "DE01010101010101010101",
    "bic": "ABCDDEFFXXX"
  }'
```

This should return a response similar to:

```json
{
    "application_id": "123",
    "payment_id": "PAY-1764173117-648016",
    "status": "VERIFIED"
}
```

or, if the document was rejected:

```json
{
    "application_id": "123",
    "payment_id": "PAY-1764173117-648016",
    "status": "REJECTED",
    "rejection_reason": "Insufficient funds"
}
```

the callback will also be sent to the License Application Service at this point with the following payload:

```json
{
    "application_id": "123",
    "payment_id": "PAY-1764173117-648016",
    "status": "VERIFIED"
}
```

or, if the document was rejected:

```json
{
    "application_id": "123",
    "payment_id": "PAY-1764173117-648016",
    "status": "REJECTED",
    "rejection_reason": "Insufficient funds"
}
```
