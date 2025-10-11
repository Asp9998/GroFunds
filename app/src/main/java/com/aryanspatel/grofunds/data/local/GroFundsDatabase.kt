package com.aryanspatel.grofunds.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.RecurringTransactionDao
import com.aryanspatel.grofunds.data.local.dao.SavingsDao
import com.aryanspatel.grofunds.data.local.dao.SyncStateDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.dao.UserSettingsDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.RecurringTransactionEntity
import com.aryanspatel.grofunds.data.local.entity.SyncStateEntity
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.local.entity.UserSettingsEntity

@Database(entities =
    [TransactionEntity::class,
        AccountSummaryEntity::class,
        SyncStateEntity::class,
        UserSettingsEntity::class,
        RecurringTransactionEntity::class
    ], version = 3)
abstract class GroFundsDatabase : RoomDatabase() {

    abstract fun accountSummaryDao() : AccountSummaryDao
    abstract fun transactionDao() : TransactionDao
    abstract fun savingDao() : SavingsDao
    abstract fun recurringTransactionDao() : RecurringTransactionDao
    abstract fun syncStateDao() : SyncStateDao
    abstract fun userSettingsDao() : UserSettingsDao
}