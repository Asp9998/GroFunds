package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountSummaryDao {

    /** 1) Create the row if it doesn't exist */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountIfAbsent(accountSummary: AccountSummaryEntity)

    /** 2) Observe for UI */
    @Query("SELECT * FROM accountSummaryEntity WHERE user_id = :userId")
    fun observeAccountSummary(userId: String) : Flow<AccountSummaryEntity?>

    @Query("SELECT * FROM accountSummaryEntity WHERE user_id = :userId")
    suspend fun getSummary(userId: String) : AccountSummaryEntity?

    /** 3) General data applier */
    @Query("""
            UPDATE accountSummaryEntity
            SET 
                total_expense = total_expense + :expenseDelta,
                total_income = total_income + :incomeDelta,
                total_saving = total_saving + :savingDelta,
                updated_at = :updatedAt
            WHERE user_id = :userId 
        """)
    // add expense,
    suspend fun applyDelta(
        userId: String,
        expenseDelta: Double,
        incomeDelta: Double,
        savingDelta: Double,
        updatedAt: Long
    )

    /** 4) add/remove Income, */
    @Query("""
            UPDATE accountSummaryEntity
            SET 
                total_income = total_income + :income,
                updated_at = :updatedAt
            WHERE user_id = :userId 
        """)
    suspend fun updateIncome(userId: String, income: Double, updatedAt: Long)


    /** 4) add/remove Expense, */
    @Query("""
            UPDATE accountSummaryEntity
            SET 
                total_expense = total_expense + :expense,
                updated_at = :updatedAt
            WHERE user_id = :userId 
        """)
    suspend fun updateExpense(userId: String, expense: Double, updatedAt: Long)

    /** 4) add/remove Saving, */
    @Query("""
            UPDATE accountSummaryEntity
            SET 
                total_saving = total_saving + :saving,
                updated_at = :updatedAt
            WHERE user_id = :userId 
        """)
    suspend fun updateSaving(userId: String, saving: Double, updatedAt: Long)

}