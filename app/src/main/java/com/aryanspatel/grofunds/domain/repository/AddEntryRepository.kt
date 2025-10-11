package com.aryanspatel.grofunds.domain.repository

import com.aryanspatel.grofunds.domain.model.DraftRef
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import kotlinx.coroutines.flow.Flow

interface AddEntryRepository {
    suspend fun createDraft(
        kind: EntryKind,
        note: String,
        currencyHint: String? = null,   // optional hints if your CF reads them
        localeHint: String? = null,
        timeZone: String? = null
    ): DraftRef

    fun observe(path: String) : Flow<ParseState>

    suspend fun saveExpense(id:String, e: ParsedEntry.Expense)

    suspend fun saveIncome(id:String, i: ParsedEntry.Income)
    suspend fun saveGoal(id:String, g:ParsedEntry.Goal)

    suspend fun deleteIfNotSaved(kind: EntryKind, id: String)

}