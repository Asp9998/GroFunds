package com.aryanspatel.grofunds.domain.mapper

import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.resolveExpenseCategoryLabels
import com.aryanspatel.grofunds.domain.usecase.resolveIncomeTypeLabel
import com.aryanspatel.grofunds.presentation.common.model.Kind
import com.aryanspatel.grofunds.presentation.common.model.Transaction

fun TransactionEntity.toDomain(): Transaction{
    val res = when(kind){
        Kind.EXPENSE.name -> resolveExpenseCategoryLabels(categoryOrTypeID, subcategoryID)
        else -> resolveIncomeTypeLabel(categoryOrTypeID)
    }
    return Transaction(
        userId = userId,
        transactionId = transactionID,
        input = input,
        kind = if (kind == Kind.INCOME.name) Kind.INCOME else Kind.EXPENSE,
        amount = amount.toString(),
        currency = currencyCode,
        categoryOrType = res.categoryId,  // return category label
        subcategory = res.subcategoryId,  // return subcategory label
        merchant = merchant,
        note = note,
        date = DateConverters.millisToStringWithDay(date),
        createdAt = createdAtUTC,
        remoteUpdatedAt = remoteUpdatedAt,
        localUpdatedAt = localUpdatedAt
    )
}