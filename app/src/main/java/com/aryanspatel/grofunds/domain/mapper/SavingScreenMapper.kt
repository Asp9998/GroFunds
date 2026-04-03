package com.aryanspatel.grofunds.domain.mapper

import com.aryanspatel.grofunds.data.local.DTO.ContributionRow
import com.aryanspatel.grofunds.data.local.DTO.SavingRow
import com.aryanspatel.grofunds.domain.usecase.DateConverters
import com.aryanspatel.grofunds.domain.usecase.resolveSavingTypeLabel
import com.aryanspatel.grofunds.presentation.common.model.ContributionState
import com.aryanspatel.grofunds.presentation.common.model.SavingState

fun SavingRow.toDomain(contributions: List<ContributionRow>, currencySymbol: String): SavingState{
    val res =  resolveSavingTypeLabel(typeId)
    val mappedContributions: List<ContributionState> = contributions.map { it.toDomain() }

    return SavingState(
        savingId = savingId,
        type = res.categoryId,  //category label is returning actually
        title = title,
        currencySymbol = currencySymbol,
        targetAmount = targetAmount,
        savedAmount = savedAmount,
        dueDate = DateConverters.millisToString(dueDate),
        note = note,
        contributions = mappedContributions
    )
}

fun ContributionRow.toDomain(): ContributionState{
    return ContributionState(
        contributionId = contributionId,
        savingId = savingId,
        note = note,
        amount = amount,
        date = DateConverters.millisToString(createdAt)
    )
}