package com.aryanspatel.grofunds.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.data.repository.AddEntryRepository
import com.aryanspatel.grofunds.presentation.common.ParseState
import com.aryanspatel.grofunds.presentation.common.ParsedEntry
import com.aryanspatel.grofunds.presentation.common.SaveState
import com.aryanspatel.grofunds.presentation.common.SubmitState
import com.aryanspatel.grofunds.presentation.common.model.AddEntryUiState
import com.aryanspatel.grofunds.presentation.common.model.EntryKind
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the “Add Entry” flow.
 *
 * Exposes three state machines:
 *  - submitState: create draft (status="pending") -> Success(draft) | Error
 *  - state:      live parse state of the created document (Pending | Ready | Error)
 *  - saveState:  saving user edits -> Success(path) | Error
 *
 * Public API:
 *  - submitNote(kind, note, hints...)        // create draft & trigger CF
 *  - start(path) / clear()                    // begin/stop listening to the doc
 *  - saveExpense(path, edits) + resetSaveState()
 *  - resetState()                             // reset submitState to Idle
 *  - deleteById(kind, id)                     // clean up draft by doc id
 */
@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val repo: AddEntryRepository,
    private val dp: DispatcherProvider
) : ViewModel() {

    // ───────────────────────── Draft creation state ─────────────────────────

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState

    private val _state = MutableStateFlow<ParseState>(ParseState.Pending(note = null))
    val state: StateFlow<ParseState> = _state

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState: StateFlow<AddEntryUiState> = _uiState
    
    /**
     * Create a draft document and emit SubmitState.Success(draft) on success.
     * The UI should then call start(draft.path) to observe parsing updates.
     */
    fun submitNote(
        kind: EntryKind,
        note: String,
        currencyHint: String? = null,
        localeHint: String? = null,
        timeZone: String? = null
    ) {
        if (note.isBlank()) {
            _submitState.value = SubmitState.Error("Note cannot be empty")
            return
        }

        _submitState.value = SubmitState.Submitting
        viewModelScope.launch(dp.iO) {
            runCatching {
                repo.createDraft(kind, note, currencyHint, localeHint, timeZone)
            }.onSuccess { draft ->
                _submitState.value = SubmitState.Success(draft)
            }.onFailure { t ->
                _submitState.value = SubmitState.Error(t.message ?: "Failed to save draft")
            }
        }
    }

    /** Reset the draft creation state machine to Idle. */
    fun resetState() { _submitState.value = SubmitState.Idle }

    // ───────────────────────── Parse/observe state ─────────────────────────



    private var watchJob: Job? = null
    private var currentPath: String? = null

    /** Start (or restart) listening to a document path produced by submitNote(). */
    fun start(path: String) {
        if (currentPath == path && watchJob?.isActive == true) return
        watchJob?.cancel()
        currentPath = path
        _state.value = ParseState.Pending(note = null)

        watchJob = viewModelScope.launch {
            repo.observe(path).collect { emitted ->
                // Ignore late emissions from a canceled/older listener
                if (currentPath == path) _state.value = emitted
            }
        }
    }

    /** Stop listening and reset the parse state to a neutral pending state. */
    fun clear() {
        currentPath = null
        watchJob?.cancel()
        watchJob = null
        _state.value = ParseState.Pending(note = null)
    }

    // ───────────────────────────── Saving state ─────────────────────────────

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState



    /** Set the ui State after parsing input  **/
    fun setParseData(){

        _uiState.update { ui ->
            when (_state.value) {
                is ParseState.Pending -> {
                    ui.copy(
                        parseError = null,
                        parsePreview = (_state.value as ParseState.Pending).note,   // show the raw note while CF is working
                        // optional: add isParsing flag to ui if you want
                    )
                }

                is ParseState.Error -> {
                    ui.copy(
                        parseError = (_state.value as ParseState.Error).message,
                        parsePreview = null
                    )
                }

                is ParseState.Ready -> {
                    val parsed = mapToParsedEntry((_state.value as ParseState.Ready).data, ui.kind)

                    // Helpers: only fill if parsed has a non-blank value
                    fun String?.orKeep(old: String): String =
                        this?.takeIf { it.isNotBlank() } ?: old

                    fun Double?.formatOrKeep(old: String): String =
                        this?.let { "%.2f".format(it) } ?: old

                    when (parsed) {
                        is ParsedEntry.Expense -> ui.copy(
                            amount = parsed.amount.formatOrKeep(ui.amount),
                            currency = parsed.currency.orKeep(ui.currency),
                            date = parsed.dateText.orKeep(ui.date),
                            note = parsed.notes.orKeep(ui.note),
                            categoryOrType = parsed.category.orKeep(ui.categoryOrType),
                            expenseSubcategory = parsed.subcategory.orKeep(ui.expenseSubcategory),
                            expenseMerchant = parsed.merchant.orKeep(ui.expenseMerchant),
                            parsePreview = null,
                            parseError = null,
                            isParsed = true
                        )

                        is ParsedEntry.Income -> ui.copy(
                            amount = parsed.amount.formatOrKeep(ui.amount),
                            currency = parsed.currency.orKeep(ui.currency),
                            date = parsed.dateText.orKeep(ui.date),
                            note = parsed.notes.orKeep(ui.note),
                            categoryOrType = parsed.type.orKeep(ui.categoryOrType),
                            parsePreview = null,
                            parseError = null,
                            isParsed = true
                        )

                        is ParsedEntry.Goal -> ui.copy(
                            amount = parsed.amount.formatOrKeep(ui.amount),
                            currency = parsed.currency.orKeep(ui.currency),
                            date = parsed.dateText.orKeep(ui.date), // you used whenText for date
                            note = parsed.notes.orKeep(ui.note),
                            goalTitle = parsed.title.orKeep(ui.goalTitle),
                            goalDueDate = parsed.dueDate.orKeep(ui.goalDueDate),
                            goalStartAmount = parsed.startAmount.formatOrKeep(ui.goalStartAmount),
                            // parsed.type -> map it where you store goal type if you add that to UI state
                            parsePreview = null,
                            parseError = null,
                            isParsed = true
                        )
                    }
                }
            }
        }
    }

    fun onInputNoteChanged(v: String)         = _uiState.update { it.copy(inputNote = v) }
    fun onAmountChanged(v: String)            = _uiState.update { it.copy(amount = v) }
    fun onCurrencyChanged(v: String)          = _uiState.update { it.copy(currency = v) }
    fun onDateChanged(v: String)              = _uiState.update { it.copy(date = v) }
    fun onCategoryOrTypeChanged(v: String)    = _uiState.update { it.copy(categoryOrType = v) }
    fun onExpenseSubChanged(v: String)        = _uiState.update { it.copy(expenseSubcategory = v) }
    fun onExpenseMerchantChanged(v: String)   = _uiState.update { it.copy(expenseMerchant = v) }
    fun onGoalTitleChanged(v: String)         = _uiState.update { it.copy(goalTitle = v) }
    fun onGoalDueDateChanged(v: String)       = _uiState.update { it.copy(goalDueDate = v) }
    fun onGoalStartAmountChanged(v: String)   = _uiState.update { it.copy(goalStartAmount = v) }
    fun onNoteChanged(v: String)              = _uiState.update { it.copy(note = v) }
    fun onDocPathChanged(v: String?)           = _uiState.update { it.copy(docPath = v) }
    fun onDraftIdChanged(v: String?)           = _uiState.update { it.copy(draftId = v) }
    fun onIsParsedChanged(v: Boolean)          = _uiState.update { it.copy(isParsed = v) }

    fun updateUiState() {
        _uiState.update {
            AddEntryUiState.INITIAL.copy()
        }
        // If you keep a parse listener job, cancel it here.
        // Also optionally delete unsaved draft (if you create drafts before save).
    }






    /** Apply edits to the same document and mark it saved. */
    fun saveExpense(path: String, edits: ParsedEntry.Expense) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch(dp.iO) {
            runCatching { repo.saveExpense(path, edits) }
                .onSuccess { _saveState.value = SaveState.Success(path) }
                .onFailure { t -> _saveState.value = SaveState.Error(t.message ?: "Failed to save") }
        }
    }

    fun saveIncome(path: String, edits: ParsedEntry.Income) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch(dp.iO) {
            runCatching { repo.saveIncome(path, edits) }
                .onSuccess { _saveState.value = SaveState.Success(path) }
                .onFailure { t -> _saveState.value = SaveState.Error(t.message ?: "Failed to save") }
        }
    }

    fun saveGoal(path: String, edits: ParsedEntry.Goal) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch(dp.iO) {
            runCatching { repo.saveGoal(path, edits) }
                .onSuccess { _saveState.value = SaveState.Success(path) }
                .onFailure { t -> _saveState.value = SaveState.Error(t.message ?: "Failed to save") }
        }
    }

    /** Reset the saving state machine to Idle. */
    fun resetSaveState() { _saveState.value = SaveState.Idle }

    // ───────────────────────────── Deletion API ─────────────────────────────

    /** Delete a draft by its document ID (users/{uid}/{collection}/{id}). */
    fun deleteDraftIfUnsaved(kind: EntryKind, id: String) {
        viewModelScope.launch(dp.iO) { runCatching { repo.deleteIfNotSaved(kind, id) } }
    }
}




