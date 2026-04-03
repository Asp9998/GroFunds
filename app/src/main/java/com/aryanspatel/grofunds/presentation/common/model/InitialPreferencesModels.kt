package com.aryanspatel.grofunds.presentation.common.model

import kotlinx.serialization.Serializable

enum class OnboardingStep { Currency, Budget, CategoryBudgets }

@Serializable
data class Country(
    val name: String,
    val iso2: String,
    val currencyCode: String,
    val symbol: String,
    val flag: String
)
sealed interface Event {
    data class Error(val message: String) : Event
    data class Success(val message: String) : Event
}

data class InitialPrefsUiState(
    val userId: String,
    val displayName: String,
    val email: String,
    val currencyCode: String,
    val currencySymbol: String,
    val monthlyExpenseBudget: String? = null,
)