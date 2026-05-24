package com.app.triflow.domain.model

data class UserPreferences(
    val pomodoroDurationMin: Int,
    val shortBreakMin: Int,
    val longBreakMin: Int,
    val pomodorosUntilLongBreak: Int,
    val timezone: String,
) {
    companion object {
        val Default = UserPreferences(
            pomodoroDurationMin = 25,
            shortBreakMin = 5,
            longBreakMin = 15,
            pomodorosUntilLongBreak = 4,
            timezone = "Europe/Rome",
        )
    }
}
