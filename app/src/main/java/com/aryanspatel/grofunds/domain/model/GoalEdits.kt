package com.aryanspatel.grofunds.domain.model

data class GoalEdits (
    val title: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val dueDate: String,
    val startAmount: Double?,
    val dateText: String,      // "yyyy-MM-dd"
    val note: String?,

)