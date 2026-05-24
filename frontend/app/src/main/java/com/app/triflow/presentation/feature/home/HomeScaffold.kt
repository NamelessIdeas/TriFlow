package com.app.triflow.presentation.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.triflow.presentation.feature.dashboard.DashboardScreen
import com.app.triflow.presentation.feature.gtd.GtdHubScreen
import com.app.triflow.presentation.feature.pomodoro.PomodoroHubScreen
import com.app.triflow.presentation.feature.secondbrain.list.NotesListScreen
import com.app.triflow.presentation.navigation.HomeTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    onLogout: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onOpenNote: (String?) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TriFlow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                HomeTab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = entry == tab,
                        onClick = { tab = entry },
                        icon = { Icon(entry.icon, contentDescription = entry.label) },
                        label = { Text(entry.label, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
        },
    ) { padding ->
        when (tab) {
            HomeTab.Dashboard -> DashboardScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onLogout = onLogout,
                onOpenQuiz = onOpenQuiz,
            )
            HomeTab.Gtd -> GtdHubScreen(
                onOpenTaskDetail = onOpenTaskDetail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            HomeTab.Pomodoro -> PomodoroHubScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            HomeTab.SecondBrain -> NotesListScreen(
                onOpenNote = onOpenNote,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}
