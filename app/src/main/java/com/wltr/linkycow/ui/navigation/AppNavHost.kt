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
import com.wltr.linkycow.ui.linkdetail.LinkDetailScreen
import com.wltr.linkycow.ui.login.LoginScreen
import com.wltr.linkycow.ui.login.LoginUiState
import com.wltr.linkycow.ui.login.LoginViewModel
import com.wltr.linkycow.ui.main.MainScreen
import com.wltr.linkycow.ui.main.MainViewModel
import com.wltr.linkycow.ui.settings.SettingsScreen
import com.wltr.linkycow.data.remote.dto.FullLinkData

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: Int) = "link_detail/$linkId"
    }
    object AddLink : Screen("add_link?linkId={linkId}") {
        fun createRoute(linkId: Int? = null) = if (linkId != null) "add_link?linkId=$linkId" else "add_link"
    }
    object Settings : Screen("settings")
    object About : Screen("about")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
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
            val linkId = backStackEntry.arguments?.getInt("linkId")
            requireNotNull(linkId) { "linkId parameter wasn't found. Please make sure it's set!" }
            LinkDetailScreen(
                navController = navController,
                linkId = linkId
            )
        }
        composable(
            route = Screen.AddLink.route,
            arguments = listOf(
                navArgument("linkId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val linkId = backStackEntry.arguments?.getInt("linkId")?.takeIf { it != -1 }
            AddLinkScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinkAdded = {
                    navController.popBackStack()
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                },
                linkId = linkId
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
            AboutScreen(navController = navController)
        }
    }
} 