package com.aryanspatel.grofunds.presentation.screen.savings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.aryanspatel.grofunds.presentation.common.model.Activity
import com.aryanspatel.grofunds.presentation.common.model.GoalData
import com.aryanspatel.grofunds.presentation.common.model.NextContribution
import com.aryanspatel.grofunds.presentation.components.Button
import kotlin.math.*



@Preview
@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingScreen() {
    var currentTab by remember { mutableStateOf("Overview") }
    var isHeaderCollapsed by remember { mutableStateOf(false) }
    var showContributionSheet by remember { mutableStateOf(false) }
    var showGoalSwitcher by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var contributionAmount by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()

    // Sample data
    val goalData = GoalData(
        name = "Emergency Fund",
        target = 5000,
        saved = 2140,
        left = 2860,
        progress = 42.8f,
        pace = -8,
        nextContribution = NextContribution("Oct 4", 50),
        eta = "Feb 15, 2026",
        etaStatus = "behind"
    )

    var activities by remember {
        mutableStateOf(listOf(
            Activity(1, "2025-09-25", 200, "Manual", "Initial deposit", "contribution"),
            Activity(2, "2025-09-20", 50, "Auto", "Weekly auto-save", "contribution"),
            Activity(3, "2025-09-18", 15, "Round-up", "Round-up from purchases", "contribution"),
            Activity(4, "2025-09-13", 100, "Transfer", "From checking account", "contribution")
        ))
    }


    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)){

        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.clickable { showGoalSwitcher = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goalData.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Switch goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Edit goal */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)

        )

        // Goal Header
        AnimatedGoalHeader(
            goalData = goalData,
            isCollapsed = isHeaderCollapsed,
            onContributeClick = { showContributionSheet = true },
        )

        // Pause Banner
        if (isPaused) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Goal paused — no scheduled contributions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Tabs
            TabRow(
            containerColor = Color.Transparent,
            selectedTabIndex = when (currentTab) {
                "Overview" -> 0
                "Activity" -> 1
                else -> 0
            },
                indicator = {},
//                divider = {}
            ) {
            listOf("Overview", "Activity").forEachIndexed { index, tab ->
                Tab(
                    selectedContentColor = MaterialTheme.colorScheme.tertiary,
                    unselectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    selected = currentTab == tab,
                    modifier = Modifier
                        .background(
                            if (currentTab == tab)
                                MaterialTheme.colorScheme.background  // background for selected tab
                            else
                                Color.Transparent
                        ),
                    onClick = { currentTab = tab;},
                    text = {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (currentTab == tab) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )
            }
        }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent),) {
            when (currentTab) {
                "Overview" -> OverviewTab(
                    goalData = goalData,
                    scrollState = scrollState
                )
                "Activity" -> ActivityTab(
                    activities = activities,
                    scrollState = scrollState
                )
            }
        }
    }

    // Bottom Sheets
    if (showContributionSheet) {
        ContributionBottomSheet(
            contributionAmount = contributionAmount,
            onAmountChange = { contributionAmount = it },
            onConfirm = {
                if (contributionAmount.isNotEmpty()) {
                    val newActivity = Activity(
                        id = activities.size + 1,
                        date = "2025-09-28",
                        amount = contributionAmount.toIntOrNull() ?: 0,
                        source = "Manual",
                        note = "Quick contribution",
                        type = "contribution"
                    )
                    activities = listOf(newActivity) + activities
                    contributionAmount = ""
                    showContributionSheet = false
                }
            },
            onDismiss = { showContributionSheet = false },
            goalData = goalData
        )
    }

    if (showGoalSwitcher) {
        GoalSwitcherBottomSheet(
            onDismiss = { showGoalSwitcher = false }
        )
    }

    if (showMoreMenu) {
        MoreMenuBottomSheet(
            onDismiss = { showMoreMenu = false }
        )
    }
}

