package com.aryanspatel.grofunds.data.local.DTO

import androidx.room.ColumnInfo

data class SavingRow(
    @ColumnInfo(name = "saving_id")
    val savingId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "type")
    val typeId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,

    @ColumnInfo(name = "saved_amount")
    val savedAmount: Double,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "note")
    val note: String
)

data class ContributionRow(

    @ColumnInfo(name = "contribution_id")
    val contributionId: String,

    @ColumnInfo(name = "saving_id")
    val savingId: String,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)