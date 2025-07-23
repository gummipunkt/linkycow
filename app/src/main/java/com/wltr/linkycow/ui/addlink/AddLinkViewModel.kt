package com.wltr.linkycow.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wltr.linkycow.data.remote.ApiClient
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import com.wltr.linkycow.data.remote.dto.CreateLinkTag
import com.wltr.linkycow.data.remote.dto.CreateLinkCollection
import com.wltr.linkycow.data.remote.dto.CollectionDto
import com.wltr.linkycow.data.remote.dto.TagDto
import com.wltr.linkycow.data.remote.dto.FullLinkData
import com.wltr.linkycow.data.remote.dto.LinkDetailData
import com.wltr.linkycow.data.remote.dto.FullLinkTag
import com.wltr.linkycow.data.remote.dto.FullLinkCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for link creation/editing operations
 */
sealed class AddLinkUiState {
    object Idle : AddLinkUiState()
    object Loading : AddLinkUiState()
    object Success : AddLinkUiState()
    data class Error(val message: String) : AddLinkUiState()
}

/**
 * State for loading collections/tags data
 */
sealed class AddLinkDataState<out T> {
    object Loading : AddLinkDataState<Nothing>()
    data class Success<T>(val data: List<T>) : AddLinkDataState<T>()
    data class Error(val message: String) : AddLinkDataState<Nothing>()
}

/**
 * ViewModel for adding and editing links.
 * Handles form state, collections/tags loading, and API interactions.
 */
class AddLinkViewModel : ViewModel() {

