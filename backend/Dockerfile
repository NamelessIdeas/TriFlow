# --- build ---
FROM golang:1.22-alpine AS builder
WORKDIR /src

RUN apk add --no-cache git
COPY go.mod go.sum* ./
RUN go mod download

COPY . .

ENV CGO_ENABLED=0 GOOS=linux
RUN go build -trimpath -ldflags="-s -w" -o /out/api  ./cmd/api
RUN go build -trimpath -ldflags="-s -w" -o /out/seed ./cmd/seed

# --- runtime ---
FROM alpine:3.20
RUN apk add --no-cache ca-certificates tzdata && \
    addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=builder /out/api  /usr/local/bin/api
COPY --from=builder /out/seed /usr/local/bin/seed
COPY migrations /app/migrations

USER app
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/api"]
