package com.app.triflow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Task
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeTab(
    val label: String,
    val icon: ImageVector,
    val destination: Destination,
) {
    Dashboard("Home", Icons.Outlined.Home, Destination.Dashboard),
    Gtd("GTD", Icons.Outlined.Task, Destination.Gtd),
    Pomodoro("Pomodoro", Icons.Outlined.PlayCircleOutline, Destination.Pomodoro),
    SecondBrain("Brain", Icons.Outlined.AutoStories, Destination.SecondBrain),
}
