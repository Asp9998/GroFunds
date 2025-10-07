package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.Color
import java.time.YearMonth

enum class Kind { EXPENSE, INCOME }

data class Transaction(
    val userId: String,
    val id: String,
    val kind: Kind,
    val amount: Double,
    val currency: String,
    val categoryOrType: String,
    val subcategory: String? = null,
    val merchant: String? = null,
    val note: String? = null,
    val date: String,
    val createdAt: Long

)

data class TransactionUiState(
    val kind: Kind,
    val month: YearMonth,
    val startDate: Long,
    val endDate: Long,
    val categoryIds: Set<String>,
    val items: List<Transaction>,
    val loading: Boolean = false,
    val error: String? = null
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
    val iconRes: Int
)