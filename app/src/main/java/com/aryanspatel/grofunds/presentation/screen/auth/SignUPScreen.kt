package com.aryanspatel.grofunds.presentation.screen.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aryanspatel.grofunds.data.model.AuthState
import com.aryanspatel.grofunds.presentation.components.AuthTextField
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel


@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    uiState: AuthState,
    onSighUpClick: (email: String, password: String, preferredName: String) -> Unit,
    onDismiss: () -> Unit = {}) {


    var preferredName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

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

    val isPreferredNameValid = preferredName.isNotBlank()
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])[A-Za-z\\d\\p{Punct}]{8,}$")


    val guidance = when {
        preferredName.isBlank() && password.isBlank() && email.isBlank() -> "Minimum 8 characters."
        !emailRegex.matches(email) -> "Please enter a valid email address."
        password.isBlank() -> "Password cannot be empty."
        !passwordRegex.matches(password) ->
            "Password must be at least 8 characters, include a number, a letter, a special character and no space."
        else -> "" // No issues → form is valid
    }

    val isFormValid = guidance.isEmpty() && isPreferredNameValid

    LaunchedEffect(Unit) {
        viewModel.resetState() // <-- sets uiState to Idle
    }

    HorizontalSlidingOverlay(
        title = "Sign up",
        onDismiss = onDismiss,
    ) {
//        Surface { }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AuthTextField(
                value = preferredName,
                onValueChange = { preferredName = it },
                label = "Preferred Name"
            )
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp)
            ) {
                Text(
                    text = guidance,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSecondary)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                text = "Create an account",
                onClick = {
                    onSighUpClick(email.trim(), password.trim(), preferredName.trim())
                    keyboardController?.hide()
                },
                enabled = isFormValid,
                cornerRadius = 50.dp
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Loading spinner
            if (uiState == AuthState.Loading) {
                ProgressIndicator()
            }
        }
    }
}

