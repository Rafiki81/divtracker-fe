package com.rafiki81.divtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rafiki81.divtracker.ui.login.LoginScreen
import com.rafiki81.divtracker.ui.register.RegisterScreen
import com.rafiki81.divtracker.ui.ticker.TickerSearchScreen
import com.rafiki81.divtracker.ui.watchlist.CreateEditWatchlistScreen
import com.rafiki81.divtracker.ui.watchlist.WatchlistDetailScreen
import com.rafiki81.divtracker.ui.watchlist.WatchlistScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Watchlist : Screen("watchlist")
    object WatchlistDetail : Screen("watchlist_detail/{itemId}") {
        fun createRoute(itemId: String) = "watchlist_detail/$itemId"
    }
    // Route for searching ticker & Quick Add
    object TickerSearch : Screen("ticker_search")
    
    // Edit existing item (Full manual control)
    object WatchlistEdit : Screen("watchlist_edit/{itemId}") {
        fun createRoute(itemId: String) = "watchlist_edit/$itemId"
    }
}

@Composable
fun AppNavigation(startDestination: String = Screen.Login.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Watchlist.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Watchlist.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.WatchlistDetail.createRoute(itemId))
                },
                onNavigateToCreate = {
                    // Navigate to Ticker Search for Quick Add
                    navController.navigate(Screen.TickerSearch.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TickerSearch.route) {
            TickerSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWatchlist = {
                    // Item added! Go back to watchlist and refresh it automatically via its LaunchedEffect
                    navController.popBackStack() 
                }
            )
        }

        composable(
            route = Screen.WatchlistDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            WatchlistDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.WatchlistEdit.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.WatchlistEdit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            // Edit Screen is only for modifying existing items now
            CreateEditWatchlistScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
