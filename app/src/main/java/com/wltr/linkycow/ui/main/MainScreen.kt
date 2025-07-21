package com.wltr.linkycow.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wltr.linkycow.data.remote.dto.Link
import com.wltr.linkycow.ui.common.ClickableUrlText
import com.wltr.linkycow.ui.navigation.Screen

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
    var isSearchExpanded by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    // Check for the refresh signal from the AddLinkScreen
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

    Scaffold(
        topBar = {
            if (isSearchExpanded) {
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onCloseSearch = {
                        isSearchExpanded = false
                        viewModel.onSearchQueryChanged("")
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Dashboard") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddLink.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Link")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            val linksToShow = if (searchQuery.isBlank()) {
                (uiState as? DashboardUiState.Success)?.links
            } else {
                searchResults
            }

            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    is DashboardUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is DashboardUiState.Success -> {
                        if (pullToRefreshState.isRefreshing) {
                            LaunchedEffect(true) {
                                viewModel.refreshDashboard()
                            }
                        }

                        LaunchedEffect(isRefreshing) {
                            if (isRefreshing) {
                                pullToRefreshState.startRefresh()
                            } else {
                                pullToRefreshState.endRefresh()
                            }
                        }

                        if (linksToShow.isNullOrEmpty() && !isSearching) {
                            Text(
                                text = if (searchQuery.isBlank()) "No links found." else "No results for '$searchQuery'",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.background,
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                                            )
                                        )
                                    ),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                linksToShow?.let {
                                    items(it) { link ->
                                        LinkItem(
                                            link = link,
                                            onClick = {
                                                navController.navigate(Screen.LinkDetail.createRoute(link.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
            )
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
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItem(link: Link, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row with title and optional type/icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Title with gradient-like styling
                Text(
                    text = link.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                // Type indicator if available
                link.type?.let { type ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // URL with subtle styling
            ClickableUrlText(
                url = link.url,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            
            // Description if available
            if (link.description != null) {
                Text(
                    text = link.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
            
            // Tags if available
            if (!link.tags.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Tag,
                        contentDescription = "Tags",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(minOf(link.tags.size, 3)) { index -> // Show max 3 tags
                            val tag = link.tags[index]
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        tag.name,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                    labelColor = MaterialTheme.colorScheme.tertiary
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        
                        // Show "+" indicator if there are more tags
                        if (link.tags.size > 3) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = "+${link.tags.size - 3}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Collection if available
            link.collection?.let { collection ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Folder,
                        contentDescription = "Collection",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = collection.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Add visual interest with a subtle divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
} 