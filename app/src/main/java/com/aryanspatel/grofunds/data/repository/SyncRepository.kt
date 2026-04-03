package com.aryanspatel.grofunds.data.repository

import android.util.Log
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.core.awaitIo
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.RecurringTransactionDao
import com.aryanspatel.grofunds.data.local.dao.SavingsDao
import com.aryanspatel.grofunds.data.local.dao.SyncStateDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.dao.UserPreferencesDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.SyncStateEntity
import com.aryanspatel.grofunds.data.mapper.toEntity
import com.aryanspatel.grofunds.data.model.RemoteTxn
import com.aryanspatel.grofunds.data.remote.model.AccountSummary
import com.aryanspatel.grofunds.data.remote.model.TransactionDoc
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions.merge
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val savingsDao: SavingsDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val accountSummaryDao: AccountSummaryDao,
    private val userSettingsDao: UserPreferencesDao,
    private val syncStateDao: SyncStateDao,
    private val dp: DispatcherProvider,
    private val db: FirebaseFirestore
){

    suspend fun pushDirtyBatch(uid: String)  = withContext(dp.iO){
        // push Transactions (updated, but not deleted)
        pushUpdatedTransactions(uid = uid)

        // push Deleted Transactions (delete from firebase)
        pushDeletedTransactions(uid = uid)

        // push savings

        // push Account Summary
        pushAccountSummary(uid = uid)

        // push User settings

        // push Recurring Transactions

    }




    suspend fun pullRemoteUpdates(uid: String) = withContext(dp.iO) {
        // pull Transactions
        pullTransactions(uid = uid)

        Log.d("SginInException", "pullRemoteUpdates:Successfully fetched transactions ")

        // pull savings

        // pull Account Summary
        pullAccountSummary(uid = uid)

        // pull User settings

        // pull Recurring Transactions
    }


    private suspend fun pushDeletedTransactions(uid: String) = withContext(dp.iO) {
        Log.d("SignOutException", "pushDeletedTransactions: inside delete Transaction")

        val softDeleted = transactionDao.getSoftDeletedTransactions(userId = uid)
        Log.d("SignOutException", "pushDeletedTransactions: $uid")
        Log.d("SignOutException", "pushDeletedTransactions: ${softDeleted.isEmpty()}")

        if(softDeleted.isEmpty()) return@withContext

        val ids = softDeleted.map { it.transactionID }
        val chunks = ids.chunked(450)

        for(chunk in chunks){

            val success = runCatching {
                retry(2){
                    withTimeout(15_000){
                        db.runBatch { batch ->
                            val docRef = db.collection("users")
                                .document(uid)
                                .collection("transactions")
                            chunk.forEach { id ->
                                batch.delete(docRef.document(id))
                            }
                        }.awaitIo(dp)
                    }
                }
            }.isSuccess

            if(success){
                Log.d("SignOutException", "pushDeletedTransactions: deleted Successfully")
                transactionDao.deleteTransaction(userId = uid, transactionIds = chunk)
            }else{

                Log.d("SignOutException", "pushDeletedTransactions: delete failed")
            }
        }
    }

    private suspend fun pushUpdatedTransactions(uid: String) = withContext(dp.iO) {
        val listOfTransactions = transactionDao.getDirty(userId = uid)

        if (listOfTransactions.isEmpty()) return@withContext

        val chunks = listOfTransactions.chunked(250)

        for (chunk in chunks) {

            chunk.forEach { curr ->

                val docRef = db.collection("users")
                    .document(uid)
                    .collection("transactions")
                    .document(curr.transactionID)

                val payload = TransactionDoc(
                    transactionID = curr.transactionID,
                    kind = curr.kind,
                    input = curr.input,
                    amount = curr.amount,
                    currency = curr.currencyCode,
                    categoryOrTypeID = curr.categoryOrTypeID,
                    subcategoryID = curr.subcategoryID,
                    merchant = curr.merchant,
                    note = curr.note,
                    date = DateConverters.millisToTimestamp(curr.date),
                    status = "saved",
                    updatedAt = null
                )

//                val updateExpense = mapOf(
//                    "transactionID" to curr.transactionID,
//                    "kind" to curr.kind,
//                    "input" to curr.input,
//                    "amount" to curr.amount,
//                    "currency" to curr.currencyCode,
//                    "categoryOrTypeID" to curr.categoryOrTypeID,
//                    "subcategoryID" to curr.subcategoryID,
//                    "merchant" to curr.merchant,
//                    "note" to curr.note,
//                    "date" to DateConverters.millisToTimestamp(curr.date),
//                    "status" to "saved",
//                    "updatedAt" to FieldValue.serverTimestamp()
//                )


                ioRetry(maxAttempts = 3, initialDelayMs = 250, maxElapsedMs = 20_000) {
                    withTimeout(12_000) {
                        docRef.set(payload, merge()).awaitIo(dp)
                    }
                }

                val serverMillis = runCatching {
                    ioRetry(maxAttempts = 2, initialDelayMs = 300, maxElapsedMs = 8_000) {
                        withTimeout(10_000) {
                            docRef.get().awaitIo(dp)
                        }.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
                    }
                }.getOrElse { 0L }

                    if (serverMillis > 0L) {
                        transactionDao.markPushed(
                            userId = uid,
                            transactionId = curr.transactionID,
                            remoteUpdatedAt = serverMillis
                        )
                    }else{
                        Log.d("RemotePushFailed", "pushUpdatedTransactions: Failed")
                    }
                }
            }
        }

    private suspend fun pullTransactions(uid: String) = withContext(dp.iO) {
        Log.d("PullTestingDebugging", "pullTransactions: Inside pull worker")
         val docRef = db.collection("users")
             .document(uid)
             .collection("transactions")

         val sinceMillis = syncStateDao.getLastPulledAt(uid) ?: 0L
         val updatedSinceTimeStamp = Timestamp(Date(sinceMillis))
         val pageSize = 300
         val results = mutableListOf<RemoteTxn>()
         var lastDoc: DocumentSnapshot? = null

         while (true) {
             var q = docRef.whereGreaterThan("updatedAt", updatedSinceTimeStamp)
                 .orderBy("updatedAt")
                 .limit(pageSize.toLong())

             if (lastDoc != null) q = q.startAfter(lastDoc)

             Log.d("PullTestingDebugging", "pullTransactions: before getting snap")


             // Page fetch with retry + timeout
             val snap = ioRetry(maxAttempts = 2, initialDelayMs = 300, maxElapsedMs = 8_000) {
                 withTimeout(6_000) { q.get().awaitIo(dp) }
             }

             Log.d("PullTestingDebugging", "pullTransactions: after pull worker")
             Log.d("PullTestingDebugging", "pullTransactions: ${snap.documents}")

             if (snap.isEmpty) break

             Log.d("PullTestingDebugging", "pullTransactions: snap is not empty}")


             for (doc in snap.documents) {
                 val updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time
                 if(updatedAt == null) continue

                 val date = doc.getTimestamp("date")?.toDate()?.time
                 results.add(
                     RemoteTxn(
                         transactionID = doc.getString("transactionID") ?: doc.id,
                         kind = doc.getString("kind"),
                         input = doc.getString("input"),
                         amount = doc.getDouble("amount"),
                         currency = doc.getString("currency"),
                         categoryID = doc.getString("categoryOrTypeID"),
                         subcategoryID = doc.getString("subcategoryID"),
                         merchant = doc.getString("merchant"),
                         note = doc.getString("note"),
                         dateMillis = date,
                         status = doc.getString("status"),
                         updatedAtMillis = updatedAt
                     )
                 )
             }
             lastDoc = snap.documents.last()
             if (snap.size() < pageSize) break
         }

         if (results.isEmpty()) return@withContext
         val ids = results.map { it.transactionID }
         val existing = transactionDao.getLocalUpdatedAtFor(uid, ids)
             .associate { it.id to it.localUpdatedAt }

         val entities = results.map {
             it.toEntity(
                 uid,
                 previousLocalUpdatedAt = existing[it.transactionID]
             )
         }
         if (entities.isNotEmpty()) transactionDao.upsertAll(entities)

         val maxRemote = results.maxOf { it.updatedAtMillis ?: sinceMillis }
         syncStateDao.upsert(SyncStateEntity(userId = uid, lastPulledAt = maxRemote))
    }

    private suspend fun pushAccountSummary(uid: String) = withContext(dp.iO) {
        val summary = accountSummaryDao.getSummary(uid)
        if(summary == null) return@withContext

        val docRef = db.collection("users")
            .document(uid)
            .collection("account_summary")
            .document("current")

        val payload = AccountSummary(
            currencyCode = summary.currencyCode,
            totalExpense = summary.totalExpense,
            totalIncome = summary.totalIncome,
            totalSaving = summary.totalSaving,
            availableCash = summary.availableCash,
            updatedAt = summary.localeUpdatedAt
        )

        ioRetry(maxAttempts = 3, initialDelayMs = 250, maxElapsedMs = 20_000) {
            withTimeout(12_000) {
                docRef.set(payload, merge()).awaitIo(dp)
            }
        }
    }

    private suspend fun pullAccountSummary(uid: String) = withContext(dp.iO){
        Log.d("SginInException", "pullAccountSummary: into pull account summary")
        val local = accountSummaryDao.getSummary(uid)

        val docRef = db.collection("users")
            .document(uid)
            .collection("account_summary")
            .document("current")


        val snap : DocumentSnapshot=
            runCatching { ioRetry(maxAttempts = 2, initialDelayMs = 300, maxElapsedMs = 8_000) {
                withTimeout(6_000) { docRef.get().awaitIo(dp) }
            }
        }.getOrElse { return@withContext }


        Log.d("SginInException", "pullAccountSummary: $snap")

        if(!snap.exists()) return@withContext

        val remoteUpdatedAt = snap.getLong("updatedAt") ?: 0L
        val localUpdatedAt  = local?.localeUpdatedAt ?: 0L

        if (remoteUpdatedAt <= localUpdatedAt) return@withContext

        val entity = AccountSummaryEntity(
            userId = uid,
            currencyCode = snap.getString("currencyCode") ?: local?.currencyCode ?: "CAD",
            totalExpense = snap.getDouble("totalExpense") ?: 0.0,
            totalIncome = snap.getDouble("totalIncome") ?: 0.0,
            totalSaving = snap.getDouble("totalSaving") ?: 0.0,
            localeUpdatedAt = remoteUpdatedAt,
            remoteUpdatedAt = remoteUpdatedAt,
            isDirty = false
        )

        Log.d("SginInException", "pullAccountSummary: $entity")

        accountSummaryDao.insertAccountIfAbsent(entity)

    }


    /** Simple retry helper with exponential backoff */
    suspend fun <T> retry(times: Int, initialDelayMs: Long = 200L, block: suspend () -> T): T {
        var last: Throwable? = null
        var delayMs = initialDelayMs
        repeat(times + 1) { attempt ->
            try {
                return block()
            } catch (t: Throwable) {
                // Don't retry permission errors
                if (t.message?.contains("PERMISSION_DENIED") == true) throw t
                last = t
                if (attempt < times) {
                    delay(delayMs)
                    delayMs *= 2
                }
            }
        }
        throw last ?: IllegalStateException("Unknown error")
    }

    suspend inline fun <T> ioRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 250,
        maxElapsedMs: Long = 20_000,
        crossinline block: suspend () -> T
    ): T {
        var attempt = 0
        var delayMs = initialDelayMs
        val start = System.nanoTime()
        var last: Throwable? = null

        while (attempt < maxAttempts) {
            try {
                return block()
            } catch (t: Throwable) {
                val code = (t as? FirebaseFirestoreException)?.code
                val retryable = when (code) {
                    Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED, Code.ABORTED,
                    Code.RESOURCE_EXHAUSTED, Code.INTERNAL, Code.UNKNOWN, Code.CANCELLED -> true
                    Code.NOT_FOUND -> false // treat as success upstream if delete
                    Code.PERMISSION_DENIED, Code.FAILED_PRECONDITION, Code.INVALID_ARGUMENT,
                    Code.UNAUTHENTICATED, Code.OUT_OF_RANGE, Code.UNIMPLEMENTED,
                    Code.DATA_LOSS, Code.OK, Code.ALREADY_EXISTS -> false
                    null -> true // network IO exceptions etc.
                }

                if (!retryable) throw t

                last = t
                attempt++

                // Stop if we’ve exceeded the time budget
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                if (elapsedMs + delayMs > maxElapsedMs || attempt >= maxAttempts) break

                // Exponential backoff with jitter (~±20%)
                val jitter = (delayMs * 0.2).toLong()
                val wait = delayMs + (-jitter..jitter).random()
                delay(wait)
                delayMs = (delayMs * 2).coerceAtMost(4_000)
            }
        }
        throw last ?: IllegalStateException("Retry failed without exception")
    }


}

