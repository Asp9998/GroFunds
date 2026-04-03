package com.aryanspatel.grofunds.domain.usecase

import com.aryanspatel.grofunds.presentation.common.model.ContributionState
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.roundToInt

// calculate projected completion date
/*:
   we need -
     1. the remaining amount
     2. paceMonthly (observed monthly contribution rate)
     3. add the corresponding fractional months to today.

 */

data class ProjectionResult(
    val projectedDateMillis: Long?,
    val paceMonthly: Double,

)

fun projectedCompletionDate(
    targetAmount: Double,
    savedAmount: Double,
    contributions: List<ContributionState>,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    kMonths: Int = 3,
): ProjectionResult {

    val remaining = (targetAmount - savedAmount).coerceAtLeast(0.0)
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

    if(remaining == 0.0){
        return ProjectionResult(today.atStartOfDay(zone).toInstant().toEpochMilli(), paceMonthly = 0.0)
    }

    val pace = paceMonthly(contributions, nowMillis, kMonths, zone)
    if(pace == 0.0 ){
        return ProjectionResult(null, paceMonthly = 0.0)
    }

    val monthsNeeded = remaining / pace
    val date = addMonthsFractional(today, monthsNeeded)
    val millis = date.atStartOfDay(zone).toInstant().toEpochMilli()
    return ProjectionResult(projectedDateMillis = millis, paceMonthly = pace)
}

fun paceMonthly(
    contributions: List<ContributionState>,
    nowMillis: Long,
    kMonths: Int,
    zone: ZoneId
) : Double {

    val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val lastK = (0 until kMonths).map { YearMonth.from(nowDate).minusMonths(it.toLong()) }
    val bucket = lastK.associateWith { 0.0 }.toMutableMap()

    for (c in contributions) {
        val timeStampMillis = DateConverters.stringToMillisWithoutDay(c.date)
        val ym = Instant.ofEpochMilli(timeStampMillis).atZone(zone).toLocalDate().let { YearMonth.from(it) }
        if (ym in bucket) bucket[ym] = (bucket[ym] ?: 0.0) + c.amount
    }
    val totals = lastK.map { bucket[it] ?: 0.0 }

    // 2) Robust aggregation
    val nonZero = totals.filter { it != 0.0 }
    if (totals.isEmpty()) return 0.0
    if (nonZero.size < 3) return (if (nonZero.isNotEmpty()) nonZero else totals).average()

    val sorted = totals.sorted()
    val n = sorted.size
    return if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
}

fun addMonthsFractional(start: LocalDate, months: Double): LocalDate {
    if (months <= 0.0) return start
    val whole = floor(months).toLong()
    val frac = months - whole
    val base = start.plusMonths(whole)
    if (frac == 0.0) return base
    val days = (frac * base.lengthOfMonth()).roundToInt()
    return base.plusDays(days.toLong())
}

// Calculate needed monthly amount
fun neededPerMonth(
    target: Double,
    saved: Double,
    dueMillis: Long?,
    todayMillis: Long
): Double?{

    if(dueMillis == null) return null
    val remaining = (target - saved).coerceAtLeast(0.0)
    if(remaining == 0.0) return 0.0
    if(dueMillis <= todayMillis) return Double.POSITIVE_INFINITY  // past due

    val monthsLeft = monthsBetweenFractional(todayMillis, dueMillis)
        .coerceAtLeast(1.0/30.0)
    return remaining / monthsLeft

}

fun monthsBetweenFractional(
    startMillis: Long,
    endMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): Double {
    val s = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
    val e = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()
    return monthsBetweenFractional(s, e)
}

fun monthsBetweenFractional(
    start: LocalDate,
    end: LocalDate
): Double {
    if (!end.isAfter(start)) return 0.0

    // Period gives calendar years, months, days between start and end.
    val period = start.until(end) // Period is non-negative here
    val wholeMonths = period.years * 12 + period.months
    val leftoverDays = period.days

    // Fraction is days divided by the length of the end month
    val denom = end.lengthOfMonth().toDouble()
    return wholeMonths + leftoverDays / denom
}


// Optional () paceMonthly calculation : $30/Month ahead 0r $17/Month behind


/*

“Your projected date reflects your recent monthly pace (last {K} months).
It smooths out occasional big deposits,
so the forecast is stable and trustworthy—helping you plan how much to add each month.”
 */

