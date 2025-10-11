package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.entity.SyncStateEntity

@Dao
interface SyncStateDao {

    @Query("SELECT last_pulled_at FROM sync_state WHERE user_id = :userId")
    suspend fun getLastPulledAt(userId: String) : Long?

//    @Query("SELECT last_pushed_at FROM sync_state WHERE user_id = :userId")
//    suspend fun getLastPushedAT(userId: String) : Long

    @Upsert
    suspend fun upsert(state: SyncStateEntity)
}