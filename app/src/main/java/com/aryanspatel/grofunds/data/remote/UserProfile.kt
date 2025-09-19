package com.aryanspatel.grofunds.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class UserProfile(
    val userId: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val currency: String = "CAD",
    val aiConsent: Boolean = false,
    val providers: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null
)

fun DocumentSnapshot.toUserProfile(): UserProfile? =
    toObject(UserProfile::class.java)
