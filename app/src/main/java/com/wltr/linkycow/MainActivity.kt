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

class MainActivity : ComponentActivity() {

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
                    val authToken by loginViewModel.authTokenFlow.collectAsState(initial = null)
                    val navController = rememberNavController()

                    // authToken is null only during the initial loading phase.
                    // After that, it will be an empty string or the token itself.
                    if (authToken != null) {
                        val startDestination = if (authToken!!.isNotEmpty()) {
                            Screen.Main.route
                        } else {
                            Screen.Login.route
                        }
                        AppNavHost(navController = navController, startDestination = startDestination)
                    } else {
                        // Show a loading spinner while we wait for DataStore
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}