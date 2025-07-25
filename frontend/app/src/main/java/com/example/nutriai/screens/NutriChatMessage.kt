package com.example.nutriai.screens

enum class Sender {
    USER, ASSISTANT
}

data class NutriChatMessage(
    val text: String,
    val sender: Sender,
    val isLoading: Boolean = false // true for loading indicator
)
