package com.arnyminerz.filamagenta.ui.reusable

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MarkdownText(markdown: String) {
    buildAnnotatedString {
        var currentStyle = SpanStyle()
        markdown.split('\n').forEach { line ->
            var c = 0
            while (c < line.length) withStyle(currentStyle) {
                val char = line[c]
                val nextChar = line[c + 1]
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
                    currentStyle = if (currentStyle.textDecoration == TextDecoration.LineThrough)
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
    }
}

@Preview
@Composable
fun MarkdownTextPreview() {
    MarkdownText(
        markdown = "This is markdown text with **bold** content.\n" +
                "This is markdown text with *italic* content.\n" +
                "**This** is where it gets complicated. With **bold and *italic* texts**."
    )
}
