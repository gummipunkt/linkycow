package com.wltr.linkycow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.wltr.linkycow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogoutClick: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel()
    val instanceUrl by viewModel.instanceUrl.collectAsState(initial = "")
    val username by viewModel.username.collectAsState(initial = "")
    
    var showCredentialsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showCredentialsDialog) {
        CredentialsEditDialog(
            currentUrl = instanceUrl,
            currentUsername = username,
            onDismiss = { showCredentialsDialog = false },
            onSave = { newUrl, newUsername, newPassword ->
                viewModel.updateCredentials(newUrl, newUsername, newPassword)
                showCredentialsDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout)) },
            text = { Text(stringResource(R.string.settings_logout_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(stringResource(R.string.settings_logout_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.settings_logout_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Section
            Text(
                text = stringResource(R.string.settings_account),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_change_credentials)) },
                        supportingContent = { Text(stringResource(R.string.settings_change_credentials_desc)) },
                        leadingContent = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCredentialsDialog = true }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_logout)) },
                        supportingContent = { Text(stringResource(R.string.settings_logout_desc)) },
                        leadingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card {
                ListItem(
                    headlineContent = { Text("About") },
                    supportingContent = { Text("App information, developer and license") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("about") }
                )
            }
            
            // Current Server Info
            if (instanceUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Current Server",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "URL: $instanceUrl",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (username.isNotEmpty()) {
                            Text(
                                text = "User: $username",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsEditDialog(
    currentUrl: String,
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Credentials") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Leave the password field empty to keep the current password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(url, username, password) },
                enabled = url.isNotBlank() && username.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 