package com.aryanspatel.grofunds.data.remote.model

import androidx.room.ColumnInfo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

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


@IgnoreExtraProperties
data class TransactionDoc(
    val transactionID: String,
    val kind: String?,
    val input: String?,
    val amount: Double?,
    val currency: String?,
    val categoryOrTypeID: String?,
    val subcategoryID: String? = null,
    val merchant: String? = null,
    val note: String?,
    val date: Timestamp,                 // Firestore accepts java.util.Date
    val status: String = "saved",
    @ServerTimestamp var updatedAt: FieldValue? = null  // set null -> server fills
)

data class AccountSummary(
    val currencyCode: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val totalSaving: Double,
    val availableCash: Double,
    val updatedAt: Long,
)