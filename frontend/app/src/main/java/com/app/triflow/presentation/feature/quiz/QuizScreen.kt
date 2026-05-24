package com.app.triflow.presentation.feature.quiz

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.domain.model.Goal
import com.app.triflow.domain.model.MainProblem
import com.app.triflow.domain.model.MethodScore
import com.app.triflow.domain.model.QuizResult
import com.app.triflow.domain.model.RecommendedMethod
import com.app.triflow.domain.model.SetupTolerance
import com.app.triflow.domain.model.WorkStyle
import com.app.triflow.ui.theme.TriFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result = state.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (result != null) "Risultato" else "Quiz TriFlow") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (result == null && state.step == QuizStep.MainProblem) {
                                onBack()
                            } else if (result == null) {
                                viewModel.back()
                            } else {
                                onBack()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Indietro")
                    }
                },
            )
        },
    ) { padding ->
        Crossfade(
            targetState = result != null,
            label = "quiz-cross",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) { showResult ->
            if (showResult && result != null) {
                ResultPane(
                    result = result,
                    onRestart = viewModel::restart,
                    onClose = onBack,
                )
            } else {
                QuestionPane(
                    state = state,
                    onChoose = viewModel::choose,
                )
            }
        }
    }
}

@Composable
private fun QuestionPane(
    state: QuizUiState,
    onChoose: (Any) -> Unit,
) {
    val totalSteps = QuizStep.entries.size
    val stepIndex = state.step.ordinal
    val progress = (stepIndex + 1).toFloat() / totalSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Passo ${stepIndex + 1} di $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Text(
            text = stepTitle(state.step),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stepSubtitle(state.step),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (state.step) {
            QuizStep.MainProblem -> mainProblemOptions().forEach { (option, label, subtitle) ->
                OptionRow(
                    label = label,
                    subtitle = subtitle,
                    selected = state.mainProblem == option,
                    onClick = { onChoose(option) },
                )
            }
            QuizStep.WorkStyle -> workStyleOptions().forEach { (option, label, subtitle) ->
                OptionRow(
                    label = label,
                    subtitle = subtitle,
                    selected = state.workStyle == option,
                    onClick = { onChoose(option) },
                )
            }
            QuizStep.SetupTolerance -> setupToleranceOptions().forEach { (option, label, subtitle) ->
                OptionRow(
                    label = label,
                    subtitle = subtitle,
                    selected = state.setupTolerance == option,
                    onClick = { onChoose(option) },
                )
            }
            QuizStep.Goal -> goalOptions().forEach { (option, label, subtitle) ->
                OptionRow(
                    label = label,
                    subtitle = subtitle,
                    selected = state.goal == option,
                    onClick = { onChoose(option) },
                )
            }
        }

        if (state.submitting) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
        }
        if (state.error != null) {
            Text(
                state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = fg)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ResultPane(
    result: QuizResult,
    onRestart: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val (badgeColor, surfaceColor, methodLabel) = methodVisual(result.recommended)
        Card(
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Il metodo consigliato per te è",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    methodLabel,
                    style = MaterialTheme.typography.displaySmall,
                    color = badgeColor,
                )
                Text(
                    result.reasoning.ifBlank { "Una combinazione su misura per il tuo stile." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            "Punteggi dei 3 sistemi",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        result.scores
            .sortedByDescending { it.score }
            .forEach { score ->
                ScoreBar(score = score)
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onRestart) { Text("Rifai il quiz") }
            TextButton(onClick = onClose) { Text("Chiudi") }
        }
    }
}

@Composable
private fun ScoreBar(score: MethodScore) {
    val (color, _, label) = methodVisual(score.method)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text("${score.score}", style = MaterialTheme.typography.titleSmall, color = color)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (score.score / 100f).coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
            )
        }
        if (score.explanation.isNotBlank()) {
            Text(
                score.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun methodVisual(method: RecommendedMethod): Triple<Color, Color, String> {
    val palette = TriFlowTheme.methodColors
    return when (method) {
        RecommendedMethod.Gtd -> Triple(palette.gtdSoft, palette.gtdSurface, "GTD")
        RecommendedMethod.Pomodoro -> Triple(palette.pomodoroSoft, palette.pomodoroSurface, "Pomodoro")
        RecommendedMethod.SecondBrain -> Triple(palette.brainSoft, palette.brainSurface, "Second Brain")
    }
}

private fun stepTitle(step: QuizStep): String = when (step) {
    QuizStep.MainProblem -> "Qual è il tuo problema principale?"
    QuizStep.WorkStyle -> "Come preferisci lavorare?"
    QuizStep.SetupTolerance -> "Quanto setup sei disposto a fare?"
    QuizStep.Goal -> "Cosa vuoi ottenere?"
}

private fun stepSubtitle(step: QuizStep): String = when (step) {
    QuizStep.MainProblem -> "Scegli la frase che ti rappresenta di più adesso."
    QuizStep.WorkStyle -> "Pensa a come arrivi più facilmente al risultato."
    QuizStep.SetupTolerance -> "Vuoi un sistema pronto subito o investi tempo per personalizzarlo?"
    QuizStep.Goal -> "Il punto d'arrivo che cerchi nelle prossime settimane."
}

private fun mainProblemOptions(): List<Triple<MainProblem, String, String>> = listOf(
    Triple(MainProblem.Overwhelm, "Mi sento sopraffatto", "Troppe cose da gestire, faccio fatica a vedere le priorità."),
    Triple(MainProblem.Distraction, "Mi distraggo facilmente", "Inizio le cose ma non riesco a portarle a termine."),
    Triple(MainProblem.KnowledgeLoss, "Perdo informazioni preziose", "Imparo cose interessanti ma poi non le ritrovo."),
)

private fun workStyleOptions(): List<Triple<WorkStyle, String, String>> = listOf(
    Triple(WorkStyle.Structured, "Strutturato", "Mi piacciono liste, processi e routine."),
    Triple(WorkStyle.Flexible, "Flessibile", "Mi adatto, dipende dalla giornata."),
    Triple(WorkStyle.Creative, "Creativo", "Lavoro per intuizioni e collegamenti."),
)

private fun setupToleranceOptions(): List<Triple<SetupTolerance, String, String>> = listOf(
    Triple(SetupTolerance.Low, "Basso", "Voglio iniziare subito senza configurare niente."),
    Triple(SetupTolerance.Medium, "Medio", "Mezz'ora di setup all'inizio va bene."),
    Triple(SetupTolerance.High, "Alto", "Sono disposto a investire tempo per ottimizzare."),
)

private fun goalOptions(): List<Triple<Goal, String, String>> = listOf(
    Triple(Goal.ShipTasks, "Concludere task", "Più cose finite, meno cose aperte."),
    Triple(Goal.FocusTime, "Più tempo di focus", "Ore di concentrazione produttiva."),
    Triple(Goal.BuildKnowledge, "Costruire conoscenza", "Una base di sapere che cresce nel tempo."),
)
