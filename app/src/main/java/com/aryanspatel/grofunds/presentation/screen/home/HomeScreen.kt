package com.aryanspatel.grofunds.presentation.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.grofunds.data.remote.UserProfile
import com.aryanspatel.grofunds.presentation.common.navigation.Destinations
import com.aryanspatel.grofunds.presentation.screen.addEntry.AddExpenseScreen
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {

    val userProfile = authViewModel.profile.collectAsStateWithLifecycle()

    var expandedChart by remember { mutableStateOf<String?>(null) }
    var showAddEntryScreen by rememberSaveable {mutableStateOf(false)}

    // Sample data
    val totalBalance = 2450.75
    val monthlyIncome = 3500.0
    val monthlyExpenses = 1049.25
    val savingsGoalsCurrent = 750.0
    val savingsGoalsTarget = 2000.0

    val expenseBreakdown = listOf(
        ExpenseBreakdown("Groceries", 350.0, Color(0xFF3B82F6)),
        ExpenseBreakdown("Transport", 200.0, Color(0xFF10B981)),
        ExpenseBreakdown("Dining", 150.0, Color(0xFFEAB308)),
        ExpenseBreakdown("Entertainment", 100.0, Color(0xFF8B5CF6))
    )

    val quickCards = listOf(
        QuickCard("income", Destinations.IncomeScreen.name, monthlyIncome, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981)),
        QuickCard("expenses", Destinations.ExpenseScreen.name, monthlyExpenses, Icons.Default.AttachMoney, Color(0xFFEF4444)),
        QuickCard("savings", Destinations.SavingScreen.name, savingsGoalsCurrent, Icons.Default.TaskAlt, Color(0xFF8B5CF6)),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {


        val scrollState = rememberScrollState()
        var summaryHeightPx by remember { mutableFloatStateOf(0f) }
        var consumedScroll by remember { mutableFloatStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                // Collapse on upward scroll
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val deltaY = available.y
                    if (deltaY < 0f && consumedScroll < summaryHeightPx) {
                        val newConsumed = (consumedScroll + -deltaY).coerceIn(0f, summaryHeightPx)
                        val consume = newConsumed - consumedScroll
                        consumedScroll += consume
                        return Offset(0f, -consume) // consume that part of the scroll
                    }
                    return Offset.Zero
                }

                // Expand only after the child has finished scrolling
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val deltaY = available.y
                    // If we are scrolling down, the list is already at the top, and the header is collapsed, expand the header
                    if (deltaY > 0f && scrollState.value == 0 && consumedScroll > 0f) {
                        val newConsumed = (consumedScroll - deltaY).coerceIn(0f, summaryHeightPx)
                        val consume = newConsumed - consumedScroll
                        consumedScroll += consume
                        return Offset(0f, -consume)
                    }
                    return Offset.Zero
                }
            }
        }
        val scale = 1f - (consumedScroll / summaryHeightPx).coerceIn(0f, 1f)


        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection) // attach nested scroll
        ) {
                Column {
                    // Header
                    HeaderSection(
                        userProfile = userProfile,
                        totalBalance,
                            monthlyIncome,
                            monthlyExpenses,
                            scale = scale,
            //                summaryHeight = summaryHeightPx,
                            onSummaryMeasured = { height -> summaryHeightPx = height } // capture summary card height
                        ){
                        navController.navigate(route = Destinations.ProfileScreen.name)
                    }


                    Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(state = scrollState)
                                .padding(bottom = 100.dp)
                        ) {

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Cards
                        QuickCardsSection(
                                quickCards = quickCards,
                                onCardClick = { cardType ->
                                    navController.navigate(route = cardType)
                                },
                            )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Charts & Analytics
                        ChartsSection(
                                expenseBreakdown = expenseBreakdown,
                                expandedChart = expandedChart,
                                onChartToggle = { chartType ->
                                    expandedChart = if (expandedChart == chartType) null else chartType
                                },
                                monthlyExpenses = monthlyExpenses,
                                savingsGoalsCurrent = savingsGoalsCurrent,
                                savingsGoalsTarget = savingsGoalsTarget
                            )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Daily Reminders
                        DailyReminderSection()

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

        // Floating Action Button
        FloatingActionButtonSection(
            modifier = Modifier.align(Alignment.BottomEnd),
            onAddEntryClick = {showAddEntryScreen = true},
        )
    }


    if(showAddEntryScreen){
        AddExpenseScreen(
            onDismiss = { showAddEntryScreen = false },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HeaderSection(
    userProfile: State<UserProfile?>,
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    scale: Float,
    onSummaryMeasured: (Float) -> Unit,
    onProfileClick: () -> Unit,
) {
    val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    val currencyFormat = NumberFormat.getCurrencyInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ) // same as card's shape
            )
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),

        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp)

    ) {
        Column(modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()){

                IconButton(onClick = onProfileClick,
                    modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(imageVector = Icons.Default.PersonOutline, contentDescription = "Profile icon")
                }

                Column {

                // Greeting
                Text(
                    text = "Good morning, ${userProfile.value?.displayName}!",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentDate,
                    color = Color.White.copy(0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Balance
                Text(
                    text = currencyFormat.format(totalBalance),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Available Balance",
                    color = Color.White.copy(0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )}
            }


            var summaryHeightPx by remember { mutableStateOf(0f) }
            val density = LocalDensity.current
            val collapsedHeightDp = with(density) { (summaryHeightPx * scale).toDp() }
            val shape = RoundedCornerShape(16.dp)
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(collapsedHeightDp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = scale
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            }
                            .onGloballyPositioned { coords ->
                                // record the initial height the first time it's laid out
                                if (summaryHeightPx == 0f) {
                                    summaryHeightPx = coords.size.height.toFloat()
                                    onSummaryMeasured(summaryHeightPx)
                                }
                            },

                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = shape,
                    ) {

                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(0.8f)
                                    )
                                )
                                Text(
                                    text = currencyFormat.format(monthlyIncome),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E8A22),
                                    )
                                )
                            }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            thickness = DividerDefaults.Thickness,
                            color = Color.White.copy(alpha = 0.3f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Expenses",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(0.8f)
                                    )
                                )
                                Text(
                                    text = currencyFormat.format(monthlyExpenses),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFAF2E2F)
                                    )
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCardsSection(
    quickCards: List<QuickCard>,
    onCardClick: (String) -> Unit
) {
    Column {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(quickCards) { card ->
                QuickCard(
                    card = card,
                    onClick = { onCardClick(card.title) },
                )
            }
        }
    }
}

