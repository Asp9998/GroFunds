package com.aryanspatel.grofunds.presentation.common.model

import androidx.compose.ui.graphics.Color

data class CurrencyInfo(
    val code: String,          // e.g., "CAD"
    val symbol: String,        // e.g., "$"
    val name: String           // e.g., "Canadian Dollar"
)

data class BudgetInfo(
    val monthlyBudget: Double,  // e.g., 2000.0
    val currencyCode: String    // e.g., "CAD"
)

data class CategoryBudget(
    val id: String,
    val label: String,
    val emoji: String? = null,  // optional for a fun accent
    val color: Color = Color(0xFF7DE3F3),
    val monthlyBudget: Double,
    val currencyCode: String
)


//private val Icons.Rounded.SupportAgent: ImageVector

// --- Dummy data ---
val demoCurrency = CurrencyInfo(
    code = "CAD",
    symbol = "$",
    name = "Canadian Dollar"
)

val demoBudget = BudgetInfo(
    monthlyBudget = 2200.0,
    currencyCode = "CAD"
)

val demoCategories = listOf(
    CategoryBudget(
        id = "groceries",
        label = "Groceries",
        emoji = "🛒",
        color = Color(0xFF34D399),
        monthlyBudget = 420.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "rent",
        label = "Rent",
        emoji = "🏠",
        color = Color(0xFF60A5FA),
        monthlyBudget = 1000.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "transport",
        label = "Transport",
        emoji = "🚌",
        color = Color(0xFFF59E0B),
        monthlyBudget = 180.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "dining",
        label = "Dining Out",
        emoji = "🍽️",
        color = Color(0xFFFB7185),
        monthlyBudget = 150.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "entertainment",
        label = "Entertainment",
        emoji = "🎬",
        color = Color(0xFFA78BFA),
        monthlyBudget = 120.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "utilities",
        label = "Utilities",
        emoji = "💡",
        color = Color(0xFF22D3EE),
        monthlyBudget = 160.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "subscriptions",
        label = "Subscriptions",
        emoji = "📦",
        color = Color(0xFF10B981),
        monthlyBudget = 60.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "shopping",
        label = "Shopping",
        emoji = "🛍️",
        color = Color(0xFFE879F9),
        monthlyBudget = 120.0,
        currencyCode = "CAD"
    ),
    CategoryBudget(
        id = "health",
        label = "Health",
        emoji = "🩺",
        color = Color(0xFFEF4444),
        monthlyBudget = 70.0,
        currencyCode = "CAD"
    )
)



// -------------------------
// Models
// -------------------------
enum class ThemeMode { System, Light, Dark }

data class YouUi(
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val currency: String,
    val timezone: String,
    val theme: ThemeMode,
    val ai: AiPrefs,
    val notif: NotifPrefs,
    val syncStatus: SyncStatus,
    val referralCode: String?,
    val proStatus: ProStatus
) {
    companion object {
        fun demo() = YouUi(
            name = "Aryan Patel",
            email = "aryansp9834@gmail.com",
            avatarUrl = null,
            currency = "CAD",
            timezone = "America/Winnipeg",
            theme = ThemeMode.System,
            ai = AiPrefs(),
            notif = NotifPrefs(),
            syncStatus = SyncStatus(status = "OK", last = "2 min ago"),
            referralCode = "ARYAN-9J2K",
            proStatus = ProStatus(isPro = true, renewsOn = "Nov 14")
        )
    }
}

data class AiPrefs(
    val tone: AiTone = AiTone.Direct,
    val focusSaving: Boolean = true,
    val focusDebt: Boolean = false,
    val focusSubs: Boolean = true,
    val cadence: AiCadence = AiCadence.Daily
)
enum class AiTone { Direct, Friendly }
enum class AiCadence { Off, Weekly, Daily }

data class NotifPrefs(
    val upcomingBills: Boolean = true,
    val anomalies: Boolean = true,
    val duplicates: Boolean = true,
    val insights: Boolean = true
)

data class SyncStatus(val status: String, val last: String)
data class ProStatus(val isPro: Boolean, val renewsOn: String)


data class ProStatuses(
    val isActive: Boolean,
    val renewsOn: String? // e.g., "Nov 02"
)