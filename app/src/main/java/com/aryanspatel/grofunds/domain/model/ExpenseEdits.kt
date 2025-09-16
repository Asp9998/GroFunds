package com.aryanspatel.grofunds.domain.model

data class ExpenseEdits(
    val amount: Double,
    val currency: String,      // "CAD"
    val category: String,
    val subcategory: String,
    val merchant: String?,     // optional
    val dateText: String,      // "yyyy-MM-dd"
    val note: String?         // optional
)
