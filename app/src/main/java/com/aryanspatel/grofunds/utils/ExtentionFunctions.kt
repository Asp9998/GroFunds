package com.aryanspatel.grofunds.utils

import kotlin.text.filterIndexed

fun cleanAmountInput(amount: String) : String {
    val clean  = amount.filterIndexed { idx, c ->
        c.isDigit() || (c == '.' && amount.indexOf('.') == idx)
    }
    return clean
}