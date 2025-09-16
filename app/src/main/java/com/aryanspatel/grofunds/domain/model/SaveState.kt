package com.aryanspatel.grofunds.domain.model

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data class Success(val path: String) : SaveState
    data class Error(val message: String) : SaveState
}