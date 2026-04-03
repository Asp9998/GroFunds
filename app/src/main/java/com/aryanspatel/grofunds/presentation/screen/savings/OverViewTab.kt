package com.aryanspatel.grofunds.presentation.screen.savings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryanspatel.grofunds.domain.usecase.formatAmountCurrSymbol
import com.aryanspatel.grofunds.presentation.common.model.GoalData
import com.aryanspatel.grofunds.presentation.common.model.InsightMetric
import com.aryanspatel.grofunds.presentation.common.model.NextContribution
import com.aryanspatel.grofunds.presentation.common.model.SavingState
import com.aryanspatel.grofunds.presentation.common.model.SavingsHeaderUi
import com.aryanspatel.grofunds.presentation.common.model.StatusVariant
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

//@Preview
@Composable
fun OverviewTab(
    saving: SavingState,
    header: SavingsHeaderUi
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background),
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item{
            Spacer(Modifier.height(1.dp))
        }

        item {
            InsightTrioCardScreen(
                savingState = saving,
                header = header
            )
        }

        // Hero Card
        item {
            ArcProgressHeroCard(
                goalData = GoalData(
                    target = saving?.targetAmount?.toInt() ?: 0 ,
                    saved = saving?.savedAmount?.toInt() ?: 0 ,
                    left = saving?.leftAmount?.toInt() ?: 0
                )
            )
        }

        // Progress Chart
        item {
            ContributionsScreen()
        }

        // Notes
        item {

            var isDark by remember { mutableStateOf(true) }

            val goalPerformance = remember {
                GoalPerformance(
                    targetAmount = 2500,
                    currentAmount = 1875,
                    weeklyTarget = 75,
                    weeklyProgress = 62,
                    daysRemaining = 12,
                    isOnTrack = false,
                    percentageComplete = 75
                )
            }

            val dailyNeeded = remember(goalPerformance) {
                val remaining = goalPerformance.targetAmount - goalPerformance.currentAmount
                val perDay = (remaining.toFloat() / goalPerformance.daysRemaining).toDouble()
                ceil(perDay).toInt()
            }

            PerformanceCard(
                isDark = isDark,
                goalPerformance = goalPerformance,
                dailyNeeded = dailyNeeded
            )

        }

        if(saving?.note != null){
            item {
                NotesCard(saving.note)
            }
        }

        item {
            Spacer(modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}


//@Preview
@Composable
fun InsightTrioCardScreen(
    savingState: SavingState,
    header: SavingsHeaderUi
) {

    val metrics = listOf(
        InsightMetric(
            label = "Original Due Date",
            value = header.originalDueDate ?: "-",
            sublabel = "Your planned finish line",
            icon = Icons.Filled.CalendarMonth,
            statusVariant = StatusVariant.AHEAD,
            context = "Projection moved −9 days from original, The user’s target date (immutable, from setup)."
        ),
        InsightMetric(
            label = "Projected Completion",
            value = header.projectedCompletionDate ?: "-",
            sublabel = "Based on your recent pace",
            icon = Icons.Filled.Flag,
            statusVariant = StatusVariant.IMPROVING,
            context = "Rolling avg: $312/mo · Last 30 days: $380, When you’re likely to finish if you keep contributing at your recent pace."
        ),
        InsightMetric(
            label = "Needed per Month",
            value = formatAmountCurrSymbol(header.neededPerMonth ?: 0.0, savingState.currencySymbol, fractionDigit = 0),
            unit = "/ mo",
            sublabel = "To hit your target date",
            icon = Icons.Filled.Lightbulb,
            statusVariant = StatusVariant.TIGHT,
            context = "Months left: 6 · Remaining: $2,520, How much you must contribute per month from now onward to still hit the original due date."
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        metrics.forEach { m ->

            // Main Card
            InsightTrioCardContent(
                metrics = m,
            )

        }
    }
}

@Composable
private fun InsightTrioCardContent(
    metrics:InsightMetric,
) {
    val current = metrics

    val cardBg =
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onTertiaryContainer,
                MaterialTheme.colorScheme.tertiaryContainer
            )
        )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Content section
            ContentSection(
                metric = current,
            )
        }
    }
}
@Composable
private fun ContentSection(
    metric: InsightMetric,
)
{
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Header with icon and chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onTertiaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = metric.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = metric.sublabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        // Metric value with animation
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (metric.unit != null) {
                Text(
                    text = metric.unit,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        // Context strip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.6f)

        ) {
            Text(
                text = metric.context,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ArcProgressHeroCard(
    goalData: GoalData
) {


    val cardBg = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer))

    val borderColor = MaterialTheme.colorScheme.onTertiary

    val progress = (goalData.saved.toFloat() / goalData.target.toFloat()) * 100f

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
    ) {

        val arcBackgroundColor = Color.LightGray
        val arcColor = MaterialTheme.colorScheme.surfaceBright



        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 8.dp)) {
            // Arc Progress Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val radius = canvasWidth * 0.4375f
                    val centerX = canvasWidth / 2f
                    val centerY = canvasHeight * 0.875f
                    val strokeWidth = 12.dp.toPx()

                    // Background Arc
                    drawArc(
                        color = arcBackgroundColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress Arc with Gradient
                    drawArc(
                        color = arcColor,
                        startAngle = 180f,
                        sweepAngle = 180f * (animatedProgress / 100f),
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress Indicator Circle
                    val angle = PI - (animatedProgress / 100f) * PI
                    val indicatorX = centerX + radius * cos(angle).toFloat()
                    val indicatorY = centerY - radius * sin(angle).toFloat()

                    // Outer circle
                    drawCircle(
                        color = arcColor,
                        radius = 16.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )

                    // Inner white circle
                    drawCircle(
                        color = arcBackgroundColor,
                        radius = 8.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )
                }

                // Center Text - Contributed Amount
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saved",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "$${goalData.saved.formatWithCommas()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surfaceBright
                    )
                }
            }

            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Target
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Target",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${goalData.target.formatWithCommas()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Color(0xFFCBD5E1))
                )

//                Spacer(modifier = Modifier.width(10.dp))

                // Remaining
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Remaining",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${goalData.left.formatWithCommas()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun NotesCard(
    noteDescription: String
) {
    val cardBg = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer))

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(16.dp))
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = noteDescription,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"Paris trip—don't touch!\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}