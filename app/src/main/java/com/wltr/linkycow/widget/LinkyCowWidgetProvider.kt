package com.wltr.linkycow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wltr.linkycow.R

class LinkyCowWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            // Zeige den Ladezustand an und starte den Worker
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setViewVisibility(R.id.widget_message_view, android.view.View.VISIBLE)
                setTextViewText(R.id.widget_message_view, "Lade...")
                setViewVisibility(R.id.widget_links_container, android.view.View.GONE)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
            
            // Starte den Worker für die Datenladung
            val workRequest = OneTimeWorkRequestBuilder<LinkyCowWidgetWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

