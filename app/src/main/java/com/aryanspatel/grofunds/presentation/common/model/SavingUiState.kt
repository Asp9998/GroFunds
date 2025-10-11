package com.aryanspatel.grofunds.presentation.common.model

data class GoalData(
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

data class NextContribution(
    val date: String,
    val amount: Int
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