@Composable
fun QuickCard(
    card: QuickCard,
    onClick: () -> Unit,
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()

    Card(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = card.color,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.title,
                color = Color.Gray,
                fontSize = 14.sp
            )

            if (card.type != "tips") {
                Text(
                    text = currencyFormat.format(card.amount),
                    color = card.color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Daily",
                    color = card.color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

//            // Expanded content
//            AnimatedVisibility(
//                visible = isExpanded,
//                enter = expandVertically() + fadeIn(),
//                exit = shrinkVertically() + fadeOut()
//            ) {
//                Column(
//                    modifier = Modifier.padding(top = 12.dp)
//                ) {
//                    Divider(modifier = Modifier.padding(bottom = 12.dp))
//
//                    when (card.type) {
//                        "income" -> {
//                            IncomeDetails()
//                        }
//                        "expenses" -> {
//                            ExpenseDetails(expenses)
//                        }
//                        "savings" -> {
//                            SavingsDetails(savingsGoalsCurrent, savingsGoalsTarget)
//                        }
//                        "tips" -> {
//                            TipsDetails()
//                        }
//                    }
//                }
//            }
        }
    }
}

@Composable
fun ChartsSection(
    expenseBreakdown: List<ExpenseBreakdown>,
    expandedChart: String?,
    onChartToggle: (String) -> Unit,
    monthlyExpenses: Double,
    savingsGoalsCurrent: Double,
    savingsGoalsTarget: Double
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Analytics",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Expense Breakdown Chart
        ExpenseBreakdownChart(
            expenseBreakdown = expenseBreakdown,
            isExpanded = expandedChart == "expenses",
            onToggle = { onChartToggle("expenses") },
            monthlyExpenses = monthlyExpenses
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Goal Progress Chart
        GoalProgressChart(
            current = savingsGoalsCurrent,
            target = savingsGoalsTarget,
            isExpanded = expandedChart == "goals",
            onToggle = { onChartToggle("goals") }
        )
    }
}

