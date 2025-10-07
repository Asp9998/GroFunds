package com.aryanspatel.grofunds.ui.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeColor(
    // Core backgrounds
    val background: Color,         // App background
    val surface: Color,            // Default surfaces (cards, sheets, menus)
    val surfaceVariant: Color,     // Alternative surface (chips, outlined cards)

    // Brand
    val primary: Color,            // Main brand color (buttons, highlights)
    val secondary: Color,          // second brand color
    val final: Color,

    // Text
    val primaryText: Color,          // Text on background
    val descriptionText: Color,


    val error: Color,

    // Floating Action Button
    val floatingActionButton: Color,
    val alternativeCardColor: Color,

    // expense / income main container color
    val expenseScreenCont: Color,
    val incomeScreenCont: Color,
    val savingsScreenCont: Color,

    val menuOptionsBackground: Color,

//    Color(0xFF673AB7) - purple button color

){

    object Day: ThemeColor(
//        background = Color.White,
        background = Color(0xFFEFF3F8),
//        background = Color(0xFFF8FAFC),
        surface = Color.White,
//        surface = Color(0xFFEFF3F8),
        surfaceVariant = Color(0xFFC5D3E0),

        primary = Color(0xFF27C767),
        secondary = Color(0xFF2ECC71),
        final= Color(0xFF673AB7),
        primaryText = Color.Black,
        descriptionText = Color.Black.copy(0.7f),
        error = Color.Red,
        floatingActionButton = Color(0xFF2196F3),
        alternativeCardColor = Color(0xFFFFD8EE),

        expenseScreenCont = Color(0xFFFFCDD2),
        incomeScreenCont = Color(0xFFC8E6C9),
        savingsScreenCont = Color(0xFFBBDEFB),

        menuOptionsBackground = Color(0xFFEFF6FB)

    )

    object Night: ThemeColor(
        background = Color.Black,
        surface = Color(0xFF121110),
        surfaceVariant = Color(0xFF2E2E2E),

        primary = Color(0xFF388E3C),
        secondary = Color(0xFF43A047),
        final = Color(0xFF673AB7),
        primaryText = Color.White,
        descriptionText = Color.White.copy(0.7f),
        error = Color.Red,
        floatingActionButton = Color(0xFF034273),
        alternativeCardColor = Color(0xFF633B48),

        expenseScreenCont = Color(0xFF2B1212),
        incomeScreenCont = Color(0xFF0E1F12),
        savingsScreenCont = Color(0xFF0A1522),

        menuOptionsBackground = Color(0xFF0F1B25)
    )
}