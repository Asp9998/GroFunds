package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class SavingScreenTab{
    Overview,
    Activity
}

data class SavingUiState(
    val savings: List<SavingState>,
    val loading: Boolean,
    val error: String? = null,
)

data class SavingState(
    val savingId: String,
    val type: String,
    val title: String,
    val currencySymbol: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val dueDate: String,
    val note: String,
    val contributions: List<ContributionState>
) {
    val leftAmount = targetAmount - savedAmount
}

data class ContributionState(
    val contributionId: String,
    val savingId: String,
    val note: String,
    val amount: Double,
    val date: String,
)

data class SavingsHeaderUi(
    val originalDueDate: String?,
    val projectedCompletionDate: String?,   // null = no projection yet
    val paceMonthly: Double,          // for copy/tooltip
    val neededPerMonth: Double?,      // null = no due; +Inf = past due
)

data class InsightMetric(
    val label: String,
    val value: String,
    val unit: String? = null,
    val sublabel: String,
    val icon: ImageVector,
    val statusVariant: StatusVariant,
    val context: String
)

enum class StatusVariant {
    AHEAD, IMPROVING, TIGHT
}


data class GoalData(
    val target: Int,
    val saved: Int,
    val left: Int,
)

data class NextContribution(
    val date: String,
    val amount: Int
)

data class GoalData2(
    val name: String,
    val target: Int,
    val saved: Int,
    val left: Int,
    val progress: Float,
    val pace: Int,
    val nextContribution: NextContribution,
    val eta: String,
    val etaStatus: String
)

data class Milestone(
    val percent: Int,
    val amount: Int,
    val completed: Boolean,
    val nextDate: String? = null,
    val needed: Int? = null
)

data class Activity(
    val id: Int,
    val date: String,
    val amount: Int,
    val source: String,
    val note: String,
    val type: String
)

data class UpcomingContribution(
    val date: String,
    val amount: Int,
    val status: String
)