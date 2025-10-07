package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.model.CategoryTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Upsert
    suspend fun saveTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET remote_updated_at = :remoteUpdatedAt, is_dirty = 0  WHERE transaction_id = :id")
    suspend fun updateRemoteUpdatedAt(id: String, remoteUpdatedAt: Long)

    @Upsert
    suspend fun upsertAll(list: List<TransactionEntity>)

    @Query("UPDATE transactions SET is_deleted = 1 WHERE transaction_id = :transactionId ")
    suspend fun markDeleted(transactionId: String)

    /** get monthly transaction list*/
    @Query(
        """
        SELECT * FROM transactions 
        WHERE user_id = :userId
            AND is_deleted = 0
            AND kind = :kind
            AND date_utc BETWEEN :startDate AND :endDate
            ORDER BY date_utc DESC
             """
    )
    fun observeMonthly(
        userId: String,
        kind: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>

    /** get monthly transaction list by category or type */
    @Query(
        """
        SELECT * FROM transactions 
        WHERE user_id = :userId AND is_deleted = 0 AND kind = :kind
            AND category_or_type IN  (:categoryIds)
            AND date_utc BETWEEN :startDate AND :endDate
            ORDER BY date_utc DESC
             """
    )
    fun observeByCategory(
        userId: String,
        kind: String,
        startDate: Long,
        endDate: Long,
        categoryIds: List<String>
    ): Flow<List<TransactionEntity>>

    /** get total amount for each category or type */
    @Query(
        """
        SELECT category_or_type as categoryId, SUM(amount) as totalAmount
        FROM transactions
        WHERE user_id = :userId AND is_deleted = 0 AND kind = :kind
            AND date_utc BETWEEN :startDate AND :endDate
        GROUP BY category_or_type
        HAVING totalAmount <> 0
        ORDER BY totalAmount DESC
    """
    )
    fun observeCategoryTotals(
        userId: String,
        kind: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryTotal>>


    /** sync helpers */
    @Query("SELECT MAX(remote_updated_at) FROM transactions WHERE user_id = :userId")
    suspend fun maxRemoteUpdatedAt(userId: String): Long?

    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
            AND remote_updated_at > :since
            ORDER BY remote_updated_at ASC
    """
    )
    suspend fun changedSince(userId: String, since: Long): List<TransactionEntity>

}

