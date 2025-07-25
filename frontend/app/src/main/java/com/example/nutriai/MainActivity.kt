package com.example.nutriai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.nutriai.navigation.AppScaffold
import com.example.nutriai.ui.theme.NutriAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriAITheme {
                val navController = rememberNavController()
                AppScaffold(navController)
            }
        }
    }
}
