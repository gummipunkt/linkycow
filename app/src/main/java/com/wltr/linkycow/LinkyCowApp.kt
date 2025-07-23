package com.wltr.linkycow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

// Notification channel ID for background link operations
const val ADD_LINK_NOTIFICATION_CHANNEL_ID = "add_link_channel"

/**
 * Application class for LinkyCow.
 * Handles app-wide initialization including notification channels.
 */
class LinkyCowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Create notification channel for background link operations.
     * Required for Android O+ to show notifications.
     */
    private fun createNotificationChannel() {
        val name = "Add Link Service"
        val descriptionText = "Notifications for adding links in the background."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        
        val channel = NotificationChannel(ADD_LINK_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        
        // Register channel with system notification manager
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
} 