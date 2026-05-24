package com.app.triflow.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Destinazioni della navigazione, type-safe via kotlinx.serialization.
 * Ogni `object` o `data class` è una rotta univoca.
 */
sealed interface Destination {

    // --- Root level ---
    @Serializable data object Splash : Destination
    @Serializable data object AuthGraph : Destination
    @Serializable data object HomeGraph : Destination
    @Serializable data object Quiz : Destination

    // --- Auth ---
    @Serializable data object Login : Destination
    @Serializable data object Register : Destination

    // --- Home (bottom bar) ---
    @Serializable data object Dashboard : Destination
    @Serializable data object Gtd : Destination
    @Serializable data object Pomodoro : Destination
    @Serializable data object SecondBrain : Destination

    // --- GTD details ---
    @Serializable data class TaskDetail(val taskId: String) : Destination
    @Serializable data object Inbox : Destination
    @Serializable data class ProcessInbox(val itemId: String) : Destination
    @Serializable data object WeeklyReview : Destination

    // --- Pomodoro ---
    @Serializable data object PomodoroStats : Destination

    // --- Second Brain ---
    @Serializable data class NoteEditor(val noteId: String?) : Destination
}
