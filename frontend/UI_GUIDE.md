# UI_GUIDE

Linguaggio visivo di TriFlow. Tema dark "near-black" con tre accenti dedicati ai tre metodi che l'app integra.

## Filosofia

- **Dark first**, alto contrasto sulle superfici interattive, superfici stratificate per gerarchia.
- **Un colore = un metodo**: viola=GTD, verde=Pomodoro, ambra=Second Brain. Questa regola è coerente in chip, badge, FAB, progress, grafici. È la mappa mentale dell'app.
- **Material 3** come base, ma i 3 metodi non si "sovrappongono" agli slot `primary/secondary/tertiary` — viaggiano su un `CompositionLocal` (`LocalMethodColors`) per restare disponibili a prescindere dallo schema.
- **Niente animazioni gratuite**: Crossfade nei wizard (Quiz), AnimatedContent per i pulsanti del timer, nient'altro.

## Palette (`ui/theme/Color.kt`)

### Superfici (dark)

| Token | Hex | Uso |
|---|---|---|
| `Background` | `#0B0B0F` | sfondo schermo |
| `Surface` | `#15151B` | card, top bar, sheet |
| `SurfaceVariant` | `#1F1F27` | chip neutri, input |
| `Outline` | `#2A2A33` | bordi sottili |
| `OutlineVariant` | `#3A3A45` | bordi enfatizzati |

### Testo

| Token | Hex | Uso |
|---|---|---|
| `OnBackground` / `OnSurface` | `#E7E7EA` | titoli, body principale |
| `OnSurfaceVariant` | `#9A9AA1` | sotto-titoli, meta |
| `OnSurfaceMuted` | `#6E6E78` | helper text, disabled |

### Accenti per metodo

| Metodo | Base | Soft | Surface (tinto) |
|---|---|---|---|
| **GTD** | `GtdPurple` `#8B5CF6` | `GtdPurpleSoft` `#B69BFA` | `GtdPurpleSurface` `#221A36` |
| **Pomodoro** | `PomodoroGreen` `#34D399` | `PomodoroGreenSoft` `#6FE3B8` | `PomodoroGreenSurface` `#14302A` |
| **Second Brain** | `BrainAmber` `#F59E0B` | `BrainAmberSoft` `#FBC678` | `BrainAmberSurface` `#33240A` |

### Stato

| Token | Hex | Uso |
|---|---|---|
| `ErrorRed` | `#EF4444` | errori, conferme distruttive |

### Mapping Material 3 (`Theme.kt`)

`TriFlowDarkScheme` mappa `primary` → `GtdPurple`, `secondary` → `PomodoroGreen`, `tertiary` → `BrainAmber`. Questo è scelta deliberata: serve un default vivibile per Compose stock (FAB, switch, Material3 components di base) anche prima che il `MethodPalette` venga consultato esplicitamente. Quando una schermata vive "dentro un metodo", **prima** legge i colori da `TriFlowTheme.methodColors`, **dopo** i colori semantici da `MaterialTheme.colorScheme`.

### Come usare i colori per metodo

```kotlin
@Composable
fun GtdHeader() {
    val palette = TriFlowTheme.methodColors
    Surface(color = palette.gtdSurface, contentColor = palette.gtd) { ... }
}
```

Oppure usa il componente `MethodBadge`:

```kotlin
MethodBadge(method = Method.SecondBrain, label = "area")
```

## Tipografia

Material 3 dark default + qualche ruolo applicato:

| Ruolo | Stile | Esempio |
|---|---|---|
| Hero / titolo schermata | `headlineSmall` | "Dettaglio task" |
| Card title | `titleMedium` | titolo nota |
| Lista row | `bodyLarge` | titolo task |
| Meta sotto-row | `bodySmall` + `OnSurfaceVariant` | "8 min · low · scadenza 2026-06-01" |
| Tag | `labelSmall` + tertiary | `#deepwork` |
| Errori | `bodySmall` + `error` | banner sopra le liste |

## Componenti riusabili (`presentation/common/`)

### `Method`

```kotlin
enum class Method { Gtd, Pomodoro, SecondBrain }
```

Risolve la palette via `LocalMethodColors`.

### `MethodBadge(method, label)`

Pill colorata (sfondo `surface tinto`, contenuto `base`). Per status task, kind pomodoro, PARA category.

### `SectionHeader(title, action)`

Riga `headlineSmall` + opzionale CTA testuale a destra. Spaziatura standard `padding(horizontal = 20.dp, vertical = 12.dp)`.

