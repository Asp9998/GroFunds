package com.aryanspatel.grofunds.data.repository

import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.common.awaitIo
import com.aryanspatel.grofunds.presentation.common.DraftRef
import com.aryanspatel.grofunds.presentation.common.ParseState
import com.aryanspatel.grofunds.presentation.common.ParsedEntry
import com.aryanspatel.grofunds.presentation.common.model.EntryKind
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
import java.text.SimpleDateFormat
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
class AddEntryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val dp: DispatcherProvider
) {

    /** Change to "input" if your CF expects that key instead of "note". */
    private companion object {
        const val INPUT_FIELD = "input"
    }

    private fun collectionFor(kind: EntryKind) = when (kind) {
        EntryKind.EXPENSE -> "expenses"
        EntryKind.INCOME  -> "incomes"
        EntryKind.GOAL    -> "goals"
    }

    // ───────────────────────────── Create ─────────────────────────────

    /**
     * Creates a draft that triggers the CF (status = "pending").
     * Returns the created document's [com.aryanspatel.grofunds.presentation.common.DraftRef] (id + fully-qualified path).
     */
    suspend fun createDraft(
        kind: EntryKind,
        note: String,
        currencyHint: String? = null,   // optional hints if your CF reads them
        localeHint: String? = null,
        timeZone: String? = null
    ): DraftRef {
        val uid = auth.currentUser?.uid ?: error("Not logged in")
        val col = collectionFor(kind)
        val docRef = db.collection("users").document(uid).collection(col).document()


        // Minimal payload; add nested _client hints if you want to pass them safely.
        val base = mutableMapOf<String, Any?>(
            INPUT_FIELD to note.trim(),
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "_client" to mapOf( // nested so CF can ignore if it wants
                "currencyHint" to currencyHint,
                "localeHint" to localeHint,
                "timeZone" to timeZone
            ).filterValues { it != null }
        )

        withTimeout(15_000) {
            docRef.set(base).awaitIo(dp)
        }
        return DraftRef(
            id = docRef.id,
            path = "users/$uid/$col/${docRef.id}",
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
    fun observe(path: String): Flow<ParseState> = callbackFlow {
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
     * - Stores date as a Firebase [com.google.firebase.Timestamp] (parsed from "yyyy-MM-dd").
     * - Sets updatedAt server timestamp.
     */
    suspend fun saveExpense(path: String, e: ParsedEntry.Expense) {
        val ref = db.document(path)

        val date: Date = withContext(dp.default) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            runCatching { sdf.parse(e.dateText) }.getOrNull() ?: Date()
        }

        val updateExpense = mapOf(
            "amount"     to e.amount,
            "currency"   to (e.currency?.uppercase(Locale.ROOT) ?: "CAD"),
            "category"   to e.category,
            "subcategory" to e.subcategory,
            "merchant"   to e.merchant?.ifBlank { null },
            "note"       to e.notes?.ifBlank { null },
            "date"       to Timestamp(date),
            "status"     to "saved",
            "userEdited" to true,
            "updatedAt"  to FieldValue.serverTimestamp()
        )

        withTimeout(15_000) {
            ref.update(updateExpense).awaitIo(dp)
        }
    }

    suspend fun saveIncome(path: String, i: ParsedEntry.Income) {
        val ref = db.document(path)

        val date: Date = withContext(dp.default) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            runCatching { sdf.parse(i.dateText) }.getOrNull() ?: Date()
        }
        val updateIncome = mapOf(
            "amount"     to i.amount,
            "currency"   to i.currency?.uppercase(Locale.ROOT),
            "type"       to i.type,
            "date"       to Timestamp(date),
            "note"       to i.notes?.ifBlank { null },
            "status"     to "saved",
            "updatedAt"  to FieldValue.serverTimestamp()
        )
        withTimeout(15_000) {
            ref.update(updateIncome).awaitIo(dp)
        }
    }

    suspend fun saveGoal(path: String, g: ParsedEntry.Goal) {
        val ref = db.document(path)

        val date: Date = withContext(dp.default) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            runCatching { sdf.parse(g.dateText) }.getOrNull() ?: Date()
        }

        val updateGoal = mapOf(
            "title"      to g.title,
            "type"   to g.type,
            "amount"  to g.amount,
            "currency"      to g.currency?.uppercase(Locale.ROOT),
            "dueDate"       to g.dueDate,
            "startAmount"   to g.startAmount,
            "date"          to Timestamp(date),
            "note"          to g.notes?.ifBlank { null },
            "status"        to "saved",
            "updatedAt"  to FieldValue.serverTimestamp()
        )

        withTimeout(15_000) {
            ref.update(updateGoal).await()
        }
    }




    // ─────────────────────────── Delete (ID) ──────────────────────────

    /**
     * Deletes users/{uid}/{collection}/{id}.
     * Use for cleanup on Reset/Back when the draft hasn't been saved.
     */

    suspend fun deleteIfNotSaved(kind: EntryKind, id: String) {
        val uid = auth.currentUser?.uid ?: error("Not logged in")
        val col = collectionFor(kind)
        val ref = db.collection("users").document(uid).collection(col).document(id)

        val status = withTimeout(10_000) {
            ref.get().awaitIo(dp).getString("status") ?: "pending"
        }
        if (!status.equals("saved", ignoreCase = true)) {
            withTimeout(10_000) { ref.delete().awaitIo(dp) }
        }
    }
}