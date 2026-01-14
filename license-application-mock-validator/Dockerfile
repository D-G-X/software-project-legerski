FROM golang:1.22-alpine AS builder

WORKDIR /app
COPY . .
RUN go build -o mock-document-validator document-validator.go

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /app/mock-document-validator .
EXPOSE 8080
CMD ["./mock-document-validator"]