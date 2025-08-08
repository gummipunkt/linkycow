package com.wltr.linkycow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wltr.linkycow.R
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.Link
import kotlinx.coroutines.flow.first

/**
 * Background worker that fetches recent links and updates the widget
 * Uses the existing API client and session management
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get authentication info
            val sessionRepository = SessionRepository(applicationContext)
            val token = sessionRepository.authTokenFlow.first()
            val instanceUrl = sessionRepository.instanceUrlFlow.first()

            if (token.isEmpty() || instanceUrl.isEmpty()) {
                // User not logged in, show empty state
                updateWidgetWithEmptyState()
                return Result.success()
            }

            // Configure API client
            ApiClient.setAuth(instanceUrl, token)
            
            // Fetch recent links (limit to 3)
            val response = ApiClient.getDashboard()

            if (response.isSuccess) {
                val dashboardResponse = response.getOrNull()
                val links = dashboardResponse?.data?.links?.take(3) ?: emptyList()
                updateWidgetWithLinks(links)
            } else {
                updateWidgetWithError()
            }

            Result.success()
        } catch (e: Exception) {
            // Log error and show error state
            updateWidgetWithError()
            Result.failure()
        }
    }

    /**
     * Updates all widgets with the fetched links (only updates data, keeps existing UI state)
     */
    private fun updateWidgetWithLinks(links: List<Link>) {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, RecentLinksWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_recent_links)
            
            // Set up refresh button (preserve functionality)
            setupRefreshButton(views)
            
            if (links.isEmpty()) {
                showEmptyState(views)
            } else {
                showLinksData(views, links)
            }
            
            // Use partiallyUpdateAppWidget to avoid full reloads
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Updates all widgets with empty state
     */
    private fun updateWidgetWithEmptyState() {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, RecentLinksWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_recent_links)
            
            // Set up refresh button (preserve functionality)
            setupRefreshButton(views)
            
            showEmptyState(views)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Updates all widgets with error state
     */
    private fun updateWidgetWithError() {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, RecentLinksWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_recent_links)
            
            // Set up refresh button (preserve functionality)
            setupRefreshButton(views)
            
            showErrorState(views)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Shows the links data in the widget
     */
    private fun showLinksData(views: RemoteViews, links: List<Link>) {
        views.setViewVisibility(R.id.widget_loading_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_empty_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_error_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_links_container, android.view.View.VISIBLE)

        // Clear existing links
        views.removeAllViews(R.id.widget_links_container)

        // Add each link as a separate view
        links.forEach { link ->
            val linkView = RemoteViews(applicationContext.packageName, R.layout.widget_link_item)
            
            // Set link data
            linkView.setTextViewText(R.id.link_title, link.name.ifEmpty { link.url })
            linkView.setTextViewText(R.id.link_url, link.url)
            
            // Set click handler for this link
            val clickIntent = Intent(applicationContext, RecentLinksWidgetProvider::class.java).apply {
                action = RecentLinksWidgetProvider.ACTION_OPEN_LINK
                putExtra(RecentLinksWidgetProvider.EXTRA_LINK_ID, link.id)
                putExtra(RecentLinksWidgetProvider.EXTRA_LINK_URL, link.url)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                link.id, // Use link ID as request code for uniqueness
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            linkView.setOnClickPendingIntent(R.id.link_title, pendingIntent)
            linkView.setOnClickPendingIntent(R.id.link_url, pendingIntent)
            
            // Add to container
            views.addView(R.id.widget_links_container, linkView)
        }
    }

    /**
     * Shows empty state in the widget
     */
    private fun showEmptyState(views: RemoteViews) {
        views.setViewVisibility(R.id.widget_loading_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_empty_state, android.view.View.VISIBLE)
        views.setViewVisibility(R.id.widget_error_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_links_container, android.view.View.GONE)
    }

    /**
     * Shows error state in the widget
     */
    private fun showErrorState(views: RemoteViews) {
        views.setViewVisibility(R.id.widget_loading_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_empty_state, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_error_state, android.view.View.VISIBLE)
        views.setViewVisibility(R.id.widget_links_container, android.view.View.GONE)
    }

    /**
     * Sets up the refresh button functionality
     */
    private fun setupRefreshButton(views: RemoteViews) {
        val refreshIntent = Intent(applicationContext, RecentLinksWidgetProvider::class.java).apply {
            action = RecentLinksWidgetProvider.ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, refreshIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
    }
} 