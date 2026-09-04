package com.edgereader.tts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edgereader.tts.ui.screens.HomeScreen
import com.edgereader.tts.ui.screens.NewBookScreen
import com.edgereader.tts.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToNewBook = { navController.navigate("new_book") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("new_book") {
            NewBookScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
