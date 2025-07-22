package com.wltr.linkycow.ui.filteredlinks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wltr.linkycow.ui.common.LinkItem
import com.wltr.linkycow.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredLinksScreen(
    navController: NavController,
    filterType: String?,
    filterId: Int?,
    filterName: String?
) {
    val viewModel: FilteredLinksViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = filterType, key2 = filterId) {
        viewModel.loadLinks(filterType, filterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = filterName ?: "Links") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is FilteredLinksUiState.Loading -> CircularProgressIndicator()
                is FilteredLinksUiState.Error -> Text(state.message)
                is FilteredLinksUiState.Success -> {
                    if (state.links.isEmpty()) {
                        Text("No links found for this filter.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.links) { link ->
                                LinkItem(
                                    link = link,
                                    onLinkClick = { linkId ->
                                        navController.navigate(Screen.LinkDetail.createRoute(linkId))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 