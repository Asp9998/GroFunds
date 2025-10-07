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

    @ColumnInfo("updated_at")
    val updatedAt: Long,
){
    val availableCash: Double get() = totalIncome - totalExpense - totalSaving
}
