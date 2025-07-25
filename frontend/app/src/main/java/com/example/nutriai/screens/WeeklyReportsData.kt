package com.example.nutriai.screens

data class DailyIntake(
    val day: String,
    val intake: Int,
    val target: Int
)

data class MacroDistribution(
    val protein: Int,
    val carbs: Int,
    val fats: Int
)
