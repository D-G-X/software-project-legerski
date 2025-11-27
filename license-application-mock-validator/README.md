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
    "id_filename": "string", // only "pdf"
    "proof_filename": "string", // only "pdf"
    "uploaded_at": "string"
}
```

The service responds with a JSON object indicating the initial state of the payment processing (PENDING):

```json
{
    "status": "string", // only "VERIFIED", "PENDING" or "REJECTED"
    "verification_id": "string",
    "rejection_reason": "string" // only present if status is "REJECTED"
}
```

The status of the document verification can be retrieved using the `verification_id` through the `/process-document/status/{verification_id}` endpoint.

The response will be similar to the one above, but the status will eventually change to "VERIFIED" or "REJECTED" after a short delay.

Rejection probability and delay can be configured in `document-validator.go`.

### Testing the Service

You can test the mock document validator service using `curl` or any API testing tool like Postman or simply the terminal.

Here’s an example `curl` command:

```bash
curl -X POST http://localhost:8083/process-document \
  -F "id_file=@./sample.pdf" \
  -F "proof_file=@./sample.pdf" \
  -F "uploaded_at=2025-05-22T10:00:00Z"
```

This should return a response similar to:

```json
{
    "status": "PENDING",
    "verification_id": "DOC-1764173117-648016"
}
```

You can then check the status of the document verification using the `verification_id`:

```bash
curl http://localhost:8083/process-document/status/{YOUR_VERIFICATION_ID}
```
> Replace `{YOUR_VERIFICATION_ID}` with the actual `verification_id` received from the previous response.

This should return a response similar to:

```json
{
    "status": "VERIFIED",
    "verification_id": "DOC-1764173117-648016"
}
```

or, if the document was rejected:

```json
{
    "status": "REJECTED",
    "verification_id": "DOC-1764173117-648016",
    "rejection_reason": "Document is corrupted or unreadable"
}
```
