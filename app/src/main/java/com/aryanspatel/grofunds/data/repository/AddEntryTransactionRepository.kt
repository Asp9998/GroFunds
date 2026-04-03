package com.aryanspatel.grofunds.data.repository

import android.util.Log
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.core.awaitIo
import com.aryanspatel.grofunds.data.local.DTO.ContributionRow
import com.aryanspatel.grofunds.data.local.DTO.SavingRow
import com.aryanspatel.grofunds.data.local.dao.AccountSummaryDao
import com.aryanspatel.grofunds.data.local.dao.SavingContributionsDao
import com.aryanspatel.grofunds.data.local.dao.SavingsDao
import com.aryanspatel.grofunds.data.local.dao.SyncStateDao
import com.aryanspatel.grofunds.data.local.dao.TransactionDao
import com.aryanspatel.grofunds.data.local.entity.AccountSummaryEntity
import com.aryanspatel.grofunds.data.local.entity.SavingContributionEntity
import com.aryanspatel.grofunds.data.local.entity.SavingsEntity
import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.remote.model.CategoryTotal
import com.aryanspatel.grofunds.data.remote.model.SavingsDoc
import com.aryanspatel.grofunds.data.remote.model.TransactionDoc
import com.aryanspatel.grofunds.domain.model.DraftRef
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
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
import java.util.Date
import java.util.Locale
import java.util.UUID
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
    private val currentUser: CurrentUserProvider,
    private val transactionDao: TransactionDao,
    private val savingsDao: SavingsDao,
    private val savingContributionsDao: SavingContributionsDao,
    private val syncStateDao: SyncStateDao,
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

//    private val userUId = auth.currentUser?.uid ?: error("Not logged in")
    private val userUid = currentUser.userIdOrNull() ?: error("Not logged in")

//    fun getCurrentUser(): String?{
//        return auth.currentUser?.uid
//    }

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
        val docRef = db.collection("users").document(userUid).collection("transactions").document(id)

        val dateMillis: Long = withContext(dp.default) {
            e.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()
        }

        val res = resolveExpenseCategoryIds(e.category, e.subcategory)
        Log.d("CategoryIssue", "saveExpense: ${e.category}, ${res.categoryId}, ${res.subcategoryId} ")

