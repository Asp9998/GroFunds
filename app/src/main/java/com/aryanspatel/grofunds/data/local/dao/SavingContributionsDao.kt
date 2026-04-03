package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.DTO.ContributionRow
import com.aryanspatel.grofunds.data.local.entity.SavingContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingContributionsDao {

    @Upsert
    suspend fun addContribution(contribution: SavingContributionEntity)

    @Query("UPDATE saving_contributions SET remote_updated_at = :remoteUpdatedAt, is_dirty = 0  WHERE contribution_id = :id")
    suspend fun updateRemoteUpdatedAt(id: String, remoteUpdatedAt: Long)

    @Query("""
        SELECT contribution_id, saving_id, note, amount, created_at 
        FROM saving_contributions 
        WHERE saving_id = :savingId
        """)
    fun observeContributionBySaving(savingId: String) : Flow<List<ContributionRow>>
}
