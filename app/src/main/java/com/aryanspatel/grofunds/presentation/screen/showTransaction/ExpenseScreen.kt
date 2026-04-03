package com.aryanspatel.grofunds.presentation.screen.showTransaction

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.aryanspatel.grofunds.domain.usecase.BuiltInExpenseCategories
import com.aryanspatel.grofunds.domain.usecase.getEmoji
import com.aryanspatel.grofunds.domain.usecase.getIcon
import com.aryanspatel.grofunds.presentation.common.model.AddEntryUiState
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.screen.addEntry.AddExpenseScreen
import com.aryanspatel.grofunds.presentation.viewmodel.ShowTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseScreen(
    viewModel: ShowTransactionViewModel = hiltViewModel()
) {

    /**
     *   ViewModel States
     */

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryTotalList by viewModel.categoryTotalList.collectAsStateWithLifecycle()
    val currentEditableTransaction by viewModel.currentTransaction.collectAsStateWithLifecycle()
    val currentYM by viewModel.month.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryIds.collectAsStateWithLifecycle()
    val listOfExpenses = uiState.items
    val groupedExpenses = listOfExpenses.groupBy { it.date }

    val totalSpent = uiState.displayTotal ?: 0.0
    val dailyAvg = viewModel.dailyAverageForMonth(totalSpent, uiState.month)
    val budget = 1500.0


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
                headerText = "Expenses",
                selectedMonth = currentYM,
                onMonthChange = { viewModel.onMonthChange(it)},
                onExportDataClick = {},
                onShowSummaryClick = { isShowSummary = !isShowSummary; viewModel.clearCategories() },
            )

            InsightHeaderSection(
                kind = uiState.kind.name,
                totalSpentOrSaved = totalSpent,
                dailyAvg = dailyAvg,
                budget = budget,
            )

            FiltersSection(
                kind = uiState.kind.name,
                categories = BuiltInExpenseCategories ,
                selectedCategories = categoryList.toList(),
                onCategoryChanged = {viewModel.toggleCategory(id = it)},
                onClearCategory = {viewModel.clearCategories()}
            )

            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if(uiState.loading){
                    item {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
                else if (groupedExpenses.isEmpty()) {
                    item {
                        EmptyState(
                            modifier = Modifier,
                            title = "expense",
                            description = "Start tracking your spending by adding your first expense.",
                            icon = Icons.Default.AttachMoney,
                            iconTint = MaterialTheme.colorScheme.surfaceDim,
                            onAddTransactionClick = {},
                        )
                    }
                } else {
                    groupedExpenses.forEach { (date, dateExpenses) ->
                        stickyHeader {
                            DateHeader(date = date, total = dateExpenses.sumOf { it.amount.toDouble() })
                        }

//                        items(dateExpenses, key = { it.transactionId}) { expense ->
                        itemsIndexed(dateExpenses) { index , expense ->
                            TransactionCard(
                                modifier = Modifier.animateItem(),
                                kind = expense.kind.name,
                                amount = expense.amount.toDoubleOrNull() ?: 0.0,
//                                categoryIcon = getIcon(expense.categoryOrType, expense.kind.name)
//                                    ?: R.drawable.other,
                                categoryIcon = getEmoji(expense.categoryOrType, expense.kind.name) ?: "🧩",
                                categoryOrType = expense.categoryOrType,
                                subcategory = expense.subcategory,
                                merchant = expense.merchant,
                                note = expense.note,
                                isExpenseOverlay = true,
                                isExcluded = false,
                                onEdit = {
                                    viewModel.setCurrTransaction(expense)
                                    isShowEditOverlay = true },
                                onDuplicate = {
                                    viewModel.setCurrTransaction(expense)
                                    createDuplicate = true; isShowEditOverlay = true},
                                onExcludeFromReport = {},
                                onDeleteTransaction = {
                                    viewModel.onDeleteTransaction(
                                        transactionId = expense.transactionId,
                                        kind = expense.kind.name,
                                        amount = expense.amount.toDoubleOrNull() ?: 0.0) }
                            )

                            if(dateExpenses.size  > 1 && index != dateExpenses.size-1){
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 50.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            }

                        }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
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
                    grandTotal = totalSpent,
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
                kind = EntryKind.EXPENSE,
                amount = currentEditableTransaction.amount,
                categoryOrType = currentEditableTransaction.categoryOrType,
                currency = currentEditableTransaction.currency,
                date = currentEditableTransaction.date,
                note = currentEditableTransaction.note ?: "",
                expenseSubcategory = currentEditableTransaction.subcategory ?: "",
                expenseMerchant = currentEditableTransaction.merchant ?: "",
                isParsed = true,
            ),
            viewModel = viewModel,
            screenTitle = if(createDuplicate) "Duplicate Expense" else "Edit Expense",
            transactionKind = EntryKind.EXPENSE,
            createDuplicate = createDuplicate,
            onDismiss = { isShowEditOverlay = false; createDuplicate = false}
        )
    }
}
