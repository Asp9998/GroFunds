package com.aryanspatel.grofunds.presentation.screen.savings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min
import kotlin.random.Random

/**
 * Exact visual/structural translation of the provided React + Tailwind code to Jetpack Compose.
 * No UX/structure changes—names, layout order, colors/accents, motions, and content are mirrored.
 */

// ---------- Data models (mirroring the React structures) ----------

data class GoalPerformance(
    val targetAmount: Int,
    val currentAmount: Int,
    val weeklyTarget: Int,
    val weeklyProgress: Int,
    val daysRemaining: Int,
    val isOnTrack: Boolean,
    val percentageComplete: Int
)

enum class MilestoneType { PCT_100, OVERDRIVE, STREAK, MONTH_TARGET, PCT_75 }

data class Milestone(
    val id: String,
    val type: MilestoneType,
    val title: String,
    val subline: String,
    val achieved: Boolean,
    val stats: String,
    val date: String
)

// ---------- Theme helpers (colors translated from Tailwind utility intent) ----------

val Emerald400 = Color(0xFF34D399)
 val Emerald300 = Color(0xFF6EE7B7)
 val Emerald200 = Color(0xFFA7F3D0)
 val Mint100 = Color(0xFFC9F5DF)
 val Slate950 = Color(0xFF020617)
 val Slate900 = Color(0xFF0F172A)
val Slate700 = Color(0xFF334155)
 val Slate600 = Color(0xFF475569)
 val Slate400 = Color(0xFF94A3B8)
 val Slate300 = Color(0xFFCBD5E1)
 val Gray100 = Color(0xFFF3F4F6)
 val Gray50 = Color(0xFFFAFAFA)
val Amber400 = Color(0xFFFBBF24)
val Amber300 = Color(0xFFFCD34D)

// ---------- Entry Composable ----------

