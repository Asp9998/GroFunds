package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "category_budgets",
    primaryKeys = ["user_id", "category_id"])
data class CategoryBudgetEntity (
    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("currency_code")
    val currencyCode: String,

    @ColumnInfo("category_id")
    val categoryId: String,

    @ColumnInfo("amount_limit")
    val amountLimit: Double,

    @ColumnInfo("period")   // Weekly | Bi-weekly | Monthly | Quarterly
    val period: String = "Monthly",

    @ColumnInfo("effective_from")
    val effectiveFrom: Long,

    @ColumnInfo("effective_to")
    val effectiveTo: Long? = null,

    @ColumnInfo("locale_updated_at")
    val localeUpdatedAt: Long,

    @ColumnInfo("remote_updated_at")
    val remoteUpdatedAt: Long = 0L,

    @ColumnInfo("is_dirty")
    val isDirty: Boolean? = true,

    @ColumnInfo("is_deleted")
    val isDeleted: Boolean? = false,

    @ColumnInfo("deleted_at")
    val deletedAt: Long? = null


)