package com.aryanspatel.grofunds.data.remote.model

import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseCurrentUserProvider @Inject constructor(
    private val auth: FirebaseAuth
) : CurrentUserProvider {
    override fun userIdOrNull(): String? {
        return auth.currentUser?.uid
    }
}