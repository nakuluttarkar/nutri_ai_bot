package com.example.nutriai.screens

data class ChatHistoryItem(
    val role: String,
    val content: String
)

data class NutriChatRequest(
    val user_input: String,
    val chat_history: List<ChatHistoryItem>,
    val summary: String = ""
)

data class NutriChatResponse(
    val response: String,
    val chat_history: List<ChatHistoryItem>,
    val summary: String
)
