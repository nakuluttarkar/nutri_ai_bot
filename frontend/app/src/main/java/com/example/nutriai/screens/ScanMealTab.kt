package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScanMealTab() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan your meal to get nutrition info!", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { /* TODO: Integrate camera scan */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Meal")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Feature coming soon!",
            color = MaterialTheme.colorScheme.primary
        )
    }
}
