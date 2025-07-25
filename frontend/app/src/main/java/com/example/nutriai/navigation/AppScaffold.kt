package com.example.nutriai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nutriai.screens.MealTrackerScreen
import com.example.nutriai.screens.HealthHubScreen
import com.example.nutriai.screens.NutriChatScreen
import com.example.nutriai.screens.SubscriptionPlanScreen

@Composable
fun AppScaffold(navController: NavHostController) {
    val navItems = listOf(
        BottomNavItem("Meal Tracker", "meal_tracker", Icons.Filled.List),
        BottomNavItem("Health Hub", "health_hub", Icons.Filled.Favorite),
        BottomNavItem("Nutri Chat", "nutri_chat", Icons.Filled.Chat),
        BottomNavItem("Subscription", "subscription_plan", Icons.Filled.Star)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "meal_tracker",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("meal_tracker") { MealTrackerScreen() }
            composable("health_hub") { HealthHubScreen() }
            composable("nutri_chat") { NutriChatScreen() }
            composable("subscription_plan") { SubscriptionPlanScreen() }
        }
    }
}
