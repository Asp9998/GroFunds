package com.aryanspatel.grofunds.data.repository

import com.aryanspatel.grofunds.data.local.dao.CategoryBudgetDao
import com.aryanspatel.grofunds.data.local.dao.UserPreferencesDao
import com.aryanspatel.grofunds.data.local.entity.CategoryBudgetEntity
import com.aryanspatel.grofunds.data.local.entity.UserPreferencesEntity
import com.aryanspatel.grofunds.domain.repository.CurrentUserFlow
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Qualifier


@Qualifier
annotation class AppScope

class UserPreferencesRepository @Inject constructor(
    private val userFlow: CurrentUserFlow,
    @AppScope private val appScope: CoroutineScope,
    private val userPrefsDao: UserPreferencesDao,
    private val categoryBudgetDao: CategoryBudgetDao,
){

    val userUId: StateFlow<String?> =
        userFlow.uidFlow.stateIn(
            appScope,
            SharingStarted.Eagerly,
            "" // initial snapshot
        )
    suspend fun completeUserPreferencesUpdate(userPreferences: UserPreferencesEntity,
                                              categoryBudgets: List<CategoryBudgetEntity>){
        upsertUserPreferences(userPreferences)
        upsertCategoryBudget(categoryBudgets)
    }


    /** User Preferences (User id, Display name, Monthly Expense Budget) */
    suspend fun upsertUserPreferences(userPreferences: UserPreferencesEntity) =
        userPrefsDao.upsert(userPreferences)

    suspend fun getUserPreferences(): UserPreferencesEntity? =
        userPrefsDao.getUserPreferences(userUId.value ?: "")

    fun observePreferences(userId: String): Flow<UserPreferencesEntity?> =
        userPrefsDao.observeUserPreferences(userId)

    suspend fun getCurrencySymbol(userUId: String = ""): String? =
        userPrefsDao.getCurrencySymbol(userUId)

    suspend fun getCurrencyCode(): String? =
        userPrefsDao.getCurrencyCode(userUId.value ?: "")


    suspend fun updateDisplayName(newDisplayName: String, localeUpdatedAt: Long) =
        userPrefsDao.updateDisplayName(userUId.value ?: "", newDisplayName, localeUpdatedAt)

    suspend fun updateExpenseBudget(newExpenseBudget: Double, localeUpdatedAt: Long) =
        userPrefsDao.updateExpenseBudget(userUId.value ?: "", newExpenseBudget, localeUpdatedAt)

    suspend fun setCurrencyCode(currencyCode: String, localeUpdatedAt: Long) =
        userPrefsDao.setCurrencyCode(userUId.value ?: "", currencyCode, localeUpdatedAt)

    /** Category wise Budget allocation's functions */

    suspend fun upsertCategoryBudget(categoryBudget: List<CategoryBudgetEntity>) =
        categoryBudgetDao.upsertListOfCategory(categoryBudget)


    /**  */





}