package com.aryanspatel.grofunds.presentation.screen.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.components.SnackBarMessage

@Composable
fun LoginScreen(
    uiState: AuthState,
    onResetState: () -> Unit,
    onLoginClick: (email: String, password: String) -> Unit,
    onResetClick: (email: String) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordScreen by remember {mutableStateOf(false)}

    val isFormValid = email.isNotBlank() && password.isNotBlank()

    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(uiState) {
        val message = when (uiState) {
            AuthState.EmailAlreadyExists -> context.getString(R.string.email_already_in_use)
            AuthState.InvalidCredentials -> context.getString(R.string.invalid_email_password)
            AuthState.NetworkError -> context.getString(R.string.no_internet_connect)
            AuthState.NoUserFound -> context.getString(R.string.no_account_found)
            is AuthState.Error -> uiState.message
            else -> null
        }
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            onResetState() // sets to Idle
        }
    }

    LaunchedEffect(Unit) {
        onResetState() // <-- sets uiState to Idle
    }


    /**
     *     Main UI with - email , password and action button and forgot password option.
     */

    HorizontalSlidingOverlay(title = "", onDismiss = onDismiss) {

        Box(Modifier.fillMaxSize()) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = stringResource(R.string.login_screen_logo_text),
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(30.dp))

                ModernTextField(
                    value = email,
                    onValueChange = {email = it},
                    label =  stringResource(R.string.auth_email_label),
                    keyboardType = KeyboardType.Email,
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(value = password,
                    onValueChange = {password = it},
                    label = stringResource(R.string.auth_password_label),
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    text = stringResource(R.string.onboarding_login_button_text),
                    onClick = {
                        keyboardController?.hide()
                        onLoginClick(email.trim(), password.trim()) },
                    enabled = isFormValid,
                    cornerRadius = 50.dp
                )

                Spacer(modifier = Modifier.height(30.dp))

                    TextButton (onClick = { showForgotPasswordScreen = true},
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)){

                        Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.forgot_password_button_text))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow Forward",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )}
                    }


                Spacer(modifier = Modifier.height(25.dp))

                // Loading spinner
                if (uiState == AuthState.Loading) {
                    ProgressIndicator()
                }
            }

            SnackBarMessage(
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbarHostState = snackbarHostState)

        }
    }

    if(showForgotPasswordScreen){
        ForgotPasswordScreen(
            uiState = uiState,
            onResetState = onResetState,
            onResetClick = {onResetClick(it)}
        ) {
            showForgotPasswordScreen = false
        }
    }

}


