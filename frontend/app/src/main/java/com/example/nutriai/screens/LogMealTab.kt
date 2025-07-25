package com.example.nutriai.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
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
import androidx.core.content.ContextCompat
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
import com.example.nutriai.data.ApiConfig
import java.util.concurrent.TimeUnit

data class FoodAnalysisResult(
    val rating: Int = 0,
    val estimatedCalories: Int = 0,
    val comment: String = "",
    val protein: Int = 0,
    val proteinTarget: Int = 0,
    val carbs: Int = 0,
    val carbsTarget: Int = 0,
    val fats: Int = 0,
    val fatsTarget: Int = 0
)

fun stripCodeBlock(jsonStr: String?): String {
    if (jsonStr == null) return ""
    // Remove ```json ... ``` or just ``` ... ```
    return jsonStr
        .replace(Regex("^```(?:json)?\\s*"), "")  // Remove starting ```
        .replace(Regex("\\s*```$"), "")           // Remove ending ```
        .trim()
}


@Composable
fun EnsureCameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) onGranted() else onDenied()
    }
    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            onGranted()
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

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
    var labelResult by remember { mutableStateOf<NutritionRatingResponse?>(null) }
    var labelDialogOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var cameraPermissionGranted by remember { mutableStateOf(false) }

    EnsureCameraPermission(
        onGranted = { cameraPermissionGranted = true },
        onDenied = { cameraPermissionGranted = false }
    )

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
                        apiUrl = ApiConfig.BASE_URL + "analyze-food/"
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
    // Gallery picker for Log Meal
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadError = null
            coroutineScope.launch {
                try {
                    val newSummary = uploadAndAnalyzeMeal(
                        context = context,
                        imageUri = uri,
                        apiUrl = ApiConfig.BASE_URL + "analyze-food/"
                    )
                    if (newSummary != null) {
                        onSummaryUpdate(newSummary)
                    } else {
                        uploadError = "No nutrition info found"
                    }
                } catch (e: Exception) {
                    uploadError = "Failed to upload or process meal!"
                }
                isUploading = false
            }
        }
    }
    // Camera launcher for Rate Label
    val labelCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            isUploading = true
            coroutineScope.launch {
                try {
                    val result = analyzeLabelApi(
                        context = context,
                        imageUri = photoUri!!,
                    )
                    if (result != null) {
                        labelResult = result
                        labelDialogOpen = true
                    } else {
                        uploadError = "Could not analyze label"
                    }
                    isUploading = false
                } catch (e: Exception) {
                    uploadError = "Label API error!"
                    isUploading = false
                }
            }
        }
    }
    // Gallery picker for Rate Label
    val labelGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            coroutineScope.launch {
                try {
                    val result = analyzeLabelApi(
                        context = context,
                        imageUri = uri,
                    )
                    if (result != null) {
                        labelResult = result
                        labelDialogOpen = true
                    } else {
                        uploadError = "Could not analyze label"
                    }
                    isUploading = false
                } catch (e: Exception) {
                    uploadError = "Label API error!"
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
        Text(
            text = "Nutri-AI",
            color = Color(0xFFE0004D),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
        )
        // Nutrition Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE0004D), Color(0xFFE0004D))
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
        Text(
            text = "Add an image of your food to do analysis",
            color = Color(0xFF222222),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Buttons for Log Meal and Rate Label (Camera + Gallery)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Log Meal Camera
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0004D)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = cameraPermissionGranted && !isUploading
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Log Meal (Camera)")
                Spacer(Modifier.width(8.dp))
                Text("Take Picture", fontWeight = FontWeight.Bold)
            }
            // Log Meal Gallery
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0004D)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Pick from Gallery")
                Spacer(Modifier.width(8.dp))
                Text("Upload From Gallery", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Add an image of nutrition label for analysis",
            color = Color(0xFF222222),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rate Label Camera
            Button(
                onClick = {
                    val file = createImageFile(context)
                    photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    labelCameraLauncher.launch(photoUri)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0004D)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = cameraPermissionGranted && !isUploading
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Rate Label (Camera)")
                Spacer(Modifier.width(8.dp))
                Text("Take Picture", fontWeight = FontWeight.Bold)
            }
            // Rate Label Gallery
            Button(
                onClick = { labelGalleryLauncher.launch("image/*") },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0004D)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Label from Gallery")
                Spacer(Modifier.width(8.dp))
                Text("Upload from Gallery", fontWeight = FontWeight.Bold)
            }
        }

        if (uploadError != null) {
            Spacer(Modifier.height(12.dp))
            Text("Error: $uploadError", color = Color.Red)
        }
        if (nutritionSummary.suggestedAlternatives.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Suggestions to improve your meal:",
                color = Color(0xFFE0004D),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Column {
                nutritionSummary.suggestedAlternatives.forEach { suggestion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", color = Color(0xFFE0004D), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(suggestion, color = Color(0xFF222222), fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

    }

    // Show label result as a dialog/card if present
    if (labelDialogOpen && labelResult != null) {
        val safeResult = labelResult!!
        AlertDialog(
            onDismissRequest = { labelDialogOpen = false },
            confirmButton = {
                TextButton(onClick = { labelDialogOpen = false }) { Text("Close") }
            },
            title = { Text("Nutrition Rating") },
            text = {
                Column {
                    Text("Rating: ${safeResult.rating}/5")
                    Text("Comment: ${safeResult.comment}")
                    safeResult.nutrition_values.let { nv ->
                        Text("Protein: ${nv.protein}")
                        Text("Sugar: ${nv.sugar}")
                        Text("Sodium: ${nv.sodium}")
                        Text("Total Calories: ${nv.total_calories}")
                    }
                    if (safeResult.warnings.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Warnings:", fontWeight = FontWeight.Bold)
                        safeResult.warnings.forEach { warning ->
                            Text(warning, color = Color.Red)
                        }
                    }
                }
            }
        )
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

// ... uploadAndAnalyzeMeal() and analyzeLabelApi() as you have them ...


/**
 * 1. Reads the image, converts to base64
 * 2. Calls /analyze-food/ POST
 * 3. Parses JSON and returns NutritionSummary
 */
suspend fun uploadAndAnalyzeMeal(
    context: Context,
    imageUri: Uri,
    apiUrl: String
): NutritionSummary? = withContext(Dispatchers.IO) {
    Log.d("NUTRI_AI_DEBUG", "Entered uploadAndAnalyzeMeal")

    try {
        // Step 1: Open image stream
        val inputStream = context.contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            Log.d("NUTRI_AI_DEBUG", "inputStream is null for uri: $imageUri")
            return@withContext null
        }
        val mimeType = context.contentResolver.getType(imageUri)
        Log.d("NUTRI_AI_DEBUG", "Selected image mimeType: $mimeType")


        // Step 2: Read bytes
        val imageBytes = inputStream.readBytes()
        Log.d("NUTRI_AI_DEBUG", "Read imageBytes, size: ${imageBytes.size}")

        // Step 3: Encode to base64
        val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        Log.d("NUTRI_AI_DEBUG", "Base64 string: ${imageBase64}")
        Log.d("NUTRI_AI_DEBUG", "Base64 string created, length: ${imageBase64.length}")

        // Step 4: Build request body (form-encoded)
        val formBody = "image_base64=${Uri.encode(imageBase64)}"
        val requestBody = formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())

        // Step 5: Create and send HTTP request
        val client = OkHttpClient.Builder()
            .connectTimeout(180, TimeUnit.SECONDS)  // Time to establish connection
            .writeTimeout(180, TimeUnit.SECONDS)    // Time to send request
            .readTimeout(180, TimeUnit.SECONDS)     // Time to wait for response
            .build()
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()
        Log.d("NUTRI_AI_DEBUG", "Sending POST request to $apiUrl")

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.d("NUTRI_AI_DEBUG", "API call failed: ${response.code}")
            return@withContext null
        }

        // Step 6: Get raw response
        val rawResponse = response.body?.string()
        Log.d("NUTRI_AI_DEBUG", "API Raw Response: $rawResponse")
        if (rawResponse == null) {
            Log.d("NUTRI_AI_DEBUG", "rawResponse is null")
            return@withContext null
        }

        // Step 7: Parse response
        val outerJson = JSONObject(rawResponse)
        val innerJsonString = outerJson.optString("response", null)

        if (innerJsonString == null) {
            Log.d("NUTRI_AI_DEBUG", "No 'response' key in API response")
            return@withContext null
        }
        val innerJson = JSONObject(innerJsonString)
        val alternatives = innerJson.optJSONArray("suggested_alternatives")
        val suggestedAlternatives = if (alternatives != null) {
            List(alternatives.length()) { i -> alternatives.getString(i) }
        } else {
            emptyList()
        }
        Log.d("NUTRI_AI_DEBUG", "Parsed innerJson: $innerJson")



        // Step 8: Parse nutrition values
        val nutritionEstimates = innerJson.optJSONObject("nutrition_estimates")
        if (nutritionEstimates == null) {
            Log.d("NUTRI_AI_DEBUG", "nutrition_estimates not found in response")
            return@withContext null
        }

        val calories = nutritionEstimates.optString("estimated_calories")
        val protein = nutritionEstimates.optString("protein")
        val carbs = nutritionEstimates.optString("carbohydrates")
        val fat = nutritionEstimates.optString("fat")
        Log.d("NUTRI_AI_DEBUG", "Extracted: calories=$calories, protein=$protein, carbs=$carbs, fat=$fat")

        // Step 9: Build and return summary
        return@withContext NutritionSummary(
            calories = calories.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0,
            caloriesTarget = 800, // or user value
            protein = protein.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0,
            proteinTarget = 70,
            carbs = carbs.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0,
            carbsTarget = 200,
            fats = fat.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0,
            fatsTarget = 50,
            suggestedAlternatives = suggestedAlternatives
        )
    } catch (e: Exception) {
        Log.d("NUTRI_AI_DEBUG", "Exception in uploadAndAnalyzeMeal: ${e.localizedMessage}")
        e.printStackTrace()
        return@withContext null
    }

}