@Composable
fun ExpenseBreakdownChart(
    expenseBreakdown: List<ExpenseBreakdown>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    monthlyExpenses: Double
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Pie Chart",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Expense Breakdown",
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(
                        text = if (isExpanded) "Less details" else "More details",
                        color = Color(0xFF2563EB),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            expenseBreakdown.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(item.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.category,
                        fontSize = 14.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = currencyFormat.format(item.amount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg/Day",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = currencyFormat.format(monthlyExpenses / 30),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "vs Last Month",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "-12%",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressChart(
    current: Double,
    target: Double,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val progress = (current / target).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Bar Chart",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Goal Progress",
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(
                        text = if (isExpanded) "Less details" else "More details",
                        color = Color(0xFF2563EB),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Emergency Fund", fontSize = 14.sp)
                Text(text = "${(progress * 100).toInt()}%", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFF8B5CF6),
                trackColor = Color(0xFFE5E7EB)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Monthly target", fontSize = 14.sp, color = Color.Gray)
                        Text(text = currencyFormat.format(200), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "This month", fontSize = 14.sp, color = Color.Gray)
                        Text(text = currencyFormat.format(250), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Remaining", fontSize = 14.sp, color = Color.Gray)
                        Text(text = currencyFormat.format(target - current), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyReminderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFEA580C)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Reminder",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9A3412)
                )
                Text(
                    text = "Don't forget to add today's expenses!",
                    fontSize = 14.sp,
                    color = Color(0xFFC2410C)
                )
            }
            Button(
                onClick = { /* Add expense action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA580C)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Add Now",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FloatingActionButtonSection(
    modifier: Modifier = Modifier,
    onAddEntryClick: () -> Unit,
) {
    Box(modifier = modifier.padding(16.dp)) {
        // FAB Menu Items
//        AnimatedVisibility(
//            visible = showFABMenu,
//            enter = expandVertically() + fadeIn(),
//            exit = shrinkVertically() + fadeOut(),
//            modifier = Modifier.align(Alignment.BottomEnd)
//        ) {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                horizontalAlignment = Alignment.End,
//                modifier = Modifier.padding(bottom = 72.dp)
//            ) {
//                FABMenuItem(
//                    icon = Icons.Default.TaskAlt,
//                    text = "Add Goal",
//                    onClick = onAddGoalClick
//                )
//                FABMenuItem(
//                    icon = Icons.Default.TrendingUp,
//                    text = "Add Income",
//                    onClick = onAddIncomeClick
//                )
//                FABMenuItem(
//                    icon = Icons.Default.AttachMoney,
//                    text = "Add Expense",
//                    onClick = onAddExpenseClick
//                )
//                FABMenuItem(
//                    icon = Icons.Default.DocumentScanner,
//                    text = "Scan Receipt",
//                    onClick = onScannerClick
//                )
//            }
//        }

        // Main FAB
        FloatingActionButton(
            onClick = { onAddEntryClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .shadow(8.dp, CircleShape),
            containerColor =MaterialTheme.colorScheme.primaryContainer,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FABMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(CircleShape)
            .width(140.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)

//            .shadow(RoundedCornerShape(24.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = when (text) {
                "Add Goal" -> Color(0xFF8B5CF6)
                "Add Income" -> Color(0xFF10B981)
                "Add Expense" -> Color(0xFFEF4444)
                "Scan Receipt" -> Color.White
                else -> Color.Gray
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


data class Expense(
    val id: Int,
    val category: String,
    val amount: Double,
    val date: String
)

data class ExpenseBreakdown(
    val category: String,
    val amount: Double,
    val color: Color
)

data class QuickCard(
    val type: String,
    val title: String,
    val amount: Double,
    val icon: ImageVector,
    val color: Color
)