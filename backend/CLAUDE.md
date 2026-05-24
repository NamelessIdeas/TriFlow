# CLAUDE.md — Guida per future sessioni di modifica

Questo documento è il primo file da leggere quando si torna su TriFlow per
modifiche o estensioni. Tutto il resto della documentazione (`README.md`,
`ARCHITECTURE.md`, `DATA_MODEL.md`, `API_CONTRACT.md`, `openapi.yaml`) è
referenziato qui dove rilevante.

## 1. Panoramica

- **Cos'è**: backend Go di un'app di produttività che integra **GTD**,
  **Pomodoro** e **Second Brain (PARA)** in un sistema unico.
- **Stack**: Go 1.22 · Gin · PostgreSQL (pgx) · Redis (go-redis) · JWT HS256
  · bcrypt · `log/slog` · `go-playground/validator` · `golang-migrate`.
- **Clienti**: API REST sotto `/api/v1`, prodotta per essere consumata da un
  client Android generato da `openapi.yaml`.

## 2. Mappa cartelle

```
cmd/api                 entrypoint API (wiring DI in main.go)
cmd/seed                binario che popola dati demo
internal/config         caricamento config da env/.env (no logica)
internal/domain         entità + interfacce repository + error types
                        (zero dipendenze esterne al progetto)
internal/repository/postgres  implementazioni pgx delle repo
internal/repository/redis     timer attivi, rate limiter, blacklist token
internal/service        logica di business e integrazione tra i 3 metodi
internal/handler        handler HTTP Gin + router.go
internal/middleware     auth (JWT), logger (slog), ratelimit, recovery, cors, error
pkg/jwt                 emissione e verifica token
pkg/hash                bcrypt wrapper
pkg/response            envelope JSON {success,data,meta,error}
pkg/validator           singleton di go-playground/validator
migrations/             0001..0005 .up.sql / .down.sql
```

Per uno schizzo discorsivo dei layer e delle scelte, vedi `ARCHITECTURE.md`.

## 3. Convenzioni di codice e naming

- **Package**: minuscolo, singolare (`handler`, `service`, `domain`).
- **File**: minuscolo con underscore (`auth_handler.go`, `task_repo.go`).
- **Interfacce repository** in `domain/*.go`, suffisso `Repository`.
  Implementazioni concrete in `repository/postgres/*_repo.go`.
- **Errori di dominio**: usare quelli in `domain/errors.go`; mai ritornare
  errori HTTP dai service. Il mapping HTTP è in `middleware/error.go`.
- **Risposte**: sempre tramite `pkg/response` (`OK`, `OKWithMeta`, `Err`).
  Nessun `c.JSON(...)` diretto nei handler.
- **UUID**: `github.com/google/uuid`. Generazione in service (`uuid.New()`),
  parsing dai path/query in `handler/helpers.go`.
- **Paginazione**: `domain.Page{Limit, Offset}` con `.Normalize()`.
- **Filtri delle list**: struct `XxxFilter` in `domain/`, costruita da query
  params nei handler.
- **Time**: `time.Time` ovunque, niente int64 unix. Date "solo data" sono
  comunque `*time.Time` in Go (Postgres riceve `DATE`).
- **Validazione input HTTP**: tag `binding:"..."` sulle struct req dei
  handler. Validazione semantica nei service.

## 4. Come aggiungere un nuovo endpoint (passo-passo)

Esempio: nuovo endpoint `POST /api/v1/tasks/{id}/snooze` che rimanda una
task di N giorni.

1. **Domain** (`internal/domain/gtd.go`): se servono nuove entità/campi/
   metodi all'interfaccia `TaskRepository`, dichiararli qui.
2. **Repository** (`internal/repository/postgres/task_repo.go`): implementare
   il nuovo metodo. Le query devono **sempre** filtrare per `user_id`.
3. **Service** (`internal/service/gtd_service.go`): aggiungere il metodo con
   la logica (validazioni, calcoli, regole). I service ricevono interfacce,
   non implementazioni concrete.
4. **Handler** (`internal/handler/gtd_handler.go`): parsare body/path con
   `c.ShouldBindJSON` e `parseUUID`; chiamare il service; tradurre l'errore
   con `mw.HandleError`; restituire via `response.OK`.
5. **Router** (`internal/handler/router.go`): registrare la route nella zona
   autenticata (`api.POST("/tasks/:id/snooze", h.GTD.Snooze)`).
6. **DTO**: se il payload è non triviale, aggiungere il tipo di request al
   service per evitare di accoppiarsi alle struct HTTP.
7. **OpenAPI** (`openapi.yaml`): aggiungere il path, le request e response
   schema. Questa è la **fonte** del client Android, non un'ottima idea
   dimenticarla.
8. **API_CONTRACT.md**: una riga in tabella + un esempio JSON.
9. **Test**: almeno un test sul service.

## 5. Migration: creare e applicare

