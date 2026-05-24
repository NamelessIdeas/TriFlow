package com.app.triflow.presentation.feature.gtd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.triflow.presentation.feature.gtd.contexts.ContextsScreen
import com.app.triflow.presentation.feature.gtd.inbox.InboxScreen
import com.app.triflow.presentation.feature.gtd.projects.ProjectsScreen
import com.app.triflow.presentation.feature.gtd.review.WeeklyReviewScreen
import com.app.triflow.presentation.feature.gtd.tasks.TasksScreen

enum class GtdSection(val label: String) {
    Inbox("Inbox"),
    Tasks("Tasks"),
    Projects("Progetti"),
    Contexts("Contesti"),
    Review("Review"),
}

@Composable
fun GtdHubScreen(
    onOpenTaskDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by rememberSaveable { mutableStateOf(GtdSection.Inbox) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = section.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            GtdSection.entries.forEach { entry ->
                Tab(
                    selected = entry == section,
                    onClick = { section = entry },
                    text = { Text(entry.label, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }
        when (section) {
            GtdSection.Inbox -> InboxScreen()
            GtdSection.Tasks -> TasksScreen(onOpenTaskDetail = onOpenTaskDetail)
            GtdSection.Projects -> ProjectsScreen()
            GtdSection.Contexts -> ContextsScreen()
            GtdSection.Review -> WeeklyReviewScreen(onOpenTaskDetail = onOpenTaskDetail)
        }
    }
}
