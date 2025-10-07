package com.aryanspatel.grofunds.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
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
import com.aryanspatel.grofunds.data.model.UserProfile
import com.aryanspatel.grofunds.presentation.common.navigation.Destinations
import com.aryanspatel.grofunds.presentation.screen.addEntry.AddExpenseScreen
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import com.aryanspatel.grofunds.presentation.viewmodel.HomeScreenViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalIncome = uiState.totalIncome
    val totalExpense = uiState.totalExpense
    val totalSaving = uiState.totalSaving

    val availableCase = uiState.availableCase

    var showAddEntryScreen by rememberSaveable {mutableStateOf(false)}





    // Sample data

    val quickCards = listOf(
        QuickCard(Destinations.IncomeScreen.name, "Incomes",totalIncome , Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981)),
        QuickCard(Destinations.ExpenseScreen.name, "Expenses", totalExpense, Icons.Default.AttachMoney, Color(0xFFEF4444)),
        QuickCard(Destinations.SavingScreen.name, "Saving Goals", totalSaving, Icons.Default.TaskAlt, Color(0xFF8B5CF6)),
    )

    Box(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
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
                        totalBalance = availableCase,
                        totalIncome = totalIncome,
                        totalExpenses = totalExpense,
                        scale = scale,
            //               summaryHeight = summaryHeightPx,
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

@Composable
fun HeaderSection(
    totalBalance: Double,
    totalIncome: Double,
    totalExpenses: Double,
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
                    text = "Good morning!",
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
                                    text = currencyFormat.format(totalIncome),
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
                                    text = currencyFormat.format(totalExpenses),
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
                    onClick = { onCardClick(card.type) },
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