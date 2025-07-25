package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WelcomeScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nutri-AI", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            Text("Smart nutrition for little ones")
            Spacer(Modifier.height(16.dp))
            Text("Track your child's nutrition\nGet personalized meal plans\nMonitor growth & development")
            Spacer(Modifier.height(32.dp))
            Button(onClick = { navController.navigate("create_account") }) {
                Text("Get Started")
            }
        }
    }
}
