package com.aryanspatel.grofunds.fakeRepo.test

import app.cash.turbine.test
import com.aryanspatel.grofunds.fakeRepo.repository.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FakeAuthRepoTest(){
    @Test
    fun `signIn success updates authState` () = runTest{
        val repo = FakeAuthRepository()
        repo.signIn(email = "a@b.com", password = "x")

        repo.authState().test{
            val u = awaitItem()
            assertThat(u?.email).isEqualTo("a@b.com")
            cancelAndIgnoreRemainingEvents()

        }
    }

    @Test
    fun `signIn failure does not update authState`() = runTest{
        val repo = FakeAuthRepository().apply {
            signInResult = Result.failure(RuntimeException("invalid"))
        }

        repo.signIn("a@b.com", "bad")

        repo.authState().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOut pushes null`() = runTest {
        val repo = FakeAuthRepository()
        repo.signIn("a@b.com", "x")
        repo.signOut()
        assertThat(repo.getCurrentUser()).isNull()
    }
}