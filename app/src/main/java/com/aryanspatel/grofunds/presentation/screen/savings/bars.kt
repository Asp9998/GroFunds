package com.aryanspatel.grofunds.presentation.screen.savings

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.aryanspatel.grofunds.domain.model.EntryKind

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

// Color scheme
object ChartColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)
//    val border = MaterialTheme.colorScheme.onBackground
//    val textPrimary = MaterialTheme.colorScheme.onPrimary
//    val textSecondary = MaterialTheme.colorScheme.onSecondary
//    val positive = MaterialTheme.colorScheme.primaryContainer
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
    var showSources by remember { mutableStateOf(false) }
    var showPaceLine by remember { mutableStateOf(true) }

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            // Header
            HeaderSection()

            Spacer(modifier = Modifier.height(8.dp))

            // Granularity selector
//            GranularitySelector(
//                selectedGranularity = selectedGranularity,
//                onGranularityChange = { selectedGranularity = it }
//            )
            AnimatedToggleButton()

            Spacer(modifier = Modifier.height(8.dp))

            // Main chart card
            Card(
                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surface
                    containerColor = Color.Transparent
                ),
//                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {

                    // Chart
                    if (!hasData) {
                        EmptyState()
                    } else {
                        BarChartCanvas(
                            data = chartData,
                            showSources = showSources,
                            showPaceLine = showPaceLine,
                        )
                    }

                    HorizontalDivider(
                        Modifier.padding(top = 10.dp),
                        DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.1f)
                    )

                    // Stats bar
                    StatsBar(
                        avgWeek = avgWeek,
                        bestWeek = bestWeek,
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
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
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No contributions yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start saving to see your progress",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondary.copy(0.7f)
        )
    }
}
@Composable
fun BarChartCanvas(
    data: List<ContributionData>,
    showSources: Boolean,
    showPaceLine: Boolean,
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

    Column(modifier = Modifier) {

        val border = MaterialTheme.colorScheme.onPrimaryFixed
        val positive = MaterialTheme.colorScheme.onPrimaryFixed

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
                    color = border,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // Zero baseline
            drawLine(
                color = border,
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
                        color = if (item.total >= 0f) positive else ChartColors.negative,
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
    avgWeek: Int,
    bestWeek: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Average/Week", "$$avgWeek")
        StatItem("Best Week", "$$bestWeek")
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
enum class Time{
    WEEKLY,
    BIEEKLY,
    MONTHLY,
    YEARLY
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedToggleButton(
    modifier: Modifier = Modifier,
    selectedOption: Time = Time.WEEKLY,
    onOptionSelected: (Time) -> Unit = {},
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.onTertiaryFixed,
    selectedTextColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSecondary,
    borderColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    val options = listOf(
         Time.WEEKLY to "Weekly",
        Time.BIEEKLY to "Bi-weekly",
        Time.MONTHLY to "Monthly",
        Time.YEARLY to "Yearly"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ).border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val optionWidth = (totalWidth - 8.dp) / 4 // Account for spacing between options

        // Calculate the offset for the sliding background
        val selectedIndex = when (selectedOption) {
            Time.WEEKLY -> 0
            Time.BIEEKLY -> 1
            Time.MONTHLY -> 2
            Time.YEARLY -> 3
        }

        val slideOffset by animateDpAsState(
            targetValue = (optionWidth + 4.dp) * selectedIndex,
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            ),
            label = "slide_animation"
        )

        // Sliding background
        Box(
            modifier = Modifier
                .offset(x = slideOffset)
                .width(optionWidth)
                .height(48.dp)
                .background(
                    color = selectedBackgroundColor,
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // Row of options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEach { (option, label) ->
                val isSelected = selectedOption == option

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No hover/ripple effect
                        ) {
                            onOptionSelected(option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) selectedTextColor else unselectedTextColor
                    )
                }
            }
        }
    }
}
