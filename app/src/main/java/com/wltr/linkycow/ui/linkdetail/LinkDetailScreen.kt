package com.wltr.linkycow.ui.linkdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wltr.linkycow.data.remote.dto.LinkDetailData
import com.wltr.linkycow.data.remote.dto.LinkDetailResponse
import com.wltr.linkycow.ui.common.ClickableUrlText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDetailScreen(
    navController: NavController,
    linkId: Int
) {
    val viewModel: LinkDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Link") },
            text = { Text("Are you sure you want to delete this link? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLink(linkId)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is LinkDetailEvent.ArchiveError -> scope.launch { snackbarHostState.showSnackbar(event.message) }
                is LinkDetailEvent.DeleteError -> scope.launch { snackbarHostState.showSnackbar(event.message) }
                is LinkDetailEvent.ArchiveSuccess, is LinkDetailEvent.DeleteSuccess -> {
                    // Navigate back and force a refresh
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(linkId) {
        viewModel.loadLink(linkId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Link Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.archiveLink(linkId) },
                        enabled = !actionInProgress
                    ) {
                        Icon(Icons.Filled.Archive, contentDescription = "Archive")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !actionInProgress
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is LinkDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LinkDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LinkDetailUiState.Success -> {
                    LinkDetails(link = state.link.response)
                }
            }
            if (actionInProgress) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun LinkDetails(link: LinkDetailData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = link.name, style = MaterialTheme.typography.headlineSmall)
        ClickableUrlText(url = link.url, style = MaterialTheme.typography.bodyLarge)
        link.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
        Divider()
        Text("Tags:", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            link.tags.forEach { tag ->
                AssistChip(onClick = { /*TODO*/ }, label = { Text(tag.name) })
            }
        }
        link.collection?.let { collection ->
            Divider()
            Text("Collection:", style = MaterialTheme.typography.titleMedium)
            Text(collection.name)
        }
    }
} 