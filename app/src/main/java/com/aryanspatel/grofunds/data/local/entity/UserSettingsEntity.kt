package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("currency_code")
    val currencyCode: String,

    @ColumnInfo("monthly_expense_limit")
    val monthlyExpenseLimit: Double? = null
)
