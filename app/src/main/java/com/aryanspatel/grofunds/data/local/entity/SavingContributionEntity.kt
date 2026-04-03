package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("saving_contributions")
data class SavingContributionEntity(

    @PrimaryKey
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

    @ColumnInfo(name = "local_updated_at")
    val localUpdatedAt: Long,

    @ColumnInfo(name = "remote_updated_at")
    val remoteUpdatedAt: Long,

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = true,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)