package com.example.nutriai.screens

import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun sendChatRequest(
    userInput: String,
    chatHistory: List<ChatMessage>,
    summary: String,
    apiUrl: String
): ChatResponse? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    // Build request JSON
    val reqJson = JSONObject().apply {
        put("user_input", userInput)
        put("chat_history", JSONArray(chatHistory.map {
            JSONObject().apply {
                put("role", it.role)
                put("content", it.content)
            }
        }))
        put("summary", summary)
    }

    val requestBody = reqJson.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(apiUrl)
        .post(requestBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            // Double parse because response is a stringified JSON
            val outerJson = JSONObject(response.body?.string() ?: return@use null)
            val generalResponse = outerJson.optString("general_response", "")

            // Parse optional fields if present
            val recipesList = if (outerJson.has("recipes")) {
                outerJson.optJSONArray("recipes")?.let { arr ->
                    List(arr.length()) { i ->
                        arr.getJSONObject(i).run {
                            Recipe(
                                name = optString("name"),
                                ingredients = optJSONArray("ingredients")?.let { ingrArr ->
                                    List(ingrArr.length()) { j -> ingrArr.getString(j) }
                                } ?: emptyList(),
                                instructions = optString("instructions")
                            )
                        }
                    }
                }
            } else null

            val pediatriciansList = if (outerJson.has("pediatricians")) {
                outerJson.optJSONArray("pediatricians")?.let { arr ->
                    List(arr.length()) { i ->
                        arr.getJSONObject(i).run {
                            Pediatrician(
                                name = optString("name"),
                                phone = optString("phone"),
                                email = optString("email")
                            )
                        }
                    }
                }
            } else null

            ChatResponse(
                general_response = generalResponse,
                recipes = recipesList,
                pediatricians = pediatriciansList
            )
        } else {
            null
        }
    }
}
