package com.aryanspatel.grofunds.fakeRepo.repository

import com.aryanspatel.grofunds.data.remote.UserProfile
import com.aryanspatel.grofunds.domain.model.AuthUser
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    // Configure these in each test
    var signInResult: Result<AuthUser> =
        Result.success(AuthUser(uid = "uid-signin", email = "a@b.com", displayName = "Aryan"))
    var signUpResult: Result<AuthUser> =
        Result.success(AuthUser(uid = "uid-signup", email = "new@b.com", displayName = "New User"))
    var resetResult: Result<Unit> = Result.success(Unit)

    private val _auth = MutableStateFlow<AuthUser?>(null)

    override fun authState(): Flow<AuthUser?> = _auth
    override fun getCurrentUser(): AuthUser? = _auth.value

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return signInResult.also { result ->
            result.getOrNull()?.let { _auth.value = it }   // mirror prod behavior
        }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return signUpResult.also { result ->
            result.getOrNull()?.let { _auth.value = it }   // mirror prod behavior
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = resetResult

    override suspend fun upsertUserProfileMinimal(name: String?): Result<Unit> = Result.success(Unit)

    override fun userProfileFlowFor(uid: String): Flow<UserProfile?> =
        MutableStateFlow(null)

    override fun signOut() { _auth.value = null }
}