package com.aryanspatel.grofunds.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("sync_state")
data class SyncStateEntity(
    @PrimaryKey @ColumnInfo("user_id")
    val userId: String,

    @ColumnInfo("last_pulled_at")
    val lastPulledAt: Long,

//    @ColumnInfo("last_pushed_at")
//    val lastPushedAt: Long
)
