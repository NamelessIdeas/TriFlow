package com.app.triflow.presentation.feature.secondbrain.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Insieme di trasformazioni pure su [TextFieldValue], per la toolbar dell'editor markdown.
 * Tutte preservano una selezione/cursore "ragionevole":
 *  - wrap inline: la selezione resta intorno al testo, mantenendo i marker.
 *  - prefisso di linea: il cursore va alla fine.
 *  - link: il cursore va dentro la parentesi dell'URL.
 *
 * Tutte le funzioni sono testabili senza Android.
 */
object MarkdownActions {

    /** Wrappa la selezione (o inserisce uno snippet vuoto) con `prefix...suffix`. */
    fun wrap(value: TextFieldValue, prefix: String, suffix: String = prefix): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end)
        val replaced = "$prefix$selected$suffix"
        val newText = text.replaceRange(start, end, replaced)
        val newStart = start + prefix.length
        val newEnd = newStart + selected.length
        return TextFieldValue(
            text = newText,
            selection = TextRange(newStart, newEnd),
        )
    }

    /** Prepende `prefix` ad ogni riga selezionata (o alla riga corrente se non c'è selezione). */
    fun prependLines(value: TextFieldValue, prefix: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0))
            .let { if (it < 0) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', end).let { if (it < 0) text.length else it }
        val block = text.substring(lineStart, lineEnd)
        val transformed = block.split('\n').joinToString("\n") { line ->
            if (line.isBlank() && block.split('\n').size == 1) "$prefix" else "$prefix$line"
        }
        val newText = text.replaceRange(lineStart, lineEnd, transformed)
        val delta = transformed.length - block.length
        val newCursor = (end + delta).coerceAtMost(newText.length)
        return TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    /** Avvolge in un code block multilinea con triple backtick. */
    fun codeBlock(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end)
        val replaced = buildString {
            if (start > 0 && text[start - 1] != '\n') append('\n')
            append("```\n")
            append(selected.ifEmpty { "" })
            if (selected.isEmpty() || !selected.endsWith('\n')) append('\n')
            append("```")
        }
        val newText = text.replaceRange(start, end, replaced)
        // Posiziona il cursore subito dopo l'apertura ```\n
        val openIndex = replaced.indexOf("```\n") + 4
        val cursor = start + openIndex
        return TextFieldValue(text = newText, selection = TextRange(cursor))
    }

    /** Inserisce `[testo](url)`, dove `testo` è la selezione corrente (o "testo"). */
    fun link(value: TextFieldValue, url: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end).ifEmpty { "testo" }
        val replaced = "[$selected]($url)"
        val newText = text.replaceRange(start, end, replaced)
        val newCursor = start + replaced.length
        return TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    fun bold(v: TextFieldValue) = wrap(v, "**")
    fun italic(v: TextFieldValue) = wrap(v, "_")
    fun strike(v: TextFieldValue) = wrap(v, "~~")
    fun codeInline(v: TextFieldValue) = wrap(v, "`")
    fun h1(v: TextFieldValue) = prependLines(v, "# ")
    fun h2(v: TextFieldValue) = prependLines(v, "## ")
    fun h3(v: TextFieldValue) = prependLines(v, "### ")
    fun listItem(v: TextFieldValue) = prependLines(v, "- ")
    fun quote(v: TextFieldValue) = prependLines(v, "> ")
}
