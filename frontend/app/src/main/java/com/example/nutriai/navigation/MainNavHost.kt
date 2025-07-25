package com.example.nutriai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriai.screens.*

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("create_account") { CreateAccountScreen(navController) }
        composable("child_profile") { ChildProfileScreen(navController) }
        composable("dietary_preferences") { DietaryPreferencesScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        // Add more screens here as you build them
    }
}
