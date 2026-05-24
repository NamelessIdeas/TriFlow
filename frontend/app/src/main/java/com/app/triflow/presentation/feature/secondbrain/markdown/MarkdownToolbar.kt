package com.app.triflow.presentation.feature.secondbrain.markdown

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onStrike: () -> Unit,
    onH1: () -> Unit,
    onH2: () -> Unit,
    onList: () -> Unit,
    onQuote: () -> Unit,
    onCodeInline: () -> Unit,
    onCodeBlock: () -> Unit,
    onLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(onClick = onBold) { Icon(Icons.Outlined.FormatBold, contentDescription = "Grassetto") }
        IconButton(onClick = onItalic) { Icon(Icons.Outlined.FormatItalic, contentDescription = "Corsivo") }
        IconButton(onClick = onStrike) { Icon(Icons.Outlined.FormatStrikethrough, contentDescription = "Barrato") }
        IconButton(onClick = onH1) {
            Icon(Icons.Outlined.Title, contentDescription = "H1")
        }
        IconButton(onClick = onH2) {
            Text(
                "H2",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onList) { Icon(Icons.Outlined.FormatListBulleted, contentDescription = "Lista") }
        IconButton(onClick = onQuote) { Icon(Icons.Outlined.FormatQuote, contentDescription = "Citazione") }
        IconButton(onClick = onCodeInline) { Icon(Icons.Outlined.Code, contentDescription = "Codice inline") }
        IconButton(onClick = onCodeBlock) { Icon(Icons.Outlined.DataObject, contentDescription = "Code block") }
        IconButton(onClick = onLink) { Icon(Icons.Outlined.Link, contentDescription = "Link") }
    }
}
