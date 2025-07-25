package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.nutriai.screens.NutritionSummary


@Composable
fun MealTrackerScreen() {
    val tabs = listOf("Log Meal", "Weekly Reports")
    var selectedTab by remember { mutableStateOf(0) }

    var nutritionSummary by remember {
        mutableStateOf(
            NutritionSummary(
                calories = 1250,
                caloriesTarget = 1800,
                protein = 60,
                proteinTarget = 70,
                carbs = 150,
                carbsTarget = 200,
                fats = 40,
                fatsTarget = 50
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }
        when (selectedTab) {
            0 -> LogMealTab(
                nutritionSummary = nutritionSummary,
                onSummaryUpdate = { newSummary -> nutritionSummary = newSummary }
            )
            1 -> WeeklyReportsTab()
        }
    }
}
