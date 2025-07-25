package com.example.nutriai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
// ... (imports remain the same)
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriChatScreen() {
    val coroutineScope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<ChatMessage>()) }
    var summary by remember { mutableStateOf("") }

    var chatResponse by remember { mutableStateOf<ChatResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Input field and send button
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Ask Nutri AI...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (userInput.isNotBlank()) {
                    isLoading = true
                    errorText = null
                    coroutineScope.launch {
                        val response = sendChatRequest(
                            userInput = userInput,
                            chatHistory = chatHistory,
                            summary = summary,
                            apiUrl = "http://YOUR_IP_OR_NGROK/chat"
                        )
                        isLoading = false
                        if (response != null) {
                            chatResponse = response
                            chatHistory = chatHistory + ChatMessage("user", userInput) +
                                    ChatMessage("assistant", response.general_response)
                            userInput = ""
                            summary = response.general_response // Or use your summary logic
                        } else {
                            errorText = "Failed to get response!"
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (isLoading) "Sending..." else "Send")
        }
        Spacer(Modifier.height(16.dp))

        // Display assistant response (always mandatory)
        chatResponse?.let { resp ->
            Text("AI: ${resp.general_response}", fontWeight = FontWeight.Bold, color = Color(0xFF1A6AC7))
            Spacer(Modifier.height(8.dp))

            // Optional recipes
            resp.recipes?.takeIf { it.isNotEmpty() }?.let { recipes ->
                Text("Recommended Recipes:", fontWeight = FontWeight.Bold)
                recipes.forEach { recipe ->
                    Text("• ${recipe.name}", fontWeight = FontWeight.SemiBold)
                    Text("  Ingredients: ${recipe.ingredients.joinToString()}")
                    Text("  Instructions: ${recipe.instructions}")
                    Spacer(Modifier.height(6.dp))
                }
            }
            // Optional pediatricians
            resp.pediatricians?.takeIf { it.isNotEmpty() }?.let { docs ->
                Text("Pediatricians:", fontWeight = FontWeight.Bold)
                docs.forEach { doc ->
                    PediatricianCard(doc)
                }
            }

        }
        errorText?.let { Text(it, color = Color.Red) }
    }
}

@Composable
fun PediatricianCard(pediatrician: Pediatrician, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(pediatrician.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Phone: ${pediatrician.phone}", style = MaterialTheme.typography.bodyMedium)
            Text("Email: ${pediatrician.email}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


