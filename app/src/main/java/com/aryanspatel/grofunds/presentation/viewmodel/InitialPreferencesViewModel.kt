package com.aryanspatel.grofunds.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.core.readRawResource
import com.aryanspatel.grofunds.data.local.entity.CategoryBudgetEntity
import com.aryanspatel.grofunds.data.local.entity.UserPreferencesEntity
import com.aryanspatel.grofunds.data.repository.UserPreferencesRepository
import com.aryanspatel.grofunds.domain.model.AuthUser
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import com.aryanspatel.grofunds.presentation.common.model.Country
import com.aryanspatel.grofunds.presentation.common.model.Event
import com.aryanspatel.grofunds.presentation.common.model.InitialPrefsUiState
import com.aryanspatel.grofunds.utils.cleanAmountInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class InitialPreferencesViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository,
    private val authRepo: AuthRepository,
    private val context: Application,
    private val json: Json

) : ViewModel(){

    val user: StateFlow<AuthUser?> =
        authRepo.authState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = authRepo.getCurrentUser()
            )

    private val _initialPrefsUiState = MutableStateFlow(InitialPrefsUiState(
        userId = "",
        displayName = "",
        email = "",
        currencyCode = "",
        currencySymbol = "",
        monthlyExpenseBudget = ""
    ))
    val initialPrefsUiState: StateFlow<InitialPrefsUiState> = _initialPrefsUiState.asStateFlow()

    private val _categoryBudgets = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryBudgets: StateFlow<Map<String, String>> = _categoryBudgets.asStateFlow()

    // One-shot UI events (success/error)
    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            user.collect{ user ->
                _initialPrefsUiState.update {
                    it.copy(
                        userId = user?.uid.orEmpty(),
                        displayName = user?.displayName.orEmpty(),
                        email = user?.email.orEmpty()
                    )
                }
            }
        }
    }

    fun countries(): List<Country> =
        json.readRawResource(context, R.raw.countries_currencies)

    fun setCurrencyCode(currencyCode: String, currencySymbol: String) {
        _initialPrefsUiState.update { it.copy(currencyCode = currencyCode, currencySymbol = currencySymbol) }
    }

    fun updateMonthlyExpenseBudget(budget: String){
        _initialPrefsUiState.update { it.copy(monthlyExpenseBudget = cleanAmountInput(budget))}
    }

    fun onBudgetSkip(){
        _initialPrefsUiState.update { it.copy(monthlyExpenseBudget = null)}
    }

    // On any category's budget value change
    fun updateSetOfCategoryBudget(categoryId: String, budgetAmount: String) {
        val parsed = cleanAmountInput(budgetAmount)
        _categoryBudgets.update { current ->
            if(parsed.isBlank()){
                current - categoryId
            }
            else{
                val value = parsed.toDoubleOrNull()
                when {
                    value == null -> current                  //  null -> no change
                    value <= 0.0 -> current - categoryId      //  <= 0 -> remove form Map
                    else -> current + (categoryId to parsed)   //  else -> add into Map
                }
            }
        }
        Log.d("CategoryBudgets", "updateSetOfCategoryBudget: ${_categoryBudgets.value} ")

    }

    fun onFinishButtonClick(){
        viewModelScope.launch(Dispatchers.IO){
            val curr = _initialPrefsUiState.value
            val uid = curr.userId
            val now = System.currentTimeMillis()

            // Basic validation
            if(uid.isBlank()){
                _events.emit(Event.Error("No Signed-in user"))
                return@launch
            }

            runCatching {
                val prefs = UserPreferencesEntity(
                    userId = uid,
                    displayName = curr.displayName,
                    currencySymbol = curr.currencySymbol,
                    currencyCode = curr.currencyCode,
                    monthlyExpenseBudget = curr.monthlyExpenseBudget?.trim()?.toDoubleOrNull(),
                    localUpdatedAt = now,
                    isDirty = true
                )

                val monthStart = startOfCurrentMonthUtc()
                val budget = _categoryBudgets.value.map { (catId, amt) ->
                        CategoryBudgetEntity(
                            userId = uid,
                            currencyCode = curr.currencyCode,
                            categoryId = catId,
                            amountLimit = amt.toDoubleOrNull() ?: 0.0,
                            effectiveFrom = monthStart,
                            localeUpdatedAt = now,
                            isDirty = true,
                        )
                }
                userPrefsRepo.completeUserPreferencesUpdate(userPreferences = prefs, budget)
            }.onSuccess {
                _events.emit(Event.Success("Preferences saved"))
            }.onFailure {e ->
                _events.emit(Event.Error(e.message ?: "Failed to save preferences"))
            }
        }
    }
    private fun startOfCurrentMonthUtc(): Long {
        val z = ZoneOffset.UTC
        val ym = YearMonth.now(z)
        val start = ym.atDay(1).atStartOfDay(z).toInstant()
        return start.toEpochMilli()
    }


}