/**
 * 1. Reads the image, converts to base64
 * 2. Calls /rate-label/ POST
 * 3. Parses JSON and returns NutritionRatingResponse
 */
suspend fun analyzeLabelApi(
    context: Context,
    imageUri: Uri,
    userQuery: String = "Analyze this baby food label"
): NutritionRatingResponse? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val imageBase64 = if (imageBytes != null) {
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } else return@withContext null

        // Try without encode if backend expects plain base64:
        val formBody = "image_base64=${Uri.encode(imageBase64)}"

        val requestBody = formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())


        val client = OkHttpClient()
        val request = Request.Builder()
            .url(ApiConfig.BASE_URL + "rate-label/")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        val rawResponse = response.body?.string() ?: return@withContext null
        Log.d("NUTRI_AI_DEBUG", "API Raw Response: $rawResponse")
        val outerJson = JSONObject(rawResponse)
        val innerJsonString = outerJson.optString("response", null)
        val cleanedInnerJson = stripCodeBlock(innerJsonString)
        Log.d("NUTRI_AI_DEBUG", "Innen Json: $cleanedInnerJson")
        if (innerJsonString == null) {
            // handle error
            return@withContext null
        }
        val resp = JSONObject(cleanedInnerJson)

        Log.d("NUTRI_AI_DEBUG", "Response: $resp")
        return@withContext NutritionRatingResponse(
            rating = resp.optString("rating"),
            comment = resp.optString("comment"),
            nutrition_values = resp.optJSONObject("nutrition_values")?.let {
                NutritionValues(
                    protein = it.optString("protein"),
                    sugar = it.optString("sugar"),
                    sodium = it.optString("sodium"),
                    total_calories = it.optString("total_calories")
                )
            } ?: NutritionValues("", "", "", ""),
            warnings = resp.optJSONArray("warnings")?.let { arr ->
                (0 until arr.length()).map { i -> arr.getString(i) }
            } ?: emptyList()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }

}
