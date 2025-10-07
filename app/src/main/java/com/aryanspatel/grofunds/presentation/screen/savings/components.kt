package com.aryanspatel.grofunds.presentation.screen.savings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GoalData(
    val target: Int,
    val saved: Int,
    val left: Int,
    val nextContribution: NextContribution
)

data class NextContribution(
    val date: String,
    val amount: Int
)

fun formatWithCommas(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(Int)
}

@Composable
fun ArcProgressHeroCard(goalData: GoalData) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
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
                        color = Color(0xFFE2E8F0),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress Arc with Gradient
                    drawArc(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF16A34A),
                                Color(0xFF22C55E)
                            )
                        ),
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
                        color = Color(0xFF16A34A),
                        radius = 16.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )

                    // Inner white circle
                    drawCircle(
                        color = Color.White,
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
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "$${goalData.saved.formatWithCommas()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${animatedProgress.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
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
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${goalData.target.formatWithCommas()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

//                Spacer(modifier = Modifier.width(10.dp))

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Color(0xFFCBD5E1))
                )


//                Spacer(modifier = Modifier.width(10.dp))

                // Saved
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saved",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${goalData.saved.formatWithCommas()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

//                Spacer(modifier = Modifier.width(10.dp))

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
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${goalData.left.formatWithCommas()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // Next Contribution Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF3B82F6), shape = RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next contribution: ",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = goalData.nextContribution.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }
                    Text(
                        text = "$${goalData.nextContribution.amount.formatWithCommas()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewArcProgressHeroCard() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//                .background(Color(0xFFF8FAFC))
                .background(Color.Transparent)
                .padding(16.dp)
        ) {
            ArcProgressHeroCard(
                goalData = GoalData(
                    target = 50000,
                    saved = 32500,
                    left = 17500,
                    nextContribution = NextContribution(
                        date = "Dec 15",
                        amount = 500
                    )
                )
            )
        }
    }
}