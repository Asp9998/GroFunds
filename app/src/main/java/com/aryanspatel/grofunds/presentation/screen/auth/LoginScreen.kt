package com.aryanspatel.grofunds.presentation.screen.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.aryanspatel.grofunds.data.model.AuthState
import com.aryanspatel.grofunds.presentation.components.AuthTextField
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    uiState: AuthState,
    onLoginClick: (email: String, password: String) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordScreen by remember {mutableStateOf(false)}

    val isFormValid = email.isNotBlank() && password.isNotBlank()

    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        val text = when (uiState) {
            AuthState.EmailAlreadyExists -> "This email is already in use with another account"
            AuthState.InvalidCredentials -> "Invalid password or email."
            AuthState.NetworkError -> "Check your internet connection."
            AuthState.NoUserFound -> "No account found with this email."
            is AuthState.Error -> uiState.message
            else -> null
        }
        if (!text.isNullOrBlank()) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            viewModel.resetState() // sets to Idle
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetState() // <-- sets uiState to Idle
    }

    HorizontalSlidingOverlay(title = "", onDismiss = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "GroFunds",
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                text = "Log in",
                onClick = {
                    keyboardController?.hide()
                    onLoginClick(email.trim(), password.trim()) },
                enabled = isFormValid,
                cornerRadius = 50.dp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.clickable {
                        showForgotPasswordScreen = true
                    },
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Arrow Forward",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))















            // Loading spinner
            if (uiState == AuthState.Loading) {
                ProgressIndicator()
            }
        }
    }

    if(showForgotPasswordScreen){
        ForgotPasswordScreen(
            uiState = uiState,
            viewModel = viewModel) {
            showForgotPasswordScreen = false
        }
    }

}


