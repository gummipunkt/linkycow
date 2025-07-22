package com.wltr.linkycow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wltr.linkycow.ui.about.AboutScreen
import com.wltr.linkycow.ui.addlink.AddLinkScreen
import com.wltr.linkycow.ui.filteredlinks.FilteredLinksScreen
import com.wltr.linkycow.ui.linkdetail.LinkDetailScreen
import com.wltr.linkycow.ui.login.LoginScreen
import com.wltr.linkycow.ui.login.LoginUiState
import com.wltr.linkycow.ui.login.LoginViewModel
import com.wltr.linkycow.ui.main.MainScreen
import com.wltr.linkycow.ui.main.MainViewModel
import com.wltr.linkycow.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel()
            val uiState by loginViewModel.uiState.collectAsState()

            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginClick = { instanceUrl, username, password ->
                    loginViewModel.login(instanceUrl, username, password)
                }
            )

            LaunchedEffect(uiState) {
                if (uiState is LoginUiState.Success) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    loginViewModel.onNavigationDone()
                }
            }
        }
        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = viewModel()
            MainScreen(
                navController = navController,
                onLogoutClick = {
                    mainViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.LinkDetail.route,
            arguments = listOf(navArgument("linkId") { type = NavType.IntType })
        ) { backStackEntry ->
            LinkDetailScreen(
                navController = navController,
                linkId = backStackEntry.arguments?.getInt("linkId") ?: -1
            )
        }
        composable(
            route = Screen.AddLink.route,
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            AddLinkScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinkAdded = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                    navController.popBackStack()
                },
                linkId = id
            )
        }
        composable(
            route = Screen.FilteredLinks.route,
            arguments = listOf(
                navArgument("filterType") { type = NavType.StringType },
                navArgument("filterId") { type = NavType.IntType },
                navArgument("filterName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            FilteredLinksScreen(
                navController = navController,
                filterType = backStackEntry.arguments?.getString("filterType"),
                filterId = backStackEntry.arguments?.getInt("filterId"),
                filterName = backStackEntry.arguments?.getString("filterName")
            )
        }
        composable(Screen.Settings.route) {
            val mainViewModel: MainViewModel = viewModel()
            SettingsScreen(
                navController = navController,
                onLogoutClick = {
                    mainViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                navController = navController
            )
        }
    }
} 