package com.aryanspatel.grofunds.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.data.local.DTO.ContributionRow
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.data.repository.UserPreferencesRepository
import com.aryanspatel.grofunds.domain.mapper.toDomain
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.neededPerMonth
import com.aryanspatel.grofunds.domain.usecase.projectedCompletionDate
import com.aryanspatel.grofunds.presentation.common.model.SavingState
import com.aryanspatel.grofunds.presentation.common.model.SavingUiState
import com.aryanspatel.grofunds.presentation.common.model.SavingsHeaderUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val repo: AddEntryTransactionRepository,
    private val userPrefRepo: UserPreferencesRepository,
    private val dispatcher: DispatcherProvider
) : ViewModel() {

    val uiState: StateFlow<SavingUiState> =
        repo.observeSavings()
            .flowOn(dispatcher.iO)
            .map { listOfSavings ->
                val currencySymbol =  userPrefRepo.getCurrencySymbol() ?: "-"
                val sortedListOfSaving: List<SavingState> =
                    listOfSavings.map { it.toDomain(selectedContributions.value, currencySymbol) }
        SavingUiState(
            savings = sortedListOfSaving,
            loading = false,
            error = null
        )
    }.onStart {
        emit(SavingUiState(savings = emptyList() , loading = true, error = null))
    }.catch {error ->
        emit(SavingUiState(savings = emptyList(), loading = false, error = error.message))
    }.stateIn(scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SavingUiState(savings = emptyList(), loading = true, error = null)
    )

    private val _selectedId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            uiState
                .map { it.savings }
                .filter { it.isNotEmpty() }
                .take(1) // run only once
                .collect { savings ->
                    if (_selectedId.value == null) _selectedId.value = savings.first().savingId
                }
        }
    }

    val selectedSaving: StateFlow<SavingState?> =
        combine(uiState, _selectedId) { ui, id ->
            ui.savings.firstOrNull { it.savingId == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedContributions: StateFlow<List<ContributionRow>> =
        _selectedId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repo.observeContributions(id)
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onSelectSaving(id: String) {
        _selectedId.value = id
    }

    val savingHeaderUi : StateFlow<SavingsHeaderUi>  =
        combine(selectedSaving, selectedContributions) {s, contribs ->
            if(s == null) return@combine SavingsHeaderUi(
                originalDueDate = null,
                projectedCompletionDate = null,
                paceMonthly = 0.0,
                neededPerMonth = null)

            withContext(dispatcher.iO){
                val now = System.currentTimeMillis()
                val proj = projectedCompletionDate(
                    targetAmount = s.targetAmount,
                    savedAmount = s.savedAmount,
                    contributions = s.contributions,
                    nowMillis = now,
                    kMonths = 3
                )

                val needed = neededPerMonth(
                    target = s.targetAmount,
                    saved = s.savedAmount,
                    dueMillis = DateConverters.stringToMillisWithoutDay(s.dueDate),
                    todayMillis = now
                )

                SavingsHeaderUi(
                    originalDueDate = s.dueDate,
                    projectedCompletionDate = DateConverters.millisToString(proj.projectedDateMillis ?: 0L),
                    paceMonthly = proj.paceMonthly,
                    neededPerMonth = needed
                )
            }
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SavingsHeaderUi(null, null, 0.0, null)
        )
}