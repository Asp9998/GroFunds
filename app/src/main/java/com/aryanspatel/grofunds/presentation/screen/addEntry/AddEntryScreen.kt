package com.aryanspatel.grofunds.presentation.screen.addEntry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.presentation.common.CURRENCY_LIST_ENUM
import com.aryanspatel.grofunds.presentation.common.EXPENSE_CATEGORY_ENUM
import com.aryanspatel.grofunds.presentation.common.GOAL_TYPE_ENUM
import com.aryanspatel.grofunds.presentation.common.INCOME_TYPE_ENUM
import com.aryanspatel.grofunds.presentation.common.ParseState
import com.aryanspatel.grofunds.presentation.common.ParsedEntry
import com.aryanspatel.grofunds.presentation.common.SaveState
import com.aryanspatel.grofunds.presentation.common.SubmitState
import com.aryanspatel.grofunds.presentation.common.model.AddEntryUiState
import com.aryanspatel.grofunds.presentation.common.model.EntryKind
import com.aryanspatel.grofunds.presentation.common.subcategoriesFor
import com.aryanspatel.grofunds.presentation.components.Button
import com.aryanspatel.grofunds.presentation.components.DatePickerField
import com.aryanspatel.grofunds.presentation.viewmodel.AddEntryViewModel
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.utils.cleanAmountInput
import kotlinx.coroutines.launch

