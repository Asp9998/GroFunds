package com.aryanspatel.grofunds.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.aryanspatel.grofunds.data.local.entity.CategoryBudgetEntity

@Dao
interface CategoryBudgetDao {
    // upsert
    @Upsert
    suspend fun upsertListOfCategory(categoryBudget: List<CategoryBudgetEntity>)

}