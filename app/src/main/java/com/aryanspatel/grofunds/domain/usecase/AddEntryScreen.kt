package com.aryanspatel.grofunds.domain.usecase

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.grofunds.domain.CURRENCY_LIST_ENUM
import com.aryanspatel.grofunds.domain.EXPENSE_CATEGORY_ENUM
import com.aryanspatel.grofunds.domain.GOAL_TYPE_ENUM
import com.aryanspatel.grofunds.domain.INCOME_TYPE_ENUM
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ExpenseEdits
import com.aryanspatel.grofunds.domain.model.GoalEdits
import com.aryanspatel.grofunds.domain.model.IncomeEdits
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import com.aryanspatel.grofunds.domain.model.SaveState
import com.aryanspatel.grofunds.domain.model.SubmitState
import com.aryanspatel.grofunds.domain.subcategoriesFor
import com.aryanspatel.grofunds.domain.viewmodel.AddEntryViewModel
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.coroutineContext

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
    // ───────────────────── Local state ─────────────────────
    var sessionKey by remember { mutableIntStateOf(0) }

    // Parsed / editable fields
    // Common
    var inputNote by rememberSaveable(sessionKey) { mutableStateOf("") }
    var categoryOrType by rememberSaveable(sessionKey) {mutableStateOf("")}
    var amount by rememberSaveable(sessionKey) { mutableStateOf("") }
    var currency by rememberSaveable(sessionKey) { mutableStateOf("CAD") }
    var whenText by rememberSaveable(sessionKey) { mutableStateOf("Today") }
    var note by rememberSaveable(sessionKey) { mutableStateOf("") }

    var isParsed by rememberSaveable(sessionKey) { mutableStateOf(false) }
    var docPath by rememberSaveable { mutableStateOf<String?>(null) }
    var draftId by rememberSaveable { mutableStateOf<String?>(null) }


    // For expense only
    var expenseSubcategory by rememberSaveable(sessionKey) { mutableStateOf("") }
    var expenseMerchant by rememberSaveable(sessionKey) { mutableStateOf("") }

    // For Goal only
    var goalTitle by rememberSaveable (sessionKey){mutableStateOf("")}
    var goalDueDate by rememberSaveable (sessionKey){mutableStateOf("")}
    var goalStartAmount by rememberSaveable (sessionKey){mutableStateOf("")}

    // Track the current Firestore draft

    // Input helpers
    val keyboardController = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // ───────────────────── ViewModel states ─────────────────────
    val submit by viewModel.submitState.collectAsStateWithLifecycle()   // draft creation
    val parse by viewModel.state.collectAsStateWithLifecycle()          // Firestore listener
    val save by viewModel.saveState.collectAsStateWithLifecycle()       // save action

    val isLoading = submit is SubmitState.Submitting || (docPath != null && !isParsed && parse is ParseState.Pending)
    val isSaving = save is SaveState.Saving


    // Toggle Options : Expense, Income & Goal
    var selectedOption by remember { mutableStateOf(EntryKind.EXPENSE) }
    var prevKind by remember { mutableStateOf(selectedOption) } // keep old value

    // reset function to clear everything
    fun reset() {
        // Delete draft by ID if entry isn't saved yet
        draftId?.let { viewModel.deleteDraftIfUnsaved(selectedOption, it) }

        // Clear UI + VM state
        viewModel.resetState()
        viewModel.resetSaveState()
        viewModel.clear()
        docPath = null
        draftId = null
        sessionKey++
    }

    // Reset when selectedOption changes
    LaunchedEffect(selectedOption) {
        if (prevKind != selectedOption) {
            draftId?.let { viewModel.deleteDraftIfUnsaved(prevKind, it) }
            reset()
            prevKind = selectedOption
        }
    }


    // ───────────────────── Effects ─────────────────────
    // Draft created -> remember ID/path and start listening
    LaunchedEffect((submit as? SubmitState.Success)?.draft) {
        val draft = (submit as? SubmitState.Success)?.draft ?: return@LaunchedEffect
        draftId = draft.id
        docPath = draft.path
        viewModel.start(draft.path)
    }

    // Listen to Firebase FireStore and set the UI States
    LaunchedEffect(parse) {
        if (parse is ParseState.Ready && !isParsed) {
            val data = (parse as ParseState.Ready).data
            val entry = mapToParsedEntry(data, selectedOption)

            var anyApplied = false
            fun <T> apply(setter: (T) -> Unit, value: T?) {
                if (value != null) { setter(value); anyApplied = true }
            }

            fun applyCommon(amountD: Double?, currencyS: String?, dateS: String?, notesS: String?) {
                apply({ amount = "%.2f".format(it) }, amountD)
                apply({ currency = it }, currencyS)
                apply({ whenText = it }, dateS)
                apply({ note = it }, notesS)
            }

            when (entry) {
                is ParsedEntry.Expense -> {
                    applyCommon(entry.amount, entry.currency, entry.dateText, entry.notes)
                    apply({ categoryOrType = it }, entry.category)
                    apply({ expenseSubcategory = it }, entry.subcategory)
                    apply({ expenseMerchant = it }, entry.merchant)
                }
                is ParsedEntry.Income -> {
                    applyCommon(entry.amount, entry.currency, entry.dateText, entry.notes)
                    apply({ categoryOrType = it }, entry.type)
                    // if you add a field: apply({ incomeSource = it }, entry.source)
                }
                is ParsedEntry.Goal -> {
                    applyCommon(entry.amount, entry.currency, entry.dueDateText, entry.notes)
                    apply({ goalTitle = it }, entry.title)
                    // Optional: seed amount with current progress if you prefer
                    if (!anyApplied && amount.isBlank()) {
                        apply({ amount = "%.2f".format(it) }, entry.amount)
                    }
                }
            }

            if (anyApplied) isParsed = true
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {


            // toggle button
            item{
                AnimatedTripleToggle(
                    selectedOption = selectedOption,
                    onOptionSelected = {selectedOption = it})
            }


            // 1) Free-form Note
            item {
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
                            text = when(selectedOption) {
                                EntryKind.EXPENSE -> "Describe your expense"
                                EntryKind.INCOME -> "Describe your income"
                                EntryKind.GOAL -> "Describe your goal"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Our AI will parse the details for you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            textAlign = TextAlign.Center
                        )
                        ModernTextField(
                            value = inputNote,
                            onValueChange = { inputNote = it },
                            placeholder = when(selectedOption){
                                EntryKind.EXPENSE -> "e.g. Groceries 50 Dollars Walmart"
                                EntryKind.INCOME -> "e.g. July first week Salary 10k"
                                EntryKind.GOAL -> "e.g. Save 17k for first year tuition fees"
                            },
                            minLines = 3,
                            maxLines = 6,
                        )

                        // Parse CTA
                        AnimatedVisibility(
                            visible = inputNote.isNotBlank(),
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GradientButton(
                                    onClick = {
                                        keyboardController?.hide(); focus.clearFocus()
                                        // Delete previous unsaved draft by ID, if any
                                        draftId?.let { viewModel.deleteDraftIfUnsaved(selectedOption, it) }
                                        // Reset UI + VM parsing state
                                        isParsed = false
                                        viewModel.clear()
                                        viewModel.resetState()
                                        docPath = null
                                        draftId = null


                                        // Create new draft (status = "pending")

                                        viewModel.submitNote(
                                            kind = selectedOption,
                                            note = inputNote,
                                            currencyHint = "CAD",
                                            localeHint = "en-CA"
                                        )
                                    },
                                    text = "Parse with AI",
                                    enabled = inputNote.isNotBlank() && !isLoading
                                )
                            }
                        }
                    }
                }
            }

            // Optional: inline loader row while pending
            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                }
            }

            // Show errors from draft create or parser
            val submitErr = (submit as? SubmitState.Error)?.message
            val parseErr = (parse as? ParseState.Error)?.message
            val err = submitErr ?: parseErr
            if (!err.isNullOrBlank() && !isParsed) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2) Summary (once parsed)
            item {
                AnimatedVisibility(
                    visible = isParsed,
                    enter = slideInVertically(animationSpec = tween(100)) + fadeIn(
                        tween(100)
                    ),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ExpenseSummaryCard(
                        amount = amount,
                        currency = currency,
                        category = categoryOrType,
                        merchant = expenseMerchant,
                        whenText = whenText
                    )
                }
            }

            // 3) Editable Details
            item {
                AnimatedVisibility(
                    visible = isParsed,
                    enter = slideInVertically(animationSpec = tween(200)) + fadeIn(
                        tween(200)
                    ),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ModernCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SectionHeader(
                                title = "Details",
                                subtitle = "Refine the parsed information",
                                icon = Icons.Default.Edit
                            )


                            // Common Fields for Expense, Income & Goal
                            // Amount & currency & type
                            ModernTextField(
                                value = amount,
                                onValueChange = { input ->
                                    val cleaned = input
                                        .replace(',', '.')
                                        .filterIndexed { idx, c ->
                                            c.isDigit() || (c == '.' && input.indexOf('.') == idx)
                                        }
                                    amount = cleaned
                                },
                                label = "Amount",
                                suffix = currency
                            )

                            ModernDropdown(
                                value = currency,
                                onValueChange = { currency = it },
                                label = "Currency",
                                options = CURRENCY_LIST_ENUM,
                            )

                            ModernDropdown(
                                label = if(selectedOption == EntryKind.EXPENSE)"Category" else "Type",
                                value = categoryOrType,
                                onValueChange = {
                                    categoryOrType = it
                                    val first = subcategoriesFor(categoryOrType).firstOrNull().orEmpty()
                                    if (expenseSubcategory !in subcategoriesFor(categoryOrType)) expenseSubcategory = first
                                },
                                options = when (selectedOption) {
                                    EntryKind.EXPENSE -> EXPENSE_CATEGORY_ENUM
                                    EntryKind.INCOME -> INCOME_TYPE_ENUM
                                    else -> GOAL_TYPE_ENUM
                                }
                            )

                            // Fields for Expense only
                            // Category, Subcategory & Merchant
                            key(selectedOption){
                                if(selectedOption == EntryKind.EXPENSE){
                                    ModernDropdown(
                                        label = "Subcategory",
                                        value = expenseSubcategory,
                                        onValueChange = { expenseSubcategory = it },
                                        options = subcategoriesFor(categoryOrType)
                                    )
                                    ModernTextField(
                                        value = expenseMerchant,
                                        onValueChange = { expenseMerchant = it },
                                        label = "Merchant (optional)",
                                    )
                                }


                                // Fields for Goal only
                                // Goal title, dueDate, startAmount

                                if(selectedOption == EntryKind.GOAL){
                                    ModernTextField(value = goalTitle,
                                        onValueChange = {goalTitle = it},
                                        label = "Title"
                                    )
                                    DatePickerField(value = goalDueDate,
                                        onValueChange = {goalDueDate  = it},
                                        label = "Due Date"
                                    )
                                    ModernTextField(value = goalStartAmount,
                                        onValueChange = {
                                                input ->
                                            val cleaned = input
                                                .replace(',', '.')
                                                .filterIndexed { idx, c ->
                                                    c.isDigit() || (c == '.' && input.indexOf('.') == idx)
                                                }
                                            goalStartAmount = cleaned
                                            },
                                        label = "Start Amount (optional)",
                                        suffix = currency
                                    )
                                }
                            }


                            // Common Fields for Expense, Income & Goal
                            // Date and Note
                            DatePickerField(
                                value = whenText,
                                onValueChange = { whenText = it }
                            )
                            ModernTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = "Additional Note (Optional)",
                                minLines = 1,
                                maxLines = 3,
                            )
                        }
                    }
                }
            }

            // 4) Actions (Reset / Save)
            item {
                AnimatedVisibility(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    visible = isParsed,
                    enter = slideInVertically(
                        animationSpec = tween(300, easing = EaseOutBounce)
                    ) + fadeIn(tween(300)),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ActionButtonsRow(
                        onReset = {
                            // Delete draft by ID if this expense isn't saved yet
                            reset()
                        },
                        onSave = {
                            val path = docPath?.takeIf { it.isNotBlank() } ?: return@ActionButtonsRow

                            val amt = amount.trim().toDoubleOrNull() ?: run {
                                // show a toast/snackbar if you want
                                return@ActionButtonsRow
                            }

                            when (selectedOption) {
                                EntryKind.EXPENSE -> {
                                    viewModel.saveExpense(
                                        path = path,
                                        edits = ExpenseEdits(
                                            amount = amt,
                                            currency = currency,
                                            category = categoryOrType,
                                            subcategory = expenseSubcategory,
                                            merchant = expenseMerchant.ifBlank { null },
                                            dateText = whenText,
                                            note = note.ifBlank { null }
                                        )
                                    )
                                }

                                EntryKind.INCOME -> {
                                    viewModel.saveIncome(
                                        path = path,
                                        edits = IncomeEdits(
                                            amount = amt,
                                            currency = currency,
                                            type = categoryOrType,
                                            dateText = whenText,
                                            note = note.ifBlank { null }
                                        )
                                    )
                                }

                                EntryKind.GOAL -> {
                                    val stAmt = goalStartAmount.trim().toDoubleOrNull() ?: 0.0
                                    val title = goalTitle.ifBlank { return@ActionButtonsRow }

                                    viewModel.saveGoal(
                                        path = path,
                                        edits = GoalEdits(
                                            title = title,
                                            amount = amt,
                                            currency = currency,
                                            type = categoryOrType,
                                            dateText = whenText,
                                            note = note.ifBlank { null },
                                            dueDate = goalDueDate,
                                            startAmount = stAmt
                                        )
                                    )
                                }
                            }
                        },
                        saveEnabled =
                                when(selectedOption) {
                                    EntryKind.EXPENSE -> {
                                        !isSaving &&
                                                amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                                categoryOrType.isNotBlank() &&
                                                expenseSubcategory.isNotBlank()
                                    }
                                    EntryKind.INCOME -> {
                                        !isSaving &&
                                                amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                                categoryOrType.isNotBlank()
                                    }
                                    EntryKind.GOAL -> {
                                        !isSaving &&
                                                amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                                categoryOrType.isNotBlank() &&
                                                goalTitle.isNotBlank() &&
                                                goalDueDate.isNotBlank()
                                    }
                                }
                    )
                }
            }
        }
    }
}

/* =============================== Static data =============================== */



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
            dueDateText = normalizeDate(km.date),
            notes = firstString(*km.notes.toTypedArray()),
            confidence = firstNumber(*km.confidence.toTypedArray()),
            type = firstString()
        )
    }
}

