package com.aryanspatel.grofunds.data.local.entity

import androidx.room.Entity

@Entity("savings")
data class SavingsEntity(
    val userId: String,
    val savingId: String,
    val targetAmount: Double,
    val startAmount: Double,
    val typeId: String,
    val title: String,
    val dueDate: String,
    val createdAt: Long,
    val isDirty: Boolean = true,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAtUTC : Long? = null
)
