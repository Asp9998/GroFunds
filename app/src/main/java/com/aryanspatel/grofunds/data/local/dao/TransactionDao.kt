package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.remote.model.CategoryTotal
import com.aryanspatel.grofunds.data.remote.model.LocalUpdatedAtRow
import kotlinx.coroutines.flow.Flow
import retrofit2.http.DELETE

@Dao
interface TransactionDao {

    @Upsert
    suspend fun saveTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET remote_updated_at = :remoteUpdatedAt, is_dirty = 0  WHERE transaction_id = :id")
    suspend fun updateRemoteUpdatedAt(id: String, remoteUpdatedAt: Long)

    @Query("UPDATE transactions SET is_deleted = 1, local_updated_at = :deletedAtUTC, deleted_at_utc = :deletedAtUTC WHERE transaction_id = :transactionId AND user_id = :userId ")
    suspend fun markDeleted(transactionId: String, userId: String, deletedAtUTC: Long)

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

    @Upsert
    suspend fun upsertAll(list: List<TransactionEntity>)

    @Query("SELECT MAX(remote_updated_at) FROM transactions WHERE user_id = :userId")
    suspend fun maxRemoteUpdatedAt(userId: String): Long?

    // load transaction, which remote updated is lower than since(last updated)
    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId AND is_deleted = 0
            AND remote_updated_at > local_updated_at
            ORDER BY remote_updated_at ASC
    """
    )
    suspend fun getDirty(userId: String): List<TransactionEntity>

    @Query("""
        SELECT transaction_id, local_updated_at FROM transactions
        WHERE user_id = :userId AND transaction_id IN (:ids)
    """)
    suspend fun getLocalUpdatedAtFor(userId: String, ids: List<String>): List<LocalUpdatedAtRow>

    // mark pushed
    @Query("UPDATE transactions SET is_dirty = 0, remote_updated_at = :remoteUpdatedAt where transaction_id = :transactionId AND user_id = :userId")
    suspend fun markPushed(userId: String, transactionId: String, remoteUpdatedAt:Long)

    // get soft deleted transactions
    @Query("""
         SELECT *  FROM transactions 
         WHERE user_id = :userId AND is_deleted = 1 
        """)
    suspend fun getSoftDeletedTransactions(userId: String) : List<TransactionEntity>

    // delete soft deleted transaction after pushed
    @Query("DELETE FROM transactions WHERE user_id = :userId AND transaction_id IN (:transactionIds)")
    suspend fun deleteTransaction(userId: String, transactionIds: List<String>)

}

