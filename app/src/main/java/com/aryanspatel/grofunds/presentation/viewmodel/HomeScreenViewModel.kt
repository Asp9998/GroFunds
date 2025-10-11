package com.aryanspatel.grofunds.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.presentation.common.model.HomeScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor (
    private val repo: AddEntryTransactionRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        observeAccountSummary()
    }

    fun observeAccountSummary(){
        viewModelScope.launch {
            repo.observeAccountSummary().collectLatest { summary ->
                _uiState.update { it.copy(
                    totalIncome = summary?.totalIncome ?: 0.0,
                    totalExpense = summary?.totalExpense ?: 0.0,
                    totalSaving = summary?.totalSaving ?: 0.0,
                    availableCase = summary?.availableCash ?: 0.0
                ) }
            }
        }
    }

}