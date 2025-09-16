package com.aryanspatel.grofunds.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class UserProfile(
    val userId: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val currency: String = "CAD",
    val aiConsent: Boolean = false,
    val providers: List<String> = emptyList(),
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val lastLoginAt: com.google.firebase.Timestamp? = null
)

fun DocumentSnapshot.toUserProfile(): UserProfile? =
    toObject(UserProfile::class.java)
