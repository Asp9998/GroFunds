package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity("recurring_transaction",
    primaryKeys = ["user_id", "transaction_id"]
)
data class RecurringTransactionEntity(
    @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("transaction_id")
    val transactionId: String,

    @ColumnInfo("kind")
    val kind: String,

    @ColumnInfo("amount")
    val amount: Double,

    @ColumnInfo("category_or_type_id")
    val categoryOrTypeId: String,

    @ColumnInfo("subcategory_id")
    val subcategoryId: String,

    @ColumnInfo("merchant")
    val merchant: String?,

    @ColumnInfo("note")
    val note: String?,

    @ColumnInfo("start_date_utc")
    val startDateUTC: Long,

    @ColumnInfo("end_date_utc")
    val endDateUTC: Long? = null,

    @ColumnInfo("frequency_type")
    val frequencyType: String,

    @ColumnInfo("interval")
    val interval: Int = 1,

    @ColumnInfo("by_monthly_day")
    val byMonthDay: Int? = null,

    @ColumnInfo("by_weekly_day")
    val byWeekDay: Int? = null

)
