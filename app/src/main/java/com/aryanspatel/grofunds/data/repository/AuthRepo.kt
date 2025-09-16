package com.aryanspatel.grofunds.data.repository

import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.common.awaitIo


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val dp: DispatcherProvider
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            withTimeout(15_000) {
                auth.signInWithEmailAndPassword(email, password)
                    .awaitIo(dp)                // IO thread, suspends until done
                    .user ?: error("No user in AuthResult")
            }
        }.mapError()

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            withTimeout(15_000) {
                auth.createUserWithEmailAndPassword(email, password)
                    .awaitIo(dp)
                    .user ?: error("No user in AuthResult")
            }
        }.mapError()

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching {
            withTimeout(15_000) {
                auth.sendPasswordResetEmail(email).awaitIo(dp)
                Unit
            }
        }.mapError()

    /** Reactive auth state for your UI (login/logout). */
    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser).isSuccess }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

/** Optional: friendlier error messages for the UI layer. */
private fun <T> Result<T>.mapError(): Result<T> = this
