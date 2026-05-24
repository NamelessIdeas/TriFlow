package com.app.triflow.presentation.feature.gtd.contexts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.triflow.presentation.common.EmptyView

@Composable
fun ContextsScreen(viewModel: ContextsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contexts by viewModel.contexts.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.newName,
                onValueChange = viewModel::onName,
                placeholder = { Text("Nuovo contesto (es. @casa)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(Modifier.padding(start = 8.dp))
            IconButton(
                onClick = viewModel::create,
                enabled = !state.creating && state.newName.isNotBlank(),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Aggiungi", tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        if (contexts.isEmpty()) {
            EmptyView(
                modifier = Modifier.fillMaxSize(),
                icon = Icons.Outlined.Tag,
                title = "Nessun contesto",
                subtitle = "I contesti GTD (es. @casa, @ufficio) ti aiutano a filtrare le task.",
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = contexts, key = { it.id }) { ctx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "@${ctx.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        IconButton(onClick = { viewModel.delete(ctx.id) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}
