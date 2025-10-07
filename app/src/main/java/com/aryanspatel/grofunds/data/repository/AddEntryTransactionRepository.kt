package com.aryanspatel.grofunds.data.repository

import android.util.Log
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.core.awaitIo
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.model.CategoryTotal
import com.aryanspatel.grofunds.domain.model.DraftRef
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.resolveExpenseCategoryIds
import com.aryanspatel.grofunds.domain.usecase.resolveIncomeTypeId
import com.aryanspatel.grofunds.domain.usecase.resolveSavingTypeId
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for creating/observing/saving/deleting GroFunds entries.
 *
 * Contract with Cloud Functions (CF):
 * - On create, CF expects a minimal document with:
 *      { INPUT_FIELD: "<user note>", status: "pending" }
 * - CF enriches the doc and eventually flips status away from "pending".
 * - Client "Save" updates the same doc with finalized fields and sets status: "saved".
 *
 * IMPORTANT: If your CF uses "input" instead of "note", change [INPUT_FIELD] to "input".
 */
@Singleton
class AddEntryTransactionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val transactionDao: TransactionDao,
    private val accountSummaryDao: AccountSummaryDao,
    private val dp: DispatcherProvider
): AddEntryRepository {

    private companion object {
        const val INPUT_FIELD = "input"
    }

    private fun kindFor(kind: EntryKind) = when (kind) {
        EntryKind.EXPENSE -> "expense"
        EntryKind.INCOME  -> "income"
        EntryKind.GOAL    -> "saving"
    }

    private val userUId = auth.currentUser?.uid ?: error("Not logged in")

    // ───────────────────────────── Create ─────────────────────────────

    /**
     * Creates a draft that triggers the CF (status = "pending").
     * Returns the created document's [DraftRef] (id + fully-qualified path).
     */
    override suspend fun createDraft(
        kind: EntryKind,
        note: String,
        currencyHint: String?,   // optional hints if your CF reads them
        localeHint: String?,
        timeZone: String?
    ): DraftRef = withContext(dp.iO){

        val uid = auth.currentUser?.uid ?: error("Not logged in")
        val docRef = db.collection("users").document(uid).collection("drafts").document()
        val kindFor = kindFor(kind)

        // Minimal payload; add nested _client hints if you want to pass them safely.
        val base = mutableMapOf<String, Any?>(
            INPUT_FIELD to note.trim(),
            "kind" to kindFor,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "_client" to mapOf(
                "currencyHint" to currencyHint,
                "localeHint" to localeHint,
                "timeZone" to timeZone
            ).filterValues { it != null }
        )

        withTimeout(15_000) {
            docRef.set(base).awaitIo(dp)
        }
        DraftRef(
            id = docRef.id,
            path = "users/$uid/drafts/${docRef.id}",
            kind = kind
        )
    }


    // ─────────────────────────── Observe ──────────────────────────────

    /**
     * Live-observe a document path created by [createDraft].
     * Emits:
     *  - ParseState.Pending(noteText)
     *  - ParseState.Error(message)
     *  - ParseState.Ready(fullData)
     */
    override fun observe(path: String): Flow<ParseState> = callbackFlow {
        val reg = db.document(path).addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(ParseState.Error(err.message ?: "Listener error"))
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(ParseState.Error("Document not found"))
                return@addSnapshotListener
            }
            trySend(snap.toParseState())
        }
        awaitClose { reg.remove() }
    }.buffer(Channel.Factory.CONFLATED)

    private fun DocumentSnapshot.toParseState(): ParseState {
        val d = data ?: emptyMap<String, Any?>()
        val status = (d["status"] as? String)?.lowercase(Locale.ROOT) ?: "pending"
        // Show the original note while pending; accept either "note" or "input"
        val pendingText = (d[INPUT_FIELD] ?: d["note"] ?: d["input"]) as? String

        return when (status) {
            "pending" -> ParseState.Pending(pendingText)
            "error"   -> ParseState.Error((d["error"] as? String) ?: "Unknown error")
            else      -> ParseState.Ready(d)
        }
    }

    // ───────────────────────────── Save ───────────────────────────────

    /**
     * Applies user edits and marks the entry as saved.
     * - Stores date as a Firebase [Timestamp] (parsed from "yyyy-MM-dd").
     * - Sets updatedAt server timestamp.
     */
    override suspend fun saveExpense(id: String, e: ParsedEntry.Expense) {
        val docRef = db.collection("users").document(userUId).collection("transactions").document(id)

        val dateMillis: Long = withContext(dp.default) {
            e.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()
        }

        val res = resolveExpenseCategoryIds(e.category, e.subcategory)
        Log.d("CategoryIssue", "saveExpense: ${e.category}, ${res.categoryId}, ${res.subcategoryId} ")

        val updateExpense = mapOf(
            "transactionID"  to id,
            "kind"           to e.kind,
            "input"          to e.input,
            "amount"         to e.amount,
            "currency"       to (e.currency?.uppercase(Locale.ROOT)),
            "categoryID"     to res.categoryId,
            "subcategoryID"  to res.subcategoryId,
            "merchant"       to e.merchant?.ifBlank { null },
            "note"           to e.notes?.ifBlank { null },
            "date"           to Timestamp(Date(dateMillis)),
            "status"         to "saved",
            "updatedAt"      to FieldValue.serverTimestamp()
        )

        val now = System.currentTimeMillis()
        val transaction = TransactionEntity(
            transactionID = id,
            userId = userUId,
            kind = e.kind.toString(),
            amount = e.amount ?: 0.0,
            currencyCode = (e.currency?.uppercase(Locale.ROOT) ?: ""),
            categoryOrTypeID = res.categoryId,
            subcategoryID = res.subcategoryId,
            merchant = e.merchant,
            note = e.notes,
            date = dateMillis,
            createdAtUTC = now,
            localeUpdatedAt = now ,
            remoteUpdatedAt = 0L,
            isDeleted = false,
            isDirty = true,
        )

        // save to firebase
        withTimeout(15_000) {
            docRef.set(updateExpense).awaitIo(dp)
        }
        // save to room
        saveTransaction(transaction)

        runCatching {
            val snap = withTimeout(10_000) { docRef.get().awaitIo(dp) }
            val serverMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            transactionDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
        }

        // update expense in account Summary
        insertAccountIfAbsent(currency = e.currency?.uppercase(Locale.ROOT) ?: "", updateAt = now)
        updateExpense(expense = e.amount ?: 0.0, updatedAt = now)

    }

    override suspend fun saveIncome(id: String, i: ParsedEntry.Income) {
        val docRef = db.collection("users").document(userUId).collection("transactions").document(id)

        val dateMillis: Long = withContext(dp.default) {
            i.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()  // prefer start-of-day over System.currentTimeMillis()
        }

        val res = resolveIncomeTypeId(i.type)

        val updateIncome = mapOf(
            "transactionID"  to id,
            "kind"           to i.kind,
            "input"          to i.input,
            "amount"         to i.amount,
            "currency"       to i.currency?.uppercase(Locale.ROOT),
            "typeID"         to res.categoryId,
            "date"           to Timestamp(Date(dateMillis)),
            "note"           to i.notes?.ifBlank { null },
            "status"         to "saved",
            "updatedAt"      to FieldValue.serverTimestamp()
        )

        val now = System.currentTimeMillis()
        val transaction = TransactionEntity(
            transactionID = id,
            userId = userUId,
            kind = i.kind.toString(),
            amount = i.amount ?: 0.0,
            currencyCode = (i.currency?.uppercase(Locale.ROOT) ?: ""),
            categoryOrTypeID = res.categoryId,
            note = i.notes,
            date = dateMillis,
            createdAtUTC = now,
            localeUpdatedAt = now ,
            remoteUpdatedAt = 0L,
            isDeleted = false,
            isDirty = true,
        )

        withTimeout(15_000) {
            docRef.set(updateIncome).awaitIo(dp)
        }

        saveTransaction(transaction)

        runCatching {
            val snap = withTimeout(10_000) { docRef.get().awaitIo(dp) }
            val serverMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            transactionDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
        }

        // update income in account Summary
        insertAccountIfAbsent(currency = i.currency?.uppercase(Locale.ROOT) ?: "", updateAt = now)
        updateIncome(income = i.amount ?: 0.0, updatedAt = now)
    }

    override suspend fun saveGoal(id: String, g: ParsedEntry.Goal) {

        val docRef = db.collection("users").document(userUId).collection("savings").document(id)

        val dateMillis: Long = withContext(dp.default) {
            g.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()  // prefer start-of-day over System.currentTimeMillis()
        }

        val res = resolveSavingTypeId(g.type)
        val now = System.currentTimeMillis()
        val updateGoal = mapOf(
            "savingID"      to id,
            "input"         to g.input,
            "title"         to g.title,
            "typeID"        to res.categoryId,
            "amount"        to g.amount,
            "currency"      to g.currency?.uppercase(Locale.ROOT),
            "dueDate"       to g.dueDate,
            "startAmount"   to g.startAmount,
            "date"          to Timestamp(Date(dateMillis)),
            "note"          to g.notes?.ifBlank { null },
            "status"        to "saved",
            "updatedAt"     to FieldValue.serverTimestamp()
        )

        withTimeout(15_000) {
            docRef.update(updateGoal).await()
        }
    }


    // ─────────────────────────── Delete (ID) ──────────────────────────

    /**
     * Deletes draft for path users/{uid}/{collection}/{id}  if not saved.
     */

    override suspend fun deleteIfNotSaved(kind: EntryKind, id: String) {
        val uid = auth.currentUser?.uid ?: error("Not logged in")
        val ref = db.collection("users").document(uid).collection("drafts").document(id)

        val status = withTimeout(10_000) {
            ref.get().awaitIo(dp).getString("status") ?: "pending"
        }
        if (!status.equals("saved", ignoreCase = true)) {
            withTimeout(10_000) { ref.delete().awaitIo(dp) }
        }
    }


    /**
     *  -------------------------- Locale - Transaction Dao ---------------------------------------
     */


    // ---------  Transaction Dao methods

    suspend fun saveTransaction(transaction: TransactionEntity) = transactionDao.saveTransaction(transaction)

