package com.aryanspatel.grofunds.domain.usecase

import com.google.firebase.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

object DateConverters {

    /**
     * Centralized converters between UI date strings, LocalDate, epoch millis, and Firebase Timestamp.
     *
     * DESIGN
     * - UI uses *date-only* strings ("EEE MMM dd, yyyy"), e.g., "MON OCT 06, 2025".
     * - Storage of *moments* uses Instant (UTC under the hood): Timestamp / epoch millis.
     * - Display to humans is based on the *system zone* (you do not store per-user zones).
     *
     * ⚠️ IMPORTANT:
     * - Material3 DatePicker's selectedDateMillis is **UTC midnight** for the picked calendar day.
     *   Never feed picker millis into functions that interpret millis in the **system zone**;
     *   use the dedicated `pickerMillisToLocalDate()` / `localDateToPickerMillis()` helpers.
     *
     * THREADING
     * - All functions are CPU-only (no I/O). Safe on main thread for one-offs.
     * - For bulk conversions, prefer Dispatchers.Default.
     */

    // UI format: "MON OCT 06, 2025"
    // Parsing is case-insensitive; output should be uppercased for consistency.
    private val UI_FORMATTER_WITHOUT_DAY: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM dd, yyyy")
            .toFormatter(Locale.US)

    private val UI_FORMATTER: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("EEE MMM dd, yyyy")
            .toFormatter(Locale.US)


    // Prefer a getter if your app can change device timezone at runtime.
    private val SYSTEM_ZONE: ZoneId = ZoneId.systemDefault()


    /**
     * String ("EEE MMM dd, yyyy") -> Firebase Timestamp.
     * Interprets the date at *local midnight* in system zone, then converts to an Instant.
     *
     * ✅ Use when saving a date-only as a precise moment anchored at local start-of-day.
     * ⚠️ DST note: On spring-forward days where 00:00 may not exist, Java chooses first valid time
     * (e.g., 01:00). The *instant* shifts ~1 hour, but the *calendar date* remains correct.
     */
    fun stringToTimestamp(s: String): Timestamp {
        val ld = LocalDate.parse(s, UI_FORMATTER)
        val instant = ld.atStartOfDay(SYSTEM_ZONE).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }

    /**
     * String ("EEE MMM dd, yyyy") -> epoch millis.
     * Same semantics as stringToTimestamp but returns millis since epoch.
     */
    fun stringToMillis(s: String): Long {
        val ld = LocalDate.parse(s, UI_FORMATTER)
        return ld.atStartOfDay(SYSTEM_ZONE).toInstant().toEpochMilli()
    }
    fun stringToMillisWithoutDay(s: String): Long {
        val ld = LocalDate.parse(s, UI_FORMATTER_WITHOUT_DAY)
        return ld.atStartOfDay(SYSTEM_ZONE).toInstant().toEpochMilli()
    }

    /**
     * Firebase Timestamp -> String ("EEE MMM dd, yyyy") in system zone.
     * ✅ Use for rendering moments (e.g., from Firestore) as a local calendar date.
     */
    fun timestampToString(ts: Timestamp): String {
        val instant = Instant.ofEpochSecond(ts.seconds, ts.nanoseconds.toLong())
        val ld = instant.atZone(SYSTEM_ZONE).toLocalDate()
        return ld.format(UI_FORMATTER).uppercase(Locale.US)
    }

    /**
     * epoch millis -> String ("EEE MMM dd, yyyy") in system zone.
     * ✅ Use for millis that represent a real Instant (system-zone interpretation for display).
     * ❌ DO NOT use this with Material3 DatePicker's selectedDateMillis (UTC midnight)! See picker helpers.
     */
    fun millisToStringWithDay(ms: Long): String {
        val ld = Instant.ofEpochMilli(ms).atZone(SYSTEM_ZONE).toLocalDate()
        return ld.format(UI_FORMATTER).uppercase(Locale.US)
    }

    fun millisToString(ms: Long): String {
        val ld = Instant.ofEpochMilli(ms).atZone(SYSTEM_ZONE).toLocalDate()
        return ld.format(UI_FORMATTER_WITHOUT_DAY)
    }

    /**
     * Firebase Timestamp -> epoch millis.
     * Straight Instant conversion (timezone-agnostic).
     */
    fun timestampToMillis(ts: Timestamp): Long {
        val instant = Instant.ofEpochSecond(ts.seconds, ts.nanoseconds.toLong())
        return instant.toEpochMilli()
    }

    /**
     * epoch millis -> Firebase Timestamp.
     * Straight Instant conversion (timezone-agnostic).
     */
    fun millisToTimestamp(ms: Long): Timestamp {
        val instant = Instant.ofEpochMilli(ms)
        return Timestamp(instant.epochSecond, instant.nano)
    }

    /**
     * String ("EEE MMM dd, yyyy") -> LocalDate (date-only, no zone).
     * ✅ Use for UI state, filters, budgets, etc.
     */
    fun stringToLocalDate(s: String): LocalDate =
        LocalDate.parse(s.trim(), UI_FORMATTER)

    fun stringToLocalDateOrNull(s: String): LocalDate? =
         runCatching { LocalDate.parse(s.trim(), UI_FORMATTER) }.getOrNull()

    /**
     * LocalDate -> millis at UTC midnight for that date.
     * ✅ Use as DatePicker initialSelectedDateMillis. Prevents off-by-one shifts.
     */
    fun localDateToPickerMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /**
     * DatePicker selectedDateMillis (UTC midnight) -> LocalDate.
     * ✅ Use when reading back from DatePicker before formatting or saving.
     */
    fun pickerMillisToLocalDate(utcMillis: Long): LocalDate =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

    /**
     * LocalDate -> UI string ("EEE MMM dd, yyyy"), uppercased.
     * Keep formatting logic in one place for consistency.
     */
    fun formatUiDate(date: LocalDate): String =
        date.format(UI_FORMATTER).uppercase(Locale.US)


    /**
     *  Date normalizing - getting used to return Date in string, no matter is the input formate.
     */
    private val INPUT_FMTS = listOf(
        "EEE MMM dd, yyyy", "MMM d, yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "d MMM yyyy"
    ).map { pat -> DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pat).toFormatter(Locale.getDefault()) }

    fun normalizeDateStrict(
        doc: Map<String, Any?>,
        result: Map<String, Any?>,
        vararg keys: String
    ): String {
        val zone = ZoneId.systemDefault()

        val v = keys.asSequence().mapNotNull { k -> doc[k] ?: result[k] }.firstOrNull()

        val date: LocalDate? = when (v) {
            is Timestamp -> Instant.ofEpochSecond(v.seconds, v.nanoseconds.toLong()).atZone(zone).toLocalDate()
            is Long      -> Instant.ofEpochMilli(v).atZone(zone).toLocalDate()
            is String    -> {
                val s = v.trim()
                s.toLongOrNull()?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                    ?: INPUT_FMTS.firstNotNullOfOrNull { fmt -> runCatching { LocalDate.parse(s, fmt) }.getOrNull() }
                    ?: runCatching { Instant.parse(s).atZone(zone).toLocalDate() }.getOrNull()
            }
            else -> null
        }

        return formatUiDate(date ?: LocalDate.now(zone)) // always "EEE MMM dd, yyyy" uppercased
    }


}
