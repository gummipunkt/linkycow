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

/**
 * UI state for the main dashboard screen
 */
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val links: List<Link>,
        val collections: List<CollectionDto>,
        val tags: List<TagDto>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

/**
 * ViewModel for the main dashboard screen.
 * Handles link loading, search, filtering, and infinite scrolling.
 */
@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = SessionRepository(application)

    // Main UI state
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Pull-to-refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Search functionality
    val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Link>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Infinite scrolling state
    private val _pagedLinks = MutableStateFlow<List<Link>>(emptyList())
    val pagedLinks: StateFlow<List<Link>> = _pagedLinks.asStateFlow()
    private var nextCursor: Int? = null
    private var isLoadingMore = false
    private var pagingInitialized = false

    // Filter state - only one filter can be active at a time
    var selectedCollectionId by mutableStateOf<Int?>(null)
        private set
    var selectedTagId by mutableStateOf<Int?>(null)
        private set

    init {
        loadDashboardData(isRefresh = false)
        setupSearchDebouncing()
    }

    /**
     * Set up search query debouncing to avoid excessive API calls
     */
    private fun setupSearchDebouncing() {
        viewModelScope.launch {
            searchQuery
                .debounce(300) // Wait 300ms after user stops typing
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

    /**
     * Trigger manual refresh (e.g., pull-to-refresh)
     */
    fun refreshDashboard() {
        loadDashboardData(isRefresh = true)
    }
    
    /**
     * Load main dashboard data with collections and tags
     */
    private fun loadDashboardData(isRefresh: Boolean) {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch

            _isRefreshing.value = true

            // Preserve existing data during refresh to avoid blank screen
            val hasData = _uiState.value is DashboardUiState.Success
            if (!hasData) {
                _uiState.value = DashboardUiState.Loading
            }

            // Clear filters on full reload (but keep them on refresh)
            if (!isRefresh) {
                 selectedCollectionId = null
                 selectedTagId = null
            }

            // Set up API authentication
            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()
            ApiClient.setAuth(url, token)

            try {
                // Load all required data in parallel
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
                    
                    // Only show error if we don't have existing data
                    if (!hasData) {
                         _uiState.value = DashboardUiState.Error(error)
                    }
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

    /**
     * Initialize paginated links for infinite scrolling
     */
    fun loadInitialLinks() {
        if (pagingInitialized) return
        pagingInitialized = true
        
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            // Set up authentication
            val token = sessionRepository.authTokenFlow.first()
            val url = sessionRepository.instanceUrlFlow.first()
            ApiClient.setAuth(url, token)
            
            // Load initial data concurrently
            val collectionsResult = ApiClient.getCollections()
            val tagsResult = ApiClient.getTags()
            val linksResult = ApiClient.getLinksPaged()
            
            if (linksResult.isSuccess && collectionsResult.isSuccess && tagsResult.isSuccess) {
                val response = linksResult.getOrThrow().data
                _pagedLinks.value = response.links
                nextCursor = response.nextCursor
                
                _uiState.value = DashboardUiState.Success(
                    links = response.links,
                    collections = collectionsResult.getOrThrow(),
                    tags = tagsResult.getOrThrow()
                )
            } else {
                val error = linksResult.exceptionOrNull()?.message
                    ?: collectionsResult.exceptionOrNull()?.message
                    ?: tagsResult.exceptionOrNull()?.message
                    ?: "An unknown error occurred"
                _uiState.value = DashboardUiState.Error(error)
            }
        }
    }

    /**
     * Load more links for infinite scrolling
     */
    fun loadMoreLinks() {
        if (isLoadingMore || nextCursor == null) return
        
        isLoadingMore = true
        viewModelScope.launch {
            val result = ApiClient.getLinksPaged(nextCursor)
            
            result.onSuccess { response ->
                val newLinks = response.data.links
                val currentLinks = _pagedLinks.value
                _pagedLinks.value = currentLinks + newLinks
                nextCursor = response.data.nextCursor
                
                // Update UI state with new links
                val currentState = _uiState.value
                if (currentState is DashboardUiState.Success) {
                    _uiState.value = currentState.copy(links = _pagedLinks.value)
                }
            }
            
            isLoadingMore = false
        }
    }

    /**
     * Apply collection or tag filter with toggle logic
     */
    fun setFilter(filter: Any?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is DashboardUiState.Success) return@launch

            val oldCollectionId = selectedCollectionId
            val oldTagId = selectedTagId

            // Determine new filter state with toggle logic
            val newCollectionId = when (filter) {
                is CollectionDto -> if (filter.id == oldCollectionId) null else filter.id
                else -> null
            }
            val newTagId = when (filter) {
                is TagDto -> if (filter.id == oldTagId) null else filter.id
                else -> null
            }

            // Update filter state (only one can be active)
            selectedCollectionId = newCollectionId
            selectedTagId = if (newCollectionId == null) newTagId else null

            // Load filtered data or reset to all links
            if (selectedCollectionId == null && selectedTagId == null) {
                loadDashboardData(isRefresh = true)
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
                // Keep existing data on filter error
            }
            
            _isRefreshing.value = false
        }
    }

    /**
     * Perform search with debouncing
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            
            val result = ApiClient.searchLinks(query)
            result.onSuccess { response ->
                _searchResults.value = response.data.links
            }.onFailure {
                // Could show error state for search, but keeping results for UX
            }
            
            _isSearching.value = false
        }
    }

    /**
     * Handle user logout
     */
    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            ApiClient.clearAuth()
        }
    }

    /**
     * Update search query (triggers debounced search)
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    /**
     * Delete a link permanently
     */
    fun deleteLink(linkId: Int) {
        viewModelScope.launch {
            try {
                val result = ApiClient.deleteLink(linkId)
                result.onSuccess {
                    // Remove from all relevant lists
                    _pagedLinks.value = _pagedLinks.value.filter { it.id != linkId }
                    _searchResults.value = _searchResults.value.filter { it.id != linkId }
                    
                    // Update main UI state
                    val currentState = _uiState.value
                    if (currentState is DashboardUiState.Success) {
                        _uiState.value = currentState.copy(
                            links = currentState.links.filter { it.id != linkId }
                        )
                    }
                }
            } catch (e: Exception) {
                // Error handling - could show a snackbar or toast
                e.printStackTrace()
            }
        }
    }


} 