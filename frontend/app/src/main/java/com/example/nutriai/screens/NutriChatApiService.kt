package com.example.nutriai.screens

import retrofit2.http.Body
import retrofit2.http.POST

interface NutriChatApiService {
    @POST("/your/api/endpoint") // <-- Change to your real endpoint!
    suspend fun sendMessage(@Body request: NutriChatRequest): NutriChatResponse
}
