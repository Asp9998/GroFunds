package com.aryanspatel.grofunds.domain.model

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Submitting : SubmitState
    data class Success(val draft: DraftRef) : SubmitState
    data class Error(val message: String) : SubmitState
}