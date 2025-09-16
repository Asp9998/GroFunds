//package com.aryanspatel.grofunds.data.repository
//
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.auth.userProfileChangeRequest
//import com.google.firebase.firestore.FirebaseFirestore
//import jakarta.inject.Inject
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.tasks.await
//import kotlin.coroutines.resume
//
//class AuthRepository @Inject constructor ( private val auth: FirebaseAuth ) {
//
//    fun getCurrentUser() = auth.currentUser
//
//    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> =
//        suspendCancellableCoroutine { cont ->
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        cont.resume(Result.success(auth.currentUser))
//                    } else {
//                        cont.resume(Result.failure(task.exception ?: Exception("Unknown error")))
//                    }
//                }
//        }
//
//    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> =
//        suspendCancellableCoroutine { cont ->
//
//            auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    val user = auth.currentUser
//                    if (task.isSuccessful && user != null) {
//                        cont.resume(Result.success(user))
//                    } else {
//                        cont.resume(Result.failure(task.exception ?: Exception("Signup failed")))
//                    }
//                }
//        }
//
//    fun signOut() {
//        auth.signOut()
//    }
//
//    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
//        return try {
//            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//}