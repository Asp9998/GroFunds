package com.aryanspatel.grofunds.fakeRepo.repository

import com.aryanspatel.grofunds.domain.model.DraftRef
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

class FakeAddEntryRepository ( private val uid:String = "U_TEST"): AddEntryRepository {

    private companion object{
        const val INPUT_FIELD = "input"
    }

    /** ------- In-memory storage ------- */
    /** ------- Document store : Path to Fields ------- */
    private val docs: MutableMap<String, MutableMap<String, Any?>> = mutableMapOf()

    /** Per-path stream: path -> flow of ParseState (replay latest) */
    private val streams: MutableMap<String, MutableSharedFlow<ParseState>> = mutableMapOf()

    private val lock = Mutex() // to keep store/stream updates atomic in tests

    private fun streamFor(path: String) =
        streams.getOrPut(path) { MutableSharedFlow(replay = 1) }

    private fun collectionFor(kind: EntryKind) = when (kind) {
        EntryKind.EXPENSE -> "expenses"
        EntryKind.INCOME  -> "incomes"
        EntryKind.GOAL    -> "goals"
    }


    // ───────────────────────────── Create ─────────────────────────────
    override suspend fun createDraft(
        kind: EntryKind,
        note: String,
        currencyHint: String?,
        localeHint: String?,
        timeZone: String?
    ): DraftRef {
        val col = collectionFor(kind)
        val id = "DRAFT_${System.nanoTime()}"
        val path = "users/$uid/$col/$id"


        val client = buildMap<String, Any> {
            currencyHint?.let {put ("currencyHint", it)}
            localeHint?.let {put ("localeHint", it)}
            timeZone?.let { put("timeZone", it) }
        }

        val payload  = mutableMapOf<String, Any?>(
            INPUT_FIELD to note.trim(),
            "status" to "pending",
            "updatedAt" to 0L,
            "createdAt" to 0L,
            "_client" to client
        )

        lock.withLock {
            docs[path]  = payload
            streamFor(path).tryEmit(ParseState.Pending(note.trim()))
        }
        return DraftRef(id = id, path = path, kind = kind)
    }

    // ─────────────────────────── Observe ──────────────────────────────
    override fun observe(path: String): Flow<ParseState> {
        val stream = streamFor(path)
        val doc = docs[path]

        val result = if(doc == null){
            ParseState.Error("Document not found")
        }
        else{
            val status = (doc["status"] as? String)?.lowercase() ?: "pending"
            when (status) {
                "pending" -> ParseState.Pending(doc[INPUT_FIELD] as? String)
                "error" -> ParseState.Error(doc["error"] as? String ?: "Unknown error")
                else -> ParseState.Ready(doc.toMap())
            }
        }

        stream.tryEmit(result)
        return stream
    }

    // ───────────────────────────── Save ───────────────────────────────
    override suspend fun saveExpense( path: String, e: ParsedEntry.Expense) {
        lock.withLock {
            val doc = docs[path] ?: return
            doc["amount"]      = e.amount
            doc["currency"]    = (e.currency?.uppercase(Locale.ROOT))
            doc["category"]    = e.category
            doc["subcategory"] = e.subcategory
            doc["merchant"]    = e.merchant?.ifBlank { null }
            doc["note" ]       = e.notes?.ifBlank { null }
            doc["date"]        = "TEST_DATE"
            doc["status"]      = "saved"
            doc["userEdited"]  = true
            doc["updatedAt"]   = 0L
            streamFor(path).tryEmit(ParseState.Ready(doc.toMap()))
        }
    }

    override suspend fun saveIncome(path: String, i: ParsedEntry.Income) {
        lock.withLock {
            val doc = docs[path] ?: return
            doc["amount"]      = i.amount
            doc["currency"]    = i.currency?.uppercase(Locale.ROOT)
            doc["type"]        = i.type
            doc["date"]        = "TEST_DATE"
            doc["note"]        = i.notes?.ifBlank { null }
            doc["status"]      = "saved"
            doc["updatedAt"]   = 0L
            streamFor(path).tryEmit(ParseState.Ready(doc.toMap()))
        }
    }

    override suspend fun saveGoal(path: String, g: ParsedEntry.Goal) {
        lock.withLock {
            val doc = docs[path] ?: return
            doc["title"]         = g.title
            doc["type"]          = g.type
            doc["amount"]        = g.amount
            doc["currency"]      = g.currency?.uppercase(Locale.ROOT)
            doc["dueDate"]       = g.dueDate
            doc["startAmount"]   = g.startAmount
            doc["date"]          = "TEST_DATE"
            doc["note"]          = g.notes?.ifBlank { null }
            doc["status"]        = "saved"
            doc["updatedAt"]     = 0L
            streamFor(path).tryEmit(ParseState.Ready(doc.toMap()))
        }
    }

    override suspend fun deleteIfNotSaved(
        kind: EntryKind,
        id: String,
    ) {
        val col = collectionFor(kind)
        val path = "users/$uid/$col/$id"

        lock.withLock {
            val status = (docs[path]?.get("status") as? String)?.lowercase() ?: "pending"
            if(status != "saved"){
                docs.remove(path)
                streamFor(path).tryEmit(ParseState.Error("Deleted"))
            }
        }
    }


    // Optional helpers for tests
    suspend fun setReady(path: String, parsed: Map<String, Any?>) {
        lock.withLock {
            val doc = docs[path] ?: error("No draft at $path")
            doc.putAll(parsed)
            doc["status"] = "ready"
            doc["updatedAt"] = 0L
            streamFor(path).emit(ParseState.Ready(doc.toMap()))
        }
    }

    suspend fun setError(path: String, message: String) {
        lock.withLock {
            val doc = docs[path] ?: error("No draft at $path")
            doc["status"] = "error"
            doc["error"] = message
            doc["updatedAt"] = 0L
            streamFor(path).emit(ParseState.Error(message))
        }
    }


}