/**
 * AddEntryScreen
 *
 * Flow:
 * 1) User writes a free-form note and taps "Parse with AI".
 * 2) We create a Firestore draft (status = "pending") and start listening to it.
 * 3) When Cloud Function finishes, we prefill editable fields and show the Details section.
 * 4) Save -> update the same doc and mark status = "saved".
 * 5) Reset/Back/Dismiss (while not saved) -> delete draft by ID for cleanliness.
 *
 * Requires ViewModel with:
 *  - submitState: SubmitState
 *  - state: ParseState (listener on the doc)
 *  - saveState: SaveState
 *  - submitNote(kind, note, ...), saveExpense(path, edits)
 *  - start(path), clear()      // start/stop observation of a doc
 *  - deleteById(kind, id)      // delete users/{uid}/{collection}/{id}
 *  - resetState(), resetSaveState()
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddExpenseScreen(
    onDismiss: () -> Unit,
    viewModel: AddEntryViewModel = hiltViewModel()
) {
    // ───────────────────── ViewModel states ─────────────────────
    val submit by viewModel.submitState.collectAsStateWithLifecycle()   // draft creation
    val parse by viewModel.state.collectAsStateWithLifecycle()          // Firestore listener
    val save by viewModel.saveState.collectAsStateWithLifecycle()       // save action
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()      // UiState collector

    // Input helpers
    val keyboardController = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val isLoading = submit is SubmitState.Submitting || (uiState.docPath != null && !uiState.isParsed && parse is ParseState.Pending)
    val isSaving = save is SaveState.Saving

    // Toggle Options : Expense, Income & Goal
    var selectedOption by remember { mutableStateOf(EntryKind.EXPENSE) }
    var prevKind by remember { mutableStateOf(selectedOption) } // keep old value

    // reset function to clear everything
    fun reset() {
        // Delete draft by ID if entry isn't saved yet
        uiState.draftId?.let { viewModel.deleteDraftIfUnsaved(selectedOption, it) }

        // Clear UI + VM state
        viewModel.resetState()
        viewModel.resetSaveState()
        viewModel.clear()
        viewModel.updateUiState()
    }

    // Reset when selectedOption changes
    LaunchedEffect(selectedOption) {
        if (prevKind != selectedOption) {
            uiState.draftId?.let { viewModel.deleteDraftIfUnsaved(prevKind, it) }
            reset()
            prevKind = selectedOption
        }
    }


    // ───────────────────── Effects ─────────────────────
    // Draft created -> remember ID/path and start listening
    LaunchedEffect((submit as? SubmitState.Success)?.draft) {
        val draft = (submit as? SubmitState.Success)?.draft ?: return@LaunchedEffect
        viewModel.onDraftIdChanged(draft.id)
        viewModel.onDocPathChanged(draft.path)
        viewModel.start(draft.path)
    }

    // Listen to Firebase FireStore and set the UI States
    LaunchedEffect(parse) {
        if (parse is ParseState.Ready && !uiState.isParsed) {
            viewModel.setParseData()
        }
    }


    // Save succeeded -> clear everything for the next add
    LaunchedEffect(save) {
        if (save is SaveState.Success) {
            reset()
        }
    }

    // System back: delete draft by ID if not saved, then dismiss
    BackHandler(true) {
        scope.launch {
            reset()
            onDismiss()
        }
    }

    // ───────────────────── UI ─────────────────────

    HorizontalSlidingOverlay(
        title = "Add Entry",
        onDismiss = {
            scope.launch {
                reset()
                onDismiss()
            }
        }
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
//            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // toggle button
                AnimatedToggleButton(
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it })

            // 1) Free-form Note
                NoteInputSection(
                    selectedOption = selectedOption,
                    inputNote = uiState.inputNote,
                    isLoading = isLoading,
                    onNoteValueChange = { viewModel.onInputNoteChanged(it )},
                    onParseButtonClick = {
                        keyboardController?.hide(); focus.clearFocus()
                        // Delete previous unsaved draft by ID, if any
                        uiState.draftId?.let { viewModel.deleteDraftIfUnsaved(selectedOption, it) }
                        // Reset UI + VM parsing state
                        viewModel.onIsParsedChanged(false)
                        viewModel.clear()
                        viewModel.resetState()
                        viewModel.onDocPathChanged(null)
                        viewModel.onDraftIdChanged(null)

                        viewModel.submitNote(
                            kind = selectedOption,
                            note = uiState.inputNote,
                            currencyHint = "CAD",
                            localeHint = "en-CA")
                    }
                )



            if(isLoading){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primaryContainer)
                }
            }else{

                // 2) Summary (once parsed)
                AnimatedVisibility(
                    visible = uiState.isParsed,
                    enter = slideInVertically(animationSpec = tween(100)) + fadeIn(
                        tween(100)
                    ),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    SummaryCard(
                        amount = uiState.amount,
                        currency = uiState.currency,
                        category = uiState.categoryOrType,
                        merchant = uiState.expenseMerchant,
                        date = uiState.date
                    )
                }


                EditableDetailsSection(
                    state = uiState,
                    selectedOption = selectedOption,
                    onAmountValueSChanged = { viewModel.onAmountChanged(it)},
                    onCurrencyValueChanged = { viewModel.onCurrencyChanged(it)},
                    onCategoryOrTypeValueChanged = { viewModel.onCategoryOrTypeChanged(it)},
                    onExpenseSubcategoryValueChanged = {viewModel.onExpenseSubChanged(it)},
                    onExpenseMerchantValueChanged = {viewModel.onExpenseMerchantChanged(it)},
                    onNoteValueChange = {viewModel.onNoteChanged(it)},
                    onWhenTextValueChange = {viewModel.onDateChanged(it)},
                    onGoalTitleValueChange = {viewModel.onGoalTitleChanged(it)},
                    onGoalDueDateValueChange = {viewModel.onGoalDueDateChanged(it)},
                    onGoalStartAmountValueChanged = {viewModel.onGoalStartAmountChanged(it)}
                )


                BottomActonButton(
                    isParsed = uiState.isParsed,
                    onResetClick = { reset()},
                    enabled = when (selectedOption) {
                        EntryKind.EXPENSE -> {
                            !isSaving &&
                                    uiState.amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                    uiState.categoryOrType.isNotBlank() &&
                                    uiState.expenseSubcategory.isNotBlank()
                        }

                        EntryKind.INCOME -> {
                            !isSaving &&
                                    uiState.amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                    uiState.categoryOrType.isNotBlank()
                        }

                        EntryKind.GOAL -> {
                            !isSaving &&
                                    uiState.amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                    uiState.categoryOrType.isNotBlank() &&
                                    uiState.goalTitle.isNotBlank() &&
                                    uiState.goalDueDate.isNotBlank()
                        }
                    },
                    onSaveButtonClick = {
                        val path =
                            uiState.docPath?.takeIf { it.isNotBlank() } ?: return@BottomActonButton

                        val amt = uiState.amount.trim().toDoubleOrNull() ?: run {
                            // show a toast/snackbar if you want
                            return@BottomActonButton
                        }

                        when (selectedOption) {
                            EntryKind.EXPENSE -> {
                                viewModel.saveExpense(
                                    path = path,
                                    edits = ParsedEntry.Expense(
                                        amount = amt,
                                        currency = uiState.currency,
                                        category = uiState.categoryOrType,
                                        subcategory = uiState.expenseSubcategory,
                                        merchant = uiState.expenseMerchant.ifBlank { null },
                                        dateText = uiState.date,
                                        notes = uiState.note.ifBlank { null },
                                    )
                                )
                            }

                            EntryKind.INCOME -> {
                                viewModel.saveIncome(
                                    path = path,
                                    edits = ParsedEntry.Income(
                                        amount = amt,
                                        currency = uiState.currency,
                                        type = uiState.categoryOrType,
                                        dateText = uiState.date,
                                        notes = uiState.note.ifBlank { null }
                                    )
                                )
                            }

                            EntryKind.GOAL -> {
                                val stAmt = uiState.goalStartAmount.trim().toDoubleOrNull() ?: 0.0
                                val title = uiState.goalTitle.ifBlank { return@BottomActonButton }

                                viewModel.saveGoal(
                                    path = path,
                                    edits = ParsedEntry.Goal(
                                        title = title,
                                        amount = amt,
                                        currency = uiState.currency,
                                        type = uiState.categoryOrType,
                                        dateText = uiState.date,
                                        notes = uiState.note.ifBlank { null },
                                        dueDate = uiState.goalDueDate,
                                        startAmount = stAmt,
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }

        // Show errors from draft create or parser
        val submitErr = (submit as? SubmitState.Error)?.message
        val parseErr = (parse as? ParseState.Error)?.message
        val err = submitErr ?: parseErr
        if (!err.isNullOrBlank() && !uiState.isParsed) {
            Snackbar {
                Text(text = err)
            }
        }
    }
}



@Composable
fun SummaryCard(
    amount: String,
    currency: String,
    category: String,
    merchant: String,
    date: String
) {
    ModernCard(
        gradient = Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text("$amount $currency",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold)
                Text(
                    "$category${if (merchant.isNotEmpty()) " • $merchant" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Text(date, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary.copy(0.8f))
            }
        }
    }
}



/**
 * Save and Reset Buttons
 */

