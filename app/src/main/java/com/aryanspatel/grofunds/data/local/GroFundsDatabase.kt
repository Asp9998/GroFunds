package com.aryanspatel.grofunds.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.CategoryBudgetDao
import com.aryanspatel.grofunds.data.local.dao.RecurringTransactionDao
import com.aryanspatel.grofunds.data.local.dao.SavingContributionsDao
import com.aryanspatel.grofunds.data.local.dao.SavingsDao
import com.aryanspatel.grofunds.data.local.dao.SyncStateDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.dao.UserPreferencesDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.CategoryBudgetEntity
import com.aryanspatel.grofunds.data.local.entity.RecurringTransactionEntity
import com.aryanspatel.grofunds.data.local.entity.SavingContributionEntity
import com.aryanspatel.grofunds.data.local.entity.SavingsEntity
import com.aryanspatel.grofunds.data.local.entity.SyncStateEntity
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.local.entity.UserPreferencesEntity

@Database(entities =
    [TransactionEntity::class,
        SavingsEntity::class,
        SavingContributionEntity::class,
        AccountSummaryEntity::class,
        SyncStateEntity::class,
        UserPreferencesEntity::class,
        RecurringTransactionEntity::class,
        CategoryBudgetEntity::class
    ], version = 5)
abstract class GroFundsDatabase : RoomDatabase() {

    abstract fun accountSummaryDao() : AccountSummaryDao
    abstract fun transactionDao() : TransactionDao
    abstract fun savingDao() : SavingsDao
    abstract fun savingContributionDao(): SavingContributionsDao
    abstract fun userPreferencesDao() : UserPreferencesDao
    abstract fun categoryBudgetDao() : CategoryBudgetDao
    abstract fun recurringTransactionDao() : RecurringTransactionDao
    abstract fun syncStateDao() : SyncStateDao

}