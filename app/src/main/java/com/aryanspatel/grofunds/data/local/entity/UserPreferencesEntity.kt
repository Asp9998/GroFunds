package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("display_name")
    val displayName: String,

    @ColumnInfo("currency_symbol")
    val currencySymbol: String,

    @ColumnInfo("currency_code")
    val currencyCode: String,

    @ColumnInfo("monthly_expense_budget")
    val monthlyExpenseBudget: Double? = null,

    @ColumnInfo("locale_updated_at")
    val localUpdatedAt: Long,

    @ColumnInfo("remote_updated_at")
    val remoteUpdatedAt: Long = 0L,

    @ColumnInfo("is_dirty")
    val isDirty: Boolean? = true,
)
