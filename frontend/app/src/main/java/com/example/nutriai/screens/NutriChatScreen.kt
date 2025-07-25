package com.example.nutriai.screens

import android.util.Log
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
import androidx.compose.ui.unit.dp
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
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Scroll to bottom when chat updates
    LaunchedEffect(chatHistory.size) {
        listState.animateScrollToItem(chatHistory.size)
        Log.d("NUTRI_CHAT", "Scrolled to chat size: ${chatHistory.size}")
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        // Chat area
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            state = listState,
            reverseLayout = false,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(chatHistory.size) { idx ->
                val msg = chatHistory[idx]
                val isUser = msg.role == "user"
                Log.d("NUTRI_CHAT", "Rendering message[$idx]: role=${msg.role}, content='${msg.content}'")
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) Color(0xFF1A6AC7) else Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp,
                        tonalElevation = 2.dp,
                        modifier = Modifier.padding(4.dp).widthIn(max = 320.dp)
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isUser) Color.White else Color.Black,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = if (isUser) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Error or status
        errorText?.let {
            Log.d("NUTRI_CHAT", "Error: $it")
            Text(it, color = Color.Red, modifier = Modifier.padding(start = 12.dp, bottom = 2.dp))
        }

        // Input area
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Nutri AI...") },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFEEEFFF),
                    focusedBorderColor = Color(0xFF1A6AC7)
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        Log.d("NUTRI_CHAT", "Send clicked, userInput='$userInput'")
                        isLoading = true
                        errorText = null
                        coroutineScope.launch {
                            Log.d("NUTRI_CHAT", "Calling sendChatRequest()")
                            val response = sendChatRequest(
                                userInput = userInput,
                                chatHistory = chatHistory,
                                summary = summary,
                                apiUrl = com.example.nutriai.data.ApiConfig.BASE_URL + "chat"
                            )
                            isLoading = false
                            if (response != null) {
                                Log.d("NUTRI_CHAT", "Got response: '${response.general_response}'")
                                // Append user input and AI response to history
                                chatHistory = chatHistory +
                                        ChatMessage("user", userInput) +
                                        ChatMessage("assistant", response.general_response)
                                Log.d("NUTRI_CHAT", "Updated chatHistory size: ${chatHistory.size}")
                                userInput = ""
                                summary = response.general_response

                                // Optional: Add recipes and pediatricians as separate messages
                                response.recipes?.forEach { r ->
                                    Log.d("NUTRI_CHAT", "Adding recipe: ${r.name}")
                                    chatHistory = chatHistory + ChatMessage(
                                        "assistant",
                                        "🍲 *${r.name}*\nIngredients: ${r.ingredients.joinToString()}\nInstructions: ${r.instructions}"
                                    )
                                }
                                response.pediatricians?.forEach { doc ->
                                    Log.d("NUTRI_CHAT", "Adding pediatrician: ${doc.name}")
                                    chatHistory = chatHistory + ChatMessage(
                                        "assistant",
                                        "👩‍⚕️ ${doc.name}\nPhone: ${doc.phone}\nEmail: ${doc.email}"
                                    )
                                }
                            } else {
                                errorText = "Failed to get response!"
                                Log.d("NUTRI_CHAT", "Response is null. Showing error.")
                            }
                        }
                    }
                },
                enabled = !isLoading && userInput.isNotBlank(),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
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
