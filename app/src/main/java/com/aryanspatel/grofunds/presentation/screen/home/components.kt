package com.aryanspatel.grofunds.presentation.screen.home


import android.annotation.SuppressLint
import android.graphics.Color as AColor
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import com.aryanspatel.grofunds.presentation.common.model.Kind
import kotlin.math.roundToInt

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*

// -----------------------------
// Minimal data models (dummy)
// -----------------------------

private data class ChartPoint(
    val label: String, // "Oct 1"
    val expense: Float,
    val income: Float,
    val savings: Float
)

private data class UpcomingItem(
    val kind: String,
    val icon: String,
    val title: String,
    val date: String,
    val tag: String,
    val amount: String,
    val overdue: Boolean = false
)

private data class MentorInsight(
    val title: String,
    val body: String,
    val priority: String, // "high" | "med" | "low"
    val step: String,
    val action: String
)

// ------------------------------------------------------------
// THE SCREEN: only 3 sections (Chart, Upcoming, Mentor Insights)
// ------------------------------------------------------------

@Preview
@Composable
fun HomeCoreScreen() {

    // Dummy data (kept identical to your React sample)
    val baseData = remember {
        listOf(
            ChartPoint("Oct 1", expense = 850f, income = 2200f, savings = 1350f),
            ChartPoint("Oct 5", expense = 920f, income = 2200f, savings = 1280f),
            ChartPoint("Oct 10", expense = 1120f, income = 2200f, savings = 1080f),
            ChartPoint("Oct 14", expense = 980f, income = 2200f, savings = 1220f)
        )
    }
    val expenses = listOf(
        RecurringItem("1", "📺", "Netflix", "-$15.99", "Monthly", "Oct 18", daysUntilDue = 4, cycleDays = 30, accent = Color(0xFF8B5CF6)),
        RecurringItem("2", "📱", "Phone Bill", "-$55.00", "Monthly", "Oct 20", daysUntilDue = 6, accent = Color(0xFF22D3EE)),
        RecurringItem("3", "☁️", "Cloud Storage", "-$2.99", "Monthly", "Oct 16", daysUntilDue = 2, accent = Color(0xFFF59E0B)),
        RecurringItem("4", "🎵", "Spotify", "-$9.99", "Monthly", "Oct 25", daysUntilDue = 11, accent = Color(0xFF10B981))
    )
    val incomes = listOf(
        RecurringItem("11", "💼", "ACME Payroll", "+$2,200", "Biweekly", "Oct 28", daysUntilDue = 14, cycleDays = 14, accent = Color(0xFF34D399)),
        RecurringItem("12", "🏦", "Interest", "+$6.40", "Monthly", "Oct 31", daysUntilDue = 17, accent = Color(0xFF60A5FA))
    )

    var selectedRange by remember { mutableStateOf("30d") }

    // Upcoming dummy (unchanged)
    val upcoming = remember {
        listOf(
            UpcomingItem(
                kind = Kind.EXPENSE.name,
                "🍔",
                "Restaurant Charge",
                "Oct 16",
                "Monthly",
                "-$45.50"),
            UpcomingItem(
                kind = Kind.EXPENSE.name,
                "📺", "Netflix Subscription", "Oct 18", "Monthly", "-$15.99"),
            UpcomingItem(
                kind = Kind.EXPENSE.name,
                "⚡", "Electric Bill", "Oct 20", "Monthly", "-$120.00", overdue = true),
            UpcomingItem(
                kind = Kind.INCOME.name,
            "⚡", "Salary", "Oct 29", "Monthly", "$2840.00")
        )
    }

    // Insights dummy (unchanged)
    val insights = remember {
        listOf(
            MentorInsight(
                "Turn weekend spikes into savings",
                "Fri–Sun spending runs 42% above weekdays. Shift $30 grocery top-up to Thu.",
                "high",
                "Create Thursday reminder",
                "Open weekly plan"
            ),
            MentorInsight(
                "Build a 1.5× buffer",
                "Route next 3 paycheques: 70% buffer, 30% to Car Fund. Target: $1,000.",
                "med",
                "Set buffer target",
                "Adjust auto-saves"
            ),
            MentorInsight(
                "Trim drip subscriptions",
                "Three sub-$15 services renewed. Kill two; annual saving ≈ $216.",
                "low",
                "Start quarterly audit",
                "View subscriptions"
            )
        )
    }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(1.dp))

            TriLineChartCard(
                data = baseData,
                selectedRange = selectedRange,
                onRangeChange = { selectedRange = it },
            )

            MentorInsightsSection(insights = insights)

