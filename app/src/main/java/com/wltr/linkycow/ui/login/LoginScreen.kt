package com.wltr.linkycow.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginClick: (String, String, String) -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var instanceUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading = uiState is LoginUiState.Loading

    // Show messages based on the state
    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Idle or Loading, do nothing here
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Title
                Text(
                    text = "LinkyCow",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Text(
                    text = "Connect to your Linkwarden instance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = instanceUrl,
                    onValueChange = { newValue ->
                        var url = newValue.replace(" ", "").lowercase()
                        if (!url.startsWith("https://")) url = "https://" + url.removePrefix("http://")
                        instanceUrl = url
                    },
                    label = { Text("Server-URL (https)") },
                    placeholder = { Text("https://your-linkwarden.com") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.None),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue.replace(" ", "").lowercase()
                    },
                    label = { Text("Username") },
                    placeholder = { Text("your-username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.None),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.None),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { onLoginClick(instanceUrl, username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            "Login",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
} 