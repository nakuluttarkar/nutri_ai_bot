package com.example.nutriai.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMealTab(
    nutritionSummary: NutritionSummary,
    onSummaryUpdate: (NutritionSummary) -> Unit = {}
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Camera launcher for Log Meal
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            isUploading = true
            uploadError = null
            coroutineScope.launch {
                try {
                    val newSummary = uploadAndAnalyzeMeal(
                        context = context,
                        imageUri = photoUri!!,
                        apiUrl = "http://YOUR_IP_OR_NGROK/analyze-food/"
                    )
                    if (newSummary != null) {
                        onSummaryUpdate(newSummary)
                    } else {
                        uploadError = "No nutrition info found"
                    }
                    isUploading = false
                } catch (e: Exception) {
                    uploadError = "Failed to upload or process meal!"
                    isUploading = false
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nutrition Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD876), Color(0xFFFF5E62))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Today's Nutrition Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    NutritionProgressBar("Protein", nutritionSummary.protein, nutritionSummary.proteinTarget, Color(0xFFFCCF31))
                    NutritionProgressBar("Carbs", nutritionSummary.carbs, nutritionSummary.carbsTarget, Color(0xFFFF8177))
                    NutritionProgressBar("Fats", nutritionSummary.fats, nutritionSummary.fatsTarget, Color(0xFFB2FFDA))
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${nutritionSummary.calories}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            " / ${nutritionSummary.caloriesTarget} kcal",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        // Log Meal Button
        Button(
            onClick = {
                val file = createImageFile(context)
                photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(photoUri)
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD876)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isUploading
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Log Meal")
            Spacer(Modifier.width(12.dp))
            Text(if (isUploading) "Uploading..." else "Log Meal", fontWeight = FontWeight.Bold)
        }

        if (uploadError != null) {
            Spacer(Modifier.height(12.dp))
            Text("Error: $uploadError", color = Color.Red)
        }
    }
}

// --- Helper Composables and Functions ---

@Composable
fun NutritionProgressBar(
    label: String,
    value: Int,
    target: Int,
    barColor: Color
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, modifier = Modifier.width(70.dp))
            LinearProgressIndicator(
                progress = (value.toFloat() / target).coerceIn(0f, 1f),
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .padding(horizontal = 8.dp),
                color = barColor,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Text("$value / $target g", color = Color.White, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
    }
}

fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", ".jpg", storageDir
    )
}

/**
 * This function:
 * 1. Reads the image, converts to base64
 * 2. Calls /analyze-food/ POST
 * 3. Parses double-encoded JSON and returns NutritionSummary
 */
suspend fun uploadAndAnalyzeMeal(
    context: Context,
    imageUri: Uri,
    apiUrl: String
): NutritionSummary? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val imageBase64 = if (imageBytes != null) {
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } else return@withContext null

        // Build request JSON
        val jsonBody = JSONObject()
        jsonBody.put("image_base64", imageBase64)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        // Outer JSON
        val outerJson = JSONObject(response.body?.string() ?: return@withContext null)
        // Parse the "response" field (inner JSON string)
        val innerJson = JSONObject(outerJson.getString("response"))

        // Example: Parse values with sensible defaults if missing
        return@withContext NutritionSummary(
            calories = innerJson.optJSONObject("nutrition_estimates")?.optString("calories")?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0,
            caloriesTarget = 800, // Set your user default/target as appropriate
            protein = innerJson.optJSONObject("nutrition_estimates")?.optString("protein")?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0,
            proteinTarget = 70,    // Set user default/target
            carbs = innerJson.optJSONObject("nutrition_estimates")?.optString("carbohydrates")?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0,
            carbsTarget = 200,     // Set user default/target
            fats = innerJson.optJSONObject("nutrition_estimates")?.optString("fat")?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0,
            fatsTarget = 50        // Set user default/target
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}
