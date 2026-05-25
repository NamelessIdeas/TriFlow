# TriFlow — Android app

TriFlow è un'app Android nativa che integra **GTD (Getting Things Done)**, **Pomodoro** e **Second Brain (PARA)** in un'unica esperienza coerente. Capture rapido nell'Inbox, esecuzione focalizzata col timer Pomodoro, conoscenza organizzata nelle note Markdown con link bidirezionali.

L'app **richiede** un backend TriFlow conforme al contratto in [`backend-contract/`](./backend-contract). Il client non inventa endpoint né campi: tutto ciò che invia o legge è definito in `openapi.yaml` / `API_CONTRACT.md` / `DATA_MODEL.md`.

## Stack tecnico

- **Linguaggio**: Kotlin 2.1.21
- **UI**: Jetpack Compose (Material 3, dark theme)
- **Architettura**: MVVM + Clean Architecture multi-modulo (`:app`, `:data`, `:domain`, `:core`)
- **DI**: Hilt 2.56.2
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 + `kotlinx.serialization`
- **Async**: Coroutines + Flow
- **Persistence**: Room 2.7.0 (cache offline tasks / note), DataStore (impostazioni), EncryptedSharedPreferences (token)
- **Navigation**: Navigation Compose type-safe (`@Serializable` destinations)
- **Time**: `kotlinx-datetime` (`Instant`, `LocalDate`)
- **Markdown**: `compose-markdown` per il preview delle note
- **Build**: AGP 8.13.2, Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- **minSdk** 26, **targetSdk/compileSdk** 36

## Struttura del progetto

```
frontend/
├── app/                  # UI Compose, ViewModel, Navigation, theme
├── data/                 # Retrofit API, Room, mapper, repository impl, DI
├── domain/               # Modelli puri, interfacce repository, use case (no Android)
├── core/                 # Primitive condivise (ApiResponse, ErrorMapper, EncryptedTokenStore)
├── backend-contract/     # Contratto OpenAPI fornito dal backend (read-only)
├── gradle/libs.versions.toml
├── ARCHITECTURE.md
├── API_INTEGRATION.md
├── UI_GUIDE.md
└── CLAUDE.md
```

Vedi [ARCHITECTURE.md](./ARCHITECTURE.md) per la mappa dei layer e [API_INTEGRATION.md](./API_INTEGRATION.md) per il mapping endpoint → repository.

## Configurazione

### Base URL del backend

`data/build.gradle.kts` espone `BuildConfig.BASE_URL`:

```kotlin
debug   { buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/v1/\"") }
release { buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/v1/\"") }
```

- `10.0.2.2` è l'host della macchina dall'emulatore Android.
- Per device fisico, cambia `BASE_URL` con l'IP della tua LAN (es. `http://192.168.1.10:8080/api/v1/`).
- Per HTTPS in release, sostituisci la URL e rimuovi `network_security_config` debug-only se vuoi.

Cleartext HTTP è abilitato **solo in debug** verso `10.0.2.2` tramite `app/src/debug/res/xml/network_security_config.xml`.

### Logging

In `debug` `HttpLoggingInterceptor` è `BODY`. In release è `NONE` (vedi `NetworkModule`).

## Come si lancia

### Prerequisiti

- **Android Studio** Ladybug+ o Koala con AGP 8.13.x supportato
- **JDK 17** (l'output bytecode è JVM 11 ma il toolchain Gradle gira con 17+)
- Emulatore Android API 26+ (o device fisico) con il backend raggiungibile

### Backend in locale

Avvia il backend TriFlow su `localhost:8080`. Dall'emulatore Android lo raggiungi a `http://10.0.2.2:8080`.

### Build & install

Dentro Android Studio:

1. Apri la cartella `frontend/`
2. **Sync Gradle** (la prima volta scarica le dipendenze e KSP genera Room/Hilt)
3. Esegui `app` su emulatore / device

Da terminale:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### Test unitari

```bash
./gradlew test
```

(esegue i test JVM in `:data`, `:domain`, `:app`)

## Funzionalità implementate

| Area | Schermate |
|---|---|
| **Auth** | Splash, Login, Register (token in `EncryptedSharedPreferences`, refresh automatico via OkHttp `Authenticator`) |
| **Dashboard** | Active timer, "ora fai questo" (next action), KPI rapidi, link al Quiz |
| **GTD** | Hub a 5 tab — Inbox (capture + processing sheet), Tasks (filtri energy/contesto/progetto/tag), Projects, Contexts, Weekly Review |
| **Pomodoro** | Timer (Canvas progress circle, start/pause/complete/abort, opzionale task link), Stats (KPI + grafico settimanale + top task) |
| **Second Brain** | Lista note (search + chip PARA), Editor Markdown con toolbar, preview, dialog link `[[note]]`, dialog "promote to task" |
| **Quiz** | Wizard 4 step con state machine, risultato con metodo consigliato + score per metodo + reasoning |

## Convenzioni

- **Naming JSON**: `snake_case` — gestito centralmente da `JsonNamingStrategy.SnakeCase` in `NetworkModule.provideJson()`. I DTO in `data/remote/dto/` usano `camelCase` Kotlin.
- **Tipi**: `Instant` da `kotlinx-datetime` per timestamp, `String` per UUID.
- **Errori**: tutti i Retrofit response passano da `ApiCallExecutor` → `Outcome<T>`. La UI riceve `DomainError` e lo trasforma in stringhe via `userMessage()`.
- **State**: ogni ViewModel espone uno o più `StateFlow` raccolti con `collectAsStateWithLifecycle()`.

## Backend contract

I file in `backend-contract/` sono il contratto autoritativo:

- `openapi.yaml` — schema OpenAPI 3 di tutti gli endpoint
- `API_CONTRACT.md` — guida discorsiva agli endpoint
- `DATA_MODEL.md` — modello dati / enums / vincoli
- `BACKEND_CLAUDE.md` — note del team backend

**Non modificare campi o endpoint** senza un cambio coordinato del contratto.

## Documentazione di progetto

- [ARCHITECTURE.md](./ARCHITECTURE.md) — layer, moduli, data flow
- [API_INTEGRATION.md](./API_INTEGRATION.md) — endpoint → repository → ViewModel, token & refresh
- [UI_GUIDE.md](./UI_GUIDE.md) — design system, colori per metodo, componenti
- [CLAUDE.md](./CLAUDE.md) — onboarding per sessioni Claude Code future
