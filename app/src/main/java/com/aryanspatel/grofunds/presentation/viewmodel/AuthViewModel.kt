package com.aryanspatel.grofunds.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.data.remote.model.UserProfile
import com.aryanspatel.grofunds.domain.model.AuthUser
import com.aryanspatel.grofunds.domain.port.SyncOrchestrator
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.common.model.UiText
import com.aryanspatel.grofunds.presentation.common.model.UserCredentials
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orchestrator: SyncOrchestrator,
    private val dp: DispatcherProvider
) : ViewModel() {


    private val _userUiState  = MutableStateFlow(UserCredentials())

    val userUiState: StateFlow<UserCredentials> = _userUiState

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])[A-Za-z\\d\\p{Punct}]{8,}$")

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    /** Reactive current Firebase user (null when signed out). */
    val user: StateFlow<AuthUser?> =
        authRepository.authState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = authRepository.getCurrentUser()
            )

    /** Reactive UserProfile for the current user (null when signed out or not created yet). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<UserProfile?> =
        user
            .flatMapLatest { u ->
                if (u == null) flowOf(null) else authRepository.userProfileFlowFor(u.uid)
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )



    init {
        viewModelScope.launch {
            _uiState.collect {state ->
                _userUiState.update{ cur ->
                    cur.copy(authState = state,
                        message = state.toUiMessage())

                }

            }
        }
    }

    fun onPreferredNameChange(name: String){
        _userUiState.update { it.copy(name = name) }
        recomputeFormHintsAndEnabled()
    }

    fun onPasswordChange(password: String){
        _userUiState.update { it.copy(password = password) }
        recomputeFormHintsAndEnabled()
    }

    fun onEmailChange(email: String){
        _userUiState.update { it.copy(email = email) }
        recomputeFormHintsAndEnabled()
    }

    fun resetUiState(){
        _userUiState.update { UserCredentials.initial() }
        _uiState.value = AuthState.Idle
    }

    private fun recomputeFormHintsAndEnabled() {
        val s = _userUiState.value
        val guidance: UiText? = when {
            s.name.isBlank() && s.password.isBlank() && s.email.isBlank() ->
                UiText.Resource(R.string.minimum_char)
            !emailRegex.matches(s.email) ->
                UiText.Resource(R.string.enter_valid_email)
            s.password.isBlank() ->
                UiText.Resource(R.string.password_not_empty)
            !passwordRegex.matches(s.password) ->
                UiText.Resource(R.string.password_requirement)
            else -> null
        }
        val enabled = guidance == null
        _userUiState.update { it.copy(guidance = guidance, enabled = enabled) }
    }

    /** Call this after the Snackbar is shown so it doesn't re-trigger. */
    fun consumeMessage() {
        _userUiState.update { it.copy(message = null) }
    }


    /** -------- Auth State ---------------- */


    fun signIn(email: String, password: String) = viewModelScope.launch(dp.iO) {
        _uiState.value = AuthState.Loading

        authRepository.signIn(email, password)
            .onSuccess {
                // Try to upsert profile; don't block sign-in if this fails
                authRepository.upsertUserProfileMinimal()
                    .onFailure {
                        _uiState.value = AuthState.Error(message = it.message ?: "Profile update failed") }
                _uiState.value = AuthState.LoggedIn

            }
            .onFailure { e ->
                _uiState.value = mapErrorToState(e)
            }
    }

    fun signUp(email: String, password: String, preferredName: String) = viewModelScope.launch(dp.iO) {
        _uiState.value = AuthState.Loading
        authRepository.signUp(email, password)
            .onSuccess {
                val nameOverride = preferredName.takeIf { it.isNotBlank() }
                authRepository.upsertUserProfileMinimal(nameOverride)
                    .onFailure {
                        _uiState.value = AuthState.Error(message = it.message ?: "Profile save failed") }
                _uiState.value = AuthState.LoggedIn
            }
            .onFailure { e ->
                _uiState.value = mapErrorToState(e)
            }
    }

    fun resetPassword(email: String) = viewModelScope.launch(dp.iO) {
        _uiState.value = AuthState.Loading
        authRepository.sendPasswordResetEmail(email)
            .onSuccess { _uiState.value = AuthState.PasswordResetEmailSent }
            .onFailure { e ->
                _uiState.value = mapErrorToState(e)
            }
    }

    fun signOut() = viewModelScope.launch{
        val pushed = orchestrator.pushAllDirtyAndWait()
        if(!pushed){
            Log.d("SingOutException", "signOut: Pushed failed, cannot sign out")
            return@launch
        }
        authRepository.signOut()
     }


    /** ------ Map AuthState → user-facing message (no Context) --------  */
    fun AuthState.toUiMessage(): UiText? = when (this) {
        AuthState.Idle -> null
        AuthState.Loading -> null
        AuthState.LoggedIn -> null
        AuthState.PasswordResetEmailSent -> null
        AuthState.EmailAlreadyExists -> UiText.Resource(R.string.email_already_in_use)
        AuthState.InvalidCredentials -> UiText.Resource(R.string.invalid_email_password)
        AuthState.NoUserFound -> UiText.Resource(R.string.no_account_found)
        AuthState.NetworkError -> UiText.Resource(R.string.no_internet_connect)
        is AuthState.Error -> UiText.Plain(this.message)
    }
}

private fun mapErrorToState(e: Throwable): AuthState = when (e) {
    is FirebaseAuthInvalidCredentialsException -> AuthState.InvalidCredentials
    is FirebaseAuthInvalidUserException        -> AuthState.NoUserFound
    is FirebaseAuthUserCollisionException      -> AuthState.EmailAlreadyExists
    is FirebaseNetworkException                -> AuthState.NetworkError
    else                                       -> AuthState.Error(e.message ?: "Unknown error")
}