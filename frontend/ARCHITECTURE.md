# ARCHITECTURE

TriFlow è un'app Android Clean-Architecture multi-modulo. L'obiettivo è isolare il dominio (regole, modelli) dalle implementazioni (rete, DB, UI), così da poter cambiare libreria di networking o storage senza toccare ViewModel e use case.

## Visione d'insieme

```
                        ┌──────────────────────────────┐
                        │            :app              │
                        │  Compose UI, ViewModel,      │
                        │  Navigation, Theme           │
                        └──────────────┬───────────────┘
                                       │  use case (suspend / Flow)
                                       ▼
                        ┌──────────────────────────────┐
                        │           :domain            │
                        │  Modelli, repository iface,  │
                        │  use case, Outcome<T>        │
                        │  (pure Kotlin — no Android)  │
                        └──────────────┬───────────────┘
                                       │  implementa
                                       ▼
       ┌────────────────────────────────────────────────────────────┐
       │                          :data                             │
       │  Retrofit API + DTO  ─┐                                    │
       │  Room (cache offline) ─┼─ Repository impl                  │
       │  EncryptedTokenStore ─┘                                    │
       └────────────────────────────────────────────────────────────┘
                                       │  ApiResponse<T>, errori HTTP
                                       ▼
                        ┌──────────────────────────────┐
                        │           :core              │
                        │  ApiResponse, ApiException,  │
                        │  ErrorMapper, ApiCallExecutor│
                        │  EncryptedTokenStore         │
                        └──────────────────────────────┘
```

## Moduli

### `:domain` (pure Kotlin)

- `model/` — data class senza dipendenze Android (`User`, `Task`, `Project`, `Note`, `PomodoroSession`, `QuizResult`, ...)
- `repository/` — **interfacce** consumate dagli use case: `AuthRepository`, `GtdRepository`, `PomodoroRepository`, `NotesRepository`, `DashboardRepository`, `QuizRepository`, `UserRepository`
- `usecase/` — un caso d'uso = una funzione iniettabile (`@Inject constructor(...)`). Ogni use case è un'unità di lavoro applicativa (es. `LoginUseCase`, `CompleteTaskUseCase`, `StartPomodoroUseCase`, `SubmitQuizUseCase`).
- `common/`
  - `Outcome<T>` — sealed interface `Success<T>` / `Failure(DomainError)`. Tutto ciò che può fallire ritorna `Outcome`. Niente eccezioni propagate.
  - `DomainError` — gerarchia stabile di errori applicativi (Network, Unauthorized, InvalidCredentials, InvalidInput, NotFound, Conflict, TimerActive, RateLimited, Server, Unknown).

Niente import `androidx.*` qui. Solo `kotlinx.coroutines`, `kotlinx.datetime`, `javax.inject`.

### `:core` (Android library, infra condivisa)

- `network/ApiResponse.kt` — modella l'envelope di risposta del backend: `{ ok, data?, error?, meta? }`.
- `network/ApiErrorCode.kt`, `ApiException.kt`, `ErrorMapper.kt` — mappano codici e HTTP status verso `DomainError`.
- `network/ApiCallExecutor.kt` — wrapper centralizzato che converte `Response<ApiResponse<T>>` in `Outcome<T>`. Tutti i repository chiamano via questo executor.
- `security/EncryptedTokenStore.kt` — wrapper su `EncryptedSharedPreferences` (AES-256-GCM + Android Keystore), espone uno `StateFlow<AuthSession?>` osservabile.

### `:data` (Android library, implementazioni)

