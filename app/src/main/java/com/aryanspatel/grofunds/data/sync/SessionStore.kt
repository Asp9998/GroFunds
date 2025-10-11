package com.aryanspatel.grofunds.data.sync

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface SessionStore {
    val userIdFlow: StateFlow<String?>
}

@Singleton
class FirebaseSessionStore @Inject constructor(
    private val auth: FirebaseAuth
) : SessionStore {
    private val _userId = MutableStateFlow(auth.currentUser?.uid)
    override val userIdFlow: StateFlow<String?> = _userId.asStateFlow()

    private val listener = FirebaseAuth.AuthStateListener {
        _userId.value = it.currentUser?.uid
    }

    init { auth.addAuthStateListener(listener) }
}