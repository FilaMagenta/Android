package com.arnyminerz.filamagenta.ui.reusable

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
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
private fun String.markdownAnnotated(typography: TextStyle) = buildAnnotatedString {
    val headlineIndex = indexOf('#')
    if (headlineIndex >= 0) {
        // This is header, count depth
        val regex = Regex("[^#]")
        var depth = 0
        while (!regex.matchesAt(this@markdownAnnotated, depth)) depth++
        val headline = substring(depth + 1)
        val headlineTypography = headlineDepthStyles.getOrElse(depth - 1) { TextStyle.Default }
        withStyle(headlineTypography.toSpanStyle()) { append(headline) }
    } else if (startsWith('-')) { // List
        val item = substring(1)
        append("$Bullet\t$item")
    } else {
        val lineLength = this@markdownAnnotated.length
        var lastStyle = typography.toSpanStyle()
        var c = 0
        pushStyle(typography.toSpanStyle())
        while (c < lineLength) {
            val char = get(c)
            val nextChar = c.takeIf { it + 1 < lineLength }?.let { get(it + 1) }
            if (char == '*' && nextChar == '*') { // Bold
                pop()
                lastStyle = if (lastStyle.fontWeight == FontWeight.Bold)
                    lastStyle.copy(fontWeight = FontWeight.Normal)
                else
                    lastStyle.copy(fontWeight = FontWeight.Bold)
                pushStyle(lastStyle)

                // Add two since the pointer is double
                c += 2
            } else if (char == '*') { // Italic
                pop()
                lastStyle = if (lastStyle.fontStyle == FontStyle.Italic)
                    lastStyle.copy(fontStyle = FontStyle.Normal)
                else
                    lastStyle.copy(fontStyle = FontStyle.Italic)
                pushStyle(lastStyle)
                c++
            } else if (char == '~') { // Strikethrough
                pop()
                lastStyle = if (lastStyle.textDecoration == TextDecoration.LineThrough)
                    lastStyle.copy(textDecoration = TextDecoration.None)
                else
                    lastStyle.copy(textDecoration = TextDecoration.LineThrough)
                pushStyle(lastStyle)
                c++
            } else if (char == '_') { // Underline
                pop()
                lastStyle = if (lastStyle.textDecoration == TextDecoration.Underline)
                    lastStyle.copy(textDecoration = TextDecoration.None)
                else
                    lastStyle.copy(textDecoration = TextDecoration.Underline)
                pushStyle(lastStyle)
                c++
            } else if (char == '[') { // Starts a link
                // Search for the closing tag
                val preClosing = indexOf(']', c + 1)
                // Search for the actual link start
                val lOpen = indexOf('(', c + 1)
                // And the ending
                val lClose = indexOf(')', c + 1)

                // Check if link is valid
                val outOfBounds = preClosing < 0 || lOpen < 0 || lClose < 0
                val overwrites = lOpen > lClose || preClosing > lOpen
                if (outOfBounds || overwrites) {
                    append(char)
                    c++
                } else {
                    val link = substring(lOpen + 1, lClose)
                    val text = substring(c + 1, preClosing)
                    pushStringAnnotation(
                        tag = "link",
                        annotation = link,
                    )
                    pushStyle(
                        lastStyle.copy(
                            textDecoration = TextDecoration.Underline,
                            color = Color(LinkColor),
                        ),
                    )
                    append(text)
                    pop()
                    c = lClose + 1
                }
            } else {
                Timber.v("Appending $char")
                append(char)
                c++
            }
        }
    }
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Visible,
    maxLines: Int = Int.MAX_VALUE,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
    ) {
        markdown.split('\n').forEach { line ->
            Timber.d("Line: $line")
            if (line.startsWith("--")) // If starts with at least two '-', add divider
                Divider()
            else {
                val annotatedString = line.markdownAnnotated(style)
                Timber.d("Annotated line: $annotatedString")

                ClickableText(
                    text = annotatedString,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
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
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun MarkdownTextPreview() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        MarkdownText(
            markdown = listOf(
                "This is markdown text with **bold** content.",
                "This is markdown text with *italic* content.",
                "**This** is where it gets complicated. With **bold and *italic* texts**.",
                "# Headers are also supported",
                "The work for separating sections",
                "## And setting",
                "Sub-sections",
                "### That get",
                "#### Deeper",
                "##### And Deeper",
                "###### And even deeper",
                "Remember _this_ ~not this~? Also works!",
                "[This](https://example.com) is a link.",
                "- Lists",
                "- are",
                "- also",
                "- supported",
                "--------",
                "That is a hr!"
            ).joinToString("\n"),
            modifier = Modifier
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
