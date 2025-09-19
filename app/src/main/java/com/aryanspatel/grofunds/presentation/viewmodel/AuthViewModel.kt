package com.aryanspatel.grofunds.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.data.remote.UserProfile
import com.aryanspatel.grofunds.data.repository.AuthRepository
import com.aryanspatel.grofunds.data.repository.UserRepository
import com.aryanspatel.grofunds.presentation.common.AuthState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dp: DispatcherProvider
) : ViewModel() {

    /** Reactive current Firebase user (null when signed out). */
    val user: StateFlow<FirebaseUser?> =
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
                if (u == null) flowOf(null) else userRepository.userProfileFlowFor(u.uid)
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    fun signIn(email: String, password: String) = viewModelScope.launch(dp.iO) {
        _uiState.value = AuthState.Loading

        authRepository.signIn(email, password)
            .onSuccess {
                // Try to upsert profile; don't block sign-in if this fails
                userRepository.upsertUserProfileMinimal()
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
                userRepository.upsertUserProfileMinimal(nameOverride)
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

    fun signOut() {
        authRepository.signOut()
        // user/profile flows will emit null automatically
    }


    fun resetState() { _uiState.value = AuthState.Idle }
}

private fun mapErrorToState(e: Throwable): AuthState = when (e) {
    is FirebaseAuthInvalidCredentialsException -> AuthState.InvalidCredentials
    is FirebaseAuthInvalidUserException        -> AuthState.NoUserFound
    is FirebaseAuthUserCollisionException      -> AuthState.EmailAlreadyExists
    is FirebaseNetworkException                -> AuthState.NetworkError
    else                                       -> AuthState.Error(e.message ?: "Unknown error")
}