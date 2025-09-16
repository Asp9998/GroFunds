package com.aryanspatel.grofunds.domain.model

sealed interface ParseState {
    data class Pending(val note: String?) : ParseState
    data class Ready(val data: Map<String, Any?>) : ParseState
    data class Error(val message: String) : ParseState
}