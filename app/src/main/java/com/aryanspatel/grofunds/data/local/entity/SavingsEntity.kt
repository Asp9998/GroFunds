package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity("savings",
    indices = [
        // For alphabetical list
        Index(value = ["user_id", "is_deleted", "title", "saving_id"])]
)
data class SavingsEntity(

    @PrimaryKey
    @ColumnInfo(name = "saving_id")
    val savingId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "input")
    val input: String,

    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,

    @ColumnInfo(name = "start_amount")
    val startAmount: Double,

    @ColumnInfo(name = "saved_amount")
    val savedAmount: Double,

    @ColumnInfo(name = "type")
    val typeId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "note")
    val note: String,

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

    @ColumnInfo(name = "deleted_at_utc")
    val deletedAtUTC : Long? = null
)
