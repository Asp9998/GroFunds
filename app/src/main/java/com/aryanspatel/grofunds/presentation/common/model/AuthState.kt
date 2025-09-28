package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.painter.Painter

data class UserCredentials(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val enabled: Boolean = false,
    val authState: AuthState = AuthState.Idle,
    val message: UiText? = null,
    val guidance: UiText? = null
){
    companion object {
        fun initial() = UserCredentials()
    }
}

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

sealed class UiText {
    data class Resource(@androidx.annotation.StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText()
    data class Plain(val value: String) : UiText()
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Painter,
)