//    suspend fun saveAllTransactions(listOfTransaction: List<TransactionEntity>) = transactionDao.upsertAll(listOfTransaction)

//    suspend fun markDelete(id: String) = transactionDao.markDeleted(id)


    fun observeTransactions(
        userId: String = userUId,
        kind: String,
        startDate: Long,
        endDate: Long,
        listOfCategory: List<String>): Flow<List<TransactionEntity>>{

        return if(listOfCategory.isEmpty()){
            transactionDao.observeMonthly(userId, kind, startDate, endDate)
        } else{
            transactionDao.observeByCategory(userId, kind, startDate, endDate, listOfCategory)
        }
    }

//    fun observeCategoryTotal

    fun observeCategoryTotal(
        userId: String = userUId,
        kind: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryTotal>>{
        return transactionDao.observeCategoryTotals(userId, kind, startDate, endDate)
    }






//
//    fun observeMonthly(userId: String, kind: String, startDate: Long, endDate: Long): Flow<List<Transaction>>
//     = dao.observeMonthly(userId, kind, startDate, endDate)
//
//    fun observeByCategory(userId: String, kind: String, listOfCategory: List<String>, startDate: Long, endDate: Long)
//     = dao.observeByCategory(userId, kind, startDate, endDate, listOfCategory)



    //  ------------ account Summary dao

    suspend fun insertAccountIfAbsent(
        currency: String,
        updateAt: Long,
    ) =
        accountSummaryDao.insertAccountIfAbsent(
            AccountSummaryEntity(
                userId = userUId,
                currencyCode = currency,
                updatedAt = updateAt,
            )
        )

//    suspend fun applyDelta(
//        expense: Double,
//        income: Double,
//        saving: Double,
//        updatedAt: Long
//    ) =
//        accountSummaryDao.applyDelta(userUId, expense, income, saving, updatedAt)

    suspend fun updateIncome(income: Double, updatedAt: Long) =
        accountSummaryDao.updateIncome(userUId, income, updatedAt)

    suspend fun updateExpense(expense: Double, updatedAt: Long) =
        accountSummaryDao.updateExpense(userUId, expense, updatedAt)

    suspend fun updateSaving(saving: Double, updatedAt: Long) =
        accountSummaryDao.updateSaving(userUId, saving, updatedAt)


    fun observeAccountSummary(): Flow<AccountSummaryEntity?> =
        accountSummaryDao.observeAccountSummary(userUId)

}
