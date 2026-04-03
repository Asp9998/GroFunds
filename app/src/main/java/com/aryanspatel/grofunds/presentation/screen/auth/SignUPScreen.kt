package com.aryanspatel.grofunds.presentation.screen.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import androidx.compose.material3.SnackbarHostState
import com.aryanspatel.grofunds.presentation.common.model.UiText
import com.aryanspatel.grofunds.presentation.common.model.UserCredentials
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.SnackBarMessage


@Composable
fun SignUpScreen(
    uiState: AuthState,
    userUiState: UserCredentials,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    resetUiState: () -> Unit,
    onConsumeMessage: () -> Unit,
    onSighUpClick: (email: String, password: String, preferredName: String) -> Unit,
    onDismiss: () -> Unit = {}) {

    // UI STATE
    val preferredName = userUiState.name
    val email = userUiState.email
    val password = userUiState.password
    val enable = userUiState.enabled

    // Local Helper State
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    val message = userUiState.message?.asString()

    LaunchedEffect(Unit) {
        resetUiState()
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            onConsumeMessage()
        }
    }

    /**
     *     Main UI with - preferred name , email , password , helping text and action button.
     */

    HorizontalSlidingOverlay(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = "Sign up",
        onDismiss = onDismiss,
    ) {

        Box(Modifier.fillMaxSize()) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(value = preferredName,
                    onValueChange = {onNameChange(it)},
                    label = stringResource(R.string.preferred_name)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(
                    value = email,
                    onValueChange = { onEmailChange(it)},
                    label = stringResource(R.string.auth_email_label),
                    keyboardType = KeyboardType.Email,
                    isFirstLetterCapital = false
                )
                Spacer(modifier = Modifier.height(10.dp))


                ModernTextField(
                    value = password,
                    onValueChange = {onPasswordChange(it)},
                    label = stringResource(R.string.auth_password_label),
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isFirstLetterCapital = false
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        text = userUiState.guidance?.asString() ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ModernButton(
                    text = stringResource(R.string.signup_screen_action_button_text),
                    onClick = {
                        onSighUpClick(email.trim(), password.trim(), preferredName.trim())
                        keyboardController?.hide()
                    },
                    enabled = enable,
                    cornerRadius = 50.dp
                )

                Spacer(modifier = Modifier.height(25.dp))

                // Loading spinner
                if (uiState == AuthState.Loading) {
                    ProgressIndicator()
                }
            }

            // “Floating” snackbar anchored to bottom-center
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding() // avoid system bars
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                )
            }

            SnackBarMessage(
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbarHostState = snackbarHostState)
        }
    }
}

@Composable fun UiText.asString(): String {
    val ctx = LocalContext.current
    return when (this) {
        is UiText.Resource -> ctx.getString(resId, *args.toTypedArray())
        is UiText.Plain -> value
    }
}

