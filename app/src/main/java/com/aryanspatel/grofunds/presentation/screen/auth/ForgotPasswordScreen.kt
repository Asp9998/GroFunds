package com.aryanspatel.grofunds.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
fun ForgotPasswordScreen(
    uiState: AuthState,
    onResetState: () -> Unit,
    onResetClick: (String) -> Unit,
    onDismiss: () -> Unit = {}
){

    var email by remember { mutableStateOf("") }

    val isFormValid = email.isNotBlank()

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(Unit) {
        onResetState() // <-- sets uiState to Idle
    }

    val context = LocalContext.current
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

    HorizontalSlidingOverlay(
        title = stringResource(R.string.reset_pass_screen_title),
        onDismiss = {
            onResetState()
            onDismiss()
        },
    ) {

        Box(Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(R.string.reset_pass_email_entering_guide) ,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.auth_email_label),
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    text = if (uiState == AuthState.PasswordResetEmailSent) stringResource(R.string.reset_pass_resend_email_button_text)
                            else stringResource(R.string.reset_pass_send_email_button_text),
                    onClick = {
                        keyboardController?.hide()
                        onResetClick(email.trim()) },
                    enabled = isFormValid,
                    cornerRadius = 50.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState == AuthState.Loading) {
                    ProgressIndicator()
                } else if(uiState == AuthState.PasswordResetEmailSent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(text = stringResource(R.string.reset_pass_check_email_message) )
                        }
                    }
                }
            }
            SnackBarMessage(
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbarHostState = snackbarHostState)
        }
    }
}