### `LoadingView` / `EmptyView` / `ErrorView`

Pattern di stato uniforme — `LoadingView` centrato, `EmptyView` con icona+titolo+sottotitolo+CTA opzionale, `ErrorView` con icona errore + messaggio + retry.

### `UiState<T>`

`Idle | Loading | Success(value) | Error(DomainError)`. Le ViewModel partono da `Loading`, le screen fanno `when (val s = state) { ... }`.

### `userMessage()`

Extension su `DomainError` → `String` italiana, da usare nella UI:

```kotlin
Text(state.error?.userMessage() ?: "", color = MaterialTheme.colorScheme.error)
```

## Navigazione

- **Type-safe**: ogni destinazione è una `@Serializable data class` / `data object` in `Destinations.kt`. Le destinazioni con argomenti (`TaskDetail(taskId)`, `NoteEditor(noteId)`) sono `data class`.
- **Subgraph Auth**: `Destination.AuthGraph` racchiude `Login`/`Register`.
- **Home con tab interno**: `Destination.HomeGraph` punta a `HomeScaffold`, che usa `NavigationBar` (Material 3) per Dashboard / GTD / Pomodoro / Second Brain. Niente subgraph per i tab — è un `when (selected)` interno.
- **Sub-hub a tab orizzontali**: `GtdHubScreen` e `PomodoroHubScreen` usano `SecondaryTabRow` (Inbox/Tasks/Projects/Contexts/Review e Timer/Stats).

## Schermate principali

### Dashboard

Card stratificate verticalmente: TimerCard (se attivo), "Next action", KPI row (3 mini-card), suggerimenti.

### GTD

- **Inbox**: capture bar in cima (TextField + send), lista item, `ModalBottomSheet` per il processing (3 azioni — task / project / discard).
- **Tasks**: barra filtri orizzontale scrollabile (status chip, energy chip, contesto chip, progetto chip), FAB `+`, `CreateTaskSheet` con tutti i campi.
- **Projects** / **Contexts**: lista + create dialog/sheet inline.
- **Weekly Review**: report card-based con metriche e checklist.

### Pomodoro

- **Timer**: Canvas progress circle a tutto schermo, etichetta del task collegato, controlli adaptive (start / pause / resume / complete / abort) che si mostrano in base a stato.
- **Stats**: KPI cards (focus minutes, sessioni, completion rate), Canvas bar chart settimanale, top task list con barre orizzontali.

### Second Brain

- **List**: search field in cima, chip PARA orizzontali, FAB, lista note con preview prima riga + tag.
- **Editor**: tab Editor/Preview, `MarkdownToolbar` con IconButton (bold/italic/h1-h3/list/quote/code/link), dialog `[[link]]` con autocompletamento, dialog "promote to task" che apre il form di creazione.

### Quiz

Wizard a 4 step. Tutta la schermata oscilla tra `QuestionPane` (LinearProgressIndicator + 3 OptionRow) e `ResultPane` (card metodo consigliato + bar per ogni score + reasoning) via `Crossfade`.

## Componenti material adottati

- `PullToRefreshBox` per il refresh delle liste server-backed (Tasks, Notes, Inbox).
- `ModalBottomSheet` per processing inbox, creazione task, promote-to-task, link picker.
- `SecondaryTabRow` per i hub interni.
- `NavigationBar` per i tab della home.
- `FilterChip` per i filtri inline (Notes PARA, Tasks status/energy).
- `AssistChip` per i tag (read-only).
- `Card`/`OutlinedCard` per i raggruppamenti.

## Iconografia

`androidx.compose.material.icons.outlined.*` e `automirrored.outlined.*`. Mai mescolare filled e outlined nella stessa schermata. Eccezione: CheckCircle (filled) per "done" è ammesso perché è un'icona di stato.

## Spaziature

- Sfondo schermo: `padding(horizontal = 20.dp)` per il content principale
- Card: `padding(horizontal = 20.dp, vertical = 12.dp)` esterno, `padding(16.dp)` interno
- Row in liste: `padding(horizontal = 20.dp, vertical = 12.dp)`
- Tra elementi correlati: `Spacer(Modifier.height(8.dp))`
- `Arrangement.spacedBy(8.dp)` per chip rows

## Accessibility

- Tutti gli `IconButton` hanno `contentDescription` testuale italiana.
- `singleLine = true` solo dove esplicitamente single-line (search field).
- Contrasto: `OnSurface #E7E7EA` su `Surface #15151B` → >12:1.