//            UpcomingSection(upcoming = upcoming)

            RecurringTransactionSection(
                expenses = expenses,
                incomes = incomes,
                onShowAllExpenses = {},
                onAddExpense = {},
                onShowAllIncome = {},
                onAddIncome = {},
                onClickItem = {}
            )

            Spacer(modifier = Modifier.height(1.dp))
        }
    }



@SuppressLint("ClickableViewAccessibility")
@Composable
private fun TriLineChartCard(
    data: List<ChartPoint>,
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    // visibleLines/onToggleLine removed per request
) {
    val cyanStroke = Color(0x4022D3EE)
    val glass = Brush.linearGradient(
        listOf(Color(0x99303A4B), Color(0x80303A4B), Color(0x66101726))
    )
    val cardShape = RoundedCornerShape(24.dp)

    // Tooltip (selected point) state
    var selectedHighlight by remember { mutableStateOf<Highlight?>(null) }
    var selectedEntrySet by remember { mutableStateOf<Triple<Float, Float, Float>?>(null) }
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    var chartSize by remember { mutableStateOf(IntSize(0, 0)) }

    // Track touch state & direction suppression
    var isTouchActive by remember { mutableStateOf(false) }
    var suppressHighlight by remember { mutableStateOf(false) }
    var touchDownX by remember { mutableStateOf(0f) }
    var touchDownY by remember { mutableStateOf(0f) }

    // Map labels
    val labels = remember(data) { data.map { it.label } }

    // Prepare LineData (always visible)
    fun buildLineData(): LineData {
        val expEntries = ArrayList<Entry>()
        val incEntries = ArrayList<Entry>()
        val savEntries = ArrayList<Entry>()
        data.forEachIndexed { i, p ->
            expEntries.add(Entry(i.toFloat(), p.expense))
            incEntries.add(Entry(i.toFloat(), p.income))
            savEntries.add(Entry(i.toFloat(), p.savings))
        }

        fun setStyle(set: LineDataSet, stroke: Color) = set.apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2f
            color = stroke.toArgb()
            highLightColor = Color.White.copy(alpha = 0.85f).toArgb()
            isHighlightEnabled = true
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val exp = LineDataSet(expEntries, "Expense").let { setStyle(it, Color(0xFFF87171)) }
        val inc = LineDataSet(incEntries, "Income").let { setStyle(it, Color(0xFF34D399)) }
        val sav = LineDataSet(savEntries, "Savings").let { setStyle(it, Color(0xFF60A5FA)) }

        return LineData(exp, inc, sav)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
//            .background(glass, cardShape)
            .padding(horizontal = 16.dp),
        shape = cardShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            // Range selector (kept)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RangeChip("7d", selectedRange) { onRangeChange("7d") }
                Spacer(Modifier.width(6.dp))
                RangeChip("30d", selectedRange) { onRangeChange("30d") }
                Spacer(Modifier.width(6.dp))
                RangeChip("90d", selectedRange) { onRangeChange("90d") }
            }

            Spacer(Modifier.height(12.dp))

            // Chart + Compose tooltip overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .onGloballyPositioned {
                        chartSize = IntSize(it.size.width, it.size.height)
                    }
            ) {
                val context = LocalContext.current

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        LineChart(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setViewPortOffsets(48f, 16f, 24f, 36f)
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(false)
                            setPinchZoom(false)
                            setDrawGridBackground(false)
                            axisRight.isEnabled = false

                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                textColor = AColor.argb(160, 148, 163, 184)
                                axisLineColor = AColor.argb(40, 34, 211, 238)
                                gridColor = AColor.argb(25, 34, 211, 238)
                                setDrawGridLines(true)
                                setDrawAxisLine(true)
                                valueFormatter = IndexAxisValueFormatter(labels)
                            }

                            axisLeft.apply {
                                textColor = AColor.argb(160, 148, 163, 184)
                                axisLineColor = AColor.argb(40, 34, 211, 238)
                                gridColor = AColor.argb(25, 34, 211, 238)
                                setDrawGridLines(true)
                                setDrawAxisLine(true)
                            }

                            legend.isEnabled = false
                            setBackgroundColor(AColor.argb(140, 30, 41, 59))

                            // Highlight config
                            isHighlightPerTapEnabled = true
                            isHighlightPerDragEnabled = true

                            // Animate in
                            animateX(600, Easing.EaseOutCubic)
                        }
                    },
                    update = { chart ->
                        chart.data = buildLineData()
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

                        // Gesture listener to suppress highlight when vertical scroll dominates
                        chart.onChartGestureListener = object : OnChartGestureListener {
                            override fun onChartGestureStart(
                                me: MotionEvent?,
                                lastPerformedGesture: ChartTouchListener.ChartGesture?
                            ) {
                                me ?: return
                                isTouchActive = true
                                suppressHighlight = false
                                touchDownX = me.x
                                touchDownY = me.y
                            }

                            override fun onChartGestureEnd(
                                me: MotionEvent?,
                                lastPerformedGesture: ChartTouchListener.ChartGesture?
                            ) {
                                isTouchActive = false
                                selectedHighlight = null
                                chart.highlightValues(null) // clear MPChart highlight
                            }

                            override fun onChartLongPressed(me: MotionEvent?) {}
                            override fun onChartDoubleTapped(me: MotionEvent?) {}
                            override fun onChartSingleTapped(me: MotionEvent?) {}

                            override fun onChartFling(
                                me1: MotionEvent?,
                                me2: MotionEvent?,
                                velocityX: Float,
                                velocityY: Float
                            ) { /* ignore */ }

                            override fun onChartScale(
                                me: MotionEvent?,
                                scaleX: Float,
                                scaleY: Float
                            ) { /* ignore */ }

                            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                                // check direction dominance: vertical scroll should suppress tooltip
                                me ?: return
                                val dx = kotlin.math.abs(me.x - touchDownX)
                                val dy = kotlin.math.abs(me.y - touchDownY)
                                // Once marked, keep suppressed for this gesture
                                if (!suppressHighlight && dy > dx * 1.15f) {
                                    suppressHighlight = true
                                    // Clear any highlight/tooltip and let parent handle scroll
                                    selectedHighlight = null
                                    chart.highlightValues(null)
                                }
                            }
                        }

                        // Low-level touch to allow parent to scroll when vertical
                        chart.setOnTouchListener { v, ev ->
                            when (ev.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    isTouchActive = true
                                    suppressHighlight = false
                                    touchDownX = ev.x
                                    touchDownY = ev.y
                                    // Disallow parent intercept until we decide direction
                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val dx = kotlin.math.abs(ev.x - touchDownX)
                                    val dy = kotlin.math.abs(ev.y - touchDownY)
                                    if (dy > dx * 1.15f) {
                                        // vertical scroll intent -> allow parent to intercept, suppress tooltip
                                        suppressHighlight = true
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                        selectedHighlight = null
                                        (v as? LineChart)?.highlightValues(null)
                                    } else {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    isTouchActive = false
                                    suppressHighlight = false
                                    selectedHighlight = null
                                    (v as? LineChart)?.highlightValues(null)
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                            }
                            // Let MPAndroidChart also handle it
                            v.onTouchEvent(ev)
                        }

                        // Update tooltip on highlight changes (only when touch active & not suppressed)
                        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                if (!isTouchActive || suppressHighlight) return
                                if (e == null || h == null) return
                                val index = e.x.toInt().coerceIn(0, data.lastIndex)
                                selectedLabel = labels.getOrNull(index)
                                data.getOrNull(index)?.let { p ->
                                    selectedEntrySet = Triple(p.expense, p.income, p.savings)
                                }
                                selectedHighlight = h
                            }

                            override fun onNothingSelected() {
                                // Only clear if touch ended/cancelled; otherwise MPChart may send this mid-gesture
                                if (!isTouchActive) {
                                    selectedHighlight = null
                                }
                            }
                        })

                        chart.invalidate()
                    }
                )

                // Tooltip box — only while finger is down and highlight exists
                val showTooltip = isTouchActive && selectedHighlight != null && !suppressHighlight
                if (showTooltip) {
                    val h = selectedHighlight!!
                    // Preferred placement near finger, then clamp inside chart bounds
                    val boxW = 220f
                    val boxH = 100f
                    val margin = 8f

                    // Try placing top-left of finger by default; if out of bounds, flip as needed
                    var targetX = h.xPx - boxW - 12f
                    var targetY = h.yPx - boxH - 12f

                    if (targetX < margin) targetX = (h.xPx + 12f).coerceAtMost(chartSize.width - boxW - margin)
                    if (targetY < margin) targetY = (h.yPx + 12f).coerceAtMost(chartSize.height - boxH - margin)
                    if (targetX + boxW > chartSize.width - margin) targetX = chartSize.width - boxW - margin
                    if (targetY + boxH > chartSize.height - margin) targetY = chartSize.height - boxH - margin

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(targetX.roundToInt(), targetY.roundToInt()) }
                            .width(220.dp)
                            .background(
                                color = Color(0xE61F2937),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color(0x5522D3EE), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = selectedLabel ?: "",
                                color = Color(0xFF93A3B5),
                                fontSize = 12.sp
                            )
                            val triple = selectedEntrySet
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DotValue("Expense", Color(0xFFF87171), triple?.first)
                                Spacer(Modifier.width(8.dp))
                                DotValue("Income", Color(0xFF34D399), triple?.second)
                                Spacer(Modifier.width(8.dp))
                                DotValue("Savings", Color(0xFF60A5FA), triple?.third)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, selected: String, onClick: () -> Unit) {
    val isSel = label == selected
    Button(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor =
            if(isSel) MaterialTheme.colorScheme.surfaceContainer else
            MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(3.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun DotValue(label: String, color: Color, value: Float?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(color, RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color(0xFF9AA7B8), fontSize = 11.sp)
        }
        Text(
            text = value?.let { "$" + "%,.0f".format(it) } ?: "—",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------
// Upcoming Section (unchanged)
// ---------------------------

@Composable
private fun UpcomingSection(upcoming: List<UpcomingItem>) {
    val shape = RoundedCornerShape(20.dp)


    Column(modifier = Modifier.padding(horizontal = 16.dp)){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer
                )),
                shape = shape
            )
            .border(1.dp, MaterialTheme.colorScheme.onBackground, shape)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Upcoming (7–14 days)",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                ))
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            upcoming.forEachIndexed { index, item ->

                val icoTint = if(item.kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.surfaceDim
                else MaterialTheme.colorScheme.surfaceTint

                val iconBackground = if(item.kind == Kind.EXPENSE.name) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.surfaceContainer

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .background(Color.Transparent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconBackground, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.icon,
                            color = icoTint
                            , fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                            ))
                        Text("${item.date} • ${item.tag}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSecondary,
                            ))
                    }
                    Text(
                        item.amount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                        ))
                }
                if(index != (upcoming.size-1)){
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 60.dp )
                    )
                }
            }
        }
    }
    }
}

