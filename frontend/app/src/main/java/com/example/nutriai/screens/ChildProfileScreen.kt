package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ChildProfileScreen(navController: NavController) {
    var childName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tell us about your child", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(value = childName, onValueChange = { childName = it }, label = { Text("Child's Name") })
            OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (dd/mm/yyyy)") })
            OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") })
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") })
            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") })
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("dietary_preferences") }) {
                Text("Continue")
            }
        }
    }
}
