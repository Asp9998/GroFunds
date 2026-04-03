package com.aryanspatel.grofunds.presentation.screen.savings

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.savedState
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.neededPerMonth
import com.aryanspatel.grofunds.domain.usecase.projectedCompletionDate
import com.aryanspatel.grofunds.presentation.common.model.SavingScreenTab
import com.aryanspatel.grofunds.presentation.viewmodel.SavingsViewModel

@Preview
@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingScreen(
    viewModel: SavingsViewModel = hiltViewModel()
) {


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSaving by viewModel.selectedSaving.collectAsStateWithLifecycle()
    val contributions = selectedSaving?.contributions
    val savingHeaderUi by viewModel.savingHeaderUi.collectAsStateWithLifecycle()


    var currentTab by remember { mutableStateOf(SavingScreenTab.Overview.name)}

    var showContributionSheet by remember { mutableStateOf(false) }
    var showGoalSwitcher by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var contributionAmount by remember { mutableStateOf("") }

    val scrollState = rememberLazyListState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        color = Color.Transparent
    ) {

        if(selectedSaving == null){

        }else{

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(0.3f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            TopHeaderSection(
                title = selectedSaving?.title ?: "",
                onGoalSwitchClick = { showGoalSwitcher = true },
                onAddClick = { showContributionSheet = true },
                onMenuClick = { showMoreMenu = !showMoreMenu },
                isManuVisible = showMoreMenu,
                onEditClick = {},
                onDeleteClick = {},
                onDismiss = {showMoreMenu = false}
            )

            TabRow(
            containerColor = Color.Transparent,
            selectedTabIndex = when (currentTab) {
                SavingScreenTab.Overview.name -> 0
                SavingScreenTab.Activity.name -> 1
                else -> 0
            },
                divider = {}
            ) {
                listOf("Overview", "Activity").forEachIndexed { index, tab ->
                    Tab(
                        selectedContentColor = MaterialTheme.colorScheme.primaryFixed,
                        unselectedContentColor = MaterialTheme.colorScheme.onSecondary.copy(0.6f),
                        selected = currentTab == tab,
                        modifier = Modifier.background(Color.Transparent),
                        onClick = { currentTab = tab;},
                        text = {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),) {
                when (currentTab) {
                    "Overview" -> OverviewTab(
                        header = savingHeaderUi,
                        saving = selectedSaving!!
                    )
                    "Activity" -> ActivityTab(
                        scrollState = scrollState
                    )
                }
            }
        }
    }
    }


    // Bottom Sheets
    if (showContributionSheet) {
        ContributionBottomSheet(
            contributionAmount = contributionAmount,
            onAmountChange = { contributionAmount = it },
            onConfirm = {},
            onDismiss = { showContributionSheet = false },
        )
    }

    if (showGoalSwitcher) {
        GoalSwitcherBottomSheet(
            listOfSavings = uiState.savings,
            selectedSavingId = selectedSaving?.savingId ?: "",
            onSavingSelected = { selectedSavingId -> viewModel.onSelectSaving(selectedSavingId) },
            onDismiss = { showGoalSwitcher = false }
        )
    }
}


@Composable
fun TopHeaderSection(
    title : String,
    isManuVisible: Boolean,
    onGoalSwitchClick: () -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)){
        Card (modifier = Modifier
            .align(Alignment.CenterStart)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onGoalSwitchClick() },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ){

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Switch goal",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Box {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    SavingMenu(
                        expanded = isManuVisible,
                        onDismiss = onDismiss,
                        onEdit = onEditClick,
                        onDelete = onDeleteClick
                    )
                }
        }

    }
}


@Composable
fun ActivityTab(
    scrollState: LazyListState
) {
    Column {

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            state = scrollState
        ) {

            item {
                MilestoneTimelineScreen()
            }

            item {
                Spacer(modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    }
}

//// Extension function for number formatting
@SuppressLint("DefaultLocale")
fun Int.formatWithCommas(): String {
    return String.format("%,d", this)
}