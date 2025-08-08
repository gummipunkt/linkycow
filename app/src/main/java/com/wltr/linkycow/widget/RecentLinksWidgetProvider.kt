package com.wltr.linkycow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wltr.linkycow.MainActivity
import com.wltr.linkycow.R

/**
 * Widget provider for displaying the 3 most recent links
 * Follows Material You design principles and integrates with the app's theme
 */
class RecentLinksWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.wltr.linkycow.REFRESH_WIDGET"
        const val ACTION_OPEN_LINK = "com.wltr.linkycow.OPEN_LINK"
        const val EXTRA_LINK_ID = "link_id"
        const val EXTRA_LINK_URL = "link_url"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        
        // Schedule background data loading
        scheduleWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_REFRESH_WIDGET -> {
                // Manual refresh triggered by user
                refreshAllWidgets(context)
            }
            ACTION_OPEN_LINK -> {
                // User tapped on a link
                handleLinkClick(context, intent)
            }
        }
    }

    /**
     * Updates a single widget instance with loading state
     */
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_recent_links)
        
        // Set up refresh button
        setupRefreshButton(context, views)
        
        // Header will be clickable through the whole widget area
        
        // Show loading state initially
        showLoadingState(views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * Sets up the refresh button functionality
     */
    private fun setupRefreshButton(context: Context, views: RemoteViews) {
        val refreshIntent = Intent(context, RecentLinksWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
    }



    /**
     * Shows loading state in the widget
     */
    private fun showLoadingState(views: RemoteViews) {
        views.setViewVisibility(R.id.widget_loading_state, android.view.View.VISIBLE)
        views.setViewVisibility(R.id.widget_empty_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_error_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_links_container, android.view.View.GONE)
    }

    /**
     * Schedules a background work request to update widget data
     */
    private fun scheduleWidgetUpdate(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .addTag("widget_update")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Refreshes all widget instances (manual refresh only, no automatic scheduling)
     */
    private fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, RecentLinksWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        // Show loading state for all widgets
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_recent_links)
            setupRefreshButton(context, views)
            showLoadingState(views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        // Schedule background data loading (without triggering onUpdate again)
        scheduleWidgetUpdate(context)
    }

    /**
     * Handles link click events from the widget
     */
    private fun handleLinkClick(context: Context, intent: Intent) {
        val linkId = intent.getIntExtra(EXTRA_LINK_ID, -1)
        val linkUrl = intent.getStringExtra(EXTRA_LINK_URL)
        
        if (linkId != -1 && linkUrl != null) {
            // Open the app with the specific link
            val openLinkIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(EXTRA_LINK_ID, linkId)
                putExtra(EXTRA_LINK_URL, linkUrl)
            }
            context.startActivity(openLinkIntent)
        }
    }
} 