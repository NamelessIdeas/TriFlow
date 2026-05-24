# API CONTRACT

Tutti gli endpoint sono sotto **`/api/v1`**.
Risposte sempre nello stesso envelope:

```jsonc
// success
{ "success": true, "data": <payload>, "meta": <opzionale, es. paginazione> }

// error
{ "success": false, "error": { "code": "string", "message": "string", "details": {...} } }
```

**Autenticazione**: tutte le route eccetto `/auth/*` e `POST /quiz/score` richiedono `Authorization: Bearer <access_token>`.

**Paginazione**: query params `limit` (default 20, max 100) e `offset` (default 0). Il meta torna `{limit, offset, total}`.

**Date**: ISO 8601 (es. `2026-06-01T10:00:00Z`); `due_date` / `defer_date` accettano anche `YYYY-MM-DD`.

---

## Codici errore comuni

| code | HTTP | quando |
|---|---|---|
| `invalid_input` | 400 | body mancante/malformato, valori fuori range |
| `unauthorized` | 401 | token mancante / scaduto / firma errata |
| `invalid_credentials` | 401 | login fallito |
| `token_invalid` | 401 | refresh token revocato/scaduto |
| `forbidden` | 403 | risorsa di altri utenti |
| `not_found` | 404 | risorsa inesistente o non posseduta |
| `already_exists` | 409 | unique violation (es. email duplicata) |
| `conflict` | 409 | stato incompatibile (es. inbox già processato) |
| `timer_active` | 409 | start con timer già attivo |
| `timer_not_found` | 404 | pause/resume/complete senza timer |
| `rate_limited` | 429 | troppe richieste su `/auth/*` |
| `internal_error` | 500 | errore non gestito |

---

## Auth

### POST `/auth/register`
**Body**
```json
{ "email": "demo@triflow.app", "password": "secret123", "display_name": "Demo" }
```
**201**
```json
{ "success": true, "data": { "id": "...", "email": "demo@triflow.app", "display_name": "Demo", "created_at": "..." } }
```

### POST `/auth/login`
**Body** `{ "email": "...", "password": "..." }`
**200**
```json
{ "success": true, "data": {
  "user": { "id": "...", "email": "...", "display_name": "..." },
  "tokens": {
    "access_token": "ey...",
    "refresh_token": "ey...",
    "access_expires_at": "2026-06-01T10:15:00Z"
  }
}}
```

### POST `/auth/refresh`
**Body** `{ "refresh_token": "..." }` → **200** stesso payload `tokens` (rotation).

### POST `/auth/logout`
**Body** `{ "refresh_token": "..." }` → **204**.

---

## Users

| metodo | path | risposta |
|---|---|---|
| GET | `/users/me` | utente corrente |
| PUT | `/users/me` | `{display_name}` → utente aggiornato |
| GET | `/users/me/preferences` | preferenze |
| PUT | `/users/me/preferences` | aggiorna preferenze (vedi esempio) |

```json
// PUT /users/me/preferences
{
  "pomodoro_duration_min": 25,
  "short_break_min": 5,
  "long_break_min": 15,
  "pomodoros_until_long_break": 4,
  "timezone": "Europe/Rome"
}
```

---

## Inbox / Tasks / Projects / Contexts (GTD)

| metodo | path | descrizione |
|---|---|---|
| POST | `/inbox` | cattura veloce: `{raw_text}` |
| GET | `/inbox` | lista non processati (paginato) |
| POST | `/inbox/:id/process` | trasforma in task/project/discard |
| POST | `/projects` | crea progetto |
| GET | `/projects` | lista paginata, filtro `?status=` |
| GET/PUT/DELETE | `/projects/:id` | dettaglio/aggiorna/elimina |
| POST | `/contexts` | crea contesto |
| GET | `/contexts` | lista contesti |
| DELETE | `/contexts/:id` | elimina |
| POST | `/tasks` | crea task |
| GET | `/tasks` | lista paginata, filtri: `status, project_id, context_id, due_before, tag` |
| GET | `/tasks/:id` | dettaglio |
| PATCH | `/tasks/:id` | aggiornamento parziale |
| DELETE | `/tasks/:id` | elimina |
| GET | `/tasks/:id/context` | task + sessioni pomodoro + note collegate |
| GET | `/reviews/weekly` | weekly review |

### Esempi

```json
// POST /inbox
{ "raw_text": "Chiamare il dentista per appuntamento" }

// POST /inbox/{id}/process
{
  "action": "task",                    // task | project | discard
  "title": "Chiamare dentista",
  "status": "next_action",
  "context_id": "uuid-del-contesto-telefono",
  "due_date": "2026-06-05",
  "priority": 3,
  "tags": ["salute"]
}

// POST /tasks
{
  "title": "Scrivere README",
  "notes": "...",
  "project_id": "uuid-progetto",
  "context_id": "uuid-contesto",
  "status": "next_action",
  "energy": "low",
  "estimated_minutes": 30,
  "priority": 2,
  "due_date": "2026-06-10",
  "tags": ["docs"]
}

// PATCH /tasks/{id}
{ "status": "done" }
```

