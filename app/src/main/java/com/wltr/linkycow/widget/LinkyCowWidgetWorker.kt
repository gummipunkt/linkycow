package com.wltr.linkycow.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wltr.linkycow.R
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import kotlinx.coroutines.flow.first
import android.appwidget.AppWidgetManager

class LinkyCowWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, LinkyCowWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        return try {
            val sessionRepository = SessionRepository(context)
            val authToken = sessionRepository.authTokenFlow.first()

            if (authToken.isEmpty()) {
                updateErrorState("Nicht angemeldet", appWidgetManager, appWidgetIds)
                return Result.success()
            }

            val response = ApiClient.getDashboard()
            if (response.isSuccess) {
                val links = response.getOrNull()?.data?.links ?: emptyList()
                if (links.isEmpty()) {
                    updateEmptyState("Keine Links gefunden", appWidgetManager, appWidgetIds)
                } else {
                    updateSuccessState(links, appWidgetManager, appWidgetIds)
                }
            } else {
                updateErrorState(response.exceptionOrNull()?.message ?: "Unbekannter Fehler", appWidgetManager, appWidgetIds)
            }
            Result.success()
        } catch (e: Exception) {
            updateErrorState(e.message ?: "Ein Fehler ist aufgetreten", appWidgetManager, appWidgetIds)
            Result.failure()
        }
    }

    private fun updateSuccessState(links: List<com.wltr.linkycow.data.remote.dto.Link>, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setViewVisibility(R.id.widget_message_view, android.view.View.GONE)
                setViewVisibility(R.id.widget_links_container, android.view.View.VISIBLE)
                removeAllViews(R.id.widget_links_container) // Wichtig: Alte Views entfernen

                links.take(5).forEach { link ->
                    val linkView = RemoteViews(context.packageName, R.layout.widget_link_item).apply {
                        setTextViewText(R.id.link_item_title, link.name.ifEmpty { "Kein Titel" })
                        setTextViewText(R.id.link_item_url, link.url)
                        
                        // Klick-Intent für den Link
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        val pendingIntent = PendingIntent.getActivity(context, link.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        setOnClickPendingIntent(R.id.link_item_root, pendingIntent)
                    }
                    addView(R.id.widget_links_container, linkView)
                }
            }
            setRefreshIntent(views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    private fun updateErrorState(message: String, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateMessageState("❌ Fehler: $message", appWidgetManager, appWidgetIds)
    }

    private fun updateEmptyState(message: String, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateMessageState(message, appWidgetManager, appWidgetIds)
    }

    private fun updateMessageState(message: String, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
         for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setViewVisibility(R.id.widget_message_view, android.view.View.VISIBLE)
                setTextViewText(R.id.widget_message_view, message)
                setViewVisibility(R.id.widget_links_container, android.view.View.GONE)
            }
            setRefreshIntent(views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun setRefreshIntent(views: RemoteViews) {
        val intent = Intent(context, LinkyCowWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, LinkyCowWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingIntent)
    }
}

