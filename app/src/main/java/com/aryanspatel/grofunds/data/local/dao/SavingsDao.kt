package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.DTO.SavingRow
import com.aryanspatel.grofunds.data.local.entity.SavingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Upsert
    suspend fun saveSaving(saving: SavingsEntity)

    @Query("UPDATE savings SET remote_updated_at = :remoteUpdatedAt, is_dirty = 0  WHERE saving_id = :id")
    suspend fun updateRemoteUpdatedAt(id: String, remoteUpdatedAt: Long)
//
//    @Query("""UPDATE savings
//            SET is_deleted = 1,
//                local_updated_at = :deletedAtUTC,
//                deleted_at_utc = :deletedAtUTC
//            WHERE saving_id = :transactionId AND
//                    user_id = :userId
//            """)
//    suspend fun markDeleted(transactionId: String, userId: String, deletedAtUTC: Long)
//
//    @Query("""
//        SELECT * from savings
//        WHERE user_id = :userId
//    """)
//    fun observeSavings(userId: String) : Flow<List<SavingsEntity>>

    @Query("""
    SELECT saving_id, user_id, input, target_amount, start_amount, saved_amount,
           type, title, date, due_date, note
    FROM savings
    WHERE user_id = :userId AND is_deleted = 0
    ORDER BY title ASC, saving_id ASC
""")
    fun observeSavings(userId: String): Flow<List<SavingRow>>
}