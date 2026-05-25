# API_INTEGRATION

Questo documento descrive **come** il client TriFlow Android dialoga col backend definito in `backend-contract/openapi.yaml`. Tutti gli endpoint sono prefissati da `BuildConfig.BASE_URL` (default `http://10.0.2.2:8080/api/v1/`).

## Envelope di risposta

Ogni risposta dal backend ha la forma:

```json
{
  "ok": true,
  "data": { ... },
  "error": { "code": "string", "message": "string", "details": { ... } },
  "meta": { "page": 1, "total": 42 }
}
```

Mappata da `core/network/ApiResponse.kt`:

```kotlin
@Serializable
data class ApiResponse<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val meta: PageMeta? = null,
)
```

`ApiCallExecutor.execute { api.call(...) }` riceve un `Response<ApiResponse<T>>`, e:

- HTTP 2xx + `ok=true` + `data!=null` → `Outcome.Success(data)`
- HTTP 2xx + `ok=false` → parse di `error` → `ApiException` → `ErrorMapper.toDomain()` → `Outcome.Failure(...)`
- HTTP non-2xx → legge `errorBody` come `ApiResponse`, applica lo stesso mapping; se non parsabile, fallback su HTTP status
- `IOException` → `Outcome.Failure(DomainError.Network)`
- `Throwable` generico → `Outcome.Failure(DomainError.Unknown(...))`

## Naming

`kotlinx.serialization` con `JsonNamingStrategy.SnakeCase` configurato in `NetworkModule.provideJson()`:

```kotlin
val createdAt: Instant   // ← serializzato/deserializzato come "created_at"
```

I DTO non hanno bisogno di `@SerialName` (eccetto edge case con acronimi).

## Endpoints → ApiInterface → Repository → UseCase

### Auth (`AuthApi`)

| Endpoint | Repository | Use case |
|---|---|---|
| `POST /auth/register` | `AuthRepository.register(...)` | `RegisterUseCase` |
| `POST /auth/login` | `AuthRepository.login(...)` | `LoginUseCase` |
| `POST /auth/refresh` | (interno) usato da `TokenAuthenticator` via `TokenRefresher` | — |
| `POST /auth/logout` | `AuthRepository.logout()` | `LogoutUseCase` |

Su login/register il backend ritorna `{ access_token, refresh_token, expires_at, user }`. La sessione finisce in `EncryptedTokenStore` come `AuthSession(access, refresh, expiresAt)`.

### Users (`UsersApi`)

| Endpoint | Repository |
|---|---|
| `GET /users/me` | `UserRepository.fetchMe()` |
| `PUT /users/me` | `UserRepository.updateMe(name, locale)` |
| `GET /users/me/preferences` | `UserRepository.fetchPreferences()` |
| `PUT /users/me/preferences` | `UserRepository.updatePreferences(...)` |

### Dashboard (`DashboardApi`)

| Endpoint | Repository | Use case | UI |
|---|---|---|---|
| `GET /dashboard` | `DashboardRepository.fetch()` | `GetDashboardUseCase` | `DashboardScreen` |

### GTD — Inbox

| Endpoint | Repository | Use case |
|---|---|---|
| `POST /inbox` (capture) | `GtdRepository.captureInbox(text)` | `CaptureInboxUseCase` |
| `GET /inbox` | `GtdRepository.observeInbox()` + `fetchInbox()` | `GetInboxUseCase` |
| `POST /inbox/{id}/process` | `GtdRepository.processInbox(id, action, ...)` | `ProcessInboxUseCase` |

### GTD — Tasks (`TasksApi`)

| Endpoint | Repository |
|---|---|
| `POST /tasks` | `createTask(draft)` |
| `GET /tasks?status=...&context_id=...&project_id=...` | `fetchTasks(filter)` (alimenta Room) |
| `GET /tasks/{id}` | `getTaskWithRelations(id)` |
| `PATCH /tasks/{id}` | `updateTask(id, patch)` |
| `DELETE /tasks/{id}` | `deleteTask(id)` |
| `GET /tasks/{id}/context` | (in `getTaskWithRelations`) |

`observeTasks(filter)` è un `Flow<List<Task>>` da Room. Si combina con `TaskFilter` lato ViewModel via `flatMapLatest`.

### GTD — Projects / Contexts / Reviews

| Endpoint | Repository |
|---|---|
| `POST /projects`, `GET /projects`, `GET /projects/{id}`, `PUT /projects/{id}`, `DELETE /projects/{id}` | `GtdRepository.*Project*` |
| `POST /contexts`, `GET /contexts`, `DELETE /contexts/{id}` | `GtdRepository.*Context*` |
| `GET /reviews/weekly` | `GtdRepository.fetchWeeklyReview()` |

### Pomodoro (`PomodoroApi`)

