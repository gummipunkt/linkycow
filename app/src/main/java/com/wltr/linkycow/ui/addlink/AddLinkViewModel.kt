package com.wltr.linkycow.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import com.wltr.linkycow.data.remote.dto.CreateLinkTag
import com.wltr.linkycow.data.remote.dto.CreateLinkCollection
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

    fun addLink(url: String, name: String, description: String, tags: String, collection: String) {
        if (url.isBlank()) {
            _uiState.value = AddLinkUiState.Error("URL cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddLinkUiState.Loading
            
            // Parse tags from comma-separated string
            val tagsList = if (tags.isNotBlank()) {
                tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { CreateLinkTag(name = it) }
            } else null
            
            // Parse collection
            val collectionObj = if (collection.isNotBlank()) {
                CreateLinkCollection(name = collection.trim())
            } else null
            
            val request = CreateLinkRequest(
                url = url,
                name = name.ifBlank { null },
                description = description.ifBlank { null },
                tags = tagsList,
                collection = collectionObj
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