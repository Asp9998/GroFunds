package com.aryanspatel.grofunds.core

import android.content.Context
import androidx.annotation.RawRes
import kotlinx.serialization.json.Json

inline fun <reified T> Json.readRawResource(
    context: Context,
    @RawRes resId: Int
) : T{
    val input = context.resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }
    return decodeFromString(input)
}