# License Application Mock Document Validation Service

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Go](https://golang.org/) version 1.22 or higher is required.


### Quick Start

1. Have Docker running.
2. Launch the `docker compose up` command in the `license-application-service` directory to build and start the mock document validator service. 

### Usage

#### Client call to the mock document validator service

The mock document validator service listens on port `8083` and exposes the following endpoint:

- `POST /process-document`: Simulates processing a document validation. Expects a JSON payload with the following structure:

```json
{
    "application_id": "string",
    "id_file": "application/pdf",
    "proof_file": "application/pdf"
}
```

The service responds with a JSON object indicating the initial state of the validation processing (PENDING):

```json
{
    "application_id": "string",
    "status": "string" // only "PENDING"
}
```

The status of the document verification can be retrieved using the `application_id` through the `/process-document/status/{application_id}` endpoint.

The response will be similar to the one above, but the status will eventually change to "VERIFIED" or "REJECTED" after a short delay:


```json
{
    "application_id": "string",
    "status": "string", // only "VERIFIED", "PENDING" or "REJECTED"
    "rejection_reason": "*string" // only present if status is "REJECTED"
}
```

#### Callback to the License Application Service

The mock document validator service also performs a callback to the License Application Service once the document verification is complete. The callback is sent to the `/validation-callback` endpoint of the License Application Service.

The callback payload has the following structure:

```json
{
    "application_id": "string",
    "status": "string", // only "VERIFIED" or "REJECTED"
    "rejection_reason": "*string" // only present if status is "REJECTED"
}
```

Rejection probability, delay and max file size (per file) can be configured in `document-validator.go`.

### Testing the Service

You can test the mock document validator service using `curl` or any API testing tool like Postman or simply the terminal.

Here’s an example `curl` command:

```bash
curl -X POST http://localhost:8083/process-document \
  -F "application_id=123" \
  -F "id_file=@./sample.pdf" \
  -F "proof_file=@./sample.pdf"
```

This should return a response similar to:

```json
{
    "application_id": "123",
    "status": "PENDING"
}
```

You can then check the status of the document verification using the `application_id`:

```bash
curl http://localhost:8083/process-document/status/{YOUR_APPLICATION_ID}
```

> Replace `{YOUR_APPLICATION_ID}` with the actual `application_id` received from the previous response.
> Rate limiting may apply to this endpoint.

This should return a response similar to:

```json
{
    "application_id": "123",
    "status": "VERIFIED"
}
```

or, if the document was rejected:

```json
{
    "application_id": "123",
    "status": "REJECTED",
    "rejection_reason": "Document is corrupted or unreadable"
}
```

the callback will also be sent to the License Application Service at this point with the following payload:

```json
{
    "application_id": "123",
    "status": "VERIFIED"
}
```

or, if the document was rejected:

```json
{
    "application_id": "123",
    "status": "REJECTED",
    "rejection_reason": "Document is corrupted or unreadable"
}
```
