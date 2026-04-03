package com.aryanspatel.grofunds.data.remote.model

import com.aryanspatel.grofunds.domain.repository.CurrentUserFlow
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class FirebaseCurrentUserProvider @Inject constructor(
    private val auth: FirebaseAuth
) : CurrentUserProvider {
    override fun userIdOrNull(): String? {
        return auth.currentUser?.uid
    }
}

class FirebaseCurrentUserFlow @Inject constructor(auth: FirebaseAuth) : CurrentUserFlow {
    override val uidFlow: Flow<String?> = callbackFlow<String?> {
        trySend(auth.currentUser?.uid).isSuccess
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            // Push new value on login/logout
            trySend(fbAuth.currentUser?.uid).isSuccess
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()
}