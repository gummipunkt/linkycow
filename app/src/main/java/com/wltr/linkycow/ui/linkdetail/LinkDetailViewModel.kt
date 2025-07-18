package com.wltr.linkycow.ui.linkdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.LinkDetailResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LinkDetailUiState {
    object Loading : LinkDetailUiState()
    data class Success(val link: LinkDetailResponse) : LinkDetailUiState()
    data class Error(val message: String) : LinkDetailUiState()
}

sealed class LinkDetailEvent {
    object ArchiveSuccess : LinkDetailEvent()
    data class ArchiveError(val message: String) : LinkDetailEvent()
    object DeleteSuccess : LinkDetailEvent()
    data class DeleteError(val message: String) : LinkDetailEvent()
}

class LinkDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LinkDetailUiState>(LinkDetailUiState.Loading)
    val uiState: StateFlow<LinkDetailUiState> = _uiState

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress

    private val _eventFlow = MutableSharedFlow<LinkDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadLink(linkId: Int) {
        viewModelScope.launch {
            _uiState.value = LinkDetailUiState.Loading
            val result = ApiClient.getLinkById(linkId)
            result.onSuccess { linkDetail ->
                _uiState.value = LinkDetailUiState.Success(linkDetail)
            }.onFailure { error ->
                _uiState.value = LinkDetailUiState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun archiveLink(linkId: Int) {
        viewModelScope.launch {
            _actionInProgress.value = true
            val result = ApiClient.archiveLink(linkId)
            result.onSuccess {
                _eventFlow.emit(LinkDetailEvent.ArchiveSuccess)
            }.onFailure { error ->
                _eventFlow.emit(LinkDetailEvent.ArchiveError(error.message ?: "Failed to archive link"))
            }
            _actionInProgress.value = false
        }
    }

    fun deleteLink(linkId: Int) {
        viewModelScope.launch {
            _actionInProgress.value = true
            val result = ApiClient.deleteLink(linkId)
            result.onSuccess {
                _eventFlow.emit(LinkDetailEvent.DeleteSuccess)
            }.onFailure { error ->
                _eventFlow.emit(LinkDetailEvent.DeleteError(error.message ?: "Failed to delete link"))
            }
            _actionInProgress.value = false
        }
    }
} 