package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.Color
import java.time.YearMonth

enum class Kind { EXPENSE, INCOME }

data class TransactionUiState(
    val kind: Kind,
    val month: YearMonth,
    val startDate: Long,
    val endDate: Long,
    val categoryIds: Set<String>,
    val items: List<Transaction>,
    val loading: Boolean = false,
    val error: String? = null,
    val displayTotal: Double? = null
)

data class Transaction(
    val userId: String,
    val transactionId: String,
    val input: String,
    val kind: Kind,
    val amount: String,
    val currency: String,
    val categoryOrType: String,
    val subcategory: String? = null,
    val merchant: String? = null,
    val note: String? = null,
    val date: String,
    val createdAt: Long,
    val remoteUpdatedAt: Long,
    val localUpdatedAt: Long,
    val isExcluded: Boolean = false
)

/** For chart showcase*/
data class CategorySlice(
    val kind: Kind,
    val name: String,
    val amount: Double,
    val budget: Double? = 100.0,
    val lastMonthSpent: Double? = 200.0,
    val compareToLastMonth: Int? = null,
    val color: Color ,
    val iconRes: Int,
    val emoji: String? = null,
)