package com.app.triflow.presentation.common

import com.app.triflow.domain.common.DomainError

/** Traduce un DomainError in un messaggio user-friendly in italiano. */
fun DomainError.userMessage(): String = when (this) {
    DomainError.Network -> "Nessuna connessione. Riprova quando sei online."
    DomainError.Unauthorized -> "Sessione scaduta. Effettua di nuovo l'accesso."
    is DomainError.InvalidCredentials -> message ?: "Email o password errati."
    is DomainError.InvalidInput -> message ?: "Dati non validi."
    DomainError.NotFound -> "Risorsa non trovata."
    is DomainError.AlreadyExists -> message ?: "Esiste già."
    is DomainError.Conflict -> message ?: "Operazione non valida nello stato attuale."
    DomainError.TimerActive -> "C'è già un timer Pomodoro attivo."
    DomainError.TimerNotFound -> "Nessun timer attivo."
    is DomainError.RateLimited -> message ?: "Troppe richieste, riprova tra qualche secondo."
    is DomainError.Server -> message ?: "Errore del server. Riprova più tardi."
    is DomainError.Unknown -> message ?: "Si è verificato un errore. Riprova."
}
