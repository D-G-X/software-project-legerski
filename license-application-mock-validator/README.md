# License Application Mock Document Validation Service

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Go](https://golang.org/) version 1.22 or higher is required.


### Quick Start

1. Have Docker running.
2. Launch the `docker compose up` command in the `license-application-service` directory to build and start the mock document validator service. 

### Usage

The mock document validator service listens on port `8083` and exposes the following endpoint:

- `POST /process-document`: Simulates processing a payment. Expects a JSON payload with the following structure:

```json
{
    "filename": "string", // only "pdf"
    "uploaded_at": "string"
}
```

The service responds with a JSON object indicating the success or failure of the payment processing:

```json
{
    "status": "string", // only "SUCCESS"
    "verification_id": "string"
}
```

### Testing the Service

You can test the mock document validator service using `curl` or any API testing tool like Postman or simply the terminal.

Here’s an example `curl` command:

```bash
curl -X POST http://localhost:8083/process-document \
  -H "Content-Type: application/json" \
  -d '{
    "document_type": "pdf",
    "filename": "./license-application-mock-validator/sample.pdf",
    "uploaded_at": "2025-05-22T10:00:00Z"
  }'
```

This should return a response similar to:

```json
{
    "status": "SUCCESS",
    "verification_id": "DOC-1764173117-648016"
}
```
