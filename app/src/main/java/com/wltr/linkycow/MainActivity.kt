package com.wltr.linkycow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wltr.linkycow.ui.login.LoginViewModel
import com.wltr.linkycow.ui.navigation.AppNavHost
import com.wltr.linkycow.ui.navigation.Screen
import com.wltr.linkycow.ui.theme.LinkyCowTheme

/**
 * Main entry point for the LinkyCow app.
 * Handles authentication state and navigation setup.
 */
class MainActivity : ComponentActivity() {

    // ViewModel for managing authentication state
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LinkyCowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

    /**
     * Main app content with authentication-aware navigation
     */
    @Composable
    private fun AppContent() {
        val authToken by loginViewModel.authTokenFlow.collectAsState(initial = null)
        val navController = rememberNavController()

        when {
            // Show loading while DataStore initializes
            authToken == null -> {
                LoadingScreen()
            }
            // Navigate to main screen if authenticated
            authToken!!.isNotEmpty() -> {
                AppNavHost(
                    navController = navController,
                    startDestination = Screen.Main.route
                )
            }
            // Navigate to login screen if not authenticated
            else -> {
                AppNavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                )
            }
        }
    }

    /**
     * Loading screen shown during app initialization
     */
    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}