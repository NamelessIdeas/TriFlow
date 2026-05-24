SHELL := /bin/bash

GO         ?= go
APP_NAME   := triflow-api
SEED_NAME  := triflow-seed
BIN_DIR    := bin

DB_URL ?= postgres://triflow:triflow@localhost:5432/triflow?sslmode=disable
MIGRATE_CMD ?= migrate -path=./migrations -database='$(DB_URL)'

.PHONY: help run build test lint tidy seed \
        migrate-up migrate-down migrate-create migrate-version \
        docker-build docker-up docker-down docker-logs

help: ## Mostra questo aiuto
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*##/ {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

run: ## Avvia l'API localmente
	$(GO) run ./cmd/api

build: ## Compila i binari in ./bin
	@mkdir -p $(BIN_DIR)
	$(GO) build -trimpath -o $(BIN_DIR)/$(APP_NAME)  ./cmd/api
	$(GO) build -trimpath -o $(BIN_DIR)/$(SEED_NAME) ./cmd/seed

test: ## Esegue i test unitari
	$(GO) test ./... -count=1 -race

lint: ## Esegue go vet (e golangci-lint se installato)
	$(GO) vet ./...
	@if command -v golangci-lint >/dev/null 2>&1; then golangci-lint run; fi

tidy: ## go mod tidy
	$(GO) mod tidy

seed: build ## Popola il DB con dati demo
	$(BIN_DIR)/$(SEED_NAME)

migrate-up: ## Applica le migration
	$(MIGRATE_CMD) up

migrate-down: ## Rollback dell'ultima migration
	$(MIGRATE_CMD) down 1

migrate-version: ## Mostra la versione corrente
	$(MIGRATE_CMD) version

migrate-create: ## NOME=foo crea una nuova coppia up/down
	@if [ -z "$$NOME" ]; then echo "Usa: make migrate-create NOME=nome"; exit 1; fi
	migrate create -ext sql -dir ./migrations -seq $$NOME

docker-build: ## Build dell'immagine API
	docker compose build api

docker-up: ## Avvia tutto in docker compose
	docker compose up -d

docker-down: ## Ferma e rimuove i container
	docker compose down

docker-logs: ## Tail dei log dell'api
	docker compose logs -f api