@Composable
fun AnimatedGoalHeader(
    goalData: GoalData,
    isCollapsed: Boolean,
    onContributeClick: () -> Unit,
) {
    val animatedPadding by animateDpAsState(
        targetValue = if (isCollapsed) 12.dp else 24.dp,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = animatedPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Due: ${goalData.eta}",
                        style = if (isCollapsed) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (!isCollapsed) {
                    Card(modifier = Modifier
                        .clickable{onContributeClick()}
                        .clip(RoundedCornerShape(6.dp))
                        ,
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
                    ){
                        Icon(imageVector = Icons.Default.Add,
                            contentDescription = "Add Funds",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(52.dp)
                        )
                    }
            }
        }
    }
}
@Composable
fun OverviewTab(
    goalData: GoalData,
    scrollState: LazyListState
) {
    LazyColumn(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        state = scrollState,
//        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            PreviewArcProgressHeroCard()
        }

        // Progress Chart
        item {
            ContributionsScreen()
        }

        // Notes
        item {
            NotesCard()
        }
    }
}


@Composable
fun NotesCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)

    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"Paris trip—don't touch!\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivityTab(
    activities: List<Activity>,
    scrollState: LazyListState
) {
    Column {
        // Filters
        Card(
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("All") },
                            selected = true
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("Contributions") },
                            selected = false
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { },
                            label = { Text("Withdrawals") },
                            selected = false
                        )
                    }
                }

                IconButton(onClick = { /* Show filter menu */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        }

        LazyColumn(
            state = scrollState
        ) {
            // Month header
            item {
                Card(
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "September 2025 · +$365",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Activity items
            items(activities) { activity ->
                ActivityItem(activity)
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (icon, iconColor) = when (activity.source) {
                "Auto" -> Icons.Default.Bolt to Color(0xFF16A34A)
                "Manual" -> Icons.Default.Add to Color(0xFF16A34A)
                "Transfer" -> Icons.Default.ArrowUpward to Color(0xFF16A34A)
                "Round-up" -> Icons.Default.CreditCard to Color(0xFF16A34A)
                else -> Icons.Default.AttachMoney to Color(0xFF16A34A)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFDCFCE7),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    activity.note,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        activity.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activity.source in listOf("Auto", "Round-up")) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val badgeColor = if (activity.source == "Auto") Color(0xFFDBEAFE) to Color(0xFF1E40AF)
                        else Color(0xFFDCFCE7) to Color(0xFF166534)
                        Box(
                            modifier = Modifier
                                .background(
                                    color = badgeColor.first,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                activity.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor.second
                            )
                        }
                    }
                }
            }
        }

        Text(
            "+${activity.amount}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF16A34A)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionBottomSheet(
    contributionAmount: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    goalData: GoalData
) {
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Contribution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = contributionAmount,
                onValueChange = onAmountChange,
                placeholder = { Text("Amount") },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(20, 50, 100).forEach { amount ->
                    OutlinedButton(
                        onClick = { onAmountChange(amount.toString()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$amount")
                    }
                }
            }

            if (contributionAmount.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                val newAmount = contributionAmount.toIntOrNull() ?: 0
                val newProgress = ((goalData.saved + newAmount).toFloat() / goalData.target * 100).roundToInt()

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "New ETA: Nov 3 (−9 days)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            "Progress will be $newProgress%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                enabled = contributionAmount.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Contribution")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSwitcherBottomSheet(
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Switch Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(2.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Emergency Fund",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "$2,140 / $5,000",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Switch to this goal */ }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "New Phone",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "$320 / $800",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Switch to this goal */ }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Vacation Fund",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "$1,200 / $3,000",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuBottomSheet(
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Pause goal */ }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pause Goal")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Archive goal */ }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Archive Goal")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Export data */ }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Export Data")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

//// Extension function for number formatting
@SuppressLint("DefaultLocale")
fun Int.formatWithCommas(): String {
    return String.format("%,d", this)
}