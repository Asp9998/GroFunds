package com.aryanspatel.grofunds.presentation.screen.initialPreferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.aryanspatel.grofunds.domain.model.CategorySeed
import com.aryanspatel.grofunds.domain.usecase.BuiltInExpenseCategories
import com.aryanspatel.grofunds.presentation.common.model.Country
import com.aryanspatel.grofunds.presentation.common.model.Event
import com.aryanspatel.grofunds.presentation.common.model.OnboardingStep
import com.aryanspatel.grofunds.presentation.common.navigation.Destinations
import com.aryanspatel.grofunds.presentation.components.ModernButton
import com.aryanspatel.grofunds.presentation.components.ModernConfirmationDialog
import com.aryanspatel.grofunds.presentation.components.ModernTextField
import com.aryanspatel.grofunds.presentation.components.SnackBarMessage
import com.aryanspatel.grofunds.presentation.screen.home.HomeScreen
import com.aryanspatel.grofunds.presentation.viewmodel.InitialPreferencesViewModel
import kotlin.collections.forEach

@Composable
fun InitialPreferencesScreen(
    viewModel: InitialPreferencesViewModel = hiltViewModel(),
    navController: NavController
) {

    /** Ui State */
    val ui by viewModel.initialPrefsUiState.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()

    /** Helping State */
    var step by remember { mutableStateOf(OnboardingStep.Currency) }
    var selectedCountry by remember { mutableStateOf<Country?>(null) }
    val canContinueBudget = ui.monthlyExpenseBudget?.toDoubleOrNull()?.let { it > 0.0} == true

    val countries = viewModel.countries()

    val listOfCategories = BuiltInExpenseCategories

    val snackbarHostState = remember { SnackbarHostState() }


    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is Event.Error -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    is Event.Success -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                TopProgress(step = step)

                Spacer(Modifier.height(12.dp))

                when (step) {
                    OnboardingStep.Currency -> CurrencyStepScreen(
                        allCountries = countries,
                        selected = selectedCountry,
                        onSelect = { country ->
                            selectedCountry = country
                        },
                        onConfirm = { country ->
                            selectedCountry = country
                            viewModel.setCurrencyCode(country.currencyCode,
                                country.symbol)
                            step = OnboardingStep.Budget
                        }
                    )

                    OnboardingStep.Budget -> BudgetStepScreen(
                        currencySymbol = ui.currencySymbol,
                        initial = ui.monthlyExpenseBudget ?: "",
                        onBack = { step = OnboardingStep.Currency },
                        onChange = { viewModel.updateMonthlyExpenseBudget(it) },
                        onContinue = { step = OnboardingStep.CategoryBudgets },
                        continueEnabled = canContinueBudget,
                        onSkipClick = {
                            viewModel.onBudgetSkip()
                            step = OnboardingStep.CategoryBudgets
                        }
                    )

                    OnboardingStep.CategoryBudgets -> CategoryBudgetsStepScreen(
                        currencySymbol = ui.currencySymbol,
                        items = listOfCategories,
                        categoryBudgets = categoryBudgets,
                        onBack = { step = OnboardingStep.Budget },
                        onChange = { id, newValue ->
                            viewModel.updateSetOfCategoryBudget(categoryId = id, budgetAmount = newValue) },
                        onFinish = {
                            viewModel.onFinishButtonClick()
                            navController.navigate(Destinations.HomeScreen.name){
                                popUpTo(Destinations.HomeScreen.name){inclusive = true}
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            SnackBarMessage(
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbarHostState)
        }
    }
}

@Composable
fun CurrencyStepScreen(
    allCountries: List<Country>,
    selected: Country?,
    onSelect: (Country) -> Unit,
    onConfirm: (Country) -> Unit
) {
    var showConfirm by remember { mutableStateOf<Country?>(null) }

    /** Normal Filtration for search */
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allCountries) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) allCountries
        else allCountries.filter {
            it.name.lowercase().contains(q) ||
                    it.iso2.lowercase().contains(q) ||
                    it.currencyCode.lowercase().contains(q)
        }
    }.distinctBy { it.iso2 }
    Column(Modifier.fillMaxSize()) {

        TitleBlock(
            icon = Icons.Rounded.Flag,
            title = "Choose your currency",
            helper = "You can’t change this later."
        )

        Spacer(Modifier.height(12.dp))

        ModernTextField(
            value = query,
            onValueChange = { query = it},
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            leadingIcon = Icons.Rounded.Search,
            placeholder = "Search Country",
            imeAction = ImeAction.Search,
            keyboardActions = KeyboardActions(
                onSearch = { }
            ),
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.inverseSurface)
                .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(16.dp))
                .weight(1f)
        ) {
            if (filtered.isEmpty()) {
                EmptyState(title = "No matches found")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.iso2 }) { country ->
                        CountryRow(
                            country = country,
                            selected = selected?.iso2 == country.iso2,
                            onClick = { onSelect(country) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        ModernButton(
            text = "Use ${selected?.currencyCode ?: ""}",
            enabled = selected != null,
            onClick = { selected?.let { showConfirm = it }  },
            modifier = Modifier.fillMaxWidth(),
            isOutlined = false,
        )
    }

    val confirmCountry = showConfirm
    if (confirmCountry != null) {
        ModernConfirmationDialog(
            title = "Lock currency to ${confirmCountry.currencyCode}?",
            text = "This can’t be changed later.",
            confirmButtonLabel = "Confirm",
            onConfirm = {showConfirm = null; onConfirm(confirmCountry)},
            onDismiss = {showConfirm = null}
        )
    }
}

@Composable
fun BudgetStepScreen(
    currencySymbol: String,
    initial: String,
    onBack: () -> Unit,
    onChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkipClick: () -> Unit,
    continueEnabled: Boolean
) {
    val focus = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.End) {
            TitleBlock(
                icon = Icons.Rounded.Savings,
                title = "Set your monthly budget",
                helper = "You can edit this anytime."
            )
            Spacer(Modifier.height(12.dp))

            ModernTextField(
                value = initial,
                onValueChange = {onChange(it)},
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                leadingIcon = Icons.Rounded.Payments,
                placeholder = "Amount",
                suffix = currencySymbol,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions (onDone = {focus.clearFocus(); keyboardController?.hide()})
            )
        }


        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                isOutlined = true)

            ModernButton(
                text = "Skip",
                onClick = onSkipClick,
                modifier = Modifier.weight(1f),
                isOutlined = true)

            ModernButton(
                text = "Continue",
                onClick = onContinue,
                enabled = continueEnabled,
                modifier = Modifier.weight(2f)
            )
        }
    }
}


@Composable
fun CategoryBudgetsStepScreen(
    currencySymbol: String,
    items: List<CategorySeed>,
    categoryBudgets: Map<String, String>,
    onBack: () -> Unit,
    onChange: (id: String, newValue: String) -> Unit,
    onFinish: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TitleBlock(
            icon = Icons.AutoMirrored.Rounded.Label,
            title = "Budgets by category",
            helper = "Set limits per category to get better alerts. (Optional)"
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.inverseSurface)
                .border(1.dp, MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(16.dp))
                .weight(1f)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { cat ->
                    CategoryBudgetRow(
                        currencySymbol = currencySymbol,
                        draft = cat,
                        monthlyBudget = categoryBudgets.filter {it.key == cat.id}.firstNotNullOfOrNull { it.value } ?: "",
                        onChange = { onChange(cat.id, it) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                isOutlined = true)

            ModernButton(
                text = "Skip",
                onClick = onFinish,
                modifier = Modifier.weight(1f),
                isOutlined = true)

            ModernButton(
                text = "Finish",
                onClick = onFinish,
                modifier = Modifier.weight(2f))
        }

    }
}