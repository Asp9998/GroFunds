package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions",
    indices = [Index(value = ["user_id", "is_deleted", "kind", "date_utc", "category_or_type"]),
        Index(value = ["remote_updated_at"]),
    ])
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo("transaction_id")
    val transactionID: String,

    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("kind")  // EXPENSE | INCOME
    val kind: String,

    @ColumnInfo("amount")
    val amount: Double,

    @ColumnInfo("currency_code")
    val currencyCode: String,

    @ColumnInfo("category_or_type")
    val categoryOrTypeID: String,

    @ColumnInfo("subcategory")
    val subcategoryID: String? = null,

    @ColumnInfo("merchant")
    val merchant: String? = null,

    @ColumnInfo("note")
    val note: String? = null,

    // Dates in UTC millis
    @ColumnInfo("date_utc")
    val date: Long,

    @ColumnInfo("created_at_utc")
    val createdAtUTC: Long,

    @ColumnInfo("remote_updated_at")
    val remoteUpdatedAt: Long = 0L,

    @ColumnInfo("locale_updated_at")
    val localeUpdatedAt: Long,

    @ColumnInfo("is_dirty")
    val isDirty: Boolean = true,

    @ColumnInfo("is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo("deleted_at_utc")
    val deletedAtUTC: Long? = null,

)