- `remote/dto/` — DTO `@Serializable` (mappano 1:1 il JSON; `camelCase` di Kotlin → `snake_case` JSON via `JsonNamingStrategy.SnakeCase`).
- `remote/api/` — 11 interfacce Retrofit (`AuthApi`, `UsersApi`, `InboxApi`, `TasksApi`, `ProjectsApi`, `ContextsApi`, `ReviewsApi`, `PomodoroApi`, `NotesApi`, `DashboardApi`, `QuizApi`). Ogni endpoint ritorna `Response<ApiResponse<...>>`.
- `remote/mapper/` — funzioni `DtoX.toDomain()` / `EnumX.toApi()` / `String.toEnumX()`. Tutte pure.
- `remote/auth/` — `AuthInterceptor` (inietta Bearer), `TokenAuthenticator` (refresh sul 401), `TokenRefresher` (Retrofit interface dedicata che NON passa dall'`AuthInterceptor` per evitare loop).
- `local/db/` — `TriFlowDatabase` Room + `TaskEntity`, `NoteEntity`, `TaskDao`, `NoteDao`, `RoomConverters` (Instant ↔ epoch ms, `List<String>` ↔ JSON), `EntityMappers`.
- `local/datastore/SettingsStore.kt` — preferenze utente non sensibili.
- `repository/` — implementazioni `@Singleton` delle interfacce di dominio, iniettate via `RepositoryModule` con `@Binds`.
- `di/` — `NetworkModule`, `DatabaseModule`, `StorageModule`, `RepositoryModule` (Hilt).

### `:app`

- `presentation/feature/<feature>/` — un sotto-package per feature: ViewModel + Screen + sub-Composables.
- `presentation/common/` — primitive UI riusate (`UiState`, `LoadingView`, `EmptyView`, `ErrorView`, `Method`, `MethodBadge`, `SectionHeader`, `userMessage()`).
- `presentation/navigation/Destinations.kt` — sealed con `@Serializable Destination.X` (type-safe Navigation Compose).
- `presentation/navigation/TriFlowNavGraph.kt` — `NavHost` con `composable<Destination.X>`. Auth flow è un subgraph; Home Tab è gestito internamente da `HomeScaffold` con `NavigationBar`.
- `ui/theme/` — `Color.kt`, `Theme.kt`, `MethodColors.kt` (palette per metodo + `CompositionLocal`).

## Data flow tipico (lettura + scrittura)

### Lettura reattiva — esempio Tasks

```
TasksScreen
   └ collectAsStateWithLifecycle(tasksFlow)
TasksViewModel
   └ filterFlow.flatMapLatest { repo.observeTasks(filter) }   ← Flow<List<Task>>
   └ refresh() ─► GetTasksUseCase ─► repo.fetchTasks()        ← scrive cache Room
GtdRepositoryImpl
   ├ observeTasks(filter): combina Room + filtri (smart cast safe via local vals)
   └ fetchTasks(): TasksApi → DTO → toDomain() → upsert in Room
```

### Scrittura — esempio "completa task"

```
TaskDetailScreen.IconButton(onClick = vm::onToggleDone)
TaskDetailViewModel.onToggleDone()
   └ CompleteTaskUseCase(taskId, done)
GtdRepositoryImpl.completeTask(taskId, done)
   └ TasksApi.complete(...) via ApiCallExecutor → Outcome<TaskDto>
   └ on Success: toDomain() → TaskDao.upsert(...)
   └ Flow Room esistente emette il nuovo stato → UI si aggiorna
```

## Stato UI: `UiState` + `StateFlow`

Pattern uniforme:

```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Error(val error: DomainError) : UiState<Nothing>
}
```

Le ViewModel partono da `Loading`, caricano via use case, e fanno `_state.value = UiState.Success(...)` / `UiState.Error(...)`. La UI fa `when (val s = state) { ... }`.

Per le schermate con filtri reattivi (Tasks, Notes) si usa `combine` o `flatMapLatest` per ricomporre la lista quando i filtri cambiano.

## Errori: dal HTTP al messaggio utente

```
Retrofit Response<ApiResponse<T>>
     │  parseError(): legge errorBody come ApiResponse, prende code+message
     ▼
ApiException(code, status, message)
     │  ErrorMapper.toDomain()
     ▼
DomainError (NotFound, Unauthorized, Conflict, ...)
     │  userMessage() in presentation/common
     ▼
String localizzata in italiano per la UI
```

`ApiCallExecutor` cattura anche `IOException` → `DomainError.Network` e `Throwable` generici → `DomainError.Unknown`. Il `TokenAuthenticator` interpreta 401: prova un refresh via `TokenRefresher` (dedicata, senza authenticator), se ok ripete la richiesta; se no, ripulisce il token e il prossimo `Outcome.Failure` sarà `Unauthorized` → Splash → Login.

## Scelte chiave (e perché)

- **Multi-modulo invece di mono-app**: compile time migliori, ma soprattutto barriere statiche — il modulo `:domain` non può importare Android/Compose, quindi è impossibile inquinare le regole con dipendenze UI o framework.
- **`Outcome<T>` invece di `Result`/`try-catch`**: gli errori applicativi diventano espliciti nel tipo di ritorno; le ViewModel non devono fare `try-catch` né conoscere `HttpException`.
- **`kotlinx.serialization` con `JsonNamingStrategy.SnakeCase`** invece di Moshi + `@SerialName` sparsi: meno boilerplate, una sola sorgente di verità in `NetworkModule.provideJson()`. I DTO restano `camelCase`.
- **`kotlinx-datetime` invece di `java.time`**: evita la necessità di core library desugaring su minSdk 26 e si presta meglio a moduli pure-Kotlin.
- **Token in `EncryptedSharedPreferences`** invece di DataStore: per il salvataggio sicuro dei JWT usiamo lo store Android cifrato con AES-256-GCM + Android Keystore. DataStore è riservato a preferenze non sensibili.
- **Authenticator OkHttp con Retrofit "refresh-only" separata**: il client principale ha `AuthInterceptor` (Bearer) + `Authenticator` (refresh). Il refresh stesso però deve girare su un client SENZA quegli interceptor, altrimenti `401 → refresh → 401 → refresh → ...` può ciclare. Quindi due Retrofit, qualificate con `@RefreshRetrofit` / `@RefreshClient`.
- **Room come cache**, non source-of-truth: scrive il backend, poi aggiorna Room; la UI osserva Room. Refresh esplicito (`PullToRefreshBox`) ri-fetcha.
- **Smart cast cross-module**: quando un `data class` da `:domain` ha proprietà nullable, Kotlin **non** promuove la proprietà a non-null dopo un check se l'oggetto vive in un altro modulo. Nelle implementazioni `:data` usiamo `val x = it.field` locali prima di operare — questa convenzione è documentata nei mapper `observeTasks`/`observeNotes`.
- **Charts custom su Canvas** invece di Vico per la Pomodoro stats: meno superficie di rischio, layout su misura per la palette per-metodo.
- **Markdown actions come funzioni pure** (`MarkdownActions.kt`): wrap/prependLines/codeBlock/link — testabili senza Compose.
