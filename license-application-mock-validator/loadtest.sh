#!/bin/bash

# HOW MANY TIMES TO SPAM
COUNT=$1

URL="http://localhost:8083/process-document"

for ((i=1; i<=COUNT; i++)); do
  #echo "---- Request $i ----"

  # POST
  RESPONSE=$(curl -s -X POST "$URL" \
    -H "Content-Type: application/json" \
    -d '{
      "document_type": "pdf",
      "filename": "./license-application-mock-validator/sample.pdf",
      "uploaded_at": "2025-05-22T10:00:00Z"
    }')

  #echo "POST RESPONSE: $RESPONSE"

  VERIFICATION_ID=$(echo "$RESPONSE" | jq -r '.verification_id')

  if [[ -z "$VERIFICATION_ID" || "$VERIFICATION_ID" == "null" ]]; then
      echo "ERROR: verification_id not found in response"
      continue
    fi

    # Status request
  STATUS=$(curl -s "http://localhost:8083/process-document/status/$VERIFICATION_ID")
  #echo "STATUS RESPONSE: $STATUS"
  #echo
done