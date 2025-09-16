package com.aryanspatel.grofunds.data.model

sealed class AuthState {

    /** No operation/state yet */
    object Idle : AuthState()

    /** Sign-up or login in progress */
    object Loading : AuthState()

    /** Successfully logged in */
    object LoggedIn : AuthState()

    /** User already exists (email collision on sign-up) */
    object EmailAlreadyExists : AuthState()

    /** Invalid password or email format */
    object InvalidCredentials : AuthState()

    /** No account found with this email */
    object NoUserFound : AuthState()

    /** Password reset email sent successfully */
    object PasswordResetEmailSent: AuthState()

    /** Network error */
    object NetworkError : AuthState()

    /** Generic error state with a message */
    data class Error(val message: String) : AuthState()
}