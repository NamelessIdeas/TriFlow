# CLAUDE.md

Onboarding per sessioni Claude Code future che lavoreranno su questo repo. Tieni questo file aggiornato man mano che evolvono convenzioni o struttura.

## Cosa è questo progetto

App Android nativa multi-modulo (`:app`, `:data`, `:domain`, `:core`) in Kotlin + Compose + Material 3, che integra GTD, Pomodoro e Second Brain (PARA). Frontend di un backend HTTP definito dal contratto in `backend-contract/`.

Documenti di riferimento da leggere PRIMA di toccare codice:
- `README.md` — overview, stack, run
- `ARCHITECTURE.md` — layer, moduli, data flow, scelte chiave
- `API_INTEGRATION.md` — endpoint ↔ repository ↔ use case, auth/refresh
- `UI_GUIDE.md` — palette, componenti, navigazione
- `backend-contract/openapi.yaml` + `API_CONTRACT.md` + `DATA_MODEL.md` — fonte di verità per i contratti backend (**non modificare**)

## Mappa moduli (5 secondi)

| Modulo | Dipende da | Cosa contiene |
|---|---|---|
| `:domain` | nessuno (pure Kotlin) | modelli, repository interface, use case, `Outcome`, `DomainError` |
| `:core` | `:domain` | `ApiResponse`, `ApiCallExecutor`, `ErrorMapper`, `EncryptedTokenStore` |
| `:data` | `:domain`, `:core` | Retrofit API+DTO, Room, mapper, repository impl, DI |
| `:app` | tutti | Compose UI, ViewModel, Navigation, Theme |

**Regola d'oro**: `:domain` non importa mai `android.*` o `androidx.*`. Se serve una libreria Android, sta in `:core` o sopra.

## Convenzioni di codice

- **Stato UI**: ogni screen ha un ViewModel `@HiltViewModel` con `private val _state = MutableStateFlow(...)` e `val state = _state.asStateFlow()`. La UI raccoglie con `collectAsStateWithLifecycle()`.
- **Risultati**: tutto ciò che può fallire ritorna `Outcome<T>` (vedi `domain/common/Outcome.kt`). Niente eccezioni propagate fuori dai repository.
- **Errori UI**: `DomainError.userMessage()` (in `presentation/common`) per stringhe italiane.
- **JSON naming**: globale `snake_case` via `JsonNamingStrategy.SnakeCase` in `NetworkModule.provideJson()`. I DTO restano `camelCase`. Non aggiungere `@SerialName` salvo eccezioni reali.
- **Date/time**: `kotlinx.datetime.Instant`. Mai `java.time` o `Date`.
- **UUID**: `typealias Uuid = String`.
- **Filtri reattivi**: lato ViewModel `MutableStateFlow<Filter>` + `flatMapLatest { repo.observe(filter) }`. Lato repository, in `observeXxx(filter)` estrai i campi in `val` locali **prima** di usarli — i campi nullable di una `data class` cross-module non vengono smart-cast.
- **Niente eccezioni nei mapper**: ritornano nullable o default sensati (vedi `EnumMappers.kt`).
- **Comments**: solo dove il "perché" non è ovvio. Niente docstring decorativi.

## Come si aggiunge una schermata (passo passo)

1. **Destinazione** in `app/.../presentation/navigation/Destinations.kt`:
   ```kotlin
   @Serializable data object MyScreen : Destination
   // o con argomenti:
   @Serializable data class MyScreen(val id: String) : Destination
   ```
2. **ViewModel** `app/.../presentation/feature/myscreen/MyScreenViewModel.kt`:
   - `@HiltViewModel class ... @Inject constructor(private val useCase: ...) : ViewModel()`
   - Stato in `MutableStateFlow(UiState.Idle)` / `UiState.Loading`.
   - Carica via `viewModelScope.launch { when (val out = useCase(...)) { ... } }`.
3. **Composable** `MyScreen.kt`:
   ```kotlin
   @Composable
   fun MyScreen(
       onBack: () -> Unit,
       vm: MyScreenViewModel = hiltViewModel(),
   ) {
       val state by vm.state.collectAsStateWithLifecycle()
       when (val s = state) { ... }
   }
   ```
4. **Registrazione** in `TriFlowNavGraph.kt`:
   ```kotlin
   composable<Destination.MyScreen> { entry ->
       val route = entry.toRoute<Destination.MyScreen>()
       MyScreen(id = route.id, onBack = navController::popBackStack)
   }
   ```

## Come si aggiunge una chiamata API

Step a partire dal contratto OpenAPI.

1. **DTO** in `data/.../remote/dto/<Area>Dto.kt`:
   ```kotlin
   @Serializable
   data class FooDto(val id: String, val createdAt: Instant)
   ```
2. **Endpoint** in `data/.../remote/api/<Area>Api.kt`:
   ```kotlin
   @GET("foo/{id}")
   suspend fun get(@Path("id") id: String): Response<ApiResponse<FooDto>>
   ```
3. **Mapper** in `data/.../remote/mapper/<Area>Mapper.kt`:
   ```kotlin
   fun FooDto.toDomain(): Foo = Foo(id = id, createdAt = createdAt)
   ```
4. **Interfaccia repository** in `domain/.../repository/FooRepository.kt`:
   ```kotlin
   suspend fun get(id: String): Outcome<Foo>
   ```
