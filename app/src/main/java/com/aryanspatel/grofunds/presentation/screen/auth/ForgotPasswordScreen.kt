package com.aryanspatel.grofunds.presentation.screen.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aryanspatel.grofunds.data.model.AuthState
import com.aryanspatel.grofunds.presentation.components.AuthTextField
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    uiState: AuthState,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit = {}
){

    var email by remember { mutableStateOf("") }

    val isFormValid = email.isNotBlank()

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.resetState() // <-- sets uiState to Idle
    }

    HorizontalSlidingOverlay(
        title = "Reset your password",
        onDismiss = {
            viewModel.resetState()
            onDismiss()
        },
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Enter the email address associated with your account to reset your password",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                text = if (uiState == AuthState.PasswordResetEmailSent) "Resend email" else "Continue",
                onClick = {
                    keyboardController?.hide()
                    viewModel.resetPassword(email.trim()) },
                enabled = isFormValid,
                cornerRadius = 50.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState == AuthState.Loading) {
                ProgressIndicator()
            }

            if (uiState == AuthState.PasswordResetEmailSent) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = "If an account exists with your email address, we just sent you an email with instructions to reset your password." )
                    }
                }
            }
        }
    }
}