package com.aryanspatel.grofunds.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ExpenseEdits
import com.aryanspatel.grofunds.domain.model.GoalEdits
import com.aryanspatel.grofunds.domain.model.IncomeEdits
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.SaveState
import com.aryanspatel.grofunds.domain.model.SubmitState
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val _state = MutableStateFlow<ParseState>(ParseState.Pending(note = null))
    val state: StateFlow<ParseState> = _state

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

    /** Apply edits to the same document and mark it saved. */
//    fun saveExpense(path: String, edits: ExpenseEdits) {
//        _saveState.value = SaveState.Saving
//        viewModelScope.launch(dp.iO) {
//            try {
//                repo.saveExpense(path, edits)
//                _saveState.value = SaveState.Success(path)
//            } catch (t: Throwable) {
//                _saveState.value = SaveState.Error(t.message ?: "Failed to save")
//            }
//        }
//    }

    fun saveExpense(path: String, edits: ExpenseEdits) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch(dp.iO) {
            runCatching { repo.saveExpense(path, edits) }
                .onSuccess { _saveState.value = SaveState.Success(path) }
                .onFailure { t -> _saveState.value = SaveState.Error(t.message ?: "Failed to save") }
        }
    }

    fun saveIncome(path: String, edits: IncomeEdits) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch(dp.iO) {
            runCatching { repo.saveIncome(path, edits) }
                .onSuccess { _saveState.value = SaveState.Success(path) }
                .onFailure { t -> _saveState.value = SaveState.Error(t.message ?: "Failed to save") }
        }
    }

    fun saveGoal(path: String, edits: GoalEdits) {
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
