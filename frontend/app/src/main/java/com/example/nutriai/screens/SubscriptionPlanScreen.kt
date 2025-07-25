package com.example.nutriai.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubscriptionCard(
    title: String,
    oldPrice: String,
    price: String,
    description: String,
    features: List<Pair<String, Boolean>>,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    oldPrice,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.LineThrough
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Text(
                    "/mo",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(description, color = Color(0xFF555555), fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                features.forEach { (feature, enabled) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (enabled) Color(0xFF22C55E) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = feature,
                            color = if (enabled) Color.Black else Color.Gray,
                            fontSize = 15.sp,
                            textDecoration = if (!enabled) TextDecoration.LineThrough else null
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Select Plan", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun SubscriptionPlanScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubscriptionCard(
            title = "Starter",
            oldPrice = "IDR 30k",
            price = "IDR 0",
            description = "Access AI tools & our recipe library for free.",
            features = listOf(
                "Nutri-Chat AI" to true,
                "Recipe & article library" to true,
                "Food Analysis" to true,
                "Personalized meal plan" to false,
                "Expert Consultations" to false
            ),
            onClick = { /* handle starter plan click */ }
        )

        SubscriptionCard(
            title = "Growth",
            oldPrice = "IDR 79k",
            price = "IDR 49k",
            description = "Complete personalized guidance for one child.",
            features = listOf(
                "All Starter features" to true,
                "Personalized meal plan" to true,
                "2x Expert Consultation" to true,
                "Detailed nutrition & growth report" to true,
                "Access to community" to true
            ),
            onClick = { /* handle growth plan click */ }
        )
    }
}

