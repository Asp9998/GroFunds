package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    @Upsert
    suspend fun upsert(settings: UserPreferencesEntity)

    @Query("""UPDATE user_preferences 
        SET monthly_expense_budget = :newExpenseBudget,
        is_dirty = 1,
        locale_updated_at = :localeUpdatedAt
        WHERE user_id = :userId
    """)
    suspend fun updateExpenseBudget(userId: String, newExpenseBudget: Double, localeUpdatedAt: Long)

    @Query("""UPDATE user_preferences 
        SET display_name = :newDisplayName,
        locale_updated_at = :localeUpdatedAt,
        is_dirty = 1
        WHERE user_id = :userId
    """)
    suspend fun updateDisplayName(userId: String, newDisplayName: String, localeUpdatedAt: Long)

    @Query("""UPDATE user_preferences
        SET currency_code = :currencyCode,
        is_dirty = 1,
        locale_updated_at = :localeUpdatedAt
        WHERE user_id = :userId
    """)
    suspend fun setCurrencyCode(userId: String, currencyCode: String, localeUpdatedAt: Long)

    @Query("""UPDATE user_preferences
         SET is_dirty = 0,
         remote_updated_at = :remoteUpdatedAt
         WHERE user_id = :userId
    """)
    suspend fun markRemoteUpdate(userId: String, remoteUpdatedAt: Long)

    // Get userPreferences
    @Query("""
        SELECT * FROM user_preferences
        WHERE user_id = :userId
    """)
    suspend fun getUserPreferences(userId: String) : UserPreferencesEntity?

    // Observe userPreferences
    @Query("""
        SELECT * FROM user_preferences
        WHERE user_id = :userId
    """)
    fun observeUserPreferences(userId: String) : Flow<UserPreferencesEntity?>

    // Observe Currency Symbol
    @Query("""
        SELECT currency_symbol from user_preferences 
        WHERE user_id = :userId
    """)
    suspend fun getCurrencySymbol(userId: String) : String?

    @Query("""
        SELECT currency_code from user_preferences 
        WHERE user_id = :userId
    """)
    suspend fun getCurrencyCode(userId: String) : String?




}