// ----------------------------------
// AI Mentor Insights (unchanged UI)
// ----------------------------------

@Composable
private fun MentorInsightsSection(insights: List<MentorInsight>) {
    Column(Modifier.fillMaxWidth()) {
        Text("AI Mentor Insights",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.width(1.dp))
            insights.forEach { insight ->
                val shape = RoundedCornerShape(16.dp)
                val priorityColors = when (insight.priority) {
                    "high" -> Brush.verticalGradient(listOf(Color(0xFFFF5A5A), Color(0xFFFFA24C)))
                    "med" -> Brush.verticalGradient(listOf(Color(0xFFFFD54A), Color(0xFFFFA24C)))
                    else -> Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF22D3EE)))
                }
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .background( Brush.linearGradient(listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer
                        )),
                            shape)
                        .border(1.dp, Color(0x3322D3EE), shape)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(48.dp)
                            .background(priorityColors, RoundedCornerShape(999.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(insight.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        ))
                    Spacer(Modifier.height(3.dp))
                    Text(insight.body,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary,
                        ))
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.6f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x3322D3EE), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(insight.step, color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}


//@Composable
//fun RecurringTransactionSection() {
//}




// ---------- Data model ----------
data class RecurringItem(
    val id: String,
    val icon: String,          // quick emoji/icon; swap with painter if you have assets
    val title: String,
    val amount: String,        // formatted (e.g., "-$15.99")
    val cadence: String,       // e.g., "Monthly", "Weekly"
    val nextDate: String,      // e.g., "Oct 18"
    val daysUntilDue: Int,     // e.g., 3
    val cycleDays: Int = 30,   // for ring progress
    val accent: Color = Color(0xFF22D3EE) // tint for the ring glow
)

