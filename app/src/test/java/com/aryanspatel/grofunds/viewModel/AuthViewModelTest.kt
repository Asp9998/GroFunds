package com.aryanspatel.grofunds.viewModel

import app.cash.turbine.test
import com.aryanspatel.grofunds.domain.model.AuthUser
import com.aryanspatel.grofunds.fakeRepo.repository.FakeAuthRepository
import com.aryanspatel.grofunds.presentation.common.model.AuthState
import com.aryanspatel.grofunds.presentation.viewmodel.AuthViewModel
import com.aryanspatel.grofunds.util.MainDispatcher
import com.aryanspatel.grofunds.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest{

    @get:Rule
    val mainRule = MainDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var dp: TestDispatcherProvider
    private lateinit var vm: AuthViewModel

    @Before
    fun setUp(){
        repo = FakeAuthRepository()
        dp  = TestDispatcherProvider(mainRule.dispatcher)
        vm = AuthViewModel(authRepository = repo, dp = dp)
    }

    @Test
    fun `Initial state is Idle and disabled`() = runTest{
        val s = vm.userUiState.value
        val s2 = vm.uiState.value
        assertThat(s2).isEqualTo(AuthState.Idle)
        assertThat(s.authState).isEqualTo(AuthState.Idle)
        assertThat(s.enabled).isFalse()
        assertThat(s.message).isNull()
    }

    @Test
    fun `valid form enables button`() = runTest {
        vm.onEmailChange("a@b.com")
        vm.onPasswordChange("AbDGef1!")
        advanceUntilIdle()

        val s = vm.userUiState.value
        assertThat(s.enabled).isTrue()
        assertThat(s.guidance).isNull()
    }

    @Test
    fun `invalid email shows guidance and disable button`() = runTest {
        vm.onEmailChange("bad-email")
        vm.onPasswordChange("AbDGef1!")
        advanceUntilIdle()

        val s  = vm.userUiState.value
        assertThat(s.enabled).isFalse()
        assertThat(s.guidance).isNotNull()
    }

    @Test
    fun `signIn success sets LoggedIn and clears message`() = runTest {
        repo.signInResult = Result.success(AuthUser("uid", "a@b.com", "Aryan"))

        vm.signIn("a@b.com", "Abcdef1!")
        advanceUntilIdle()

        val s = vm.userUiState.value
        assertThat(s.authState).isEqualTo(AuthState.LoggedIn)
        assertThat(s.message).isNull()
    }

    @Test
    fun `signIn failure sets message (generic)`() = runTest {
        repo.signInResult = Result.failure(RuntimeException("invalid"))

        vm.signIn("a@b.com", "wrong")
        advanceUntilIdle()

        val s = vm.userUiState.value
        // depending on your mapErrorToState, this might be AuthState.Error
        assertThat(s.message).isNotNull()

        vm.consumeMessage()
        advanceUntilIdle()
        assertThat(vm.userUiState.value.message).isNull()
    }

    @Test
    fun `password reset success sets PasswordResetEmailSent`() = runTest {
        repo.resetResult = Result.success(Unit)

        vm.resetPassword("a@b.com")
        advanceUntilIdle()

        assertThat(vm.userUiState.value.authState).isEqualTo(AuthState.PasswordResetEmailSent)
    }

    @Test
    fun `resetUiState clears fields and returns to Idle`() = runTest {
        vm.onEmailChange("a@b.com")
        vm.onPasswordChange("Abcdef1!")
        vm.resetUiState()
        advanceUntilIdle()

        val s = vm.userUiState.value
        assertThat(s.email).isEmpty()
        assertThat(s.password).isEmpty()
        assertThat(s.authState).isEqualTo(AuthState.Idle)
    }

    // Turbine test for user flow end-to-end
    @Test
    fun `user flow updates after signIn`() = runTest {
        repo.authState().test {
            assertThat(awaitItem()).isNull() // initial
            repo.signIn("a@b.com", "x")
            val u = awaitItem()
            assertThat(u?.email).isEqualTo("a@b.com")
            cancelAndIgnoreRemainingEvents()
        }
    }

}