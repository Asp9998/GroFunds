package com.aryanspatel.grofunds.presentation.screen.showTransaction

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlin.math.*
import androidx.compose.ui.graphics.ImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import com.aryanspatel.grofunds.presentation.common.model.CategorySlice
import com.aryanspatel.grofunds.presentation.common.model.Kind
import com.aryanspatel.grofunds.presentation.components.ModernIconBadge
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Donut chart with leader lines, circular icon badges at label anchors, and percentage text.
 *
 * Highlights:
 * - Uses only a fraction of available width (square aspect) so side labels have room.
 * - Auto-hides labels below [minPctForLabel].
 * - Simple vertical de-collision pass per side to reduce label overlap.
 * - Center label shows [grandTotal] via [currency] formatter.
 *
 * Inputs are intentionally UI-ready (amount + color + icon).
 */
@SuppressLint("LocalContextResourcesRead")
@Composable
fun ExpenseDonutChart(
    kind: String,
    categoryTotalList: List<CategorySlice>,
    grandTotal: Double,
    modifier: Modifier = Modifier,
    holeRatio: Float = 0.62f,
    minPctForLabel: Float = 3f,
    gapDegrees: Float = 1.5f,
    iconGap: Dp = 0.dp,
    leaderLen: Dp = 32.dp,                 // ↑ longer leaders by default
    labelTextSizeSp: Int = 12,
    iconSizeDp: Dp = 18.dp,
    chartWidthFraction: Float = 0.4f,      // ← use 40% of available width
    currency: (Float) -> String = { "$" + "%,.0f".format(it) }
) {

    // colors
    val innerCircleColor = MaterialTheme.colorScheme.surface
    val back = MaterialTheme.colorScheme.surfaceVariant
    val percentageColor = MaterialTheme.colorScheme.onSecondary

    // Filter zeros, stable order
    val slices = categoryTotalList

    val textMeasurer = rememberTextMeasurer()
    val ctx = LocalContext.current
    val iconBitmaps = remember(slices, ctx) {
        slices.map { s ->
            s.iconRes.let { id ->
                BitmapFactory.decodeResource(ctx.resources, id)?.asImageBitmap()
            }
        }
    }
    val icon = slices.map { slices -> slices.emoji }

    val backgroundColor =
        if(kind == Kind.EXPENSE.name)
            Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.onSecondaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer))
        else Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.primaryContainer))

    val borderColor = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onBackground

    val iconBackground = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.surfaceContainer
    val iconTint = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.surfaceDim
    else MaterialTheme.colorScheme.surfaceTint

    val emojiColor = LocalContentColor.current.copy(alpha = 1f);
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
        ){
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(chartWidthFraction)   // ← 50% width by default
                .aspectRatio(1f)
        ) {
            if (grandTotal<= 0f) {
                val d = size.minDimension
                val r = d / 2f
                drawCircle(back, r, center)
                drawCircle(innerCircleColor, r * holeRatio, center)
                return@Canvas
            }

            val d = size.minDimension
            val radius = d / 2f
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)

            // Slices with tiny gap
            var startAngle = -90.0f
            slices.forEach { s ->
                val sweep = (s.amount / grandTotal) * 360
                val sweepAdj = max(0.0, sweep - gapDegrees)
                if (sweepAdj > 0f) {
                    drawArc(
                        color = s.color,
                        startAngle = startAngle + gapDegrees / 2f,
                        sweepAngle = sweepAdj.toFloat(),
                        useCenter = true,
                        topLeft = topLeft,
                        size = arcSize
                    )
                }
                startAngle += sweep.toFloat()
            }

            // Donut hole
            val innerR = radius * holeRatio
            drawCircle(innerCircleColor, innerR, center)

            // Label geometry
            val outerR = radius * 0.92f
            val lineLenPx = leaderLen.toPx()
            val minRowGap = (labelTextSizeSp.sp.toPx() + 4.dp.toPx())

            data class LabelPlan(
                val sideLeft: Boolean,
                val angleDeg: Float,
                val anchor: Offset,          // end of leader line
                val bitmap: ImageBitmap?,    // icon
                val pctText: String,
                val color: Color
            )

            val plans = mutableListOf<LabelPlan>()
            startAngle = -90f
            slices.forEachIndexed { idx, s ->
                val sweep = (s.amount / grandTotal) * 360f
                val mid = startAngle + sweep / 2f
                val pct = (s.amount / grandTotal) * 100f
                startAngle += sweep.toFloat()
                if (pct < minPctForLabel) return@forEachIndexed

                val rad = Math.toRadians(mid.toDouble())
                val lineStartR = outerR + 4.dp.toPx()
                val pEnd = Offset(
                    x = center.x + ((lineStartR + lineLenPx) * cos(rad)).toFloat(),
                    y = center.y + ((lineStartR + lineLenPx) * sin(rad)).toFloat()
                )
                val left = (mid % 360f).let { it > 90f && it < 270f }
                plans += LabelPlan(
                    sideLeft = left,
                    angleDeg = mid.toFloat(),
                    anchor = pEnd,
                    bitmap = iconBitmaps[idx],
                    pctText = "${pct.roundToInt()}%",
                    color = s.color
                )
            }

            // Overlap nudging per side
            fun adjust(sideLeft: Boolean): List<LabelPlan> {
                val side = plans.filter { it.sideLeft == sideLeft }
                    .sortedBy { it.anchor.y }
                    .toMutableList()
                if (side.isEmpty()) return emptyList()
                var lastY = side.first().anchor.y
                for (i in 1 until side.size) {
                    val desired = side[i].anchor.y
                    val adjustedY = max(desired, lastY + minRowGap)
                    if (adjustedY > desired) {
                        val cur = side[i]
                        side[i] = cur.copy(anchor = Offset(cur.anchor.x, adjustedY))
                    }
                    lastY = side[i].anchor.y
                }
                return side
            }

            val adjusted = (adjust(true) + adjust(false)).associateBy { it.angleDeg }

            // Draw lines + icon + %
            val iconPx = iconSizeDp.toPx()
            val textStyle = TextStyle(
                color = percentageColor,
                fontSize = labelTextSizeSp.sp,
                fontWeight = FontWeight.SemiBold
            )

            startAngle = -90f
            slices.forEach { s ->
                val sweep = (s.amount / grandTotal) * 360f
                val mid = startAngle + sweep / 2f
                startAngle += sweep.toFloat()

                val pct = (s.amount / grandTotal) * 100f
                if (pct < minPctForLabel) return@forEach

                val plan = adjusted[mid.toFloat()]!!
                val rad = Math.toRadians(plan.angleDeg.toDouble())
                val lineStartR = outerR + 4.dp.toPx()
                val pStart = Offset(
                    x = center.x + (lineStartR * cos(rad)).toFloat(),
                    y = center.y + (lineStartR * sin(rad)).toFloat()
                )
                // unit vector along the radial
                val badgePadding = 6.dp.toPx()                               // space between icon edge and badge
                val stroke = 2.dp.toPx()

                val unitX = cos(rad).toFloat()
                val unitY = sin(rad).toFloat()

                val gapPx = iconGap.toPx()           // visual gap between line and badge (e.g., 2dp)
                val iconHalf = iconPx / 2f
                val badgeRadius = iconHalf + badgePadding   // circle will be larger than icon

                // place the badge center slightly beyond the original anchor
                val iconCenter = Offset(
                    x = plan.anchor.x + (gapPx + badgeRadius) * unitX,
                    y = plan.anchor.y + (gapPx + badgeRadius) * unitY
                )

                // end the line BEFORE the badge edge (gap + round-cap correction)
                val lineEnd = Offset(
                    x = iconCenter.x - (badgeRadius + gapPx + stroke / 2f) * unitX,
                    y = iconCenter.y - (badgeRadius + gapPx + stroke / 2f) * unitY
                )
                drawLine(
                    color = plan.color,
                    start = pStart,
                    end = lineEnd,
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )

                // draw badge background (fill + optional ring)
                drawCircle(
                    color = iconBackground,
                    radius = badgeRadius,
                    center = iconCenter
                )

                s.emoji.let {
                    val left = (iconCenter.x - iconHalf).roundToInt()
                    val top  = (iconCenter.y - iconHalf).roundToInt()
                    drawText(
                        textLayoutResult = textMeasurer.measure(it ?: "🧩", TextStyle(fontSize = 16.sp)),
                        color = emojiColor,
                        topLeft = Offset(
                            x = left.toFloat(),
                            y = top.toFloat()
                        ),
                    )
                }

                // draw the icon centered inside the badge
//                plan.bitmap?.let { bmp ->
//                    val left = (iconCenter.x - iconHalf).roundToInt()
//                    val top  = (iconCenter.y - iconHalf).roundToInt()
//                    drawImage(
//                        colorFilter = ColorFilter.tint(iconTint),
//                        image = bmp,
//                        dstOffset = IntOffset(left, top),
//                        dstSize = IntSize(iconPx.roundToInt(), iconPx.roundToInt())
//                    )
//                }

                // place the percentage text AFTER the badge along the same radial
                val textAfter = gapPx + (badgeRadius * 2f) + 4.dp.toPx()   // badge diameter + a little space
                val textX = iconCenter.x + textAfter * unitX
                val textY = iconCenter.y + textAfter * unitY

                val layout = textMeasurer.measure(plan.pctText, textStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = textX - layout.size.width / 2f,
                        y = textY - layout.size.height / 2f
                    )
                )
            }
        }

        // Center labels
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (grandTotal > 0f) {
                Text(
                    text = currency(grandTotal.toFloat()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "No Entry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
    }
}


enum class SortType {
    SPENT, NAME
}

enum class CategoryStatus {
    OVER, AT_RISK, ON_TRACK
}


@Composable
fun CategorySummaryList(
    kind: String,
    categories: List<CategorySlice>,
    modifier: Modifier = Modifier,
) {
    var sortBy by remember { mutableStateOf(SortType.SPENT) }

    val maxSpent = remember(categories) {
        categories.maxOfOrNull { it.amount } ?: 1.0
    }

    Column(modifier = modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(bottom = 10.dp)
    ) {
        // Header with sorting
        CategoryListHeader(
            kind = kind,
            sortBy = sortBy,
            onSortChange = { sortBy = it }
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        categories.forEachIndexed { index, summary ->
            CategorySummaryRow(
                kind = kind,
                category = summary,
                maxSpent = maxSpent
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun CategoryListHeader(
    kind: String,
    sortBy: SortType,
    onSortChange: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortChip(
                        kind = kind,
                        label = "Spent",
                        selected = sortBy == SortType.SPENT,
                        onClick = { onSortChange(SortType.SPENT) }
                    )
                    SortChip(
                        kind = kind,
                        label = "A–Z",
                        selected = sortBy == SortType.NAME,
                        onClick = { onSortChange(SortType.NAME) }
                    )
                }
            }
        }
    }
}

@Composable
fun SortChip(
    kind: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.surfaceContainer

    FilterChip(
        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = background),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
            borderWidth = 0.dp,
            borderColor = Color.Transparent),
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySummaryRow(
    kind: String,
    category: CategorySlice,
    maxSpent: Double,
    modifier: Modifier = Modifier
) {

    val iconBackground = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.surfaceContainer
    val iconTint = if(kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.surfaceDim
                            else MaterialTheme.colorScheme.surfaceTint

    // Calculations
    val percentUsed = remember(category) {
        category.budget?.let {
            min(100.0, (category.amount / it) * 100)
        } ?: ((category.amount / maxSpent) * 100)
    }

    val status = remember(category) {
        category.budget?.let { budget ->
            when {
                category.amount > budget -> CategoryStatus.OVER
                category.amount > budget -> CategoryStatus.AT_RISK
                else -> CategoryStatus.ON_TRACK
            }
        } ?: CategoryStatus.ON_TRACK
    }

    val delta = category.amount - (category.lastMonthSpent ?: 0.0)
    val deltaPercent = if ((category.lastMonthSpent ?: 0.0 )> 0) {
        (delta / (category.lastMonthSpent ?: 0.0) * 100)
    } else null

    val progressColor = when (status) {
        CategoryStatus.OVER -> MaterialTheme.colorScheme.error
        CategoryStatus.AT_RISK -> Color(0xFFF59E0B) // Amber
        CategoryStatus.ON_TRACK -> MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (percentUsed / 100).toFloat(),
        label = "progress"
    )

    // Content description for accessibility
    val contentDesc = buildString {
        append("${category.amount}, spent ${category.amount.roundToInt()}")
        category.budget?.let { append(" of ${it.roundToInt()} budget, ${percentUsed.roundToInt()}% used") }
        if (category.lastMonthSpent == 0.0 || (category.lastMonthSpent ?: 0.0) < 5.0) {
            append(", new")
        } else if (deltaPercent != null) {
            val sign = if (delta > 0) "+" else ""
            append(", $sign${deltaPercent.roundToInt()}% vs last month")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
//            .background(MaterialTheme.colorScheme.surface)
            .padding( 16.dp)
            .semantics { this.contentDescription = contentDesc },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(RoundedCornerShape(10.dp))
//                .background(iconBackground),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                painter = painterResource(category.iconRes),
//                contentDescription = null,
//                modifier = Modifier.size(20.dp),
//                tint = iconTint)
//        }

        ModernIconBadge(
            text = category.emoji ?: "🧩",
            background = Color.Transparent,
            iconTint = iconTint
        )

        // Center content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Category name
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Utilized and budget
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category.amount.roundToInt().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                if(category.kind == Kind.EXPENSE){
                    Text(
                        text = "of ${category.budget?.roundToInt() ?: "–"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.budget != null) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            if(category.budget != null && category.kind == Kind.EXPENSE){
                ModernLinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        if(category.compareToLastMonth != null || category.lastMonthSpent != null ) {
            Box(
                modifier = Modifier
                    .height(48.dp).width(80.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center

            ){
                Column(modifier = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "-9%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(text = "vs last month",
                        style = MaterialTheme .typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ModernLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    height: Dp = 4.dp,
    glowEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(height / 2))
                .then(
                    if (glowEnabled) {
                        Modifier.shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(height / 2),
                            spotColor = color.copy(alpha = 0.4f),
                            ambientColor = color.copy(alpha = 0.2f)
                        )
                    } else Modifier
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f),
                            color,
                            color.copy(alpha = 0.95f)
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}

