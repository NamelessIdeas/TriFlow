# ARCHITECTURE

## Visione

TriFlow non è "tre app affiancate" ma un sistema unico. Le strutture dati e
gli endpoint sono pensati per **collegare** GTD, Pomodoro e Second Brain:

- una `task` GTD può essere "lavorata in" sessioni Pomodoro (FK `task_id` su
  `pomodoro_sessions`),
- una `note` del Second Brain può puntare a task o project (`note_refs`),
- una nota può essere "promossa" a task (endpoint `POST /notes/{id}/promote-to-task`),
- la dashboard unifica i tre mondi in un singolo aggregato.

## Layer

```
   handler  ← HTTP/Gin: parsing/validation, mapping errori, niente logica
      ↓
   service  ← logica di business, validazioni semantiche, integrazione
      ↓
 repository ← persistenza (pgx + Redis), nessuna conoscenza di HTTP
      ↓
   domain   ← entità, interfacce, error types (zero deps esterne)
```

**Regola d'oro:** la freccia delle dipendenze punta sempre verso il **basso**.
Il package `domain` non importa nulla del progetto. I service vedono solo
`domain.*Repository`, non le implementazioni concrete. Il wiring è fatto a
mano in `cmd/api/main.go`.

### Perché niente ORM
Le query del prodotto sono poche, specifiche e includono cose come la
full-text search di Postgres, indici GIN su `tsvector`, join condizionati.
Con un ORM perderemmo controllo per zero guadagno. Usiamo `pgx` direttamente.

## Scelte tecniche

| Tema | Scelta | Perché |
|---|---|---|
| Linguaggio | Go 1.22 | richiesto + scelta sana per backend HTTP |
| HTTP | Gin | richiesto, performance solide, ecosistema maturo |
| DB | PostgreSQL (pgx) | richiesto; FTS nativa per le note |
| Cache | Redis (go-redis) | stato timer attivi, rate limit, blacklist refresh token |
| Auth | JWT HS256 + refresh rotation | mobile-friendly, stateless lato access |
| ID | UUID v4 generati lato server | semplici e ordinabili a sufficienza |
| Paginazione | `limit`/`offset` | semplice da consumare da Android |
| Logger | `log/slog` (stdlib) | zero dipendenze in più, output JSON |
| Validazione | go-playground/validator | tag su struct, semplice |
| Migration | `golang-migrate` (CLI/container) | non importato nel binario, mantiene leggero |
| Risposta API | envelope `{success, data, meta, error}` | uniforme, parser singolo lato client |

## Flussi chiave

### Pomodoro (timer in Redis)

```
client                  service.Pomodoro                  Redis           Postgres
  |  POST /pomodoros/start  ----> Set(ActiveTimer, TTL)
  |  ... timer corre ...
  |  POST /pomodoros/pause  ----> Get → mutate → Set
  |  POST /pomodoros/resume ----> Get → mutate → Set
  |  POST /pomodoros/complete --> Get → INSERT pomodoro_sessions, Del
```

Il server **non** tiene loop attivi: il client computa il tempo residuo dai
timestamp salvati. Se Redis perde la chiave (es. restart) il client perde solo
il timer in corso, mai sessioni già concluse (che sono in PostgreSQL).

### Refresh token rotation

Login emette `(access, refresh)`. `refresh_tokens` su Postgres è la verità
persistente (jti, expires_at, revoked_at). Refresh: verifica firma, controlla
blacklist Redis, controlla riga DB, **revoca il vecchio** (rotation) ed
emette nuova coppia. Logout revoca + mette in blacklist.

### Quiz scoring

Funzione pura nel service: per ognuna delle 4 risposte assegna punti ai 3
metodi tramite `scoreMatrix`. Il totale viene normalizzato 0..100 dividendo
per il massimo teorico (40 punti). Si testa senza alcun mock.

## Sicurezza

- Password con bcrypt (`DefaultCost`).
- JWT firmato HS256; secret minimo 16 char enforced in `config.Load`.
- Rate limit su `/auth/*` per IP (fixed-window).
- Tutte le query repository filtrano per `user_id` → impossibile leggere o
  modificare risorse di altri utenti.
- CORS configurabile da env.

## Errori

`domain/errors.go` definisce error types riusati ovunque (es. `ErrNotFound`,
`ErrConflict`, `ErrInvalidInput`, `ErrInvalidCreds`, `ErrTimerActive`).
Il repository postgres traduce errori pgx (es. `23505 → ErrAlreadyExists`).
Gli handler chiamano `middleware.HandleError(err)` che mappa il tipo su HTTP.

## Test

I service più strategici hanno test unitari con repo mockati in-memory:
- `quiz_service_test.go` → scoring funzione pura, 5 casi.
- `pomodoro_service_test.go` → start/pause/resume/complete/abort, tempo
  iniettato via `svc.now`.