| Endpoint | Repository | Use case |
|---|---|---|
| `POST /pomodoros/start` | `start(params)` | `StartPomodoroUseCase` |
| `POST /pomodoros/pause` | `pause()` | `PausePomodoroUseCase` |
| `POST /pomodoros/resume` | `resume()` | `ResumePomodoroUseCase` |
| `GET /pomodoros/current` | `fetchActive()` + `observeActive()` | `ObserveActiveTimerUseCase` |
| `POST /pomodoros/complete` | `complete()` | `CompletePomodoroUseCase` |
| `POST /pomodoros/abort` | `abort()` | `AbortPomodoroUseCase` |
| `GET /pomodoros/sessions` | `fetchSessions(filter)` | — |
| `GET /pomodoros/stats?range=...` | `fetchStats(range)` | `GetPomodoroStatsUseCase` |

Il timer locale fa il countdown via `ticker` (1 Hz) partendo da `ActiveTimer.remainingSec` ricevuto dal server. `observeActive()` rifetcha periodicamente e su transizioni di stato.

### Second Brain — Notes (`NotesApi`)

| Endpoint | Repository |
|---|---|
| `POST /notes` | `createNote(draft)` |
| `GET /notes?q=&para_category=&tag=` | `fetchNotes(filter)` (alimenta Room) |
| `GET /notes/{id}` | `getNote(id)` |
| `PATCH /notes/{id}` | `updateNote(id, patch)` |
| `DELETE /notes/{id}` | `deleteNote(id)` |
| `GET /notes/{id}/backlinks` | `getBacklinks(id)` |
| `GET /notes/{id}/links`, `POST /notes/{id}/links`, `DELETE /notes/{id}/links/{targetId}` | `addLink(...)`, `removeLink(...)` |
| `POST /notes/{id}/refs`, `DELETE /notes/{id}/refs/{refType}/{refId}` | `linkRef(...)`, `unlinkRef(...)` |
| `POST /notes/{id}/promote-to-task` | `promoteToTask(id, params)` |

### Quiz (`QuizApi`)

| Endpoint | Repository | Use case |
|---|---|---|
| `POST /quiz/score` | `QuizRepository.score(answers)` | `SubmitQuizUseCase` |

## Auth flow & token refresh

Token storage: `core/security/EncryptedTokenStore.kt` su `EncryptedSharedPreferences` (`AES256_GCM`/`AES256_SIV` con master key del Keystore). Espone:

```kotlin
val session: StateFlow<AuthSession?>
fun save(session: AuthSession)
fun clear()
```

`AuthSession` contiene `accessToken`, `refreshToken`, `accessExpiresAt: Instant?` e ha `isExpired()` con un piccolo skew.

### Iniezione del Bearer — `AuthInterceptor`

Su ogni richiesta non in whitelist (login, register, refresh) aggiunge:

```
Authorization: Bearer <accessToken>
```

### Refresh sul 401 — `TokenAuthenticator`

`OkHttp` invoca `Authenticator.authenticate(response)` quando il server restituisce 401. L'implementazione:

1. Legge il refresh token dallo store. Se manca → ritorna `null` (l'errore propaga).
2. Chiama `TokenRefresher.refresh(...)`.
3. Se ok, salva i nuovi token e re-firma la request originale col nuovo access.
4. Se fallisce, pulisce lo store. La UI vedrà `Unauthorized` e SplashScreen porterà al Login.

**Perché Retrofit separato per il refresh?** Il client principale ha `AuthInterceptor` e `Authenticator`. Se il refresh stesso girasse su questo client e fosse anch'esso 401, l'`Authenticator` verrebbe richiamato → loop. La soluzione è una `OkHttpClient` priva di quegli interceptor, qualificata con `@RefreshClient`, e una `Retrofit` qualificata `@RefreshRetrofit`, che fornisce un `TokenRefresher` autonomo (`@POST("auth/refresh")`).

## Logging

`HttpLoggingInterceptor` con livello da `BuildConfig.ENABLE_HTTP_LOGGING`:

- `debug` → `BODY` (utile per ispezionare payload)
- `release` → `NONE`

## Cleartext

`app/src/debug/res/xml/network_security_config.xml` permette cleartext solo verso `10.0.2.2`. In release, niente cleartext: serve un endpoint HTTPS o aggiornare la config.

## Mapping errori HTTP

`ErrorMapper.toDomain(ApiException)`:

| Codice / status | DomainError |
|---|---|
| `invalid_credentials` o 401 su `auth/*` | `InvalidCredentials` |
| 401 altrove | `Unauthorized` |
| `invalid_input` / 400 / 422 | `InvalidInput(message, details)` |
| 404 | `NotFound` |
| `already_exists` / 409 (su create) | `AlreadyExists` |
| `conflict` / 409 (generale) | `Conflict` |
| `timer_active` | `TimerActive` |
| `timer_not_found` | `TimerNotFound` |
| 429 | `RateLimited` |
| 5xx | `Server` |
| Altro | `Unknown` |

Vedi `presentation/common/userMessage()` per le stringhe italiane visualizzate.
