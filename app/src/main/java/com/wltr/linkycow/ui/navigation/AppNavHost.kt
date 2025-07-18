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
import com.wltr.linkycow.ui.addlink.AddLinkScreen
import com.wltr.linkycow.ui.linkdetail.LinkDetailScreen
import com.wltr.linkycow.ui.login.LoginScreen
import com.wltr.linkycow.ui.login.LoginUiState
import com.wltr.linkycow.ui.login.LoginViewModel
import com.wltr.linkycow.ui.main.MainScreen
import com.wltr.linkycow.ui.main.MainViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: Int) = "link_detail/$linkId"
    }
    object AddLink : Screen("add_link")
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
        composable(Screen.AddLink.route) {
            AddLinkScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinkAdded = {
                    navController.popBackStack()
                    // This is a bit of a hack to force the MainViewModel to refresh.
                    // A better solution would involve a shared repository that both screens observe.
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                }
            )
        }
    }
} 