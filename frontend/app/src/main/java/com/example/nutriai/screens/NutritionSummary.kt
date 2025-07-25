package com.example.nutriai.screens

data class NutritionSummary(
    val calories: Int,
    val caloriesTarget: Int,
    val protein: Int,
    val proteinTarget: Int,
    val carbs: Int,
    val carbsTarget: Int,
    val fats: Int,
    val fatsTarget: Int,
    val suggestedAlternatives: List<String> = emptyList()
)
