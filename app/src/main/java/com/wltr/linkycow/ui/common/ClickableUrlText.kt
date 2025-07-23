package com.wltr.linkycow.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle

/**
 * A composable that displays a clickable URL text with Material3 styling.
 * Uses the modern Text composable instead of deprecated ClickableText.
 * 
 * @param url The URL to display and make clickable
 * @param style Text style to apply to the URL
 */
@Composable
fun ClickableUrlText(url: String, style: TextStyle) {
    val uriHandler = LocalUriHandler.current
    
    // Build annotated string with clickable URL annotation
    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(url)
        }
        pop()
    }

    // Use modern Text composable with onClick support
    Text(
        text = annotatedString,
        style = style,
        onClick = { offset ->
            // Find and handle URL annotation clicks
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (e: Exception) {
                        // Silently handle malformed URLs - could add error callback if needed
                    }
                }
        }
    )
} 