private data class KeyMap(
    val amount: List<String> = emptyList(),
    val currency: List<String> = listOf("currency", "currencyCode"),
    val category: List<String> = emptyList(),
    val subcategory: List<String> = emptyList(),
    val party: List<String> = emptyList(),   // merchant/vendor or payer/source
    val date: List<String> = listOf("date", "when"),
    val notes: List<String> = listOf("notes", "memo", "description"),
    val confidence: List<String> = listOf("confidence", "confidenceScore"),
    // extras only for GOAL, etc.
    val extras: Map<String, List<String>> = emptyMap()
)

fun mapToParsedEntry(
    doc: Map<String, Any?>,
    kind: EntryKind
): ParsedEntry {
    val result = (doc["result"] as? Map<*, *>)?.mapKeys { it.key.toString() } ?: emptyMap<String, Any?>()

    fun firstString(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) as? String }?.takeIf { it.isNotBlank() }

    fun firstNumber(vararg keys: String): Double? {
        val v = keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) }
        return when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }

    fun firstTimestamp(vararg keys: String): Timestamp? =
        keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) as? Timestamp }

    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val inputPatterns = listOf("yyyy-MM-dd", "yyyy/MM/dd", "MMM d, yyyy", "d MMM yyyy")

    fun normalizeDate(keys: List<String>): String? {
        val ts = firstTimestamp(*keys.toTypedArray())
        if (ts != null) return dateFmt.format(ts.toDate())
        val s = firstString(*keys.toTypedArray()) ?: return null
        return inputPatterns.firstNotNullOfOrNull { pat ->
            runCatching { dateFmt.format(SimpleDateFormat(pat, Locale.getDefault()).parse(s)!!) }.getOrNull()
        }
    }

    val km = when (kind) {
        EntryKind.EXPENSE -> KeyMap(
            amount   = listOf("amount", "total", "value"),
            currency = listOf("currency", "currencyCode"),
            category = listOf("category", "categoryOrType", "mainCategory"),
            subcategory = listOf("subcategory", "subCategory"),
            party    = listOf("merchant", "vendor", "payee", "store"),
            date     = listOf("date", "when", "purchasedAt"),
            notes    = listOf("notes", "memo", "description")
        )
        EntryKind.INCOME -> KeyMap(
            amount   = listOf("amount", "income", "total", "value"),
            currency = listOf("currency", "currencyCode"),
            category = listOf("category", "type", "incomeType"),
            party    = listOf("source", "payer", "employer", "from"),
            date     = listOf("date", "when", "receivedAt", "paidAt"),
            notes    = listOf("notes", "memo", "description")
        )
        EntryKind.GOAL -> KeyMap(
            // For goals, "amount" means target; current/progress goes in extras
            amount   = listOf("target", "targetAmount", "goalAmount", "amount"),
            currency = listOf("currency", "currencyCode"),
            date     = listOf("due", "dueDate", "deadline"),
            notes    = listOf("notes", "memo", "description"),
            extras   = mapOf(
                "name"          to listOf("name", "goal", "title"),
                "currentAmount" to listOf("current", "saved", "currentAmount", "progressAmount", "startAmount")
            )
        )
    }

    fun up(s: String?) = s?.uppercase(Locale.ROOT)

    return when (kind) {
        EntryKind.EXPENSE -> ParsedEntry.Expense(
            amount      = firstNumber(*km.amount.toTypedArray()),
            currency    = up(firstString(*km.currency.toTypedArray())),
            category    = firstString(*km.category.toTypedArray()),
            subcategory = firstString(*km.subcategory.toTypedArray()),
            merchant    = firstString(*km.party.toTypedArray()),
            dateText    = normalizeDate(km.date),
            notes       = firstString(*km.notes.toTypedArray()),
            confidence  = firstNumber(*km.confidence.toTypedArray())
        )

        EntryKind.INCOME -> ParsedEntry.Income(
            amount     = firstNumber(*km.amount.toTypedArray()),
            currency   = up(firstString(*km.currency.toTypedArray())),
            type   = firstString(*km.category.toTypedArray()),
            dateText   = normalizeDate(km.date),
            notes      = firstString(*km.notes.toTypedArray()),
            confidence = firstNumber(*km.confidence.toTypedArray())
        )

        EntryKind.GOAL -> ParsedEntry.Goal(
            title = firstString(*(km.extras["name"] ?: emptyList()).toTypedArray()),
            amount = firstNumber(*km.amount.toTypedArray()),
            startAmount = firstNumber(*(km.extras["currentAmount"] ?: emptyList()).toTypedArray()),
            currency = up(firstString(*km.currency.toTypedArray())),
            dueDate = normalizeDate(km.date),
            notes = firstString(*km.notes.toTypedArray()),
            confidence = firstNumber(*km.confidence.toTypedArray()),
            type = firstString(),
            dateText  = normalizeDate(km.date),
        )
    }
}