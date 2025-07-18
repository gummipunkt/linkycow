package com.wltr.linkycow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

const val ADD_LINK_NOTIFICATION_CHANNEL_ID = "add_link_channel"

class LinkyCowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Add Link Service"
        val descriptionText = "Notifications for adding links in the background."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(ADD_LINK_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
} 