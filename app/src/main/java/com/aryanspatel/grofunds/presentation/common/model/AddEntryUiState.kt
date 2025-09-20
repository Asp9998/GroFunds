package com.aryanspatel.grofunds.presentation.common.model

import com.aryanspatel.grofunds.domain.model.DraftRef
import com.aryanspatel.grofunds.domain.model.EntryKind

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

) {
    companion object {
        fun initial(kind: EntryKind) = AddEntryUiState(kind = kind)
    }
}

/**
 * Submit State is about storing the input note to the firebase
 */
sealed interface SubmitState {
    data object Idle : SubmitState
    data object Submitting : SubmitState
    data class Success(val draft: DraftRef) : SubmitState
    data class Error(val message: String) : SubmitState
}

/**
 * Save State is about Storing the final Details into firebase
 */
sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data class Success(val path: String) : SaveState
    data class Error(val message: String) : SaveState
}
