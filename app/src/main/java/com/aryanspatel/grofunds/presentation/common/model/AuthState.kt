package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.painter.Painter

sealed class AuthState {
    object Idle : AuthState()                            /** No operation/state yet */

    object Loading : AuthState()                         /** Sign-up or login in progress */

    object LoggedIn : AuthState()                        /** Successfully logged in */

    object EmailAlreadyExists : AuthState()              /** User already exists (email collision on sign-up) */

    object InvalidCredentials : AuthState()              /** Invalid password or email format */

    object NoUserFound : AuthState()                     /** No account found with this email */

    object PasswordResetEmailSent: AuthState()           /** Password reset email sent successfully */

    object NetworkError : AuthState()                    /** Network error */

    data class Error(val message: String) : AuthState()  /** Generic error state with a message */
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Painter,
)