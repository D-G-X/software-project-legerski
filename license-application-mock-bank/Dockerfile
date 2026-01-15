FROM golang:1.22-alpine AS builder

WORKDIR /build
COPY . .

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -o mock-bank ./bank.go


FROM alpine:3.19

WORKDIR /app
RUN apk add --no-cache ca-certificates
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder /build/mock-bank .

EXPOSE 8080
ENTRYPOINT ["./mock-bank"]