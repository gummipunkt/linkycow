package com.wltr.linkycow.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddLinkUiState {
    object Idle : AddLinkUiState()
    object Loading : AddLinkUiState()
    object Success : AddLinkUiState()
    data class Error(val message: String) : AddLinkUiState()
}

class AddLinkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AddLinkUiState>(AddLinkUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun addLink(url: String, name: String, description: String) {
        if (url.isBlank()) {
            _uiState.value = AddLinkUiState.Error("URL cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddLinkUiState.Loading
            val request = CreateLinkRequest(
                url = url,
                name = name.ifBlank { null },
                description = description.ifBlank { null }
            )
            val result = ApiClient.createLink(request)
            result.onSuccess {
                _uiState.value = AddLinkUiState.Success
            }.onFailure { exception ->
                _uiState.value = AddLinkUiState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }
} 