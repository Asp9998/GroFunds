package com.aryanspatel.grofunds.presentation.screen.showTransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.usecase.BuiltInIncomeTypes
import com.aryanspatel.grofunds.domain.usecase.getEmoji
import com.aryanspatel.grofunds.domain.usecase.getIcon
import com.aryanspatel.grofunds.presentation.common.model.AddEntryUiState
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.viewmodel.ShowTransactionViewModel

@Composable
fun IncomeScreen(viewModel: ShowTransactionViewModel = hiltViewModel()) {

    /**
     *   ViewModel States
     */
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryTotalList by viewModel.categoryTotalList.collectAsStateWithLifecycle()
    val currentEditableTransaction by viewModel.currentTransaction.collectAsStateWithLifecycle()
    val currentYM by viewModel.month.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryIds.collectAsStateWithLifecycle()
    val listOfIncomes = uiState.items
    val groupedIncomes = listOfIncomes.groupBy { it.date }
    val totalSaved = uiState.displayTotal ?: 0.0

    /**
     * Helping States
     */
    var isShowSummary by remember {mutableStateOf(false)}
    var isShowEditOverlay by remember { mutableStateOf(false) }
    var createDuplicate by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            TopAppBarSection(
                kind = uiState.kind.name,
                headerText = "Incomes",
                selectedMonth = currentYM,
                onMonthChange = {viewModel.onMonthChange(it)},
                onExportDataClick = {},
                onShowSummaryClick = { isShowSummary = true},
            )

            InsightHeaderSection(
                kind = uiState.kind.name,
                totalSpentOrSaved = totalSaved,
                isExpenseOverlay = false,
            )

            FiltersSection(
                kind = uiState.kind.name,
                categories = BuiltInIncomeTypes ,
                selectedCategories = categoryList.toList(),
                onCategoryChanged = {viewModel.toggleCategory(id = it)},
                onClearCategory = {viewModel.clearCategories()}
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if(uiState.loading){
                    item {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
                else if (groupedIncomes.isEmpty()) {
                    item {
                        EmptyState(
                            title = "income",
                            description = "start tracking your income flow by adding your first income.",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = MaterialTheme.colorScheme.surfaceTint,
                            onAddTransactionClick = {}
                        )
                    }
                }else {
                    groupedIncomes.forEach { (date, dateIncomes) ->
                        stickyHeader {
                            DateHeader(date = date, total = dateIncomes.sumOf { it.amount.toDoubleOrNull() ?: 0.0 })
                        }

                        items(dateIncomes, key = { it.transactionId }) { income ->
                            TransactionCard(
                                modifier = Modifier.animateItem(),
                                kind = income.kind.name,
                                amount = income.amount.toDoubleOrNull() ?: 0.0,
//                                categoryIcon = getIcon(income.categoryOrType, income.kind.name)
//                                    ?: R.drawable.other,
                                categoryIcon = getEmoji(income.categoryOrType, income.kind.name) ?: "🧩",
                                categoryOrType = income.categoryOrType,
                                note = income.note,
                                isExpenseOverlay = false,
                                isExcluded = false,
                                onEdit = {viewModel.setCurrTransaction(income)
                                    isShowEditOverlay = true },
                                onDuplicate = {
                                    viewModel.setCurrTransaction(income)
                                    createDuplicate = true; isShowEditOverlay = true},
                                onExcludeFromReport = {},
                                onDeleteTransaction = {
                                    viewModel.onDeleteTransaction(
                                        transactionId = income.transactionId,
                                        kind = income.kind.name,
                                        amount = income.amount.toDoubleOrNull() ?: 0.0) },
                            )
                        }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier.padding(bottom = 10.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            }
        }
    }

    if(isShowSummary){
        HorizontalSlidingOverlay(
            title = "Expenses Summary",
            onDismiss = { isShowSummary = false },
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ExpenseDonutChart(
                    kind = uiState.kind.name,
                    categoryTotalList = categoryTotalList,
                    grandTotal = totalSaved,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    holeRatio = 0.62f,
                    minPctForLabel = 3f,
                )
                CategorySummaryList(
                    kind = uiState.kind.name,
                    categories = categoryTotalList)
            }
        }
    }
    if(isShowEditOverlay){
        EditDuplicateTransaction(
            transaction = AddEntryUiState(
                kind = EntryKind.INCOME,
                amount = currentEditableTransaction.amount,
                categoryOrType = currentEditableTransaction.categoryOrType,
                currency = currentEditableTransaction.currency,
                date = currentEditableTransaction.date,
                note = currentEditableTransaction.note ?: "",
                isParsed = true,
            ),
            viewModel = viewModel,
            screenTitle = if(createDuplicate) "Duplicate Expense" else "Edit Expense",
            transactionKind = EntryKind.INCOME,
            createDuplicate = createDuplicate,
            onDismiss = { isShowEditOverlay = false; createDuplicate = false}
        )
    }
}