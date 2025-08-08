package com.wltr.linkycow.ui.linkdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.LinkDetailResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class LinkDetailUiState {
    object Loading : LinkDetailUiState()
    data class Success(
        val link: LinkDetailResponse,
        val previewImage: ByteArray? = null,
        val imageError: String? = null,
        val baseUrl: String = ""
    ) : LinkDetailUiState()
    data class Error(val message: String) : LinkDetailUiState()
}

sealed class LinkDetailEvent {
    data class DeleteSuccess(val message: String = "Link deleted successfully.") : LinkDetailEvent()
    data class DeleteError(val message: String) : LinkDetailEvent()
}

class LinkDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LinkDetailUiState>(LinkDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress = _actionInProgress.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LinkDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val sessionRepository = SessionRepository(application)

    fun loadLink(linkId: Int) {
        viewModelScope.launch {
            _actionInProgress.value = true
            // Don't set to Loading here to avoid flicker on refresh
            if (_uiState.value !is LinkDetailUiState.Success) {
                _uiState.value = LinkDetailUiState.Loading
            }
            val baseUrl = sessionRepository.instanceUrlFlow.first()
            val result = ApiClient.getLinkById(linkId)
            result.onSuccess { linkDetail ->
                // Start with the main data
                val successState = LinkDetailUiState.Success(
                    link = linkDetail,
                    baseUrl = baseUrl
                )
                _uiState.value = successState

                // Then fetch the image
                if (linkDetail.response.preview != null) {
                    val imageResult = ApiClient.getLinkPreviewImage(linkId)
                    imageResult.onSuccess { imageBytes ->
                        val currentState = _uiState.value
                        if (currentState is LinkDetailUiState.Success) {
                            _uiState.value = currentState.copy(previewImage = imageBytes, imageError = null)
                        }
                    }.onFailure { imageError ->
                        val currentState = _uiState.value
                        if (currentState is LinkDetailUiState.Success) {
                            _uiState.value = currentState.copy(imageError = imageError.message ?: "Failed to load image")
                        }
                    }
                }
            }.onFailure { error ->
                _uiState.value = LinkDetailUiState.Error(error.message ?: "An unknown error occurred")
            }
            _actionInProgress.value = false
        }
    }



    fun deleteLink(linkId: Int) {
        viewModelScope.launch {
            _actionInProgress.value = true
            try {
                val result = ApiClient.deleteLink(linkId)
                result.onSuccess {
                    _eventFlow.emit(LinkDetailEvent.DeleteSuccess())
                }
            } catch (e: Exception) {
                // Error handling - same as in MainViewModel
                e.printStackTrace()
                _eventFlow.emit(LinkDetailEvent.DeleteError("Delete failed: ${e.message}"))
            }
            _actionInProgress.value = false
        }
    }
} 