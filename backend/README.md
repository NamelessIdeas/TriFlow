# TriFlow — Backend

Backend in Go di un'app di produttività che integra **GTD**, **Pomodoro** e **Second Brain (PARA)** in un unico sistema coerente: le task GTD si lavorano in sessioni Pomodoro e le note del Second Brain si collegano a task, progetti e altre note.

## Stack

- Go 1.22+, Gin, pgx, go-redis, golang-jwt, bcrypt, go-playground/validator, slog
- PostgreSQL 16, Redis 7
- Migration via `golang-migrate` (file `.sql` in `migrations/`)

## Avvio rapido (Docker)

```bash
cp .env.example .env       # opzionale: l'API in Docker prende già da compose
make docker-up             # avvia postgres + redis + migrate + api
make docker-logs           # log dell'api
# API su http://localhost:8080
curl http://localhost:8080/health
```

## Avvio locale (senza Docker)

```bash
# 1. avvia solo postgres+redis con compose
docker compose up -d postgres redis

# 2. config + migrate + run
cp .env.example .env
make migrate-up
make run
# oppure: make seed   # popola dati demo

# Utente demo dopo seed:
# email: demo@triflow.app  password: demopass123
```

## Comandi utili

```bash
make help            # mostra tutti i target
make run             # avvia l'API in foreground
make build           # binari in ./bin
make test            # test unitari con -race
make lint            # go vet (+ golangci-lint se installato)
make seed            # popola dati demo
make migrate-up      # applica migration
make migrate-down    # rollback ultima migration
make migrate-create NOME=add_foo
make docker-up       # tutto via docker compose
make docker-down
```

## Struttura

```
cmd/api          entrypoint API
cmd/seed         binario seed dati demo
internal/config  caricamento config da env
internal/domain  entità + interfacce repository (no deps esterne)
internal/repository/postgres  implementazioni pgx
internal/repository/redis     stati volatili + ratelimit + blacklist
internal/service              logica di business e integrazione
internal/handler              handler Gin + router
internal/middleware           auth, logger, ratelimit, recovery, cors, error
pkg/jwt /hash /response /validator   utility riutilizzabili
migrations/      file SQL up/down
```

## Documentazione

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — layer, scelte tecniche e perché
- [`DATA_MODEL.md`](DATA_MODEL.md) — entità, relazioni, diagramma ER
- [`API_CONTRACT.md`](API_CONTRACT.md) — elenco endpoint con request/response
- [`openapi.yaml`](openapi.yaml) — specifica OpenAPI 3.0 (per generare il client Android)
- [`CLAUDE.md`](CLAUDE.md) — guida per future sessioni di modifica del codice
