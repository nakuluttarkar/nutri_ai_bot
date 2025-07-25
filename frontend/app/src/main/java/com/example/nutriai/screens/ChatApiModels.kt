package com.example.nutriai.screens

data class ChatRequest(
    val user_input: String,
    val chat_history: List<ChatMessage>,
    val summary: String
)

data class ChatMessage(
    val role: String,    // "user" or "assistant"
    val content: String
)

data class ChatResponse(
    val general_response: String,
    val recipes: List<Recipe>? = null,
    val pediatricians: List<Pediatrician>? = null
)

data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val instructions: String
)

data class Pediatrician(
    val name: String,
    val phone: String,
    val email: String
)