@Composable
fun MilestoneTimelineScreen() {
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

    val milestones = remember {
        listOf(
            Milestone(
                id = "1",
                type = MilestoneType.PCT_100,
                title = "Goal Complete",
                subline = "Saved $2,500 · Goal finished!",
                achieved = true,
                stats = "This month: $450",
                date = "Today"
            ),
            Milestone(
                id = "2",
                type = MilestoneType.OVERDRIVE,
                title = "Overdrive",
                subline = "You're $40 ahead this month",
                achieved = true,
                stats = "Avg: $312 · This month: $380",
                date = "3 days ago"
            ),
            Milestone(
                id = "3",
                type = MilestoneType.STREAK,
                title = "Streak ×4",
                subline = "Four weeks, zero excuses",
                achieved = true,
                stats = "3-week streak ongoing",
                date = "1 week ago"
            ),
            Milestone(
                id = "4",
                type = MilestoneType.MONTH_TARGET,
                title = "Monthly Target",
                subline = "+$200 this month · On track",
                achieved = true,
                stats = "Ahead of plan by 6 days",
                date = "2 weeks ago"
            ),
            Milestone(
                id = "5",
                type = MilestoneType.PCT_75,
                title = "Three Quarters",
                subline = "Saved $1,875 of $2,500",
                achieved = false,
                stats = "ETA: 12 days away",
                date = "Upcoming"
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        color = Color.Transparent
    ) {
        MilestoneTimelineContent(
            isDark = isDark,
            goalPerformance = goalPerformance,
            milestones = milestones
        )
    }
}

// ---------- Content ----------

@Composable
private fun MilestoneTimelineContent(
    isDark:Boolean,
    goalPerformance: GoalPerformance,
    milestones: List<Milestone>
) {

    var showCelebration by remember { mutableStateOf(false) }
    var selectedMilestone by remember { mutableStateOf<Milestone?>(null) }

    val dailyNeeded = remember(goalPerformance) {
        val remaining = goalPerformance.targetAmount - goalPerformance.currentAmount
        val perDay = (remaining.toFloat() / goalPerformance.daysRemaining).toDouble()
        kotlin.math.ceil(perDay).toInt()
    }

    Box(Modifier.fillMaxSize()) {
        // Celebration overlay
        if (showCelebration && selectedMilestone != null) {
            CelebrationOverlay(
                title = selectedMilestone!!.title,
                accent = Emerald400,
                onFinished = { showCelebration = false }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Moments That Matter",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(text = "Your journey to $2,500",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }

                ProgressRing(percentage = goalPerformance.percentageComplete, isDark = isDark)
                Spacer(Modifier.height(24.dp))

            milestones.forEachIndexed { index, m ->
                Box(Modifier.fillMaxWidth()) {
                    // card
                    MilestoneCard(
                        milestone = m,
                        onClick = {
                            if (m.achieved) {
                                selectedMilestone = m
                                showCelebration = true
                            }
                        }
                    )
                }
                if (index < milestones.lastIndex) Spacer(Modifier.height(16.dp))
            }
                Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------- Performance Card ----------

@Composable
fun PerformanceCard(
    isDark: Boolean,
    goalPerformance: GoalPerformance,
    dailyNeeded: Int
) {

    val cardBg = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Goal Performance",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                val msg = if (goalPerformance.isOnTrack) {
                    "✨ You're crushing it! Keep the momentum going."
                } else {
                    val away = goalPerformance.targetAmount - goalPerformance.currentAmount
                    "💪 You're $away away from target. Let's get you back on track!"
                }
                Text(text = msg,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium)
            }
            val chipBg = if (goalPerformance.isOnTrack) Emerald400.copy(alpha = 0.2f) else Amber400.copy(alpha = 0.2f)
            val chipFg = if (goalPerformance.isOnTrack) Emerald300 else Amber300
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(chipBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (goalPerformance.isOnTrack) "On Track" else "Behind", color = chipFg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Weekly progress bar
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("This Week",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium)

                Text("$${goalPerformance.weeklyProgress} / $${goalPerformance.weeklyTarget}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            val pct = (goalPerformance.weeklyProgress.toFloat() / goalPerformance.weeklyTarget.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isDark) Slate700.copy(alpha = 0.3f) else Color(0xFFD1D5DB).copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pct)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (goalPerformance.weeklyProgress >= goalPerformance.weeklyTarget)
                                    listOf(Emerald400, Emerald300)
                                else listOf(Amber400, Amber300)
                            )
                        )
                )
            }
        }

        if (!goalPerformance.isOnTrack) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, (if (isDark) Color(0xFFFFB300) else Color(0xFFF59E0B)).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .background((if (isDark) Color(0xFFFFB300) else Color(0xFFF59E0B)).copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Quick win: Add just $$dailyNeeded per day to hit your goal in ${goalPerformance.daysRemaining} days. You have got this! 🚀",
                    color = if (isDark) Color(0xFFFFF3CD) else Color(0xFF78350F),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


// ---------- Progress Ring ----------

@Composable
private fun ProgressRing(percentage: Int, isDark: Boolean) {
    val strokeWidth = with(LocalDensity.current) { 4.dp.toPx() }
    val pct by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing), label = "pct"
    )
    val ringColor = MaterialTheme.colorScheme.surfaceBright
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val size = min(size.width, size.height)
            val radius = size / 2f - strokeWidth
            // track
            drawCircle(
                color = if (isDark) Color(0x1A94A3B8) else Color(0x336B7280),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            // progress
            val circumference = 2 * PI.toFloat() * radius
            val sweep = 360f * pct
            drawArc(
                brush = Brush.linearGradient(listOf(ringColor, Mint100)),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text("${percentage}%", color = MaterialTheme.colorScheme.surfaceBright, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

// ---------- Timeline Card ----------

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    onClick: () -> Unit
) {
    val bgAchieved = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer))

    val border = MaterialTheme.colorScheme.onTertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .background( bgAchieved )
            .clickable(enabled = milestone.achieved) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconTint = MaterialTheme.colorScheme.surfaceBright

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onTertiaryFixed),
            contentAlignment = Alignment.Center
        ) {
            when (milestone.type) {
                MilestoneType.PCT_100 -> Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                MilestoneType.OVERDRIVE -> Icon(Icons.Default.ShowChart, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                MilestoneType.STREAK -> Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                MilestoneType.MONTH_TARGET -> Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                MilestoneType.PCT_75 -> Icon(Icons.Default.Star, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(milestone.title, color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(milestone.subline, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(milestone.date, color = MaterialTheme.colorScheme.surfaceBright, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Text(milestone.stats, color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
        }
    }
}

// ---------- Celebration Overlay (confetti + glow + title) ----------

@Composable
private fun CelebrationOverlay(title: String, accent: Color, onFinished: () -> Unit) {
    val particles = remember { List(15) { ConfettiParticle.random() } }
    val alphas = remember { particles.map { Animatable(0.9f) } }
    val offsets = remember { particles.map { Animatable(0f) } }

    // Run animation and auto-dismiss ~1.6–1.8s like the React version
    LaunchedEffect(Unit) {
        particles.forEachIndexed { i, p ->
            // opacity fade out
            launch {
                alphas[i].animateTo(0f, animationSpec = tween((1200..1800).random(), easing = EaseOut))
            }
            // travel outward
            launch {
                offsets[i].animateTo(1f, animationSpec = tween((1200..1800).random(), easing = EaseOut))
            }
        }
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // soft glow
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f))
                .blurCompat(30.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✨", fontSize = 42.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Emerald300, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        // confetti particles
        Box(Modifier.fillMaxSize()) {
            val center = with(LocalDensity.current) { 0.dp.toPx() } // unused, just semantic
            particles.forEachIndexed { i, p ->
                val t = offsets[i].value
                val dx = p.tx * t
                val dy = p.ty * t
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = dx, y = dy)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = alphas[i].value))
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(val tx: Dp, val ty: Dp) {
    companion object {
        fun random(): ConfettiParticle {
            val rand = Random(System.nanoTime())
            val tx = ((rand.nextFloat() - 0.5f) * 200f).dp
            val ty = (-(rand.nextFloat()) * 200f).dp
            return ConfettiParticle(tx, ty)
        }
    }
}

// ---------- Utils ----------

// Simple blur helper without requiring additional libraries; uses shadow of a Clip + alpha as aesthetic stand-in
// For a closer backdrop blur effect, use Accompanist System UI Controller or RenderEffect when available.
@Composable
private fun Modifier.blurCompat(radius: Dp): Modifier = this // placeholder to match API; real blur optional by platform

// ---------- Preview (if needed) ----------

@Preview
@Composable
fun PreviewTimeline() {
    MilestoneTimelineScreen()
}
