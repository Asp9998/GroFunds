package com.aryanspatel.grofunds.presentation.common.model

import com.aryanspatel.grofunds.presentation.common.ParseState

data class AddEntryUiState(
    val kind: EntryKind = EntryKind.EXPENSE,

    // common State Field
    val inputNote: String = "",
    val amount: String = "",
    val categoryOrType: String = "",
    val currency: String = "CAD",
    val date: String = "Today",
    val note: String = "",

    // expense State only
    val expenseSubcategory: String = "",
    val expenseMerchant: String = "",

    // goal State only
    val goalTitle: String = "",
    val goalDueDate: String = "",
    val goalStartAmount: String = "",

    // draft/parse
    val draftId: String? = null,
    val docPath: String? = null,
    val isParsed: Boolean = false,
    val parsePreview: String? = null,
    val parseError: String? = null,

    // save
    val isSaving: Boolean = false
) {
    companion object {
        val INITIAL = AddEntryUiState()
    }
}

enum class EntryKind {
    EXPENSE,
    INCOME,
    GOAL
}
