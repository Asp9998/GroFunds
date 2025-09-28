package com.aryanspatel.grofunds

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.common.model.UiText
import com.aryanspatel.grofunds.presentation.common.model.UserCredentials
import com.aryanspatel.grofunds.presentation.screen.auth.LoginScreen
import com.google.common.truth.Truth.assertThat
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenTest{
    @get:Rule val compose = createComposeRule()

    @Test
    fun login_shows_snackbar_when_message_present() {
        val user = UserCredentials(message = UiText.Plain("Invalid credentials"))
        compose.setContent {
            LoginScreen(
                userUiState = user,
                uiState = AuthState.Idle,
                onEmailChange = {},
                onPasswordChange = {},
                onResetUiState = {},
                onConsumeMessage = {},
                onLoginClick = { _, _ -> },
                onResetClick = {},
                onDismiss = {}
            )
        }
        compose.waitForIdle()
        compose.onNodeWithText("Invalid credentials", substring = false).assertExists()
    }

    @Test
    fun login_button_disabled_when_email_or_password_blank() {
        // Email blank → UI should compute enabled = false
        val state = UserCredentials(email = "", password = "x")
        compose.setContent {
            LoginScreen(
                userUiState = state,
                uiState = AuthState.Idle,
                onEmailChange = {},
                onPasswordChange = {},
                onResetUiState = {},
                onConsumeMessage = {},
                onLoginClick = { _, _ -> },
                onResetClick = {},
                onDismiss = {}
            )
        }
        compose.onNodeWithTag("signInButton").assertIsNotEnabled()
    }

    @Test
    fun login_typing_calls_callbacks() {
        var emailArg: String? = null
        var passArg: String? = null

        compose.setContent {
            LoginScreen(
                userUiState = UserCredentials(), // both blank initially
                uiState = AuthState.Idle,
                onEmailChange = { emailArg = it },
                onPasswordChange = { passArg = it },
                onResetUiState = {},
                onConsumeMessage = {},
                onLoginClick = { _, _ -> },
                onResetClick = {},
                onDismiss = {}
            )
        }

        compose.onNodeWithTag("emailField").performTextInput("a@b.com")
        compose.onNodeWithTag("passwordField").performTextInput("anypass")

        assertThat(emailArg).isEqualTo("a@b.com")
        assertThat(passArg).isEqualTo("anypass")
    }

    @Test
    fun login_submit_calls_onSubmit_when_both_nonblank() {
        var clicked = false
        val state = UserCredentials(email = "a@b.com", password = "x") // both non-blank → enabled

        compose.setContent {
            LoginScreen(
                userUiState = state,
                uiState = AuthState.Idle,
                onEmailChange = {},
                onPasswordChange = {},
                onResetUiState = {},
                onConsumeMessage = {},
                onLoginClick = { _, _ -> clicked = true },
                onResetClick = {},
                onDismiss = {}
            )
        }

        compose.onNodeWithTag("signInButton").assertIsEnabled().performClick()
        assertThat(clicked).isTrue()
    }

}