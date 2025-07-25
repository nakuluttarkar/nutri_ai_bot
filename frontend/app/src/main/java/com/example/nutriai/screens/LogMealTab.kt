package com.example.nutriai.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD876)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = cameraPermissionGranted && !isUploading
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Log Meal (Camera)")
                Spacer(Modifier.width(8.dp))
                Text("Log Meal", fontWeight = FontWeight.Bold)
            }
            // Log Meal Gallery
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0C3FC)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Pick from Gallery")
                Spacer(Modifier.width(8.dp))
                Text("From Gallery", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFa3ffd6)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = cameraPermissionGranted && !isUploading
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Rate Label (Camera)")
                Spacer(Modifier.width(8.dp))
                Text("Rate Label", fontWeight = FontWeight.Bold)
            }
            // Rate Label Gallery
            Button(
                onClick = { labelGalleryLauncher.launch("image/*") },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AD3FF)),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Label from Gallery")
                Spacer(Modifier.width(8.dp))
                Text("Label Gallery", fontWeight = FontWeight.Bold)
            }
        }

        if (uploadError != null) {
            Spacer(Modifier.height(12.dp))
            Text("Error: $uploadError", color = Color.Red)
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
                    Text("Rating: ${safeResult.rating}")
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
        val rawResponse = response.body?.string() ?: return@withContext null
        println("API Raw Response: $rawResponse") // <-- Add this
        val outerJson = JSONObject(rawResponse)

        // Parse the "response" field (inner JSON string)
        val innerJson = JSONObject(outerJson.getString("response"))

        // Parse values with sensible defaults if missing
        return@withContext NutritionSummary(
            calories = innerJson.optJSONObject("nutrition_estimates")?.optString("calories")?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0,
            caloriesTarget = 800, // Set user default/target as appropriate
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

        val jsonBody = JSONObject().apply {
            put("image_base64", imageBase64)
            put("user_query", userQuery)
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(ApiConfig.BASE_URL + "rate-label/")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        val rawResponse = response.body?.string() ?: return@withContext null
        println("API Raw Response: $rawResponse") // <-- Add this
        val resp = JSONObject(rawResponse)


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
