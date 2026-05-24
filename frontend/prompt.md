Sei uno sviluppatore Android senior. Devi creare l'app Android nativa per il
backend "TriFlow", un'app di produttività che integra GTD, Pomodoro e Second
Brain in un unico flusso.

## Stato di partenza (importante)
Il progetto Android è già stato creato in Android Studio con il template Empty
Activity (Compose), Kotlin DSL, minSdk 26: esistono già la cartella app/, il
Gradle wrapper, settings.gradle.kts e i build.gradle.kts. NON ricreare lo
scheletro e NON toccare local.properties: lavora dentro la struttura esistente,
aggiungendo moduli/package, dipendenze nei build.gradle.kts e codice. Dopo ogni
modifica alle dipendenze ricordami di fare il Gradle sync in Android Studio. Il
Gradle sync e l'esecuzione dell'app li faccio io in Android Studio: tu scrivi e
modifichi i file, io faccio Sync e Run e ti incollo eventuali errori di build.

## Contratto del backend (fonte di verità)
Nella cartella ./backend-contract trovi il contratto del backend già sviluppato:
openapi.yaml e API_CONTRACT.md sono la fonte di verità per endpoint e modelli,
DATA_MODEL.md descrive le entità e le relazioni, BACKEND_CLAUDE.md dà contesto
sulle convenzioni e sul formato delle risposte. Leggili PRIMA di proporre la
struttura e allinea DTO ed endpoint a quei file, senza inventare campi. Se
qualcosa non è chiaro o manca nel contratto, chiedimelo prima di assumerlo.

## Stack tecnologico (vincolante)
- Linguaggio: Kotlin
- IDE target: Android Studio, build con Gradle (Kotlin DSL, build.gradle.kts)
- Networking: Retrofit + OkHttp (interceptor per auth, logging, gestione errori)
- Serializzazione: kotlinx.serialization o Moshi (scegli e usa coerentemente)
- Async: Coroutines + Flow
- Architettura: MVVM + Clean Architecture (data / domain / presentation)
- DI: Hilt
- Storage locale: DataStore per token e preferenze; Room per cache offline di
  task e note
- UI: Jetpack Compose con Material 3, tema scuro coerente con il mockup fornito
  (sfondo quasi nero, accenti viola per GTD, verde per Pomodoro, ambra per
  Second Brain)
- Navigazione: Navigation Compose
- minSdk 26, targetSdk aggiornato

## Architettura a layer
Organizza il codice in layer chiari (a package o a moduli Gradle, motiva la scelta):
- data        -> Retrofit API services, DTO, mappers, repository impl, Room, DataStore
- domain      -> modelli di dominio, interfacce repository, use case
- presentation-> ViewModel, stati UI, screen Compose, componenti riutilizzabili
- core/di     -> moduli Hilt, config Retrofit/OkHttp, gestione token
Mantieni la dipendenza unidirezionale: presentation -> domain <- data.

## Networking (dettagli)
- Configura OkHttp con: interceptor che aggiunge il Bearer token, interceptor di
  logging (solo in debug), e un Authenticator che usa il refresh token per
  rinnovare l'access token scaduto e ripetere la richiesta.
- Centralizza la gestione delle risposte in un wrapper Result/Either coerente con
  il formato success/error del backend (vedi API_CONTRACT.md).
- Base URL configurabile per ambiente (debug = emulatore 10.0.2.2, release).

## Funzionalità e schermate
Costruisci queste sezioni, rispecchiando l'integrazione tra i tre sistemi:

1. AUTH: schermate di registrazione e login, salvataggio sicuro dei token,
   logout, redirect automatico in base allo stato di autenticazione.

2. DASHBOARD (home unificata): task di oggi, eventuale timer Pomodoro attivo con
   controlli rapidi, note recenti, progressi della giornata. Usa l'endpoint
   aggregato del backend.

3. GTD:
   - Inbox con cattura veloce (campo + bottone "+")
   - Processamento di un item (trasformalo in task o progetto)
   - Lista task con filtri per context/@tag e status
   - Dettaglio task: note, sessioni Pomodoro collegate, note Second Brain collegate
   - Vista progetti e schermata di Weekly Review

4. POMODORO:
   - Timer a schermo intero con animazione/progress circolare, start/pause/stop
   - Possibilità di collegare la sessione a un task
   - Lo stato del timer va sincronizzato col backend (sessione attiva su Redis):
     gestisci correttamente il caso di app chiusa/riaperta recuperando la sessione
   - Schermata statistiche (pomodori per giorno, focus per progetto) con grafici

5. SECOND BRAIN:
   - Lista note con ricerca, filtro per categoria PARA e per tag
   - Editor nota in Markdown con anteprima
   - Visualizzazione backlink/collegamenti e azione "promuovi a task GTD"

6. QUIZ DI RACCOMANDAZIONE: replica il questionario del mockup (problema
   principale, come preferisci lavorare, quanto setup, cosa vuoi ottenere) con i
   bottoni a scelta singola; invia le risposte all'endpoint del backend e mostra
   il risultato con il metodo consigliato e i punteggi dei tre sistemi.

## Qualità e robustezza
- Gestione stati UI: Loading / Success / Error / Empty in ogni schermata
- Cache offline-first dove sensato (task e note via Room), con sync al ritorno online
- Gestione errori utente-friendly (messaggi chiari, retry)
- Pull-to-refresh sulle liste
- Preview Compose per i componenti principali
- Test: unit test sui ViewModel e sui mapper DTO->dominio

## Documentazione da produrre (obbligatoria)
Crea questi file Markdown nel progetto:
- README.md          : descrizione app, come configurarla, come puntare al backend,
                       come buildarla in Android Studio
- ARCHITECTURE.md    : spiegazione dei layer, scelte (Hilt, Compose, Room) e flusso
                       dei dati da API a UI
- API_INTEGRATION.md : mappatura tra endpoint del backend e service Retrofit/use case,
                       più la strategia di gestione token e refresh
- UI_GUIDE.md        : sistema di design (colori per i 3 metodi, tipografia,
                       componenti riutilizzabili) coerente col mockup

## Output finale (obbligatorio)
Alla fine, dopo aver verificato col mio aiuto che il progetto compili in Android
Studio, genera un file CLAUDE.md nella root pensato per future sessioni di
modifica, contenente:
- panoramica dell'app e dello stack
- mappa dei moduli/package e responsabilità
- convenzioni Kotlin/Compose usate e pattern MVVM adottato
- come aggiungere una nuova schermata passo-passo (DTO -> mapper -> repository ->
  use case -> ViewModel -> screen + navigazione)
- come aggiungere una chiamata API e gestirne errori e cache
- come è gestita l'autenticazione e il refresh dei token
- comandi gradle utili e note sul testing
- dipendenza dal contratto API del backend: cosa fare se il contratto cambia

## Modo di lavorare
Procedi per fasi: PRIMA leggi i file in ./backend-contract, poi proponi la
struttura del progetto, i DTO e i modelli di dominio, e FERMATI per mia conferma.
Poi implementa il layer data (Retrofit + Room + DataStore + DI), poi domain e use
case, poi le schermate una sezione alla volta (auth, dashboard, GTD, pomodoro,
second brain, quiz). Dopo ogni blocco che tocca le dipendenze, ricordami di fare
il Gradle sync. Spiega le scelte importanti man mano. Codice idiomatico Kotlin,
Compose pulito, niente logica di business nelle UI.
