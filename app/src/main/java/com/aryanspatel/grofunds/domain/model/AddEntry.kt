package com.aryanspatel.grofunds.domain.model

import androidx.compose.ui.graphics.Color


enum class EntryKind {
    EXPENSE,
    INCOME,
    GOAL
}

data class DraftRef(
    val id: String,
    val path: String,
    val kind: EntryKind
)

/**
 * Parse State is about getting details from the firebase after submission of the input note
 */
sealed interface ParseState {
    data class Pending(val note: String?) : ParseState
    data class Ready(val data: Map<String, Any?>) : ParseState
    data class Error(val message: String) : ParseState
}

sealed class ParsedEntry {
    data class Expense(
        val kind: String?,
        val input: String?,
        val amount: Double?,
        val currency: String?,
        val category: String?,
        val subcategory: String?,
        val merchant: String?,
        val dateText: String?,
        val notes: String?,
        val confidence: Double? = null
    ) : ParsedEntry()

    data class Income(
        val kind: String?,
        val input: String?,
        val amount: Double?,
        val currency: String?,
        val type: String?,
        val dateText: String?,
        val notes: String?,
        val confidence: Double? = null
    ) : ParsedEntry()

    data class Goal(
        val input: String?,
        val title: String?,
        val type: String?,
        val amount: Double?,
        val startAmount: Double?,
        val currency: String?,
        val dueDate: String?,
        val dateText: String?,
        val notes: String?,
        val confidence: Double? = null
    ) : ParsedEntry()
}

data class CategoryResolution(
    val categoryId: String,
    val subcategoryId: String? = null,
)

/**
 *  category seed for category id - ids get stored in database, not labels
 */
data class CategorySeed(
    val id: String,
    val name: String,            // display (en-CA default)
    val parentId: String? = null,
    val iconRes: Int? = null,
    val color: Color? = null
)