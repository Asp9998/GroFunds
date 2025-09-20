//package com.aryanspatel.grofunds.domain.usecase
//
//import com.aryanspatel.grofunds.presentation.common.model.EntryKind
//import com.aryanspatel.grofunds.presentation.common.model.ParsedEntry
//import java.time.Instant
//import java.time.format.DateTimeFormatter
//
//
//interface ParsedEntryMapper {
//    fun map(doc: Map<String, Any?>, kind: EntryKind): ParsedEntry
//}
//
//class ParsedEntryMapperImpl : ParsedEntryMapper {
//
//    private val outFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    private val inFormatters = listOf(
//        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
//        DateTimeFormatter.ofPattern("MMM d, yyyy"),
//        DateTimeFormatter.ofPattern("d MMM yyyy")
//    )
//
//    override fun map(doc: Map<String, Any?>, kind: EntryKind): ParsedEntry {
//        val result = (doc["result"] as? Map<*, *>)?.mapKeys { it.key.toString() } ?: emptyMap<String, Any?>()
//
//        fun firstString(vararg keys: String): String? =
//            keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) as? String }?.takeIf { it.isNotBlank() }
//
//        fun firstNumber(vararg keys: String): Double? {
//            val v = keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) }
//            return when (v) {
//                is Number -> v.toDouble()
//                is String -> v.toDoubleOrNull()
//                else -> null
//            }
//        }
//
//        fun firstInstant(vararg keys: String): Instant? =
//            keys.firstNotNullOfOrNull { k -> (doc[k] ?: result[k]) }
//                ?.let { v ->
//                    when (v) {
//                        is Instant -> v
//                        is Long -> Instant.ofEpochMilli(v)
//                        // If your data layer still passes Firestore Timestamp, convert there before calling map()
//                        else -> null
//                    }
//                }
//
//        fun normalizeDate(keys: List<String>): String? {
//            firstInstant(*keys.toTypedArray())?.let { inst ->
//                return outFormatter.format(inst.atZone(ZoneId.systemDefault()).toLocalDate())
//            }
//            val s = firstString(*keys.toTypedArray()) ?: return null
//            return inFormatters.firstNotNullOfOrNull { fmt ->
//                runCatching { outFormatter.format(LocalDate.parse(s, fmt)) }.getOrNull()
//            }
//        }
//
//        val km = when (kind) {
//            EntryKind.EXPENSE -> KeyMap(
//                amount   = listOf("amount", "total", "value"),
//                currency = listOf("currency", "currencyCode"),
//                category = listOf("category", "categoryOrType", "mainCategory"),
//                subcategory = listOf("subcategory", "subCategory"),
//                party    = listOf("merchant", "vendor", "payee", "store"),
//                date     = listOf("date", "when", "purchasedAt"),
//                notes    = listOf("notes", "memo", "description")
//            )
//            EntryKind.INCOME -> KeyMap(
//                amount   = listOf("amount", "income", "total", "value"),
//                currency = listOf("currency", "currencyCode"),
//                category = listOf("category", "type", "incomeType"),
//                party    = listOf("source", "payer", "employer", "from"),
//                date     = listOf("date", "when", "receivedAt", "paidAt"),
//                notes    = listOf("notes", "memo", "description")
//            )
//            EntryKind.GOAL -> KeyMap(
//                amount   = listOf("target", "targetAmount", "goalAmount", "amount"),
//                currency = listOf("currency", "currencyCode"),
//                date     = listOf("due", "dueDate", "deadline"),
//                notes    = listOf("notes", "memo", "description"),
//                extras   = mapOf(
//                    "name"          to listOf("name", "goal", "title"),
//                    "currentAmount" to listOf("current", "saved", "currentAmount", "progressAmount", "startAmount")
//                )
//            )
//        }
//
//        fun up3(s: String?): String? = s?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.length == 3 }
//
//        return when (kind) {
//            EntryKind.EXPENSE -> ParsedEntry.Expense(
//                amount      = firstNumber(*km.amount.toTypedArray()),
//                currency    = up3(firstString(*km.currency.toTypedArray())),
//                category    = firstString(*km.category.toTypedArray()),
//                subcategory = firstString(*km.subcategory.toTypedArray()),
//                merchant    = firstString(*km.party.toTypedArray()),
//                dateText    = normalizeDate(km.date),
//                notes       = firstString(*km.notes.toTypedArray()),
//                confidence  = firstNumber(*km.confidence.toTypedArray())
//            )
//
//            EntryKind.INCOME -> ParsedEntry.Income(
//                amount     = firstNumber(*km.amount.toTypedArray()),
//                currency   = up3(firstString(*km.currency.toTypedArray())),
//                type       = firstString(*km.category.toTypedArray()),
//                dateText   = normalizeDate(km.date),
//                notes      = firstString(*km.notes.toTypedArray()),
//                confidence = firstNumber(*km.confidence.toTypedArray())
//            )
//
//            EntryKind.GOAL -> ParsedEntry.Goal(
//                title        = firstString(*(km.extras["name"] ?: emptyList()).toTypedArray()),
//                amount       = firstNumber(*km.amount.toTypedArray()),
//                startAmount  = firstNumber(*(km.extras["currentAmount"] ?: emptyList()).toTypedArray()),
//                currency     = up3(firstString(*km.currency.toTypedArray())),
//                dueDate      = normalizeDate(km.date),
//                notes        = firstString(*km.notes.toTypedArray()),
//                confidence   = firstNumber(*km.confidence.toTypedArray()),
//                type         = firstString(),                // as in your code: optional
//                dateText     = normalizeDate(km.date)
//            )
//        }
//    }
//}