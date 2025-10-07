package com.aryanspatel.grofunds.presentation.screen.auth

import android.util.Log
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.common.model.UserCredentials
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.components.SnackBarMessage

@Composable
fun ForgotPasswordScreen(
    userUiState: UserCredentials,
    uiState: AuthState,
    onEmailChange: (String) -> Unit,
    onResetUiState: () -> Unit,
    onConsumeMessageClick: () -> Unit,
    onResetClick: (String) -> Unit,
    onDismiss: () -> Unit = {}
){

    val email = userUiState.email
    val enable = email.isNotBlank()

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    val message = userUiState.message?.asString()

    LaunchedEffect(Unit) {
        onResetUiState()
    }

    LaunchedEffect(message) {
        Log.d("LonINMessage", "SignUp: $message")

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            onConsumeMessageClick()
        }
    }

    HorizontalSlidingOverlay(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = stringResource(R.string.reset_pass_screen_title),
        onDismiss = {
            onResetUiState()
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
                    onValueChange = { onEmailChange(it) },
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
                    enabled = enable,
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