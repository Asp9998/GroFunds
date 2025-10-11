package com.aryanspatel.grofunds.data.remote.model

import androidx.room.ColumnInfo

data class CategoryTotal(
    val categoryId: String,
    val totalAmount: Double
)

data class LocalUpdatedAtRow(
    @ColumnInfo(name = "transaction_id") val id: String,
    @ColumnInfo(name = "local_updated_at") val localUpdatedAt: Long
)