package com.wltr.linkycow.widget

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Utility object for triggering widget updates from within the app
 * Helps keep widgets in sync when links are added, updated, or deleted
 */
object WidgetManager {

    private const val WIDGET_UPDATE_WORK_NAME = "widget_update_work"

    /**
     * Triggers a widget update to refresh displayed links
     * This should be called when:
     * - A new link is added
     * - A link is updated or deleted  
     * - User manually refreshes data
     */
    fun updateWidgets(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .addTag("widget_update")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace any pending updates to prevent multiple concurrent updates
            workRequest
        )
    }
} 