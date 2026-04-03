package com.aryanspatel.grofunds.data.remote.model

import androidx.room.ColumnInfo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CategoryTotal(
    val categoryId: String,
    val totalAmount: Double
)

data class LocalUpdatedAtRow(
    @ColumnInfo(name = "transaction_id") val id: String,
    @ColumnInfo(name = "local_updated_at") val localUpdatedAt: Long
)


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
    val isExcluded: Boolean  = false,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @ServerTimestamp var updatedAt: Date? = null
)

data class SavingsDoc(
    val kind: String?,
    val savingsId: String,
    val input: String?,
    val title: String?,
    val type: String?,
    val amount: Double?,
    val currencyId: String?,
    val dueDate: Timestamp?,
    val createdAt: Timestamp,
    val note: String?,
    val status: String = "saved",
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @ServerTimestamp var updatedAt: Date? = null
)

data class SavingContributionDoc(
    val contributionId: String,
    val savingId: String,
    val note: String,
    val amount: Double,
    @get:PropertyName("date") @set:PropertyName("date")
    @ServerTimestamp var date: Date? = null
)

data class AccountSummary(
    val currencyCode: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val totalSaving: Double,
    val availableCash: Double,
    val updatedAt: Long,
)