package com.arnyminerz.filamagenta.ui.reusable

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import timber.log.Timber

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

/**
 * The color given to links in [MarkdownText].
 * @author Arnau Mora
 * @since 20221019
 */
private const val LinkColor = 0xff64B5F6

/**
 * The character used by [MarkdownText] to mark list items.
 * @author Arnau Mora
 * @since 20221019
 */
private const val Bullet = '\u2022'

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Visible,
    maxLines: Int = Int.MAX_VALUE,
) {
    markdown.split('\n').forEach { line ->
        if (line.startsWith("--")) // If starts with at least two '-', add divider
            Divider()
        else {
            val annotatedString = buildAnnotatedString {
                val headlineIndex = line.indexOf('#')
                if (headlineIndex >= 0) {
                    // This is header, count depth
                    val regex = Regex("[^#]")
                    var depth = 0
                    while (!regex.matchesAt(line, depth)) depth++
                    val headline = line.substring(depth + 1)
                    val typography = headlineDepthStyles.getOrElse(depth) { TextStyle.Default }
                    withStyle(typography.toSpanStyle()) { append(headline) }
                } else if (line.startsWith('-')) { // List
                    val item = line.substring(1)
                    append("$Bullet\t$item")
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
                            currentStyle =
                                if (currentStyle.textDecoration == TextDecoration.Underline)
                                    currentStyle.copy(textDecoration = TextDecoration.None)
                                else
                                    currentStyle.copy(textDecoration = TextDecoration.Underline)
                            c++
                        } else if (char == '[') { // Starts a link
                            // Search for the closing tag
                            val preClosing = line.indexOf(']', c + 1)
                            // Search for the actual link start
                            val lOpen = line.indexOf('(', c + 1)
                            // And the ending
                            val lClose = line.indexOf(')', c + 1)

                            // Check if link is valid
                            if (preClosing < 0 || lOpen < 0 || lClose < 0 || lOpen > lClose || preClosing > lOpen) {
                                append(char)
                                c++
                            } else {
                                val link = line.substring(lOpen + 1, lClose)
                                val text = line.substring(c + 1, preClosing)
                                pushStringAnnotation(
                                    tag = "link",
                                    annotation = link,
                                )
                                withStyle(
                                    currentStyle.copy(
                                        textDecoration = TextDecoration.Underline,
                                        color = Color(LinkColor),
                                    ),
                                ) { append(text) }
                                pop()
                                c = lClose + 1
                            }
                        } else {
                            append(char)
                            c++
                        }
                    }
                }
                append('\n')
            }

            val uriHandler = LocalUriHandler.current
            ClickableText(
                text = annotatedString,
                modifier = modifier,
                overflow = overflow,
                maxLines = maxLines,
                style = style,
                softWrap = softWrap,
                onClick = {
                    annotatedString
                        .getStringAnnotations("link", it, it)
                        .firstOrNull()?.let { stringAnnotation ->
                            try {
                                uriHandler.openUri(stringAnnotation.item)
                            } catch (e: ActivityNotFoundException) {
                                Timber.e(e, "Could not find link handler.")
                            }
                        }
                }
            )
        }
    }
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
                    "Remember _this_ ~not this~? Also works!\n" +
                    "[This](https://example.com) is a link.\n" +
                    "- Lists\n" +
                    "- are\n" +
                    "- also\n" +
                    "- supported\n" +
                    "--------\n" +
                    "That is a hr!",
            modifier = Modifier
                .padding(horizontal = 8.dp),
        )
    }
}
