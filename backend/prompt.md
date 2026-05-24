Sei un ingegnere backend senior. Devi creare da zero il backend di un'app di
produttività chiamata "TriFlow" che integra TRE metodi in un unico sistema
coerente: GTD (Getting Things Done), Tecnica del Pomodoro e Second Brain (BASB).
L'idea chiave non è tenerli separati, ma farli collaborare: un task GTD può
essere lavorato in sessioni Pomodoro, e le note del Second Brain possono essere
collegate a task, progetti e altre note.

## Stack tecnologico (vincolante)
- Linguaggio: Go (Golang), versione 1.22+
- Framework HTTP: Gin
- Database relazionale: PostgreSQL (usa pgx come driver; per le migration usa
  golang-migrate con file .sql versionati in /migrations)
- Cache / stato volatile: Redis (go-redis) — usalo per: stato dei timer Pomodoro
  attivi, rate limiting, blacklist dei refresh token, e cache delle query pesanti
- Autenticazione: JWT (access token breve + refresh token), password con bcrypt
- Config via variabili d'ambiente + file .env (usa godotenv per lo sviluppo)
- Validazione input con go-playground/validator
- Logging strutturato (zap o slog)

## Architettura
Usa una struttura a layer pulita e idiomatica Go:
/cmd/api            -> entrypoint main.go
/internal/config    -> caricamento config da env
/internal/domain    -> entità e interfacce dei repository (nessuna dipendenza esterna)
/internal/repository-> implementazioni PostgreSQL/Redis
/internal/service   -> logica di business (qui vive l'integrazione tra i 3 sistemi)
/internal/handler   -> handler HTTP Gin
/internal/middleware-> auth, logging, rate limit, recovery, CORS
/migrations         -> file SQL di migration
/pkg                -> utility riutilizzabili (jwt, hashing, response helper)
Applica dependency injection manuale (interfacce nei layer alti, implementazioni
iniettate in main.go). Non usare un ORM pesante.

## Dominio funzionale da modellare
Modella questi aggregati e le loro relazioni:

1. USER: registrazione, login, profilo, preferenze (durata pomodoro/pausa, ecc).

2. GTD:
   - Inbox item (cattura veloce, non ancora processato)
   - Task con: titolo, note, status (inbox/next_action/waiting/scheduled/done),
     context/tag (@casa, @ufficio, @telefono...), energia, durata stimata,
     priorità, due_date, defer_date
   - Project (obiettivo con più task, status active/someday/completed)
   - Endpoint per "processare" un inbox item trasformandolo in task/project.
   - Weekly review: endpoint che restituisce un riepilogo (inbox da svuotare,
     task in waiting, progetti senza next action).

3. POMODORO:
   - PomodoroSession: collegabile opzionalmente a un task GTD
   - Stati gestiti in Redis per la sessione attiva (running/paused) con TTL,
     persistiti su PostgreSQL al completamento
   - Cicli configurabili (es. 25/5, lungo dopo 4 pomodori)
   - Statistiche: pomodori al giorno/settimana, tempo focus per task e per progetto

4. SECOND BRAIN:
   - Note con contenuto in Markdown, titolo, tag, e categoria PARA
     (Project/Area/Resource/Archive)
   - Link bidirezionali tra note (backlinks) e link da note verso task o progetti
   - Ricerca full-text sulle note (usa la full-text search di PostgreSQL)

5. INTEGRAZIONE (la parte più importante):
   - Un task può mostrare le sue sessioni Pomodoro e le note collegate
   - Una nota può "promuovere" un'idea in un task GTD (endpoint dedicato)
   - Dashboard unificata: endpoint che aggrega task di oggi, timer attivo,
     note recenti e progressi.

6. QUIZ DI RACCOMANDAZIONE:
   - Endpoint che riceve le risposte di un breve questionario (problema
     principale, stile di lavoro, tolleranza al setup, obiettivo) e restituisce
     quale metodo è più adatto all'utente con una spiegazione e un punteggio per
     ognuno dei tre sistemi. La logica di scoring deve stare nel service layer.

## Requisiti tecnici trasversali
- Tutti gli endpoint sotto /api/v1
- Risposte JSON con formato uniforme (wrapper success/error coerente)
- Gestione errori centralizzata con error types custom mappati su status HTTP
- Paginazione su tutte le liste (cursor o limit/offset, scegli e documenta)
- Rate limiting via Redis sugli endpoint di auth
- Migration SQL eseguibili e con rollback
- Seed opzionale con dati demo (un comando o flag)
- Dockerfile + docker-compose.yml che avvii api + postgres + redis
- Makefile con target: run, build, test, migrate-up, migrate-down, lint, docker-up
- Test unitari sui service principali (almeno scoring del quiz e logica pomodoro)
  con i repository mockati tramite le interfacce.

## Documentazione da produrre (obbligatoria)
Crea questi file Markdown:
- README.md            : descrizione progetto, come avviarlo, requisiti, comandi
- ARCHITECTURE.md      : spiegazione dei layer, scelte tecniche e perché
- DATA_MODEL.md        : entità, relazioni e diagramma (in mermaid)
- API_CONTRACT.md      : elenco completo degli endpoint con metodo, path, request
                         body, response body di esempio e codici di stato
- openapi.yaml         : specifica OpenAPI 3.0 completa e valida di tutta l'API
                         (questa servirà per generare il client Android)

## Output finale (obbligatorio)
Alla fine, dopo aver completato e verificato che il progetto compili, genera un
file CLAUDE.md nella root pensato per future sessioni di modifica. Deve contenere:
- panoramica del progetto e dello stack
- mappa delle cartelle e responsabilità di ogni layer
- convenzioni di codice e di naming usate
- come aggiungere un nuovo endpoint passo-passo (dal domain all'handler)
- come creare/applicare una migration
- comandi utili (test, run, docker)
- note sulle parti delicate (gestione timer in Redis, refresh token, scoring quiz)
- cosa NON fare per non rompere i contratti dell'API verso il frontend Android

## Modo di lavorare
Procedi per fasi: prima proponi la struttura cartelle e lo schema del database e
fermati per conferma; poi implementa migration + domain, poi repository, poi
service, poi handler e middleware, infine docker, test e documentazione. Spiega
brevemente le scelte importanti man mano. Mantieni il codice idiomatico e
commentato dove serve.
