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

sealed class AddLinkUiState {
    object Idle : AddLinkUiState()
    object Loading : AddLinkUiState()
    object Success : AddLinkUiState()
    data class Error(val message: String) : AddLinkUiState()
}

sealed class AddLinkDataState<out T> {
    object Loading : AddLinkDataState<Nothing>()
    data class Success<T>(val data: List<T>) : AddLinkDataState<T>()
    data class Error(val message: String) : AddLinkDataState<Nothing>()
}

class AddLinkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AddLinkUiState>(AddLinkUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _collectionsState = MutableStateFlow<AddLinkDataState<CollectionDto>>(AddLinkDataState.Loading)
    val collectionsState = _collectionsState.asStateFlow()

    private val _tagsState = MutableStateFlow<AddLinkDataState<TagDto>>(AddLinkDataState.Loading)
    val tagsState = _tagsState.asStateFlow()

    // Editing
    private val _editLink = MutableStateFlow<FullLinkData?>(null)
    val editLink = _editLink.asStateFlow()

    init {
        loadCollections()
        loadTags()
    }

    fun initializeForEdit(link: Any) {
        val fullLink = when (link) {
            is FullLinkData -> link
            is LinkDetailData -> link.toFullLinkData()
            else -> throw IllegalArgumentException("Unsupported link type")
        }
        _editLink.value = fullLink
    }

    private fun loadCollections() {
        viewModelScope.launch {
            _collectionsState.value = AddLinkDataState.Loading
            val result = ApiClient.getCollections()
            result.onSuccess {
                _collectionsState.value = AddLinkDataState.Success(it)
            }.onFailure { e ->
                _collectionsState.value = AddLinkDataState.Error(e.message ?: "Error loading categories")
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            _tagsState.value = AddLinkDataState.Loading
            val result = ApiClient.getTags()
            result.onSuccess {
                _tagsState.value = AddLinkDataState.Success(it)
            }.onFailure { e ->
                _tagsState.value = AddLinkDataState.Error(e.message ?: "Error loading tags")
            }
        }
    }

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
            } else {
                CreateLinkCollection(name = "Unsorted") // Default auf "Unsorted" setzen
            }
            
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

    fun addOrUpdateLink(
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ) {
        val editing = _editLink.value
        if (editing == null) {
            addLink(url, name, description, tags, collection)
        } else {
            updateLink(editing.id, url, name, description, tags, collection)
        }
    }

    private fun updateLink(
        linkId: Int,
        url: String,
        name: String,
        description: String,
        tags: String,
        collection: String
    ) {
        if (url.isBlank()) {
            _uiState.value = AddLinkUiState.Error("URL darf nicht leer sein.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AddLinkUiState.Loading
            val tagsList = if (tags.isNotBlank()) {
                tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { CreateLinkTag(name = it) }
            } else emptyList() // Immer ein Array, nie null!
            // Collection-Objekt mit ID bestimmen
            val col = if (collection.isNotBlank()) {
                val state = collectionsState.value
                if (state is AddLinkDataState.Success) state.data.find { it.name == collection.trim() } else null
            } else {
                val state = collectionsState.value
                if (state is AddLinkDataState.Success) state.data.find { it.name.equals("unorganized", ignoreCase = true) } else null
            }
            val collectionObj = CreateLinkCollection(id = col?.id, name = col?.name ?: collection.ifBlank { "unorganized" }, ownerId = col?.ownerId)
            val request = CreateLinkRequest(
                id = linkId, // ID für Update setzen!
                url = url,
                name = name.ifBlank { null },
                description = description.ifBlank { null },
                tags = tagsList,
                collection = collectionObj
            )
            val result = ApiClient.updateLink(linkId, request)
            result.onSuccess {
                _uiState.value = AddLinkUiState.Success
            }.onFailure { exception ->
                _uiState.value = AddLinkUiState.Error(exception.message ?: "Unbekannter Fehler beim Aktualisieren")
            }
        }
    }
} 

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

fun com.wltr.linkycow.data.remote.dto.Tag.toFullLinkTag() = FullLinkTag(
    id = id,
    name = name,
    ownerId = ownerId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

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