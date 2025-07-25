package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Good Morning, Sarah!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* TODO: Navigate to log meal */ }) {
                Text("Log Meal")
            }
            Button(onClick = { /* TODO: Navigate to scan food */ }) {
                Text("Scan Food")
            }
            // Add more cards and navigation buttons as you expand features!
        }
    }
}