- Nuova migration:
  ```bash
  make migrate-create NOME=add_snooze_columns
  # crea migrations/0006_add_snooze_columns.up.sql / .down.sql
  ```
- Applicare/rollback:
  ```bash
  make migrate-up
  make migrate-down   # rollback dell'ultima
  ```
- In Docker le migration partono automaticamente (servizio `migrate` in
  `docker-compose.yml`, condition: `service_completed_successfully`).
- **Regola**: ogni `.up.sql` deve avere il corrispondente `.down.sql`. Niente
  `DROP TABLE` distruttivi senza pensare al rollback.
- Indici GIN/FTS: ricorda di mettere `IF EXISTS` nelle DROP per non rompere
  ripartenze parziali.

## 6. Comandi utili

```bash
# sviluppo locale
make run                 # API in foreground
make test                # test con -race
make build               # binari in ./bin
make lint                # go vet (+ golangci-lint se installato)
make tidy                # go mod tidy

# database
make migrate-up
make migrate-down
make migrate-version
make seed                # crea utente demo + dati

# docker (full stack)
make docker-up           # postgres + redis + migrate + api
make docker-logs
make docker-down
```

## 7. Parti delicate

### Timer Pomodoro in Redis
- Chiave: `pomodoro:active:{user_id}` → JSON `ActiveTimer`.
- TTL = durata residua + 5 minuti di margine.
- Il server **non** mantiene loop: il client computa il residuo dai
  timestamp (`StartedAt`, `ElapsedBeforePauseSec`).
- Su pause aggiungiamo `now-StartedAt` a `ElapsedBeforePauseSec` e settiamo
  `PausedAt`. Su resume azzeriamo `PausedAt` e riassegniamo `StartedAt = now`.
- `Complete`/`Abort` persistono su `pomodoro_sessions` e cancellano Redis.
- **Non spostare lo stato in PostgreSQL**: la natura volatile è una scelta,
  non una limitazione.

### Refresh token + blacklist
- `refresh_tokens` Postgres è verità persistente; la blacklist Redis è una
  cache "veloce" con TTL pari all'expiry residua del token.
- Su refresh: parse → check blacklist → check riga DB (e expires_at, revoked_at)
  → **revoca il vecchio** (rotation) → emette nuovo. Il logout fa la stessa
  revoca + blacklist.
- Cambiare l'algoritmo o il `JWT_SECRET` invalida tutti i token vecchi. Per
  rotazione "soft" servirà introdurre un `kid` nel header e supportare due
  secret in parallelo.

### Quiz scoring
- È una funzione pura: `scoreMatrix` (map dichiarativa) + somma + normalizzazione.
- **Tuning**: per cambiare le raccomandazioni si modifica solo la matrice;
  i test in `quiz_service_test.go` coprono i tre casi canonici e vanno
  aggiornati se cambiano i pesi.
- Il quiz NON richiede auth: è progettato come hook di onboarding.

### Full-text search note
- `notes.search_tsv` è aggiornato da un trigger SQL su INSERT/UPDATE di
  `title` e `content_md`. Non valorizzare mai a mano `search_tsv` da Go.
- Il filtro nelle query usa `plainto_tsquery('simple', $)` per evitare la
  dipendenza da una specifica lingua dell'utente.

### CASCADE su `user_id`
- Tutte le FK applicative usano `ON DELETE CASCADE`. Cancellare un utente
  rimuove tutto il suo perimetro. Se in futuro va aggiunta un'entità,
  ricordarsi questa scelta.

## 8. Cosa NON fare (per non rompere il contratto con il client Android)

1. **Non rinominare campi JSON** in `domain/*.go`. Se vanno cambiati, fare
   un breaking version (`/api/v2`) o aggiungere nuovi campi senza rimuovere
   i vecchi.
2. **Non rimuovere campi** dalla response anche se "sembrano inutili". I
   client tipizzati di OpenAPI possono crashare in deserializzazione.
3. **Non cambiare gli enum** (`status`, `kind`, `para_category`, ecc.) senza
   coordinarlo con il frontend.
4. **Non variare il formato dell'envelope** `{success,data,meta,error}`.
5. **Non spostare endpoint** senza tenere il vecchio path come alias per
   almeno una versione.
6. **Non cambiare i codici errore** in `middleware/error.go`. Sono parte del
   contratto (`invalid_input`, `not_found`, `timer_active`, ecc.).
7. **Mantenere `openapi.yaml` aggiornato** in PR con la stessa modifica del
   codice. Il client Android è rigenerato da quel file.
8. **Non spostare lo stato volatile su Postgres**: il design `Redis = stato
   in-flight`, `Postgres = sessioni concluse` va preservato.
9. **Non aggiungere `tag` come tabella unica polimorfica**: `task_tags` e
   `note_tags` sono separati apposta, sia per indici locali sia per cascade.
10. **Niente ORM**: pgx + query SQL esplicite. Se si è tentati di
    introdurre un ORM, prima allinearsi con i maintainer.
