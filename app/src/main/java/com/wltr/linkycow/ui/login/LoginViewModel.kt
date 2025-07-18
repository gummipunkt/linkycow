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

// Represents the different states of the login UI
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState() // Changed from data class to object
    data class Error(val message: String) : LoginUiState()
}


class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(getApplication())
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Expose the auth token flow to be observed by the UI
    val authTokenFlow = sessionRepository.authTokenFlow

    fun login(instanceUrl: String, username: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val loginRequest = LoginRequest(username = username, password = password)
            val result = ApiClient.login(instanceUrl, loginRequest)

            result.onSuccess { response ->
                sessionRepository.saveSession(instanceUrl, username, password, response.response.token)
                _uiState.value = LoginUiState.Success
            }.onFailure { exception ->
                _uiState.value = LoginUiState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }

    // Call this function when you want to reset the UI state, e.g., after navigating away
    fun onNavigationDone() {
        _uiState.value = LoginUiState.Idle
    }
} 