//        val updateExpense = mapOf(
//            "transactionID"        to id,
//            "kind"                 to e.kind,
//            "input"                to e.input,
//            "amount"               to e.amount,
//            "currency"             to (e.currency?.uppercase(Locale.ROOT)),
//            "categoryOrTypeID"     to res.categoryId,
//            "subcategoryID"        to res.subcategoryId,
//            "merchant"             to e.merchant?.ifBlank { null },
//            "note"                 to e.notes?.ifBlank { null },
//            "date"                 to Timestamp(Date(dateMillis)),
//            "status"               to "saved",
//            "updatedAt"            to FieldValue.serverTimestamp()
//        )

        val payload  = TransactionDoc(
            transactionID   = id,
            kind            = e.kind,
            input           = e.input,
            amount          = e.amount,
            currency        = e.currency?.uppercase(Locale.ROOT),
            categoryOrTypeID= res.categoryId,
            subcategoryID   = res.subcategoryId,
            merchant        = e.merchant?.ifBlank { null },
            note            = e.notes?.ifBlank { null },
            date            = Timestamp(Date(dateMillis)),
            status          = "saved",
            isExcluded      = false  ,
            updatedAt       = null                  // important: let server set it
        )

        val now = System.currentTimeMillis()
        val transaction = TransactionEntity(
            transactionID = id,
            userId = userUid,
            input = e.input ?: "",
            kind = e.kind.toString(),
            amount = e.amount ?: 0.0,
            currencyCode = (e.currency?.uppercase(Locale.ROOT) ?: ""),
            categoryOrTypeID = res.categoryId,
            subcategoryID = res.subcategoryId,
            merchant = e.merchant,
            note = e.notes,
            date = dateMillis,
            createdAtUTC = now,
            localUpdatedAt = now ,
            remoteUpdatedAt = 0L,
            isDeleted = false,
            isDirty = true,
        )

        // save to firebase
        withTimeout(15_000) {
            docRef.set(payload).awaitIo(dp)
        }

        insertAccountIfAbsent(currency = e.currency?.uppercase(Locale.ROOT) ?: "", updateAt = now)
        saveTransaction(transaction)
        runCatching {
            val snap = withTimeout(10_000) { docRef.get().awaitIo(dp) }
            val serverMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            transactionDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
        }

    }

    override suspend fun saveIncome(id: String, i: ParsedEntry.Income) {
        val docRef = db.collection("users").document(userUid).collection("transactions").document(id)

        val dateMillis: Long = withContext(dp.default) {
            i.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()  // prefer start-of-day over System.currentTimeMillis()
        }

        val res = resolveIncomeTypeId(i.type)

//        val updateIncome = mapOf(
//            "transactionID"  to id,
//            "kind"           to i.kind,
//            "input"          to i.input,
//            "amount"         to i.amount,
//            "currency"       to i.currency?.uppercase(Locale.ROOT),
//            "categoryOrTypeID"         to res.categoryId,
//            "date"           to Timestamp(Date(dateMillis)),
//            "note"           to i.notes?.ifBlank { null },
//            "status"         to "saved",
//            "updatedAt"      to FieldValue.serverTimestamp())

        val payload  = TransactionDoc(
            transactionID   = id,
            kind            = i.kind,
            input           = i.input,
            amount          = i.amount,
            currency        = i.currency?.uppercase(Locale.ROOT),
            categoryOrTypeID= res.categoryId,
            note            = i.notes?.ifBlank { null },
            date            = Timestamp(Date(dateMillis)),
            status          = "saved",
            isExcluded      = false,
            updatedAt       = null                   // important: let server set it
        )

        val now = System.currentTimeMillis()
        val transaction = TransactionEntity(
            transactionID = id,
            userId = userUid,
            input = i.input ?: "",
            kind = i.kind.toString(),
            amount = i.amount ?: 0.0,
            currencyCode = (i.currency?.uppercase(Locale.ROOT) ?: ""),
            categoryOrTypeID = res.categoryId,
            note = i.notes,
            date = dateMillis,
            createdAtUTC = now,
            localUpdatedAt = now ,
            remoteUpdatedAt = 0L,
            isDeleted = false,
            isDirty = true,
        )

        withTimeout(15_000) {
            docRef.set(payload).awaitIo(dp)
        }

        // update income in account Summary
        insertAccountIfAbsent(currency = i.currency?.uppercase(Locale.ROOT) ?: "", updateAt = now)
        saveTransaction(transaction)
        runCatching {
            val snap = withTimeout(10_000) { docRef.get().awaitIo(dp) }
            val serverMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            transactionDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
        }

    }

    override suspend fun saveGoal(id: String, g: ParsedEntry.Goal) {

        val docRef = db.collection("users").document(userUid).collection("savings").document(id)

        val dateMillis: Long = withContext(dp.default) {
            g.dateText?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()  // prefer start-of-day over System.currentTimeMillis()
        }

        val dueDateMillis: Long = withContext(dp.default) {
            g.dueDate?.let { txt ->
                runCatching { DateConverters.stringToMillis(txt) }.getOrNull()
            } ?: System.currentTimeMillis()  // prefer start-of-day over System.currentTimeMillis()
        }

        val res = resolveSavingTypeId(g.type)
        val now = System.currentTimeMillis()

        val payload = SavingsDoc(
            kind = g.kind.toString(),
            savingsId = id,
            input = g.input,
            title = g.title,
            type = g.type,
            amount = g.amount,
            currencyId = g.currency,
            dueDate = Timestamp(Date(dueDateMillis)),
            createdAt = Timestamp(Date(dateMillis)),
            note = g.notes,
            status = "saved",
            updatedAt = null,
        )

        val saving = SavingsEntity(
            savingId = id,
            userId = userUid,
            input = g.input ?: "",
            targetAmount = g.amount ?: 0.0,
            startAmount = g.startAmount ?: 0.0,
            savedAmount = g.startAmount ?: 0.0,
            typeId = res.categoryId,
            title = g.title ?: "",
            date = dateMillis,
            dueDate = dueDateMillis,
            note = g.notes ?: "",
            createdAt = now,
            localUpdatedAt = now,
            remoteUpdatedAt = 0L,
            isDirty = true,
            isDeleted = false,
        )

        val uuid = UUID.randomUUID().toString()

        val initialContribution = SavingContributionEntity(
            contributionId = uuid,
            savingId = id,
            note = "Start Amount",
            amount = g.startAmount ?: 0.0,
            createdAt = dateMillis,
            localUpdatedAt = now,
            remoteUpdatedAt = 0L,
            isDirty = true,
            isDeleted = false,
        )


        withTimeout(15_000) {
            docRef.set(payload).await()
        }

        insertAccountIfAbsent(currency = g.currency?.uppercase(Locale.ROOT) ?: "", updateAt = now)
        saveSaving(saving)
        addContribution(initialContribution)

        runCatching {
            val snap = withTimeout(10_000) { docRef.get().awaitIo(dp) }
            val serverMillis = snap.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            savingsDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
            savingContributionsDao.updateRemoteUpdatedAt(id = id, remoteUpdatedAt = serverMillis)
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

    suspend fun saveTransaction(transaction: TransactionEntity) {
        transactionDao.saveTransaction(transaction)
        // update account summary -- adding

        when(transaction.kind){
            EntryKind.EXPENSE.name -> updateExpense(expense = transaction.amount, updatedAt = transaction.localUpdatedAt)
            EntryKind.INCOME.name -> updateIncome(income = transaction.amount, updatedAt = transaction.localUpdatedAt)
            else -> updateSaving(saving = transaction.amount, updatedAt = transaction.localUpdatedAt)
        }
    }

    suspend fun markTransactionDelete(transactionId: String, kind: String, amount: Double, deletedAtUTC: Long){
        transactionDao.markDeleted(transactionId, userId = userUid, deletedAtUTC = deletedAtUTC)

        when(kind){
            EntryKind.EXPENSE.name -> updateExpense(expense = -amount, updatedAt = deletedAtUTC)
            EntryKind.INCOME.name -> updateIncome(income = -amount, updatedAt = deletedAtUTC)
            else -> updateSaving(saving = -amount, updatedAt = deletedAtUTC)
        }

    }




//    suspend fun saveAllTransactions(listOfTransaction: List<TransactionEntity>) = transactionDao.upsertAll(listOfTransaction)

//    suspend fun markDelete(id: String) = transactionDao.markDeleted(id)


    fun observeTransactions(
        userId: String = userUid,
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

        userId: String = userUid,
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


    // --------------- Savings dao

    suspend fun saveSaving(saving: SavingsEntity){
        savingsDao.saveSaving(saving)
    }

    fun observeSavings(): Flow<List<SavingRow>> =
        savingsDao.observeSavings(userId = userUid)


    // ----------------- saving contribution dao

    suspend fun addContribution(contribution: SavingContributionEntity){
        savingContributionsDao.addContribution(contribution)

        updateSaving(saving = contribution.amount, updatedAt = contribution.localUpdatedAt)
    }

    fun observeContributions(savingId: String): Flow<List<ContributionRow>> = savingContributionsDao.observeContributionBySaving(savingId)


    //  ------------ account Summary dao

    suspend fun insertAccountIfAbsent(
        currency: String,
        updateAt: Long,
    ) =
        accountSummaryDao.insertAccountIfAbsent(
            AccountSummaryEntity(
                userId = userUid,
                currencyCode = currency,
                remoteUpdatedAt = updateAt,
                localeUpdatedAt = updateAt,
                isDirty = false,
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
        accountSummaryDao.updateIncome(userUid, income, updatedAt)

    suspend fun updateExpense(expense: Double, updatedAt: Long) =
        accountSummaryDao.updateExpense(userUid, expense, updatedAt)

    suspend fun updateSaving(saving: Double, updatedAt: Long) =
        accountSummaryDao.updateSaving(userUid, saving, updatedAt)


    fun observeAccountSummary(): Flow<AccountSummaryEntity?> =
        accountSummaryDao.observeAccountSummary(userUid)
}
