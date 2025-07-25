package com.example.nutriai.screens
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun WeeklyReportsTab(
    weeklyData: List<DailyIntake> = demoWeekData,
    macroData: MacroDistribution = demoMacro
) {
    val week = weeklyData.map { it.day to it.intake }
    val macroList = listOf(macroData.protein.toFloat(), macroData.carbs.toFloat(), macroData.fats.toFloat())
    val macroColors = listOf(Color(0xFFFCCF31), Color(0xFFFF8177), Color(0xFFB2FFDA))

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weekly Reports", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        // Weekly Calorie Intake Bar Chart
        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Calorie Intake", fontWeight = FontWeight.Bold)
                WeeklyBarChart(
                    data = week,
                    barColor = Color(0xFFFFA500),
                    maxValue = 900
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Macronutrient Pie Chart
        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Macronutrient Distribution", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                SimplePieChart(
                    values = macroList,
                    colors = macroColors
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    SimplePieLegend("Protein", macroColors[0])
                    SimplePieLegend("Carbs", macroColors[1])
                    SimplePieLegend("Fats", macroColors[2])
                }
            }
        }
    }
}


@Composable
fun WeeklyBarChart(
    data: List<Pair<String, Int>>,
    barColor: Color,
    maxValue: Int = data.maxOf { it.second }
) {
    Row(
        Modifier
            .height(120.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .height((100 * value / maxValue).dp)
                        .width(24.dp)
                        .background(barColor, shape = RoundedCornerShape(6.dp))
                )
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


@Composable
fun SimplePieChart(
    values: List<Float>,
    colors: List<Color>,
    size: Dp = 120.dp
) {
    val sum = values.sum()
    val angles = values.map { 360f * (it / sum) }
    Canvas(Modifier.size(size)) {
        var startAngle = -90f
        for (i in values.indices) {
            drawArc(
                color = colors[i],
                startAngle = startAngle,
                sweepAngle = angles[i],
                useCenter = true,
                size = Size(size.toPx(), size.toPx())
            )
            startAngle += angles[i]
        }
    }
}

@Composable
fun SimplePieLegend(label: String, color: Color) {
    Row(
        Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// --- Demo Data for preview ---
val demoWeekData = listOf(
    DailyIntake("Mon", 700, 800),
    DailyIntake("Tue", 750, 800),
    DailyIntake("Wed", 800, 800),
    DailyIntake("Thu", 720, 800),
    DailyIntake("Fri", 780, 800),
    DailyIntake("Sat", 820, 800),
    DailyIntake("Sun", 800, 800)
)
val demoMacro = MacroDistribution(protein = 30, carbs = 50, fats = 20)

