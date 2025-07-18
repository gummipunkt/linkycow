package com.wltr.linkycow.ui.share

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.wltr.linkycow.R
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import com.wltr.linkycow.ui.theme.LinkyCowTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedUrl = intent?.getStringExtra(Intent.EXTRA_TEXT)
        
        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type && sharedUrl != null) {
            setContent {
                LinkyCowTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SavingLinkScreen()
                    }
                }
            }
            
            // Start the save process
            saveLinkAndFinish(sharedUrl)
        } else {
            // Invalid intent, finish immediately
            finish()
        }
    }
    
    private fun saveLinkAndFinish(url: String) {
        lifecycleScope.launch {
            try {
                // Get session info
                val sessionRepository = SessionRepository(this@ShareActivity)
                val token = sessionRepository.authTokenFlow.first()
                val instanceUrl = sessionRepository.instanceUrlFlow.first()
                
                if (token.isEmpty() || instanceUrl.isEmpty()) {
                    showErrorNotification("Please login to LinkyCow first")
                    finish()
                    return@launch
                }
                
                // Set auth and make API call
                ApiClient.setAuth(instanceUrl, token)
                val result = ApiClient.createLink(CreateLinkRequest(url = url))
                
                if (result.isSuccess) {
                    showSuccessNotification(url)
                } else {
                    showErrorNotification(result.exceptionOrNull()?.message ?: "Failed to save link")
                }
                
            } catch (e: Exception) {
                showErrorNotification("Error: ${e.message}")
            } finally {
                finish()
            }
        }
    }
    
    private fun showSuccessNotification(url: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LinkyCow Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Link Saved!")
            .setContentText(url)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(SUCCESS_NOTIFICATION_ID, notification)
    }
    
    private fun showErrorNotification(errorMessage: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LinkyCow Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Failed to Save Link")
            .setContentText(errorMessage)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "linkycow_share_channel"
        private const val SUCCESS_NOTIFICATION_ID = 100
        private const val ERROR_NOTIFICATION_ID = 101
    }
}

@Composable
fun SavingLinkScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Saving link...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
} 