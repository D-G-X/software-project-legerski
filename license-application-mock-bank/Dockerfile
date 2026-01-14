FROM golang:1.22-alpine AS builder

WORKDIR /app
COPY . .
RUN go build -o mock-bank bank.go

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /app/mock-bank .
EXPOSE 8080
CMD ["./mock-bank"]