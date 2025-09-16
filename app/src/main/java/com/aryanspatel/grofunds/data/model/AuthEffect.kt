package com.aryanspatel.grofunds.data.model

sealed interface AuthEffect {
    data class Message(val text: String) : AuthEffect
    data object SignedIn : AuthEffect
}