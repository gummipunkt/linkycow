package com.wltr.linkycow.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionRepository = SessionRepository(application)
    
    val instanceUrl: StateFlow<String> = sessionRepository.instanceUrlFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(),
        initialValue = ""
    )
    val username: StateFlow<String> = sessionRepository.usernameFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(),
        initialValue = ""
    )
    
    fun updateCredentials(newUrl: String, newUsername: String, newPassword: String) {
        viewModelScope.launch {
            // Clean and normalize the URL
            val cleanUrl = newUrl.trim().lowercase().replace(" ", "")
            val cleanUsername = newUsername.trim().lowercase().replace(" ", "")
            
            // Update session with new credentials
            if (newPassword.isNotEmpty()) {
                // If password is provided, use it
                sessionRepository.saveSession(cleanUrl, cleanUsername, newPassword)
                
                // Try to login with new credentials to validate them
                val loginResult = ApiClient.login(cleanUrl, cleanUsername, newPassword)
                loginResult.onSuccess { response ->
                    // Update the session with the new token
                    sessionRepository.saveSession(cleanUrl, cleanUsername, newPassword, response.response.token)
                    ApiClient.setAuth(cleanUrl, response.response.token)
                }
            } else {
                // If no new password, keep the existing token but update URL and username
                val currentToken = sessionRepository.authTokenFlow.first()
                sessionRepository.saveSession(cleanUrl, cleanUsername, "", currentToken)
                ApiClient.setAuth(cleanUrl, currentToken)
            }
        }
    }
} 