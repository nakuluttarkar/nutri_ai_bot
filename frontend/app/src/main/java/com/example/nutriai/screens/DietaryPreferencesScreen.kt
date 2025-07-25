package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DietaryPreferencesScreen(navController: NavController) {
    var notes by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Dietary Preferences", style = MaterialTheme.typography.headlineSmall)
            // Add more form fields and checkboxes as you need
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Additional Notes") })
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("dashboard") }) {
                Text("Complete Setup")
            }
        }
    }
}
