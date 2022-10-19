package com.arnyminerz.filamagenta.ui.reusable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

private val headlineDepthStyles
    @Composable
    get() = listOf(
        MaterialTheme.typography.headlineLarge,
        MaterialTheme.typography.headlineMedium,
        MaterialTheme.typography.headlineSmall,
        MaterialTheme.typography.titleLarge,
        MaterialTheme.typography.titleMedium,
        MaterialTheme.typography.titleSmall,
    )

@Composable
fun MarkdownText(markdown: String) {
    val annotatedString = buildAnnotatedString {
        markdown.split('\n').forEach { line ->
            val headlineIndex = line.indexOf('#')
            if (headlineIndex >= 0) {
                // This is header, count depth
                val regex = Regex("[^#]")
                var depth = 0
                while (!regex.matchesAt(line, depth)) depth++
                val headline = line.substring(depth + 1)
                val typography = headlineDepthStyles.getOrElse(depth) { TextStyle.Default }
                withStyle(typography.toSpanStyle()) { append(headline) }
            } else {
                var currentStyle = SpanStyle()
                var c = 0
                while (c < line.length) withStyle(currentStyle) {
                    val char = line[c]
                    val nextChar = c.takeIf { it + 1 < line.length }?.let { line[it + 1] }
                    if (char == '*' && nextChar == '*') { // Bold
                        currentStyle = if (currentStyle.fontWeight == FontWeight.Bold)
                            currentStyle.copy(fontWeight = FontWeight.Normal)
                        else
                            currentStyle.copy(fontWeight = FontWeight.Bold)
                        // Add two since the pointer is double
                        c += 2
                    } else if (char == '*') { // Italic
                        currentStyle = if (currentStyle.fontStyle == FontStyle.Italic)
                            currentStyle.copy(fontStyle = FontStyle.Normal)
                        else
                            currentStyle.copy(fontStyle = FontStyle.Italic)
                        c++
                    } else if (char == '~') { // Strikethrough
                        currentStyle =
                            if (currentStyle.textDecoration == TextDecoration.LineThrough)
                                currentStyle.copy(textDecoration = TextDecoration.None)
                            else
                                currentStyle.copy(textDecoration = TextDecoration.LineThrough)
                        c++
                    } else if (char == '_') { // Underline
                        currentStyle = if (currentStyle.textDecoration == TextDecoration.Underline)
                            currentStyle.copy(textDecoration = TextDecoration.None)
                        else
                            currentStyle.copy(textDecoration = TextDecoration.Underline)
                        c++
                    } else {
                        append(char)
                        c++
                    }
                }
            }
            append('\n')
        }
    }
    Text(annotatedString)
}

@Preview
@Composable
fun MarkdownTextPreview() {
    Column {
        MarkdownText(
            markdown = "This is markdown text with **bold** content.\n" +
                    "This is markdown text with *italic* content.\n" +
                    "**This** is where it gets complicated. With **bold and *italic* texts**.\n" +
                    "# Headers are also supported\n" +
                    "The work for separating sections\n" +
                    "## And setting\n" +
                    "Sub-sections\n" +
                    "### That get\n" +
                    "#### Deeper\n" +
                    "##### And Deeper\n" +
                    "###### And even deeper\n" +
                    "Remember _this_ ~not this~? Also works!"
        )
    }
}
