package com.aryanspatel.grofunds.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.expenseCategoryById
import com.aryanspatel.grofunds.domain.usecase.incomeTypeById
import com.aryanspatel.grofunds.domain.usecase.resolveExpenseCategoryIds
import com.aryanspatel.grofunds.presentation.common.model.Kind
import com.aryanspatel.grofunds.presentation.common.model.Transaction
import com.aryanspatel.grofunds.presentation.common.model.TransactionUiState
import com.aryanspatel.grofunds.domain.usecase.resolveExpenseCategoryLabels
import com.aryanspatel.grofunds.domain.usecase.resolveIncomeTypeId
import com.aryanspatel.grofunds.domain.usecase.resolveIncomeTypeLabel
import com.aryanspatel.grofunds.domain.usecase.subcategoriesFor
import com.aryanspatel.grofunds.presentation.common.model.CategorySlice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShowTransactionViewModel @Inject constructor(
    private val repo: AddEntryTransactionRepository,
    private val dp: DispatcherProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    /** 1) Determine KIND from nav arg (fallback to EXPENSES) */
    private val kind: Kind = when (savedStateHandle.get<String>("kind")?.lowercase()) {
        "income", "incomes" -> Kind.INCOME
        else -> Kind.EXPENSE
    }

    /** 2) Month State (Default to current month) */
    private val _month = MutableStateFlow(YearMonth.now(ZoneId.systemDefault()))
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    /** 3) Category Filter (empty = all) */
    private val _categoryIds = MutableStateFlow<Set<String>>(emptySet())
    val categoryIds:StateFlow<Set<String>> = _categoryIds.asStateFlow()

    /** 4) Derived date range for current month */
    private val dateRange: Flow<Pair<Long, Long>> =
        _month.map { ym ->
            val start = ym.startOfMonthMillis()
            val end = ym.endExclusiveMillis()
            start to end
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionUiState> =
        combine(dateRange, _categoryIds) { (start, end), cats ->
            Triple(start, end, cats)
        }
            .flatMapLatest { (start, end, cats) ->
                repo.observeTransactions(kind = kind.name, startDate = start, endDate = end, listOfCategory = cats.toList())
                    .map { entities ->
                        val domain: List<Transaction> = entities.map{ it.toDomain() }
                        TransactionUiState(
                            kind = kind,
                            month = _month.value,
                            startDate = start,
                            endDate = end,
                            categoryIds = cats,
                            items = domain,
                            loading = false,
                            error = null
                        )
                    }
                    .onStart {
                        emit(
                            TransactionUiState(
                                kind = kind,
                                month = _month.value,
                                startDate = start,
                                endDate = end,
                                categoryIds = cats,
                                items = emptyList(),
                                loading = true,
                                error = null
                            )
                        )
                    }
                    .catch { e ->
                        emit(
                            TransactionUiState(
                                kind = kind,
                                month = _month.value,
                                startDate = start,
                                endDate = end,
                                categoryIds = cats,
                                items = emptyList(),
                                loading = false,
                                error = e.message
                            )
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionUiState(
                    kind = kind,
                    month = _month.value,
                    startDate = _month.value.startOfMonthMillis(),
                    endDate = _month.value.endExclusiveMillis(),
                    categoryIds = emptySet(),
                    items = emptyList(),
                    loading = true
                )
            )

    /** 6) Category wise sorting functions */
    fun toggleCategory(id: String) {
        _categoryIds.update { set -> if (id in set) set - id else set + id }
    }
    fun clearCategories(){
        _categoryIds.update { emptySet() }
    }

    /** 7) Month wise sorting functions */
    fun onMonthChange(yearMonth: YearMonth){
        _month.value = yearMonth
    }

    /** 8) Daily Average calculator */
    fun dailyAverageForMonth(
        totalForMonthToDate: Double,
        month: YearMonth,
    ): Double {
        val today = LocalDate.now()
        val daysDivisor =
            if (YearMonth.from(today) == month) today.dayOfMonth
            else month.lengthOfMonth()

        return if (daysDivisor > 0) totalForMonthToDate / daysDivisor else 0.0
    }

    private val kindFlow = uiState.map { it.kind }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryTotalList: StateFlow<List<CategorySlice>> =
        combine(kindFlow, _month) { kind, month ->
            kind.name to month
        }
            .flatMapLatest { (kindName, month) ->
                repo.observeCategoryTotal(
                    kind = kindName,
                    startDate = month.startOfMonthMillis(),
                    endDate = month.endExclusiveMillis()
                )
            }
            .map { list ->
                list.map { ct ->
                    val meta = when(kind){
                        Kind.EXPENSE -> expenseCategoryById[ct.categoryId]
                        else -> incomeTypeById[ct.categoryId]
                    }
                    CategorySlice(
                        kind = kind,
                        name = meta?.name ?: "",
                        amount = ct.totalAmount,
                        color = meta?.color ?: Color.Gray,
                        iconRes = meta?.iconRes ?: R.drawable.other
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    /** Current transaction save, update and delete functionality */

    private val _currentTransaction = MutableStateFlow(Transaction(
        userId = "",
        id = "",
        kind = Kind.EXPENSE,
        amount = 0.0,
        currency = "",
        categoryOrType = "",
        subcategory = "",
        merchant = "",
        note = "",
        date = "",
        createdAt = 0L,
    ))
    val currentTransaction: StateFlow<Transaction> = _currentTransaction.asStateFlow()

    fun onAmountUpdate(amount: String) = _currentTransaction.update { it.copy(amount = amount.toDouble()) }
    fun onCurrencyUpdate(currency: String) = _currentTransaction.update { it.copy(currency = currency)}

    fun onCategoryOrTypeUpdate(v: String) {
        _currentTransaction.update { it.copy(categoryOrType = v) }
        if(uiState.value.kind == Kind.EXPENSE) {
            onSubCategoryUpdate(subcategoriesFor(v)[0])
        }
    }
    fun onSubCategoryUpdate(subCate: String) = _currentTransaction.update { it.copy(subcategory = subCate) }
    fun onMerchantUpdate(merchant : String) = _currentTransaction.update { it.copy(merchant = merchant) }
    fun onDateUpdate(date: String) = _currentTransaction.update { it.copy(date = date) }
    fun onNoteUpdate(note: String) = _currentTransaction.update { it.copy(note = note) }

    fun setCurrTransaction(transaction: Transaction){
        _currentTransaction.value = transaction
    }

    // for edit transaction, update the existing transaction
    fun onSaveTransaction(isCreateDuplicate: Boolean){
        viewModelScope.launch {
        val uuid = UUID.randomUUID().toString()
        val curr = _currentTransaction.value
        val res = when(curr.kind){
            Kind.EXPENSE -> resolveExpenseCategoryIds(curr.categoryOrType, curr.subcategory)
            else -> resolveIncomeTypeId(curr.categoryOrType)
        }
        val dateMillis: Long = withContext(dp.default) {
            curr.date.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()
        }
        val now = System.currentTimeMillis()
        repo.saveTransaction(TransactionEntity(
            transactionID = if(isCreateDuplicate) uuid else curr.id,
            userId = curr.userId,
            kind = curr.kind.name,
            amount = curr.amount,
            currencyCode = curr.currency,
            categoryOrTypeID = res.categoryId,
            subcategoryID = res.subcategoryId,
            merchant = curr.merchant ,
            note = curr.note,
            date = dateMillis,
            createdAtUTC = curr.createdAt,
            localeUpdatedAt = now,
            isDirty = true,
            isDeleted = false,
        ))
    }
    }

    // for duplicate transaction, add new one for specific date



}

private fun TransactionEntity.toDomain(): Transaction{
    val res = when(kind){
        Kind.EXPENSE.name -> resolveExpenseCategoryLabels(categoryOrTypeID, subcategoryID)
        else -> resolveIncomeTypeLabel(categoryOrTypeID)
    }
    return Transaction(
        userId = userId,
        id = transactionID,
        kind = if (kind == Kind.INCOME.name) Kind.INCOME else Kind.EXPENSE,
        amount = amount,
        currency = currencyCode,
        categoryOrType = res.categoryId,  // return category label
        subcategory = res.subcategoryId,  // return subcategory label
        merchant = merchant,
        note = note,
        date = DateConverters.millisToString(date),
        createdAt = createdAtUTC
    )
}


private val APP_ZONE: ZoneId = ZoneId.systemDefault() // or use systemDefault()

/** Start of this YearMonth in millis (00:00:00.000 at local zone). */
fun YearMonth.startOfMonthMillis(zone: ZoneId = APP_ZONE): Long =
    atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

/** Start of next month in millis — use this as end-exclusive bound. */
fun YearMonth.endExclusiveMillis(zone: ZoneId = APP_ZONE): Long =
    plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

fun millisToLocalDate(millis: Long, zone: ZoneId = APP_ZONE): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
