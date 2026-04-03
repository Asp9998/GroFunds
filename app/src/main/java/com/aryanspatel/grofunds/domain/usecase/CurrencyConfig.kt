package com.aryanspatel.grofunds.domain.usecase


fun formatAmountCurrSymbol(
    amount: Double,
    symbol: String,
    fractionDigit: Int = 2,
    spaceAfterSymbol: Boolean = false
): String {
    val safe = if (amount.isFinite()) amount else 0.0

    val nf = java.text.NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = fractionDigit
        maximumFractionDigits = fractionDigit
        isGroupingUsed = true
    }

    val number = nf.format(kotlin.math.abs(safe))
    val glue = if (spaceAfterSymbol) "\u00A0" else ""
    val body = "$symbol$glue$number"
    return if (safe < 0.0) "-$body" else body
}