```json
// GET /reviews/weekly
{
  "success": true,
  "data": {
    "inbox_to_process": [ /* InboxItem */ ],
    "waiting_tasks":    [ /* Task */ ],
    "projects_without_next_action": [ /* Project */ ],
    "generated_at": "2026-06-01T08:00:00Z"
  }
}
```

---

## Pomodoro

| metodo | path | descrizione |
|---|---|---|
| POST | `/pomodoros/start` | avvia timer (errore `timer_active` se ce n'è già uno) |
| POST | `/pomodoros/pause` | pausa |
| POST | `/pomodoros/resume` | riprende |
| GET | `/pomodoros/current` | stato attivo (404 se nessuno) |
| POST | `/pomodoros/complete` | chiude e salva su DB |
| POST | `/pomodoros/abort` | scarta e salva come 'aborted' |
| GET | `/pomodoros/sessions` | lista sessioni (paginata, `from`, `to`) |
| GET | `/pomodoros/stats` | statistiche aggregate `from..to` |

```json
// POST /pomodoros/start
{
  "task_id": "uuid-task",   // opzionale
  "kind": "focus",          // focus | short_break | long_break (default: focus)
  "cycle_index": 1,
  "duration_sec": 1500      // opzionale, default dalle preferenze
}

// GET /pomodoros/current → 200
{
  "success": true,
  "data": {
    "user_id": "...",
    "task_id": "...",
    "kind": "focus",
    "planned_duration_sec": 1500,
    "cycle_index": 1,
    "started_at": "...",
    "paused_at": null,
    "elapsed_before_pause_sec": 0
  }
}

// GET /pomodoros/stats?from=2026-05-25&to=2026-06-01 → 200
{
  "pomodoros_completed": 17,
  "focus_seconds": 25500,
  "by_day":  { "2026-05-25": 3, "2026-05-26": 4 },
  "by_task": { "uuid-task-1": 9000 }
}
```

---

## Notes (Second Brain / PARA)

| metodo | path | descrizione |
|---|---|---|
| POST | `/notes` | crea nota |
| GET | `/notes` | lista paginata, filtri: `para_category, tag, q` (full-text) |
| GET | `/notes/:id` | dettaglio |
| PATCH | `/notes/:id` | aggiorna |
| DELETE | `/notes/:id` | elimina |
| GET | `/notes/:id/backlinks` | note che linkano questa |
| GET | `/notes/:id/links` | note linkate da questa |
| POST | `/notes/:id/links` | aggiungi link → `{target_note_id}` |
| DELETE | `/notes/:id/links/:targetId` | rimuovi link |
| POST | `/notes/:id/refs` | collega a task/project → `{ref_type, ref_id}` |
| DELETE | `/notes/:id/refs/:refType/:refId` | scollega |
| POST | `/notes/:id/promote-to-task` | crea una task GTD da questa nota |

```json
// POST /notes
{
  "title": "Setup TriFlow MVP",
  "content_md": "# Idee\n- onboarding\n- quiz...",
  "para_category": "project",
  "tags": ["mvp"]
}

// POST /notes/{id}/promote-to-task
{
  "title": "Costruire onboarding",
  "project_id": "uuid-progetto",
  "context_id": "uuid-contesto",
  "status": "next_action",
  "due_date": "2026-06-10",
  "priority": 4
}
```

---

## Dashboard (integrazione)

`GET /dashboard` → aggregato unificato

```json
{
  "success": true,
  "data": {
    "today_tasks":          [ /* Task */ ],
    "active_timer":         { /* ActiveTimer o omesso */ },
    "recent_notes":         [ /* Note */ ],
    "pomodoros_today":      5,
    "focus_seconds_week":   45000,
    "generated_at":         "2026-06-01T08:00:00Z"
  }
}
```

---

## Quiz di raccomandazione

`POST /quiz/score` — **pubblico**, no auth.

```json
// request
{
  "main_problem":    "overwhelm",        // overwhelm | distraction | knowledge_loss
  "work_style":      "structured",       // structured | flexible | creative
  "setup_tolerance": "medium",           // low | medium | high
  "goal":            "ship_tasks"        // ship_tasks | focus_time | build_knowledge
}

// response 200
{
  "success": true,
  "data": {
    "recommended_method": "gtd",
    "reasoning": "Getting Things Done ti dà una pipeline chiara...",
    "scores": [
      { "method": "gtd",          "score": 87, "explanation": "..." },
      { "method": "pomodoro",     "score": 54, "explanation": "..." },
      { "method": "second_brain", "score": 22, "explanation": "..." }
    ]
  }
}
```