    // Main operation state
    private val _uiState = MutableStateFlow<AddLinkUiState>(AddLinkUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Collections loading state
    private val _collectionsState = MutableStateFlow<AddLinkDataState<CollectionDto>>(AddLinkDataState.Loading)
    val collectionsState = _collectionsState.asStateFlow()

    // Tags loading state
    private val _tagsState = MutableStateFlow<AddLinkDataState<TagDto>>(AddLinkDataState.Loading)
    val tagsState = _tagsState.asStateFlow()

    // Edit mode state - contains link data when editing existing link
    private val _editLink = MutableStateFlow<FullLinkData?>(null)
    val editLink = _editLink.asStateFlow()

    init {
        loadCollections()
        loadTags()
    }

    /**
     * Initialize ViewModel for editing an existing link
     */
    fun initializeForEdit(link: Any) {
        val fullLink = when (link) {
            is FullLinkData -> link
            is LinkDetailData -> link.toFullLinkData()
            else -> throw IllegalArgumentException("Unsupported link type")
        }
        _editLink.value = fullLink
    }

    /**
     * Load available collections from API
     */
    private fun loadCollections() {
        viewModelScope.launch {
            _collectionsState.value = AddLinkDataState.Loading
            
            val result = ApiClient.getCollections()
            result.onSuccess { collections ->
                _collectionsState.value = AddLinkDataState.Success(collections)
            }.onFailure { exception ->
                _collectionsState.value = AddLinkDataState.Error(
                    exception.message ?: "Error loading collections"
                )
            }
        }
    }

    /**
     * Load available tags from API
     */
    private fun loadTags() {
        viewModelScope.launch {
            _tagsState.value = AddLinkDataState.Loading
            
            val result = ApiClient.getTags()
            result.onSuccess { tags ->
                _tagsState.value = AddLinkDataState.Success(tags)
            }.onFailure { exception ->
                _tagsState.value = AddLinkDataState.Error(
                    exception.message ?: "Error loading tags"
                )
            }
        }
    }

    /**
     * Create a new link with the provided data
     */
    private fun addLink(url: String, name: String, description: String, tags: String, collection: String) {
        if (url.isBlank()) {
            _uiState.value = AddLinkUiState.Error("URL cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddLinkUiState.Loading
            
            val request = buildCreateLinkRequest(
                url = url,
                name = name,
                description = description,
                tags = tags,
                collection = collection
            )
            
            val result = ApiClient.createLink(request)
            result.onSuccess {
                _uiState.value = AddLinkUiState.Success
            }.onFailure { exception ->
                _uiState.value = AddLinkUiState.Error(
                    exception.message ?: "An unknown error occurred"
                )
            }
        }
    }

    /**
     * Main entry point for creating or updating a link
     */
    fun addOrUpdateLink(
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ) {
        val editingLink = _editLink.value
        if (editingLink == null) {
            addLink(url, name, description, tags, collection)
        } else {
            updateLink(editingLink.id, url, name, description, tags, collection)
        }
    }

    /**
     * Update an existing link
     */
    private fun updateLink(
        linkId: Int,
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ) {
        if (url.isBlank()) {
            _uiState.value = AddLinkUiState.Error("URL cannot be empty.")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AddLinkUiState.Loading
            
            val request = buildUpdateLinkRequest(
                linkId = linkId,
                url = url,
                name = name,
                description = description,
                tags = tags,
                collection = collection
            )
            
            val result = ApiClient.updateLink(linkId, request)
            result.onSuccess {
                _uiState.value = AddLinkUiState.Success
            }.onFailure { exception ->
                _uiState.value = AddLinkUiState.Error(
                    exception.message ?: "Unknown error during update"
                )
            }
        }
    }

    /**
     * Build request object for creating a new link
     */
    private fun buildCreateLinkRequest(
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ): CreateLinkRequest {
        return CreateLinkRequest(
            url = url,
            name = name.ifBlank { null },
            description = description.ifBlank { null },
            tags = parseTagsString(tags),
            collection = parseCollectionString(collection, defaultName = "Unsorted")
        )
    }

    /**
     * Build request object for updating an existing link
     */
    private fun buildUpdateLinkRequest(
        linkId: Int,
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ): CreateLinkRequest {
        // Find collection with ID from available collections
        val collectionData = findCollectionByName(collection)
        val resolvedCollection = CreateLinkCollection(
            id = collectionData?.id,
            name = collectionData?.name ?: collection.ifBlank { "unorganized" },
            ownerId = collectionData?.ownerId
        )
        
        return CreateLinkRequest(
            id = linkId, // Required for updates
            url = url,
            name = name.ifBlank { null },
            description = description.ifBlank { null },
            tags = parseTagsString(tags),
            collection = resolvedCollection
        )
    }

    /**
     * Parse comma-separated tags string into API format
     */
    private fun parseTagsString(tags: String): List<CreateLinkTag>? {
        if (tags.isBlank()) return null
        
        return tags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { CreateLinkTag(name = it) }
    }

    /**
     * Parse collection string into API format
     */
    private fun parseCollectionString(collection: String, defaultName: String): CreateLinkCollection {
        val collectionName = collection.ifBlank { defaultName }
        return CreateLinkCollection(name = collectionName.trim())
    }

    /**
     * Find collection data by name from loaded collections
     */
    private fun findCollectionByName(name: String): CollectionDto? {
        val state = collectionsState.value
        return if (state is AddLinkDataState.Success && name.isNotBlank()) {
            state.data.find { it.name.equals(name.trim(), ignoreCase = true) }
        } else {
            // Default to "unorganized" collection if available
            if (state is AddLinkDataState.Success) {
                state.data.find { it.name.equals("unorganized", ignoreCase = true) }
            } else null
        }
    }
}

/**
 * Extension function to convert LinkDetailData to FullLinkData for editing
 */
fun LinkDetailData.toFullLinkData(): FullLinkData = FullLinkData(
    id = id,
    name = name,
    type = type,
    description = description,
    createdById = createdById,
    collectionId = collectionId,
    icon = icon,
    iconWeight = iconWeight,
    color = color ?: "",
    url = url,
    textContent = textContent,
    preview = preview,
    image = image,
    pdf = pdf,
    readable = readable,
    monolith = monolith,
    lastPreserved = lastPreserved,
    importDate = importDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags.map { it.toFullLinkTag() },
    collection = collection?.toFullLinkCollection()
)

/**
 * Extension function to convert Tag to FullLinkTag
 */
fun com.wltr.linkycow.data.remote.dto.Tag.toFullLinkTag() = FullLinkTag(
    id = id,
    name = name,
    ownerId = ownerId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Extension function to convert Collection to FullLinkCollection
 */
fun com.wltr.linkycow.data.remote.dto.Collection.toFullLinkCollection() = FullLinkCollection(
    id = id,
    name = name,
    description = description ?: "",
    icon = icon,
    iconWeight = iconWeight,
    color = color ?: "",
    parentId = parentId,
    isPublic = isPublic,
    ownerId = ownerId,
    createdById = createdById,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinnedBy = pinnedBy
) 