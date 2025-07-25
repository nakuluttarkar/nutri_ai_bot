package com.example.nutriai.screens

import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import java.util.concurrent.TimeUnit


suspend fun sendChatRequest(
    userInput: String,
    apiUrl: String
): ChatResponse? = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)  // Time to establish connection
        .writeTimeout(180, TimeUnit.SECONDS)    // Time to send request
        .readTimeout(180, TimeUnit.SECONDS)     // Time to wait for response
        .build()


    // Build request JSON
    val reqJson = JSONObject().apply {
        put("user_input", userInput)
    }

    val requestBody = reqJson.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(apiUrl)
        .post(requestBody)
        .build()

    client.newCall(request).execute().use { response ->
        val raw = response.body?.string()
        Log.d("NUTRI_CHAT", "Raw HTTP response: $raw")
        if (response.isSuccessful && raw != null) {
            val outerJson = JSONObject(raw)
            Log.d("NUTRI_CHAT", "Parsed outerJson: $outerJson")

            if (outerJson.has("response")) {
                val innerString = outerJson.getString("response")
                Log.d("NUTRI_CHAT", "Double-parsing inner JSON: $innerString")

                // Try to parse as JSON; if fails, treat as plain text
                return@use try {
                    val json = JSONObject(innerString)

                    val generalResponse = json.optString("general_response", "")
                    Log.d("NUTRI_CHAT", "Parsed general_response: '$generalResponse'")

                    val recipesList = if (json.has("recipes")) {
                        json.optJSONArray("recipes")?.let { arr ->
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

                    val pediatriciansList = if (json.has("pediatricians")) {
                        json.optJSONArray("pediatricians")?.let { arr ->
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
                } catch (e: Exception) {
                    Log.d("NUTRI_CHAT", "response is plain string, not JSON")
                    ChatResponse(
                        general_response = innerString,
                        recipes = null,
                        pediatricians = null
                    )
                }
            } else {
                // No "response" key: treat as regular direct OpenAI-like output (rare)
                val generalResponse = outerJson.optString("general_response", "")
                Log.d("NUTRI_CHAT", "Direct general_response: '$generalResponse'")

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

                return@use if (generalResponse.isNotBlank() || recipesList != null || pediatriciansList != null) {
                    ChatResponse(
                        general_response = generalResponse,
                        recipes = recipesList,
                        pediatricians = pediatriciansList
                    )
                } else {
                    null
                }
            }
        } else {
            Log.d("NUTRI_CHAT", "HTTP request failed or body was null.")
            null
        }
    }
}
