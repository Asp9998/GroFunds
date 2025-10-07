package com.aryanspatel.grofunds.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class, AccountSummaryEntity::class], version = 2)
abstract class GroFundsDatabase : RoomDatabase() {

    abstract fun transactionDao() : TransactionDao
    abstract fun accountSummaryDao() : AccountSummaryDao

//    companion object{
//        @Volatile
//        private var INSTANCE : GroFundsDatabase? = null
//
//        fun getInstance(context: Context): GroFundsDatabase {
//            return INSTANCE ?: synchronized(this){
//                val instance = Room.databaseBuilder(
//                    context = context.applicationContext,
//                    klass = GroFundsDatabase::class.java,
//                    name = "grofunds_database")
//                    .addCallback(object : Callback() {
//                    override fun onOpen(db: SupportSQLiteDatabase) {
//                        super.onOpen(db)
//                        android.util.Log.d("GroFundsDB", "onOpen: DB is open (path=${db.path})")
//                    }
//                })
//                    .fallbackToDestructiveMigration()
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}