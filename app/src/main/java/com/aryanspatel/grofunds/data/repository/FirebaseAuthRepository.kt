package com.aryanspatel.grofunds.data.repository

import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.core.awaitIo
import com.aryanspatel.grofunds.data.remote.model.UserProfile
import com.aryanspatel.grofunds.data.remote.model.toUserProfile
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import com.aryanspatel.grofunds.domain.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val dp: DispatcherProvider
) : AuthRepository{

    private val _auth = MutableStateFlow(auth.currentUser?.toDomain())

    private val authListener = FirebaseAuth.AuthStateListener { fa ->
        _auth.value = fa.currentUser?.toDomain()
    }

    override fun authState(): Flow<AuthUser?> = _auth

    init { auth.addAuthStateListener(authListener) }
    override fun getCurrentUser(): AuthUser? = _auth.value

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        runCatching {
            withTimeout(15_000) {
                val authResult = auth.signInWithEmailAndPassword(email, password)
                    .awaitIo(dp)
                val fbUser: FirebaseUser = authResult.user ?: error("No user in AuthResult")
                val user = fbUser.toDomain()
                _auth.value = user
                user
            }
        }.mapError()

    override suspend fun signUp(email: String, password: String): Result<AuthUser> =
        runCatching {
            withTimeout(15_000) {
                val authResult =  auth.createUserWithEmailAndPassword(email, password)
                    .awaitIo(dp)
                val fbUser: FirebaseUser = authResult.user ?: error("No user in AuthResult")
                val user = fbUser.toDomain()
                _auth.value = user
                user

            }
        }.mapError()

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching {
            withTimeout(15_000) {
                auth.sendPasswordResetEmail(email).awaitIo(dp)
                Unit
            }
        }.mapError()


    /**
     * Upserts a minimal user profile:
     * - Writes only non-null fields (doesn't wipe existing data)
     * - Sets createdAt exactly once (transaction-safe)
     * - Always updates updatedAt / lastLoginAt
     *
     * Call this right after sign-in / sign-up succeeds.
     */
    override suspend fun upsertUserProfileMinimal(name: String?): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: return Result.success(Unit) // nothing to do if signed out
            val uid = user.uid
            val docRef = db.collection("users").document(uid)

            // Choose a stable displayName: override > Firebase displayName > email prefix
            val displayName = name?.takeIf { it.isNotBlank() }
                ?: user.displayName
                ?: user.email?.substringBefore('@')?.replaceFirstChar { it.titlecase() }

            // Normalize provider ids (drop the internal "firebase")
            val providers = user.providerData
                .mapNotNull { it.providerId }
                .filterNot { it == "firebase" }
                .distinct()

            // Prepare non-null updates in **camelCase** to match your data class
            val updates = mutableMapOf(
                "userId" to uid,
                "displayName" to displayName,
                "email" to user.email?.lowercase(),
                "providers" to providers,
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastLoginAt" to FieldValue.serverTimestamp()
            ).pruneNulls()

            withTimeout(15_000) {
                // Transaction ensures createdAt is set only once
                withContext(dp.iO) {
                    db.runTransaction { txn ->
                        val snap = txn.get(docRef)
                        if (!snap.exists() || !snap.contains("createdAt")) {
                            txn.set(docRef, mapOf("createdAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                        }
                        txn.set(docRef, updates, SetOptions.merge())
                        null
                    }.await()
                }
            }
        }

    /** Live updates for a specific UID (useful when auth changes). */
    override fun userProfileFlowFor(uid: String): Flow<UserProfile?> = callbackFlow {
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) close(err) else trySend(snap?.toUserProfile()).isSuccess
            }
        awaitClose { reg.remove() }
    }
}

/** Optional: friendlier error messages for the UI layer. */
private fun <T> Result<T>.mapError(): Result<T> = this


/** Remove entries with null values so we don't overwrite fields with nulls. */
private fun MutableMap<String, Any?>.pruneNulls(): MutableMap<String, Any?> {
    val it = entries.iterator()
    while (it.hasNext()) if (it.next().value == null) it.remove()
    return this
}

private fun FirebaseUser.toDomain() = AuthUser(
    uid = uid,
    email = email,
    displayName = displayName
)
