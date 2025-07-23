package com.wltr.linkycow.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink

/**
 * A composable that displays a clickable URL text with Material3 styling.
 * Uses the modern Text composable with LinkAnnotation for URL handling.
 * 
 * @param url The URL to display and make clickable
 * @param style Text style to apply to the URL
 */
@Composable
fun ClickableUrlText(url: String, style: TextStyle) {
    // Build annotated string with modern LinkAnnotation
    val annotatedString = buildAnnotatedString {
        withLink(
            LinkAnnotation.Url(url)
        ) {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(url)
            }
        }
    }

    // Use modern Text composable with LinkAnnotation support
    Text(
        text = annotatedString,
        style = style
    )
} 