@Composable
fun BottomActonButton(
    isParsed: Boolean,
    enabled: Boolean,
    onResetClick: () -> Unit,
    onSaveButtonClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        visible = isParsed,
        enter = slideInVertically(
            animationSpec = tween(200, easing = EaseOutBounce)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically() + fadeOut()
    ) {
        ActionButtonsRow(
            onReset = onResetClick,
            onSave = onSaveButtonClick,
            saveEnabled = enabled

        )
    }
}

@Composable
fun ActionButtonsRow(onReset: () -> Unit, onSave: () -> Unit, saveEnabled: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

        Button(onClick = onReset,
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.entry_screen_reset_button_text),
            isOutlined = true)

        Button(onClick = onSave,
            enabled = saveEnabled,
            modifier = Modifier.weight(2f),
            text = stringResource(R.string.entry_screen_save_button_text))

    }
}


/**
 * Main Editable Details section - inputs' final refining
 * Only appears after Extracting state fields form input not, disappears on reset, save button click.
 */

@Composable
fun EditableDetailsSection(
    state: AddEntryUiState,
    selectedOption: EntryKind,
    onAmountValueSChanged : (String) -> Unit,
    onCurrencyValueChanged : (String) -> Unit,
    onCategoryOrTypeValueChanged: (String) -> Unit,
    onExpenseSubcategoryValueChanged: (String) -> Unit,
    onExpenseMerchantValueChanged: (String) -> Unit,
    onNoteValueChange: (String) -> Unit,
    onWhenTextValueChange: (String) -> Unit,
    onGoalTitleValueChange: (String) -> Unit,
    onGoalDueDateValueChange: (String) -> Unit,
    onGoalStartAmountValueChanged: (String) -> Unit,
) {

    AnimatedVisibility(
        visible = state.isParsed,
        enter = slideInVertically(animationSpec = tween(200)) + fadeIn(
            tween(200)
        ),
        exit = slideOutVertically() + fadeOut()
    ) {
        ModernCard {

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader(
                    title = stringResource(R.string.header_title),
                    subtitle = stringResource(R.string.header_description),
                    icon = Icons.Default.Edit
                )

                /**
                 * Common Fields - Amount, Currency, Category or Type
                 */
                ModernTextField(
                    value = state.amount,
                    onValueChange = { onAmountValueSChanged(cleanAmountInput(it)) },
                    label = stringResource(R.string.amount_label),
                    keyboardType = KeyboardType.Number,
                    suffix = state.currency,
                )

                ModernDropdownTextField(
                    value = state.currency,
                    onValueChange = { onCurrencyValueChanged(it) },
                    label = stringResource(R.string.currency_label),
                    options = CURRENCY_LIST_ENUM,
                )

                ModernDropdownTextField(
                    label = if (selectedOption == EntryKind.EXPENSE) stringResource(R.string.category_label)
                            else stringResource(R.string.type_label),
                    value = state.categoryOrType,
                    onValueChange = {
                        // save categoryOrType
                        onCategoryOrTypeValueChanged(it)
                        // change the subcategory's options according to Category
                        val curr = subcategoriesFor(state.categoryOrType).firstOrNull().orEmpty()
                        if (state.expenseSubcategory !in subcategoriesFor(state.categoryOrType)) onExpenseSubcategoryValueChanged(curr)
                    },
                    options = when (selectedOption) {
                        EntryKind.EXPENSE -> EXPENSE_CATEGORY_ENUM
                        EntryKind.INCOME -> INCOME_TYPE_ENUM
                        else -> GOAL_TYPE_ENUM
                    }
                )

                /**
                 * Fields for Expense only - Subcategory, Merchant
                 */
                key(selectedOption) {
                    if (selectedOption == EntryKind.EXPENSE) {
                        ModernDropdownTextField(
                            label = stringResource(R.string.sub_cat_label),
                            value = state.expenseSubcategory,
                            onValueChange = { onExpenseSubcategoryValueChanged(it) },
                            options = subcategoriesFor(state.categoryOrType)
                        )
                        ModernTextField(
                            value = state.expenseMerchant,
                            onValueChange = { onExpenseMerchantValueChanged(it) },
                            label = stringResource(R.string.merchant_label),
                        )
                    }


                    /**
                     * Fields for goal only - Title, DueDate, StartAmount
                     */
                    if (selectedOption == EntryKind.GOAL) {
                        ModernTextField(
                            value = state.goalTitle,
                            onValueChange = { onGoalTitleValueChange(it) },
                            label = stringResource(R.string.goal_title_label),
                        )
                        DatePickerField(
                            value = state.goalDueDate,
                            onValueChange = { onGoalDueDateValueChange(it) },
                            label = stringResource(R.string.goal_due_date_label)
                        )
                        ModernTextField(
                            value = state.goalStartAmount,
                            onValueChange = { onGoalStartAmountValueChanged(cleanAmountInput(it)) },
                            label = stringResource(R.string.goal_start_amount_label),
                            suffix = state.currency,
                        )
                    }
                }

                /**
                 * common Fields - Date of Entry, Note.
                 */
                DatePickerField(
                    value = state.date,
                    label = stringResource(R.string.entry_date_label),
                    onValueChange = { onWhenTextValueChange(it)}
                )
                ModernTextField(
                    value = state.note,
                    onValueChange = { onNoteValueChange(it) },
                    label = stringResource(R.string.note_label),
                    maxLines = 3,
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp))
        Column {
            Text(title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Note input section - input text field and parse button
 */
@Composable
fun NoteInputSection(selectedOption: EntryKind,
                     inputNote: String,
                     isLoading: Boolean,
                     onNoteValueChange: (String) -> Unit,
                     onParseButtonClick: () -> Unit
) {
    ModernCard(
        gradient = Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            )
        )
    ) {


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = when (selectedOption) {
                    EntryKind.EXPENSE -> stringResource(R.string.add_expense_title)
                    EntryKind.INCOME -> stringResource(R.string.add_income_title)
                    EntryKind.GOAL -> stringResource(R.string.add_goal_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.common_headline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )
            ModernTextField(
                value = inputNote,
                onValueChange = { onNoteValueChange(it) },
                placeholder = when (selectedOption) {
                    EntryKind.EXPENSE -> stringResource(R.string.add_expense_example)
                    EntryKind.INCOME -> stringResource(R.string.add_income_example)
                    EntryKind.GOAL -> stringResource(R.string.add_goal_example)
                },
                minLines = 3,
                maxLines = 6,
            )

            // Parse Button
            AnimatedVisibility(
                visible = inputNote.isNotBlank(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        text = stringResource(R.string.parse_button_text),
                        enabled = inputNote.isNotBlank() && !isLoading,
                        onClick = onParseButtonClick,
                        elevation = 10.dp
                    )
                }
            }
        }
    }
}





