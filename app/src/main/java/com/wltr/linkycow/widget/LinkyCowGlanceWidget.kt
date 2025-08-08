package com.wltr.linkycow.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.wltr.linkycow.R
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.Link
import kotlinx.coroutines.flow.first
import androidx.glance.action.actionStartActivity
import com.wltr.linkycow.MainActivity

sealed class WidgetState {
    object Loading : WidgetState()
    data class Success(val links: List<Link>) : WidgetState()
    data class Error(val message: String) : WidgetState()
}

class LinkyCowGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadWidgetData(context)
        
        provideContent {
            GlanceTheme {
                WidgetContent(state = state)
            }
        }
    }

    @Composable
    private fun WidgetContent(state: WidgetState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp)
        ) {
            WidgetHeader()
            Spacer(modifier = GlanceModifier.height(16.dp))
            
            when (state) {
                is WidgetState.Loading -> {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lade Links...", style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface))
                    }
                }
                is WidgetState.Success -> {
                    if (state.links.isEmpty()) {
                        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Keine Links gefunden.", style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface))
                        }
                    } else {
                        LazyColumn {
                            items(state.links.take(10)) { link ->
                                LinkItem(link)
                            }
                        }
                    }
                }
                is WidgetState.Error -> {
                     Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Fehler: ${state.message}", style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.error))
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Letzte Links",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.defaultWeight()
            )
            // Klick öffnet jetzt einfach die App, kein benutzerdefinierter Callback
            Image(
                provider = ImageProvider(R.drawable.ic_refresh),
                contentDescription = "App öffnen",
                modifier = GlanceModifier.clickable(onClick = actionStartActivity<MainActivity>())
            )
        }
    }

import androidx.glance.action.actionStartActivity
import android.content.Intent
import android.net.Uri

// ...

    @Composable
    private fun LinkItem(link: Link) {
        Column(
            modifier = GlanceModifier
                .padding(vertical = 8.dp)
                .clickable(
                    onClick = actionStartActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                    )
                )
        ) {
            Text(
                text = link.name.ifEmpty { "Kein Titel" },
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GlanceTheme.colors.onSurface)
            )
            Text(
                text = link.url,
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

private suspend fun loadWidgetData(context: Context): WidgetState {
    val sessionRepository = SessionRepository(context)
    val authToken = sessionRepository.authTokenFlow.first()

    if (authToken.isEmpty()) {
        return WidgetState.Error("Nicht angemeldet")
    }

    return try {
        val response = ApiClient.getDashboard()
        if (response.isSuccess) {
            WidgetState.Success(response.getOrNull()?.data?.links ?: emptyList())
        } else {
            WidgetState.Error(response.exceptionOrNull()?.message ?: "Unbekannter Fehler")
        }
    } catch (e: Exception) {
        WidgetState.Error(e.message ?: "Netzwerkfehler")
    }
}
