package com.wltr.linkycow.ui.addlink

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wltr.linkycow.data.remote.dto.CollectionDto
import com.wltr.linkycow.data.remote.dto.TagDto
import com.wltr.linkycow.data.remote.dto.FullLinkData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.wltr.linkycow.data.remote.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkScreen(
    onNavigateBack: () -> Unit,
    onLinkAdded: () -> Unit,
    linkId: Int? = null
) {
    val viewModel: AddLinkViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val collectionsState by viewModel.collectionsState.collectAsState()
    val tagsState by viewModel.tagsState.collectAsState()
    val context = LocalContext.current

    // Felder für das Formular
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf<CollectionDto?>(null) }
    var tagInput by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(listOf<TagDto>()) }
    var tagSuggestions by remember { mutableStateOf(listOf<TagDto>()) }
    var tagDropdownExpanded by remember { mutableStateOf(false) }
    var collectionDropdownExpanded by remember { mutableStateOf(false) }

    val isLoading = uiState is AddLinkUiState.Loading

    // Linkdaten laden, falls linkId gesetzt
    LaunchedEffect(linkId) {
        if (linkId != null) {
            val result = ApiClient.getLinkById(linkId)
            result.onSuccess { linkDetailResponse ->
                val editLink = linkDetailResponse.response
                viewModel.initializeForEdit(editLink)
                url = editLink.url
                name = editLink.name
                description = editLink.description ?: ""
                selectedCollection = editLink.collection?.let {
                    CollectionDto(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        icon = it.icon,
                        iconWeight = it.iconWeight,
                        color = it.color,
                        parentId = it.parentId,
                        isPublic = it.isPublic,
                        ownerId = it.ownerId,
                        createdById = it.createdById,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
                selectedTags = editLink.tags.map { tag ->
                    TagDto(
                        id = tag.id,
                        name = tag.name,
                        ownerId = tag.ownerId,
                        createdAt = tag.createdAt,
                        updatedAt = tag.updatedAt
                    )
                }
            }
        }
    }

    // Autocomplete-Logik für Tags
    LaunchedEffect(tagInput, tagsState) {
        tagSuggestions = if (tagInput.isBlank() || tagsState !is AddLinkDataState.Success) emptyList()
        else (tagsState as AddLinkDataState.Success<TagDto>).data.filter {
            it.name.contains(tagInput, ignoreCase = true) && selectedTags.none { t -> t.id == it.id }
        }
    }

    // Show messages and navigate back on success
    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is AddLinkUiState.Success -> {
                Toast.makeText(
                    context,
                    if (linkId != null) "Link erfolgreich aktualisiert!" else "Link erfolgreich hinzugefügt!",
                    Toast.LENGTH_SHORT
                ).show()
                onLinkAdded()
            }
            is AddLinkUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (linkId != null) "Link bearbeiten" else "Neuen Link hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val tagString = selectedTags.joinToString(",") { it.name } +
                        if (tagInput.isNotBlank() && selectedTags.none { it.name.equals(tagInput, true) }) ",${tagInput.trim()}" else ""
                    viewModel.addOrUpdateLink(
                        url,
                        name,
                        description,
                        tagString.trim(',').trim(),
                        selectedCollection?.name ?: ""
                    )
                },
                icon = { Icon(Icons.Filled.Save, contentDescription = if (linkId != null) "Aktualisieren" else "Speichern") },
                text = { Text(if (linkId != null) "Aktualisieren" else "Speichern") },
                expanded = !isLoading
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = "Neuen Link hinzufügen",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL *") },
                        placeholder = { Text("https://example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        placeholder = { Text("Titel des Links") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beschreibung") },
                        placeholder = { Text("Notizen ...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    // Tags Section
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tag,
                                    contentDescription = "Tags",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Tags",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            }
                            // Chips für ausgewählte Tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedTags.forEach { tag ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(tag.name) },
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                selectedTags = selectedTags.filter { it.id != tag.id }
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Entfernen")
                                            }
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            labelColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                                // Chip für neuen Tag (freie Eingabe)
                                if (tagInput.isNotBlank() && selectedTags.none { it.name.equals(tagInput, true) }) {
                                    AssistChip(
                                        onClick = {
                                            selectedTags = selectedTags + TagDto(
                                                id = -1 * (tagInput.hashCode()),
                                                name = tagInput.trim(),
                                                ownerId = 0,
                                                createdAt = "",
                                                updatedAt = ""
                                            )
                                            tagInput = ""
                                        },
                                        label = { Text(tagInput.trim()) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                            labelColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                            }
                            // Autocomplete Textfeld
                            ExposedDropdownMenuBox(
                                expanded = tagDropdownExpanded,
                                onExpandedChange = { tagDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = tagInput,
                                    onValueChange = {
                                        tagInput = it
                                        tagDropdownExpanded = it.isNotBlank() && tagSuggestions.isNotEmpty()
                                    },
                                    label = { Text("Tag suchen oder neu eingeben") },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    enabled = !isLoading,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagDropdownExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                        cursorColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = tagDropdownExpanded,
                                    onDismissRequest = { tagDropdownExpanded = false }
                                ) {
                                    tagSuggestions.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion.name) },
                                            onClick = {
                                                selectedTags = selectedTags + suggestion
                                                tagInput = ""
                                                tagDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Collection Section
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Collection",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Kategorie",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            }
                            // Dropdown für Collections
                            when (collectionsState) {
                                is AddLinkDataState.Loading -> {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                                is AddLinkDataState.Error -> {
                                    Text((collectionsState as AddLinkDataState.Error).message, color = MaterialTheme.colorScheme.error)
                                }
                                is AddLinkDataState.Success -> {
                                    val collections = (collectionsState as AddLinkDataState.Success<CollectionDto>).data
                                    ExposedDropdownMenuBox(
                                        expanded = collectionDropdownExpanded,
                                        onExpandedChange = { collectionDropdownExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedCollection?.name ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Kategorie auswählen") },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            enabled = !isLoading,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = collectionDropdownExpanded) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                                                cursorColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            shape = MaterialTheme.shapes.small,
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = collectionDropdownExpanded,
                                            onDismissRequest = { collectionDropdownExpanded = false }
                                        ) {
                                            collections.forEach { col ->
                                                DropdownMenuItem(
                                                    text = { Text(col.name) },
                                                    onClick = {
                                                        selectedCollection = col
                                                        collectionDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Visual separator before save button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
                }
            }
        }
    }
} 