package com.aryanspatel.grofunds.domain.model

//data class ParsedEntry(
//    val amount: Double? = null,
//    val currency: String? = null,
//    val categoryOrType: String? = null,
//    val dateText: String? = null, // "yyyy-MM-dd"
//    val notes: String? = null,
//    val confidence: Double? = null,
//
//    // Expense only
//    val subcategory: String? = null,
//    val merchant: String? = null,
//
//    // Goal only
//    val title: String? = null,
//    val dueDate: String? = null,
//    val startDate: String? = null
//
//
//)

sealed class ParsedEntry {
    data class Expense(
        val amount: Double?,
        val currency: String?,
        val category: String?,
        val subcategory: String?,
        val merchant: String?,
        val dateText: String?,
        val notes: String?,
        val confidence: Double?
    ) : ParsedEntry()

    data class Income(
        val amount: Double?,
        val currency: String?,
        val type: String?,     // optional income type/category
        val dateText: String?,
        val notes: String?,
        val confidence: Double?
    ) : ParsedEntry()

    data class Goal(
        val title: String?,         // goal name/title
        val type: String?,
        val amount: Double?, // target goal amount
        val startAmount: Double?,// progress so far (optional)
        val currency: String?,
        val dueDateText: String?,  // normalized date (yyyy-MM-dd)
        val notes: String?,
        val confidence: Double?
    ) : ParsedEntry()
}