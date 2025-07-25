package com.example.nutriai.screens

data class NutritionRatingResponse(
    val rating: String,
    val comment: String,
    val nutrition_values: NutritionValues,
    val warnings: List<String>
)

data class NutritionValues(
    val protein: String,
    val sugar: String,
    val sodium: String,
    val total_calories: String
)
