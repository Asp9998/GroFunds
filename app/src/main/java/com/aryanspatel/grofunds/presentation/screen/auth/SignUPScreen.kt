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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.AuthState
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.ProgressIndicator
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import androidx.compose.material3.SnackbarHostState
import com.aryanspatel.grofunds.presentation.components.SnackBarMessage


@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    uiState: AuthState,
    onSighUpClick: (email: String, password: String, preferredName: String) -> Unit,
    onDismiss: () -> Unit = {}) {


    var preferredName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
            viewModel.resetState() // sets to Idle
        }
    }

    val isPreferredNameValid = preferredName.isNotBlank()
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])[A-Za-z\\d\\p{Punct}]{8,}$")


    val guidance = when {
        preferredName.isBlank() && password.isBlank() && email.isBlank() -> context.getString(R.string.minimum_char)
        !emailRegex.matches(email) -> context.getString(R.string.enter_valid_email)
        password.isBlank() -> context.getString(R.string.password_not_empty)
        !passwordRegex.matches(password) -> context.getString(R.string.password_requirement)
        else -> "" // No issues → form is valid
    }

    val isFormValid = guidance.isEmpty() && isPreferredNameValid

    LaunchedEffect(Unit) {
        viewModel.resetState() // <-- sets uiState to Idle
    }

    /**
     *     Main UI with - preferred name , email , password , helping text and action button.
     */

    HorizontalSlidingOverlay(
        title = "Sign up",
        onDismiss = onDismiss,
    ) {

        Box(Modifier.fillMaxSize()) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(value = preferredName,
                    onValueChange = {preferredName = it},
                    label = stringResource(R.string.preferred_name)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ModernTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.auth_email_label),
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(10.dp))


                ModernTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.auth_password_label),
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        text = guidance,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    text = stringResource(R.string.signup_screen_action_button_text),
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

