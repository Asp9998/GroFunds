package com.aryanspatel.grofunds.domain.repository

import com.aryanspatel.grofunds.data.remote.model.UserProfile
import com.aryanspatel.grofunds.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun authState(): Flow<AuthUser?>
    fun getCurrentUser(): AuthUser?
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun upsertUserProfileMinimal(name: String? = null): Result<Unit>
    fun userProfileFlowFor(uid: String): Flow<UserProfile?>
    fun signOut()
}