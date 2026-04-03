package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountSummaryEntity")
data class AccountSummaryEntity(
    @PrimaryKey
    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("currencyCode")
    val currencyCode: String,

    @ColumnInfo("total_expense")
    val totalExpense: Double = 0.0,

    @ColumnInfo("total_income")
    val totalIncome: Double = 0.0,

    @ColumnInfo("total_saving")
    val totalSaving: Double = 0.0,

    @ColumnInfo("remote_updated_at")
    val remoteUpdatedAt: Long,

    @ColumnInfo("locale_updated_at")
    val localeUpdatedAt: Long,

    @ColumnInfo("is_dirty")
    val isDirty: Boolean = true,


){
    val availableCash: Double get() = totalIncome - totalExpense - totalSaving
}
