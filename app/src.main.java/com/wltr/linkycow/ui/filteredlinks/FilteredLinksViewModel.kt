package com.wltr.linkycow.ui.filteredlinks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.Link
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FilteredLinksUiState {
    object Loading : FilteredLinksUiState()
    data class Success(val links: List<Link>) : FilteredLinksUiState()
    data class Error(val message: String) : FilteredLinksUiState()
}

class FilteredLinksViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FilteredLinksUiState>(FilteredLinksUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadLinks(filterType: String?, filterId: Int?) {
        if (filterType == null || filterId == null) {
            _uiState.value = FilteredLinksUiState.Error("Invalid filter provided.")
            return
        }

        viewModelScope.launch {
            _uiState.value = FilteredLinksUiState.Loading
            val result = when (filterType) {
                "collection" -> ApiClient.getLinksByCollection(filterId)
                "tag" -> ApiClient.getLinksByTag(filterId)
                else -> {
                    _uiState.value = FilteredLinksUiState.Error("Unknown filter type.")
                    return@launch
                }
            }

            result.onSuccess { response ->
                _uiState.value = FilteredLinksUiState.Success(response.response)
            }.onFailure { error ->
                _uiState.value = FilteredLinksUiState.Error(error.message ?: "An unknown error occurred.")
            }
        }
    }
} 