// ---------- Section composable ----------
@Composable
fun RecurringTransactionSection(
    expenses: List<RecurringItem>,
    incomes: List<RecurringItem>,
    onShowAllExpenses: () -> Unit,
    onAddExpense: () -> Unit,
    onShowAllIncome: () -> Unit,
    onAddIncome: () -> Unit,
    onClickItem: (RecurringItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().padding(horizontal = 16.dp)
        ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RecurringCard(
            title = "Recurring Expenses",
            subtitle = "Your upcoming charges at a glance",
            items = expenses,
            headerTint = Color(0xFFF87171),
            onShowAll = onShowAllExpenses,
            onAdd = onAddExpense,
            onClickItem = onClickItem
        )

        RecurringCard(
            title = "Recurring Income",
            subtitle = "Expected paycheques & deposits",
            items = incomes,
            headerTint = Color(0xFF34D399),
            onShowAll = onShowAllIncome,
            onAdd = onAddIncome,
            onClickItem = onClickItem
        )
    }
}

// ---------- Card ----------
@Composable
private fun RecurringCard(
    title: String,
    subtitle: String,
    items: List<RecurringItem>,
    headerTint: Color,
    onShowAll: () -> Unit,
    onAdd: () -> Unit,
    onClickItem: (RecurringItem) -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    var expanded by remember { mutableStateOf(false) }

    // Glassy background + border
    val glass = Brush.linearGradient(
        colors = listOf(Color(0x80303A4B), Color(0x66303A4B)),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val borderCol = Color(0x3322D3EE)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(glass, shape)
            .border(1.dp, borderCol, shape),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Accent bar + titles
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 36.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(headerTint.copy(alpha = 0.9f), headerTint.copy(alpha = 0.4f))
                            ),
                            RoundedCornerShape(999.dp)
                        )
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(subtitle, color = Color(0xFF9AA7B8), fontSize = 12.sp)
                }
                // Actions: Show all + Add
                TextButton(onClick = onShowAll, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("Show all", color = Color(0xFF7DE3F3), fontSize = 12.sp)
                }
                Spacer(Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onAdd,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color(0x4D22D3EE), Color(0x4D22D3EE)))
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                ) {
                    Text("+ Add", color = Color(0xFF7DE3F3), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items
            val collapsedCount = 3
            val showItems = if (expanded) items else items.take(collapsedCount)

            if (showItems.isEmpty()) {
                EmptyHint()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    showItems.forEach { item ->
                        RecurringRow(item = item, onClick = { onClickItem(item) })
                    }
                }
            }

            // Footer: expand/collapse or subtle hint
            if (items.size > collapsedCount) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (expanded) "Show less" else "Show more",
                        color = Color(0xFF7DE3F3),
                        fontSize = 12.sp
                    )
                }
            } else {
                // Cute hint to attract interaction
                Text(
                    "Pro tip: set alerts for any charge that shifts amount/date.",
                    color = Color(0xFF8793A6),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ---------- Row (item) ----------
@Composable
private fun RecurringRow(item: RecurringItem, onClick: () -> Unit) {
    val rowShape = RoundedCornerShape(14.dp)
    val glow = item.accent.copy(alpha = 0.12f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(Color(0x14121A24))
            .border(1.dp, Color(0x1F22D3EE), rowShape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon bubble
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(item.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.icon, fontSize = 18.sp)
        }

        Spacer(Modifier.width(10.dp))

        // Title + meta
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgePill(label = item.cadence)
                Spacer(Modifier.width(6.dp))
                Text("Next: ${item.nextDate}", color = Color(0xFF8EA0B6), fontSize = 11.sp)
            }
        }

        Spacer(Modifier.width(8.dp))

        // Amount + Due ring
        Column(horizontalAlignment = Alignment.End) {
            Text(
                item.amount,
                color = Color(0xFFDEE5EF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            DueRing(
                daysUntilDue = item.daysUntilDue,
                cycleDays = item.cycleDays,
                accent = item.accent,
                glow = glow
            )
        }
    }
}

// ---------- Small pieces ----------
@Composable
private fun BadgePill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x1F22D3EE))
            .border(1.dp, Color(0x3322D3EE), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = Color(0xFF9EDFF0), fontSize = 11.sp)
    }
}

/**
 * Animated radial ring showing how close you are to the next charge date.
 * The closer to due, the fuller the ring. Displays "in Xd".
 */
@Composable
private fun DueRing(
    daysUntilDue: Int,
    cycleDays: Int,
    accent: Color,
    glow: Color
) {
    // Progress increases as due approaches: (cycleDays - daysUntilDue) / cycleDays
    val raw = (cycleDays - daysUntilDue).coerceIn(0, cycleDays).toFloat() / cycleDays.toFloat().coerceAtLeast(1f)
    val progress by animateFloatAsState(
        targetValue = raw,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "due-progress"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2f + 2f
            val size = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // soft outer glow
            drawCircle(color = glow, radius = size.minDimension / 2f + 8f, center = center)

            // back track
            drawArc(
                color = Color(0x33405263),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // progress
            drawArc(
                brush = Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f))),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text("in ${daysUntilDue}d", color = Color(0xFF9CB3C9), fontSize = 9.sp, lineHeight = 10.sp)
    }
}

@Composable
private fun EmptyHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0FFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No recurring yet", color = Color(0xFFB6C3D6), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Add your first one to stay ahead of bills & paydays.",
            color = Color(0xFF8EA0B6),
            fontSize = 11.sp
        )
    }
}

