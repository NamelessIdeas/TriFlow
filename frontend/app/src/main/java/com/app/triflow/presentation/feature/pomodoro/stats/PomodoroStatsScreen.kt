package com.app.triflow.presentation.feature.pomodoro.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.PomodoroStats
import com.app.triflow.presentation.common.EmptyView
import com.app.triflow.presentation.common.ErrorView
import com.app.triflow.presentation.common.LoadingView
import com.app.triflow.presentation.common.SectionHeader
import com.app.triflow.presentation.common.UiState
import com.app.triflow.ui.theme.TriFlowTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroStatsScreen(viewModel: PomodoroStatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state is UiState.Loading,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val s = state) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(error = s.error, onRetry = viewModel::refresh)
            is UiState.Success -> Content(stats = s.value, days = viewModel.last7Days(), taskTitleResolver = { id ->
                tasks.firstOrNull { it.id == id }?.title ?: id.take(8)
            })
            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun Content(
    stats: PomodoroStats,
    days: List<LocalDate>,
    taskTitleResolver: (String) -> String,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { KpiRow(stats) }

        item { SectionHeader(title = "Ultimi 7 giorni") {} }
        item { DailyChart(days = days, byDay = stats.byDay) }

        item { SectionHeader(title = "Focus per task") {} }
        if (stats.byTask.isEmpty()) {
            item {
                EmptyView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    icon = Icons.Outlined.QueryStats,
                    title = "Nessuna sessione",
                    subtitle = "Inizia un focus per popolare le statistiche.",
                )
            }
        } else {
            val maxSec = stats.byTask.values.max()
            stats.byTask.entries
                .sortedByDescending { it.value }
                .take(8)
                .forEach { (id, seconds) ->
                    item { TaskBar(taskTitleResolver(id), seconds, maxSec) }
                }
        }
    }
}

@Composable
private fun KpiRow(stats: PomodoroStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Kpi(
            modifier = Modifier.weight(1f),
            value = stats.pomodorosCompleted.toString(),
            label = "Pomodori",
            color = MaterialTheme.colorScheme.secondary,
        )
        val hours = stats.focusSeconds / 3600
        val minutes = (stats.focusSeconds % 3600) / 60
        Kpi(
            modifier = Modifier.weight(1f),
            value = "${hours}h ${minutes}m",
            label = "Focus",
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun Kpi(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyChart(days: List<LocalDate>, byDay: Map<LocalDate, Int>) {
    val barColor = TriFlowTheme.methodColors.pomodoro
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val values = days.map { byDay[it] ?: 0 }
    val maxValue = (values.max().coerceAtLeast(1))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                val barCount = days.size
                val gap = 12.dp.toPx()
                val totalGap = gap * (barCount + 1)
                val barWidth = (size.width - totalGap) / barCount
                val baseY = size.height
                val maxBarHeight = size.height - 16.dp.toPx()

                values.forEachIndexed { index, value ->
                    val left = gap + index * (barWidth + gap)
                    val h = (value.toFloat() / maxValue) * maxBarHeight
                    // Track
                    drawRoundRect(
                        color = track,
                        topLeft = Offset(left, 16.dp.toPx()),
                        size = Size(barWidth, maxBarHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    )
                    if (value > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, baseY - h),
                            size = Size(barWidth, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                days.forEach { day ->
                    Text(
                        text = dayLabel(day.dayOfWeek),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskBar(title: String, seconds: Int, maxSec: Int) {
    val fraction = (seconds.toFloat() / maxSec).coerceIn(0f, 1f)
    val barColor = TriFlowTheme.methodColors.pomodoro
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            Text(
                if (h > 0) "${h}h ${m}m" else "${m}m",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor),
            )
        }
    }
}

private fun dayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Lun"
    DayOfWeek.TUESDAY -> "Mar"
    DayOfWeek.WEDNESDAY -> "Mer"
    DayOfWeek.THURSDAY -> "Gio"
    DayOfWeek.FRIDAY -> "Ven"
    DayOfWeek.SATURDAY -> "Sab"
    DayOfWeek.SUNDAY -> "Dom"
}