5. **Implementazione** in `data/.../repository/FooRepositoryImpl.kt`:
   ```kotlin
   override suspend fun get(id: String): Outcome<Foo> =
       apiExecutor.execute { api.get(id) }.map { it.toDomain() }
   ```
6. **Use case** in `domain/.../usecase/foo/`:
   ```kotlin
   class GetFooUseCase @Inject constructor(private val repo: FooRepository) {
       suspend operator fun invoke(id: String): Outcome<Foo> = repo.get(id)
   }
   ```
7. **DI binding** in `data/.../di/RepositoryModule.kt`:
   ```kotlin
   @Binds @Singleton abstract fun bindFooRepository(impl: FooRepositoryImpl): FooRepository
   ```
8. Se l'API è nuova, registrala in `NetworkModule`:
   ```kotlin
   @Provides @Singleton fun provideFooApi(r: Retrofit): FooApi = r.create(FooApi::class.java)
   ```

## Auth & token — punti di interesse

- Lo store sicuro è `core/security/EncryptedTokenStore.kt`. La sessione viene letta via `StateFlow<AuthSession?>`.
- `data/.../remote/auth/AuthInterceptor.kt` inietta il Bearer. **Whitelist le route che NON devono avere il token** (login, register, refresh) — già configurato.
- `TokenAuthenticator.kt` gestisce il 401: prova `TokenRefresher.refresh(...)`, aggiorna lo store e ri-firma. Su fallimento → `clear()`. La UI vede `Unauthorized` e SplashScreen reindirizza al Login.
- **Non aggiungere un `Authenticator` al `@RefreshClient`** o l'auth refresh stesso potrebbe riciclare. Mantieni separati i due client OkHttp.

## Comandi Gradle utili

```bash
./gradlew :app:assembleDebug         # build APK debug
./gradlew :app:installDebug          # installa su emulatore/device
./gradlew test                       # tutti i test JVM
./gradlew :data:test                 # solo data
./gradlew :app:test                  # solo app
./gradlew :domain:test               # solo domain
./gradlew :app:lintDebug             # lint
./gradlew --refresh-dependencies     # forza refresh dipendenze
```

Su Android Studio l'utente userà Gradle Sync e il Run button.

## Aggiornare dipendenze

Tutte le versioni sono in `gradle/libs.versions.toml`. Modifica solo `[versions]`. Dopo:
1. Gradle Sync (lo fa l'utente in Android Studio).
2. Se KSP o Kotlin: verifica che le combinazioni siano compatibili (Hilt, Room, KSP, Kotlin si influenzano).
3. Se AGP: usa l'Upgrade Assistant di Android Studio.

### Combo verificate (al momento)

- AGP `8.13.2` ← funziona
- Kotlin `2.1.21` ← richiesto da Compose Compiler 2.1.x
- KSP `2.1.21-2.0.2` ← deve matchare Kotlin major.minor.patch
- Hilt `2.56.2` ← compatibile con AGP 8.13.x
- Room `2.7.0` ← richiesto su KSP 2.x (la 2.6.1 ha bug `Continuation<?>` variance)

## Gotcha & lezioni dal passato

- **AGP 9.x + Hilt < 2.57**: incompatibili. Restare su AGP 8.x finché Hilt non rilascia il fix.
- **AGP 9.x auto-applica `kotlin-android`** — su 8.x devi dichiarare esplicitamente `alias(libs.plugins.kotlin.android)` nei moduli Android.
- **Smart cast cross-module**: una `data class` nel modulo `:domain` con campo `val x: Foo?` non viene smart-cast a non-null nel modulo `:data`. Estrai sempre in un `val` locale prima dell'uso. Esempi già fissati: `GtdRepositoryImpl.observeTasks`, `NotesRepositoryImpl.observeNotes`, `TaskDetailScreen.NoteRow`, `NotesListScreen.NoteRow`.
- **Room + KSP 2.x**: `@Query` con `DELETE/UPDATE` che ritorna `Unit` esplode con "unexpected jvm signature V" su Room 2.6.x. Usa `Int` come ritorno e bumpa Room a `2.7.0`. `@Upsert` deve ritornare `Long` / `List<Long>`.
- **`kotlinOptions { jvmTarget = ... }` è deprecato**. Usa `kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }` fuori dal blocco `android { }`.
- **`android.useAndroidX=true` e `android.enableJetifier=false`** in `gradle.properties` — entrambi necessari.

## Backend contract (non toccare)

`backend-contract/` contiene `openapi.yaml`, `API_CONTRACT.md`, `DATA_MODEL.md`, `BACKEND_CLAUDE.md`. È **read-only** dal punto di vista del frontend. Se serve un nuovo endpoint o un campo aggiunto, è una conversazione col team backend, non una modifica unilaterale.

## Come lavorare in coppia con l'utente

L'utente esegue Gradle Sync e Run dentro Android Studio. Tu scrivi il codice e aspetti il suo feedback ("sync ok" / errori).

- **Procedi per fasi** e fermati per conferma a fine fase. Non concatenare fasi senza ok.
- **Spiega solo le scelte importanti**, in italiano, breve. Niente paragrafi su decisioni banali.
- **Niente aggiunte non richieste**: no feature flag inventati, no helper "ti potrebbe servire", no error handling per scenari impossibili.
- **Niente file di documentazione non richiesti**. Questo `CLAUDE.md`, README e doc files sono stati esplicitamente richiesti — ulteriori file di doc vanno chiesti prima.
