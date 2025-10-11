package com.aryanspatel.grofunds.di

import android.content.Context
import androidx.room.Room
import com.aryanspatel.grofunds.core.DefaultDispatcherProvider
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.data.local.GroFundsDatabase
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.RecurringTransactionDao
import com.aryanspatel.grofunds.data.local.dao.SavingsDao
import com.aryanspatel.grofunds.data.local.dao.SyncStateDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.dao.UserSettingsDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()


    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        return db
    }

    @Provides @Singleton
    fun provideGroFundsDatabase(@ApplicationContext context: Context): GroFundsDatabase
     = Room.databaseBuilder(
        context,
        GroFundsDatabase::class.java,
         "grofunds_database")
         .fallbackToDestructiveMigration()
         .build()


    @Provides @Singleton
    fun provideTransactionDao(groFundsDatabase: GroFundsDatabase): TransactionDao
    = groFundsDatabase.transactionDao()

    @Provides @Singleton
    fun provideAccountSummaryDao(groFundsDatabase: GroFundsDatabase): AccountSummaryDao
    = groFundsDatabase.accountSummaryDao()

    @Provides @Singleton
    fun provideSavingsDao(groFundsDatabase: GroFundsDatabase): SavingsDao
            = groFundsDatabase.savingDao()

    @Provides @Singleton
    fun provideRecurringTransactionDao(groFundsDatabase: GroFundsDatabase): RecurringTransactionDao
            = groFundsDatabase.recurringTransactionDao()

    @Provides @Singleton
    fun provideSyncStateDao(groFundsDatabase: GroFundsDatabase): SyncStateDao
            = groFundsDatabase.syncStateDao()

    @Provides @Singleton
    fun provideUserSettingsDao(groFundsDatabase: GroFundsDatabase): UserSettingsDao
            = groFundsDatabase.userSettingsDao()




}