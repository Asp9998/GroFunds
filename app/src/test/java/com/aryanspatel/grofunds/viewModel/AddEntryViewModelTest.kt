package com.aryanspatel.grofunds.viewModel

import app.cash.turbine.test
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.aryanspatel.grofunds.domain.model.*
import com.aryanspatel.grofunds.fakeRepo.repository.FakeAddEntryRepository
import com.aryanspatel.grofunds.presentation.common.model.SaveState
import com.aryanspatel.grofunds.presentation.common.model.SubmitState
import com.aryanspatel.grofunds.presentation.viewmodel.AddEntryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddEntryViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    /** Test DispatcherProvider that maps everything to the test dispatcher */
    private val dp = object : DispatcherProvider {
        override val iO = dispatcher
        override val default = dispatcher
        override val main = dispatcher
        override val unconfined = dispatcher
    }

    private lateinit var repo: FakeAddEntryRepository
    private lateinit var vm: AddEntryViewModel
    private var eventJob: Job? = null
    private val uid = "U_TEST"


    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAddEntryRepository(uid = uid)
        vm = AddEntryViewModel(repo, dp)
    }

    @After
    fun tearDown() {
        eventJob?.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun submitNote_startsObservation_and_setsDraftFields() = runTest(scheduler) {
        // Arrange
        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onInputNoteChanged("coffee 4.50 CAD")

        // Act
        vm.onParsedButtonClick() // calls submitNote under the hood
        runCurrent()

        // Assert submit/parsed basics
        assertThat(vm.submitState.value).isInstanceOf(SubmitState.Success::class.java)
        val draft = (vm.submitState.value as SubmitState.Success).draft
        assertThat(vm.uiState.value.draftId).isEqualTo(draft.id)
        assertThat(vm.uiState.value.docPath).isEqualTo(draft.path)
        assertThat(vm.parsedState.value).isInstanceOf(ParseState.Pending::class.java)

        // Simulate CF parsing -> Ready
        repo.setReady(draft.path, mapOf(
            "status" to "ready",
            "amount" to 4.5,
            "currency" to "CAD",
            "category" to "food",
            "subcategory" to "coffee",
            "merchant" to "Tim's",
            "dateText" to "2025-09-27",
            "notes" to "morning"
        ))
        advanceUntilIdle()

        // ViewModel should map Ready -> UI fields + isParsed=true
        val ui = vm.uiState.value
        assertThat(vm.parsedState.value).isInstanceOf(ParseState.Ready::class.java)
        assertThat(ui.isParsed).isTrue()
        assertThat(ui.amount).isEqualTo("4.50")
        assertThat(ui.currency).isEqualTo("CAD")
        assertThat(ui.categoryOrType).isEqualTo("food")
        assertThat(ui.expenseSubcategory).isEqualTo("coffee")
        assertThat(ui.expenseMerchant).isEqualTo("Tim's")
        assertThat(ui.parsePreview).isNull()
        assertThat(ui.parseError).isNull()
    }

    @Test
    fun canSave_rules_work_for_all_kinds() = runTest(scheduler) {
        // EXPENSE
        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onAmountChanged("4.50")
        vm.onCategoryOrTypeChanged("food")
        vm.onExpenseSubChanged("coffee")
        assertThat(vm.canSave.first()).isTrue()

        // INCOME
        vm.onSelectedOptionChanged(EntryKind.INCOME)
        vm.onAmountChanged("10.00")
        vm.onCategoryOrTypeChanged("salary")
        assertThat(vm.canSave.first()).isTrue()

        // GOAL (requires title + due date + amount + type)
        vm.onSelectedOptionChanged(EntryKind.GOAL)
        vm.onAmountChanged("5000")
        vm.onCategoryOrTypeChanged("saving")
        vm.onGoalTitleChanged("Emergency fund")
        vm.onGoalDueDateChanged("2025-12-31")
        assertThat(vm.canSave.first()).isTrue()
    }

    @Test
    fun onSaveButtonClick_routes_and_setsSuccess_for_expense() = runTest(scheduler) {
        // Seed a draft and pretend it's parsed
        val draft = repo.createDraft(EntryKind.EXPENSE, "coffee 4.50 CAD", "CAD", "en-CA", null)
        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onDocPathChanged(draft.path)
        vm.onDraftIdChanged(draft.id)
        vm.onIsParsedChanged(true)

        // Fill editable UI fields
        vm.onAmountChanged("4.50")
        vm.onCurrencyChanged("CAD")
        vm.onCategoryOrTypeChanged("food")
        vm.onExpenseSubChanged("coffee")
        vm.onExpenseMerchantChanged("Tim's")
        vm.onDateChanged("2025-09-27")
        vm.onNoteChanged("morning")

        // Act
        vm.onSaveButtonClick()
        advanceUntilIdle()

        // Assert
        assertThat(vm.saveState.value).isInstanceOf(SaveState.Success::class.java)
        // repo emits Ready(data) after save; ViewModel doesn't mirror it to parsedState, but success is enough
    }

    @Test
    fun onSaveButtonClick_income_and_goal_success() = runTest(scheduler) {
        // INCOME
        val d1 = repo.createDraft(EntryKind.INCOME, "salary 100", null, null, null)
        vm.onSelectedOptionChanged(EntryKind.INCOME)
        vm.onDocPathChanged(d1.path)
        vm.onDraftIdChanged(d1.id)
        vm.onIsParsedChanged(true)

        vm.onAmountChanged("100.00")
        vm.onCurrencyChanged("CAD")
        vm.onCategoryOrTypeChanged("salary")
        vm.onDateChanged("2025-09-01")

        vm.onSaveButtonClick()
        advanceUntilIdle()
        assertThat(vm.saveState.value).isInstanceOf(SaveState.Success::class.java)

        // GOAL
        val d2 = repo.createDraft(EntryKind.GOAL, "save 5k", null, null, null)
        vm.onSelectedOptionChanged(EntryKind.GOAL)
        vm.onDocPathChanged(d2.path)
        vm.onDraftIdChanged(d2.id)
        vm.onIsParsedChanged(true)

        vm.onAmountChanged("5000")
        vm.onCurrencyChanged("CAD")
        vm.onCategoryOrTypeChanged("saving")
        vm.onGoalTitleChanged("Emergency fund")
        vm.onGoalDueDateChanged("2025-12-31")
        vm.onGoalStartAmountChanged("1000")
        vm.onDateChanged("2025-09-01")

        vm.onSaveButtonClick()
        advanceUntilIdle()
        assertThat(vm.saveState.value).isInstanceOf(SaveState.Success::class.java)
    }

    @Test
    fun resetScreen_deletes_pending_draft_and_clears_ui() = runTest(scheduler) {
        // Create a pending draft (unsaved)
        val d = repo.createDraft(EntryKind.EXPENSE, "coffee", null, null, null)

        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onDocPathChanged(d.path)
        vm.onDraftIdChanged(d.id)
        vm.onIsParsedChanged(true)
        vm.onAmountChanged("4.50")
        vm.onCategoryOrTypeChanged("food")
        vm.onExpenseSubChanged("coffee")

        // Act: reset
        vm.resetScreen(EntryKind.INCOME) // switches kind too
        advanceUntilIdle()

        // Assert UI cleared and draft id/path removed
        assertThat(vm.uiState.value.draftId).isNull()
        assertThat(vm.uiState.value.docPath).isNull()
        assertThat(vm.submitState.value).isEqualTo(SubmitState.Idle)
        assertThat(vm.saveState.value).isEqualTo(SaveState.Idle)

        // observe removed draft -> fake emits Error("Deleted")
        repo.observe(d.path).test {
            val first = awaitItem()
            assertThat(first).isInstanceOf(ParseState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onParsedButtonClick_recreates_draft_and_restarts_observe() = runTest(scheduler) {
        // Existing draft
        val old = repo.createDraft(EntryKind.EXPENSE, "old", null, null, null)
        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onDocPathChanged(old.path)
        vm.onDraftIdChanged(old.id)
        vm.onIsParsedChanged(true)

        // Provide new note
        vm.onInputNoteChanged("sandwich 8 CAD")
        vm.onParsedButtonClick()
        runCurrent()

        // New draft created, old one removed
        assertThat(vm.submitState.value).isInstanceOf(SubmitState.Success::class.java)
        val newDraft = (vm.submitState.value as SubmitState.Success).draft
        assertThat(newDraft.path).isNotEqualTo(old.path)
        assertThat(vm.parsedState.value).isInstanceOf(ParseState.Pending::class.java)
    }

    @Test
    fun submit_empty_note_emits_error_event() = runTest(scheduler) {
        vm.onSelectedOptionChanged(EntryKind.EXPENSE)
        vm.onInputNoteChanged("") // blank
        val events = mutableListOf<String>()

        // collect events
        eventJob = launch(dispatcher) {
            vm.events.test {
                // Trigger
                vm.onParsedButtonClick()
                runCurrent()

                // An error event should be emitted
                val msg = awaitItem()
                events.add(msg)
                cancelAndIgnoreRemainingEvents()
            }
        }
        advanceUntilIdle()

        assertThat(vm.submitState.value).isInstanceOf(SubmitState.Error::class.java)
        assertThat(events.first()).contains("Note cannot be empty")
    }
}
