package com.wltr.linkycow.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.Link
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val links: List<Link>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(application)
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Link>>(emptyList())
    val searchResults: StateFlow<List<Link>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        loadDashboard()

        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Wait for 500ms of no new input
                .collect { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        _isSearching.value = false
                    } else {
                        _isSearching.value = true
                        performSearch(query)
                    }
                }
        }

        // Auto-refresh when the app starts
        viewModelScope.launch {
            // Wait a moment for the initial load to complete, then refresh
            kotlinx.coroutines.delay(1000)
            refreshDashboard()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            // First, get the session info
            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()

            if (token.isEmpty() || url.isEmpty()) {
                _uiState.value = DashboardUiState.Error("Session not found.")
                return@launch
            }

            // Set auth for the ApiClient
            ApiClient.setAuth(url, token)

            // Now, get the dashboard
            val result = ApiClient.getDashboard()
            result.onSuccess { dashboardResponse ->
                _uiState.value = DashboardUiState.Success(dashboardResponse.data.links)
            }.onFailure { error ->
                _uiState.value = DashboardUiState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            val result = ApiClient.searchLinks(query)
            result.onSuccess { searchResponse ->
                _searchResults.value = searchResponse.data.links
            }.onFailure { error ->
                // Optionally, handle search errors in the UI
                _searchResults.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // First, get the session info
            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()

            if (token.isEmpty() || url.isEmpty()) {
                _uiState.value = DashboardUiState.Error("Session not found.")
                _isRefreshing.value = false
                return@launch
            }

            // Set auth for the ApiClient
            ApiClient.setAuth(url, token)

            // Now, get the dashboard
            val result = ApiClient.getDashboard()
            result.onSuccess { dashboardResponse ->
                _uiState.update {
                    if (it is DashboardUiState.Success) {
                        it.copy(links = dashboardResponse.data.links)
                    } else {
                        DashboardUiState.Success(dashboardResponse.data.links)
                    }
                }
            }.onFailure {
                // Optionally handle refresh error, e.g., show a snackbar
            }
            _isRefreshing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            ApiClient.clearAuth()
        }
    }
} 