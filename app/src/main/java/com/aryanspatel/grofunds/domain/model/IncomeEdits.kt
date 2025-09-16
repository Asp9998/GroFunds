package com.aryanspatel.grofunds.domain.model

data class IncomeEdits(
    val amount: Double,
    val currency: String,      // "CAD"
    val type: String,
    val dateText: String,      // "yyyy-MM-dd"
    val note: String?         // optional
)
