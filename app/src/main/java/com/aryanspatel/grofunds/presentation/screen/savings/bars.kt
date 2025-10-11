package com.aryanspatel.grofunds.presentation.screen.savings

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.toColorInt

// Data models
data class ContributionData(
    val week: String,
    val paycheque: Float,
    val roundup: Float,
    val manual: Float,
    val withdrawal: Float,
    val total: Float,
    val trend: Float,
    val pace: Float = 250f,
)

enum class Granularity(val label: String) {
    WEEKLY("Weekly"),
    BI_WEEKLY("Bi-weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly")
}

// Color scheme
object ChartColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)
    val border = Color(0xFF475569)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF94A3B8)
    val positive = Color(0xFF3B82F6)
    val negative = Color(0xFFEF4444)
    val paycheque = Color(0xFF10B981)
    val roundup = Color(0xFF3B82F6)
    val manual = Color(0xFFA855F7)
    val trend = Color(0xFF8B5CF6)
    val pace = Color(0xFFFBBF24)
}

@Preview
@Composable
fun ContributionsScreen() {
    var selectedGranularity by remember { mutableStateOf(Granularity.WEEKLY) }
    var showSources by remember { mutableStateOf(false) }
    var showTrendLine by remember { mutableStateOf(true) }
    var showPaceLine by remember { mutableStateOf(true) }
    var selectedBar by remember { mutableStateOf<ContributionData?>(null) }
    var showDetails by remember { mutableStateOf(false) }

    // Sample data
    val chartData = remember {
        listOf(
            ContributionData("W1", 200f, 15f, 50f, 0f, 265f, 265f),
            ContributionData("W2", 200f, 22f, 0f, -50f, 172f, 218f),
            ContributionData("W3", 200f, 18f, 100f, 0f, 318f, 251f),
            ContributionData("W4", 0f, 12f, 25f, 0f, 37f, 198f),
            ContributionData("W5", 200f, 25f, 0f, 0f, 225f, 193f),
            ContributionData("W6", 200f, 19f, 75f, -100f, 194f, 152f),
            ContributionData("W7", 200f, 21f, 50f, 0f, 271f, 197f),
            ContributionData("W8", 200f, 16f, 0f, 0f, 216f, 227f),
            ContributionData("W9", 0f, 0f, 0f, 0f, 0f, 162f),
            ContributionData("W10", 200f, 28f, 100f, 0f, 328f, 181f),
            ContributionData("W11", 200f, 24f, 50f, 0f, 274f, 200f),
            ContributionData("W12", 200f, 20f, 0f, -75f, 145f, 216f),
        )
    }

    val hasData = chartData.any { it.total != 0f }
    val totalSaved = chartData.sumOf { it.total.toDouble() }.toInt()
    val avgWeek = if (chartData.isNotEmpty()) totalSaved / chartData.size else 0
    val bestWeek = chartData.maxOfOrNull { it.total }?.toInt() ?: 0
    val streak = chartData.count { it.total > 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            HeaderSection()

            Spacer(modifier = Modifier.height(8.dp))

            // Granularity selector
            GranularitySelector(
                selectedGranularity = selectedGranularity,
                onGranularityChange = { selectedGranularity = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main chart card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column {

                    // Chart
                    if (!hasData) {
                        EmptyState()
                    } else {
                        BarChartCanvas(
                            data = chartData,
                            showSources = showSources,
                            showTrendLine = showTrendLine,
                            showPaceLine = showPaceLine,
                            onBarClick = { data ->
                                selectedBar = data
                                showDetails = true
                            }
                        )
                    }

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.1f)
                    )

                    // Stats bar
                    StatsBar(
                        totalSaved = totalSaved,
                        avgWeek = avgWeek,
                        bestWeek = bestWeek,
                        streak = streak
                    )
                }
            }

            // Details sheet
            AnimatedVisibility(
                visible = showDetails && selectedBar != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                selectedBar?.let { data ->
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailsSheet(
                        data = data,
                        onDismiss = { showDetails = false }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Contributions Over Time",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun GranularitySelector(
    selectedGranularity: Granularity,
    onGranularityChange: (Granularity) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChartColors.border)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Granularity.values().forEach { granularity ->
                Surface(
                    modifier = Modifier
                        .clickable { onGranularityChange(granularity) }
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedGranularity == granularity) {
                        androidx.tv.material3.MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color.Transparent
                    }
                ) {
                    Text(
                        text = granularity.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (selectedGranularity == granularity) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondary
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = ChartColors.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ChartColors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No contributions yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChartColors.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start saving to see your progress",
            fontSize = 14.sp,
            color = ChartColors.textSecondary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "💡 Try $25/week to finish by Dec 10",
            fontSize = 14.sp,
            color = Color(0xFF3B82F6)
        )
    }
}

//@Composable
//fun BarChartCanvas(
//    data: List<ContributionData>,
//    showSources: Boolean,
//    showTrendLine: Boolean,
//    showPaceLine: Boolean,
//    onBarClick: (ContributionData) -> Unit,
//) {
//    val maxValue = data.maxOf { max(it.total, it.pace) }
//    val minValue = data.minOf { min(it.total, 0f) }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Canvas(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(400.dp)
//        ) {
//            val chartWidth = size.width - 100f
//            val chartHeight = size.height - 90f
//            val barWidth = chartWidth / (data.size * 1.5f)
//            val spacing = barWidth * 0.5f
//
//            val yRange = maxValue - minValue
//            val yScale = chartHeight / yRange
//            val zeroY = chartHeight - (0 - minValue) * yScale + 40f
//
//            // Draw grid lines
//            for (i in 0..4) {
//                val y = 40f + (chartHeight / 4) * i
//                drawLine(
//                    color = ChartColors.border,
//                    start = Offset(80f, y),
//                    end = Offset(80f + chartWidth, y),
//                    strokeWidth = 1f,
//                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
//                )
//            }
//
//            // Draw zero baseline
//            drawLine(
//                color = ChartColors.border,
//                start = Offset(80f, zeroY),
//                end = Offset(80f + chartWidth, zeroY),
//                strokeWidth = 3f
//            )
//
//            // Draw ideal pace line
//            if (showPaceLine) {
//                val paceY = chartHeight - (data[0].pace - minValue) * yScale + 40f
//                drawLine(
//                    color = ChartColors.pace,
//                    start = Offset(80f, paceY),
//                    end = Offset(80f + chartWidth, paceY),
//                    strokeWidth = 3f,
//                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
//                )
//            }
//
//            // Draw bars
//            data.forEachIndexed { index, item ->
//                val x = 80f + index * (barWidth + spacing)
//
//                if (showSources) {
//                    // Stacked bars
//                    var currentY = zeroY
//
//                    // Draw each source
//                    listOf(
//                        item.paycheque to ChartColors.paycheque,
//                        item.roundup to ChartColors.roundup,
//                        item.manual to ChartColors.manual
//                    ).forEach { (value, color) ->
//                        if (value > 0) {
//                            val barHeight = value * yScale
//                            drawRoundRect(
//                                color = color,
//                                topLeft = Offset(x, currentY - barHeight),
//                                size = Size(barWidth, barHeight),
//                                cornerRadius = CornerRadius(4f, 4f)
//                            )
//                            currentY -= barHeight
//                        }
//                    }
//
//                    // Draw withdrawal
//                    if (item.withdrawal < 0) {
//                        val barHeight = -item.withdrawal * yScale
//                        drawRoundRect(
//                            color = ChartColors.negative,
//                            topLeft = Offset(x, zeroY),
//                            size = Size(barWidth, barHeight),
//                            cornerRadius = CornerRadius(4f, 4f)
//                        )
//                    }
//                } else {
//                    // Single bar
//                    val barHeight = kotlin.math.abs(item.total) * yScale
//                    val barY = if (item.total >= 0) {
//                        zeroY - barHeight
//                    } else {
//                        zeroY
//                    }
//
//                    drawRoundRect(
//                        color = if (item.total >= 0) ChartColors.positive else ChartColors.negative,
//                        topLeft = Offset(x, barY),
//                        size = Size(barWidth, barHeight),
//                        cornerRadius = CornerRadius(8f, 8f)
//                    )
//                }
//            }
//
//
//            // Draw Y-axis labels
//            for (i in 0..4) {
//                val value = ((yRange / 4) * i + minValue).toInt()
//                val y = chartHeight - (value - minValue) * yScale + 40f
//                drawContext.canvas.nativeCanvas.drawText(
//                    "$${value}",
//                    40f,
//                    y + 5f,
//                    android.graphics.Paint().apply {
//                        color = "#94A3B8".toColorInt()
//                        textSize = 28f
//                        textAlign = android.graphics.Paint.Align.RIGHT
//                    }
//                )
//            }
//
//            // Draw X-axis labels
//            data.forEachIndexed { index, item ->
//                val x = 80f + index * (barWidth + spacing) + barWidth / 2
//                drawContext.canvas.nativeCanvas.drawText(
//                    item.week,
//                    x,
//                    size.height - 10f,
//                    android.graphics.Paint().apply {
//                        color = "#94A3B8".toColorInt()
//                        textSize = 28f
//                        textAlign = android.graphics.Paint.Align.CENTER
//                    }
//                )
//            }
//        }
//    }
//}


@Composable
fun BarChartCanvas(
    data: List<ContributionData>,
    showSources: Boolean,
    showTrendLine: Boolean,
    showPaceLine: Boolean,
    onBarClick: (ContributionData) -> Unit,
) {
    // ── CHANGES: scale by contributions only (ignore pace), add headroom ─────────────
    val maxPositive = data.maxOfOrNull {
        if (showSources) (max(0f, it.paycheque) + max(0f, it.roundup) + max(0f, it.manual))
        else max(0f, it.total)
    } ?: 0f
    val maxNegative = data.minOfOrNull {
        if (showSources) min(0f, it.withdrawal) else min(0f, it.total)
    } ?: 0f

    // Add ~10% headroom so bars don’t touch the top/bottom
    val yTop = (if (maxPositive > 0f) maxPositive else 1f) * 1.10f
    val yBottom = (if (maxNegative < 0f) maxNegative else 0f) * 1.10f

    // Mapping function from value → canvas Y
    fun mapY(value: Float, chartHeight: Float, topPad: Float): Float {
        val yRange = (yTop - yBottom).coerceAtLeast(1f)
        val yScale = chartHeight / yRange
        return topPad + (yTop - value) * yScale
    }
    // ────────────────────────────────────────────────────────────────────────────────

    Column(modifier = Modifier.padding(16.dp)) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val leftPad = 80f
            val rightPad = 20f
            val topPad = 40f
            val bottomPad = 50f

            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad

            val barWidth = chartWidth / (data.size * 1.5f)
            val spacing = barWidth * 0.5f

            val zeroY = mapY(0f, chartHeight, topPad)

            // Grid lines
            repeat(5) { i ->
                val v = yTop - (i * (yTop - yBottom) / 4f)
                val y = mapY(v, chartHeight, topPad)
                drawLine(
                    color = ChartColors.border,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // Zero baseline
            drawLine(
                color = ChartColors.border,
                start = Offset(leftPad, zeroY),
                end = Offset(leftPad + chartWidth, zeroY),
                strokeWidth = 3f
            )

            // Ideal pace line (drawn within bounds; still ignores for scaling)
            if (showPaceLine && data.isNotEmpty()) {
                val pace = data[0].pace
                val paceY = mapY(pace, chartHeight, topPad)
                drawLine(
                    color = ChartColors.pace,
                    start = Offset(leftPad, paceY),
                    end = Offset(leftPad + chartWidth, paceY),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
                )
            }

            // Bars
            data.forEachIndexed { index, item ->
                val x = leftPad + index * (barWidth + spacing)

                if (showSources) {
                    // Stacked positives
                    var curTop = zeroY
                    val positiveParts = listOf(
                        item.paycheque to ChartColors.paycheque,
                        item.roundup to ChartColors.roundup,
                        item.manual to ChartColors.manual
                    ).filter { it.first > 0f }

                    positiveParts.forEach { (value, color) ->
                        val top = mapY(value, chartHeight, topPad)
                        val barTop = min(top, curTop)
                        val barBottom = max(top, curTop)
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, barTop),
                            size = Size(barWidth, barBottom - barTop),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        curTop = barTop
                    }

                    // Negative (withdrawal)
                    if (item.withdrawal < 0f) {
                        val negY = mapY(item.withdrawal, chartHeight, topPad)
                        drawRoundRect(
                            color = ChartColors.negative,
                            topLeft = Offset(x, zeroY),
                            size = Size(barWidth, negY - zeroY),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                } else {
                    // Single net bar
                    val top = mapY(max(0f, item.total), chartHeight, topPad)
                    val bottom = mapY(min(0f, item.total), chartHeight, topPad)
                    val barTop = min(top, bottom)
                    val barBottom = max(top, bottom)

                    drawRoundRect(
                        color = if (item.total >= 0f) ChartColors.positive else ChartColors.negative,
                        topLeft = Offset(x, barTop),
                        size = Size(barWidth, barBottom - barTop),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }

            // Y labels
            repeat(5) { i ->
                val v = yTop - (i * (yTop - yBottom) / 4f)
                val y = mapY(v, chartHeight, topPad)
                drawContext.canvas.nativeCanvas.drawText(
                    "$${v.toInt()}",
                    leftPad - 10f,
                    y + 5f,
                    android.graphics.Paint().apply {
                        color = "#94A3B8".toColorInt()
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // X labels
            data.forEachIndexed { index, item ->
                val x = leftPad + index * (barWidth + spacing) + barWidth / 2
                drawContext.canvas.nativeCanvas.drawText(
                    item.week,
                    x,
                    size.height - 10f,
                    android.graphics.Paint().apply {
                        color = "#94A3B8".toColorInt()
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun StatsBar(
    totalSaved: Int,
    avgWeek: Int,
    bestWeek: Int,
    streak: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Average/Week", "$$avgWeek", Color(0xFF3B82F6))
        StatItem("Best Week", "$$bestWeek", Color(0xFFA855F7))
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ChartColors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun DetailsSheet(
    data: ContributionData,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChartColors.background)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${data.week} Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net contribution: $${data.total.toInt()}",
                        fontSize = 14.sp,
                        color = ChartColors.textSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ChartColors.textSecondary
                    )
                }
            }

            Divider(color = ChartColors.border)

            // Details grid
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceCard("Paycheque", data.paycheque.toInt(), ChartColors.paycheque, Modifier.weight(1f))
                    SourceCard("Round-up", data.roundup.toInt(), ChartColors.roundup, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceCard("Manual", data.manual.toInt(), ChartColors.manual, Modifier.weight(1f))
                    if (data.withdrawal < 0) {
                        SourceCard("Withdrawal", data.withdrawal.toInt(), ChartColors.negative, Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Note")
                    }
                }
            }
        }
    }
}

@Composable
fun SourceCard(label: String, amount: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ChartColors.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = ChartColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$$amount",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ChartColors.textPrimary
            )
        }
    }
}