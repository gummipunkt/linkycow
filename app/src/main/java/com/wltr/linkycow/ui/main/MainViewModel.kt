package com.wltr.linkycow.ui.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.local.SessionRepository
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.CollectionDto
import com.wltr.linkycow.data.remote.dto.Link
import com.wltr.linkycow.data.remote.dto.TagDto
import com.wltr.linkycow.data.remote.dto.SearchResponse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val links: List<Link>,
        val collections: List<CollectionDto>,
        val tags: List<TagDto>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(application)

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Link>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _pagedLinks = MutableStateFlow<List<Link>>(emptyList())
    val pagedLinks: StateFlow<List<Link>> = _pagedLinks.asStateFlow()
    private var nextCursor: Int? = null
    private var isLoadingMore = false
    private var pagingInitialized = false

    var selectedCollectionId by mutableStateOf<Int?>(null)
        private set
    var selectedTagId by mutableStateOf<Int?>(null)
        private set

    init {
        loadDashboardData(isRefresh = false)

        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        _isSearching.value = false
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun refreshDashboard() {
        loadDashboardData(isRefresh = true)
    }
    
    private fun loadDashboardData(isRefresh: Boolean) {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch

            _isRefreshing.value = true

            // Only set full-screen loading state if we don't have data yet.
            // This prevents the screen from blanking out on a pull-to-refresh.
            val hasData = _uiState.value is DashboardUiState.Success
            if (!hasData) {
                _uiState.value = DashboardUiState.Loading
            }

            // Reset filters on any full load/refresh
            if (!isRefresh) { // Maybe keep filters on refresh? For now, clear them.
                 selectedCollectionId = null
                 selectedTagId = null
            }

            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()
            ApiClient.setAuth(url, token)

            try {
                val linksResult = ApiClient.getDashboard()
                val collectionsResult = ApiClient.getCollections()
                val tagsResult = ApiClient.getTags()

                if (linksResult.isSuccess && collectionsResult.isSuccess && tagsResult.isSuccess) {
                    _uiState.value = DashboardUiState.Success(
                        links = linksResult.getOrThrow().data.links,
                        collections = collectionsResult.getOrThrow(),
                        tags = tagsResult.getOrThrow()
                    )
                } else {
                    val error = linksResult.exceptionOrNull()?.message
                        ?: collectionsResult.exceptionOrNull()?.message
                        ?: tagsResult.exceptionOrNull()?.message
                        ?: "An unknown error occurred"
                    if (!hasData) {
                         _uiState.value = DashboardUiState.Error(error)
                    }
                    // If it was a refresh and it failed, we just stop the indicator
                    // but keep the old data. Maybe show a snackbar.
                }
            } catch (e: Exception) {
                if (!hasData) {
                    _uiState.value = DashboardUiState.Error(e.message ?: "An unexpected error occurred")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadInitialLinks() {
        if (pagingInitialized) return
        pagingInitialized = true
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()
            ApiClient.setAuth(url, token)
            val collectionsResult = ApiClient.getCollections()
            val tagsResult = ApiClient.getTags()
            val result = ApiClient.getLinksPaged()
            if (result.isSuccess && collectionsResult.isSuccess && tagsResult.isSuccess) {
                val response = result.getOrThrow().data
                _pagedLinks.value = response.links
                nextCursor = response.nextCursor
                _uiState.value = DashboardUiState.Success(
                    links = response.links,
                    collections = collectionsResult.getOrThrow(),
                    tags = tagsResult.getOrThrow()
                )
            } else {
                val error = result.exceptionOrNull()?.message
                    ?: collectionsResult.exceptionOrNull()?.message
                    ?: tagsResult.exceptionOrNull()?.message
                    ?: "An unknown error occurred"
                _uiState.value = DashboardUiState.Error(error)
            }
        }
    }

    fun loadMoreLinks() {
        if (isLoadingMore || nextCursor == null) return
        isLoadingMore = true
        viewModelScope.launch {
            val result = ApiClient.getLinksPaged(nextCursor)
            result.onSuccess {
                val response = it.data
                val current = _pagedLinks.value
                _pagedLinks.value = current + response.links
                nextCursor = response.nextCursor
                // Update UI-State, damit die neuen Links angezeigt werden
                val currentState = _uiState.value
                if (currentState is DashboardUiState.Success) {
                    _uiState.value = currentState.copy(links = _pagedLinks.value)
                }
            }
            isLoadingMore = false
        }
    }

    fun setFilter(filter: Any?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is DashboardUiState.Success) return@launch

            val oldSelectedCollectionId = selectedCollectionId
            val oldSelectedTagId = selectedTagId

            // Toggle logic
            val newCollectionId = if (filter is CollectionDto) if(filter.id == oldSelectedCollectionId) null else filter.id else null
            val newTagId = if (filter is TagDto) if(filter.id == oldSelectedTagId) null else filter.id else null

            selectedCollectionId = newCollectionId
            selectedTagId = if (newCollectionId == null) newTagId else null // Only one can be active

            if (selectedCollectionId == null && selectedTagId == null) {
                loadDashboardData(isRefresh = true) // Reload all links
                return@launch
            }
            
            _isRefreshing.value = true
            
            val result = if (selectedCollectionId != null) {
                ApiClient.getLinksByCollection(selectedCollectionId!!)
            } else {
                ApiClient.getLinksByTag(selectedTagId!!)
            }

            result.onSuccess { filteredResponse ->
                 _uiState.value = currentState.copy(links = filteredResponse.response)
            }.onFailure {
                // Keep old data on failure
            }
            _isRefreshing.value = false
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val result = ApiClient.searchLinks(query)
            result.onSuccess {
                _searchResults.value = it.data.links
            }.onFailure {
                // Optionally handle search error state
            }
            _isSearching.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            ApiClient.clearAuth()
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
} 