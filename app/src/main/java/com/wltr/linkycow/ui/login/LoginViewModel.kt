package com.wltr.linkycow.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI states for the login process
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel for user authentication.
 * Handles login process, session persistence, and auth state management.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(getApplication())
    
    // Login UI state
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Expose auth token flow for MainActivity to observe
    val authTokenFlow = sessionRepository.authTokenFlow

    /**
     * Authenticate user with Linkwarden server
     * @param instanceUrl Server URL (e.g., "https://links.example.com")
     * @param username User's login name
     * @param password User's password
     */
    fun login(instanceUrl: String, username: String, password: String) {
        // Prevent duplicate login attempts
        if (_uiState.value is LoginUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val loginRequest = LoginRequest(username = username, password = password)
            val result = ApiClient.login(instanceUrl, loginRequest)

            result.onSuccess { response ->
                // Save session data for persistence
                sessionRepository.saveSession(instanceUrl, username, password, response.response.token)
                _uiState.value = LoginUiState.Success
            }.onFailure { exception ->
                _uiState.value = LoginUiState.Error(
                    exception.message ?: "An unknown error occurred"
                )
            }
        }
    }

    /**
     * Reset UI state to idle (e.g., after navigation)
     */
    fun resetUiState() {
        _uiState.value = LoginUiState.Idle
    }
} 