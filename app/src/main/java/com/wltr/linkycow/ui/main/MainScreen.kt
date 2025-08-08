package com.wltr.linkycow.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wltr.linkycow.data.remote.dto.CollectionDto
import com.wltr.linkycow.data.remote.dto.TagDto
import com.wltr.linkycow.ui.common.LinkItem
import com.wltr.linkycow.ui.navigation.Screen
import androidx.compose.ui.res.stringResource
import com.wltr.linkycow.R
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    onLogoutClick: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedLinkIds by viewModel.selectedLinkIds.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    // Effect to handle user-initiated pull-to-refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshDashboard()
        }
    }

    // Effect to handle the refresh signal from other screens
    val refresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.observeAsState()

    LaunchedEffect(refresh?.value) {
        if (refresh?.value == true) {
            viewModel.refreshDashboard()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
        }
    }

    // Effect to stop the refresh indicator when the ViewModel is no longer loading
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    val pagedLinks by viewModel.pagedLinks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = remember { LazyListState() }

    // Initiales Paging laden
    LaunchedEffect(Unit) {
        viewModel.loadInitialLinks()
    }

    // Infinite Scrolling: Wenn das letzte Element sichtbar wird, lade mehr
    LaunchedEffect(pagedLinks, listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        if (lastVisible != null && total > 0 && lastVisible >= total - 3) {
            viewModel.loadMoreLinks()
        }
    }

    Scaffold(
        topBar = {
            when {
                isSelectionMode -> {
                    SelectionTopAppBar(
                        selectedCount = selectedLinkIds.size,
                        onClearSelection = { viewModel.toggleSelectionMode() },
                        onSelectAll = { viewModel.selectAllLinks() },
                        onDeleteSelected = { viewModel.deleteSelectedLinks() }
                    )
                }
                isSearchExpanded -> {
                    SearchTopAppBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onCloseSearch = {
                            isSearchExpanded = false
                            viewModel.onSearchQueryChanged("")
                        }
                    )
                }
                else -> {
                    TopAppBar(
                        title = { Text("Dashboard") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        actions = {
                            IconButton(onClick = { isSearchExpanded = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddLink.createRoute(null)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Link")
            }
        }
    ) { paddingValues ->
        val currentState = uiState

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            // Content area that is replaced by a loading spinner on initial load
            when (currentState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is DashboardUiState.Success -> {
                    // Main content with filter chips and link list
                    Column(modifier = Modifier.fillMaxSize()) {
                        FilterChips(
                            collections = currentState.collections,
                            tags = currentState.tags,
                            onFilterSelected = { filter -> viewModel.setFilter(filter) },
                            selectedCollectionId = viewModel.selectedCollectionId,
                            selectedTagId = viewModel.selectedTagId,
                            isEnabled = !isRefreshing
                        )

                        val linksToShow = if (searchQuery.isBlank()) pagedLinks else searchResults
                        if (linksToShow.isEmpty() && !isSearching) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isBlank()) stringResource(R.string.main_no_links) else stringResource(R.string.main_no_results, searchQuery)
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(linksToShow) { link ->
                                    LinkItem(
                                        link = link,
                                        onLinkClick = { linkId ->
                                            navController.navigate(Screen.LinkDetail.createRoute(linkId))
                                        },
                                        onDelete = { linkId ->
                                            viewModel.deleteLink(linkId)
                                        },
                                        isSelectionMode = isSelectionMode,
                                        isSelected = link.id in selectedLinkIds,
                                        onToggleSelection = { linkId ->
                                            viewModel.toggleLinkSelection(linkId)
                                        },
                                        onLongClick = {
                                            viewModel.toggleSelectionMode()
                                            viewModel.toggleLinkSelection(link.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Separate loading indicator for the search functionality
            if (isSearching && searchQuery.isNotBlank()) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
            )
        }
    }
}

@Composable
fun FilterChips(
    collections: List<CollectionDto>,
    tags: List<TagDto>,
    onFilterSelected: (Any?) -> Unit,
    selectedCollectionId: Int?,
    selectedTagId: Int?,
    isEnabled: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(collections) { collection ->
                FilterChip(
                    enabled = isEnabled,
                    selected = collection.id == selectedCollectionId,
                    onClick = { onFilterSelected(collection) },
                    label = { Text(collection.name) },
                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(tags) { tag ->
                FilterChip(
                    enabled = isEnabled,
                    selected = tag.id == selectedTagId,
                    onClick = { onFilterSelected(tag) },
                    label = { Text(tag.name) },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search links...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = { 
            Text("$selectedCount ausgewählt")
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Auswahl beenden")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Alle auswählen")
            }
            IconButton(
                onClick = onDeleteSelected,
                enabled = selectedCount > 0
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Ausgewählte löschen")
            }
        }
    )
} 