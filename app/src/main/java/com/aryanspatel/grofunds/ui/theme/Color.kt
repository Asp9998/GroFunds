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

    // Text
    val primaryText: Color,          // Text on background
    val descriptionText: Color,


    val error: Color,

    // Floating Action Button
    val floatingActionButton: Color,
    val alternativeCardColor: Color,

){

    object Day: ThemeColor(
        background = Color.White,
//        surface = Color(0xFFF7F8FA),
        surface = Color(0xFFECEFF1),
        surfaceVariant = Color(0xFFEEEFFD),
//        primary = Color(0xFF2ECC71),
        primary = Color(0xFF27C767),
        secondary = Color(0xFF2ECC71),
        primaryText = Color.Black,
        descriptionText = Color.Black.copy(0.8f),
        error = Color.Red,
        floatingActionButton = Color(0xFF2196F3),
        alternativeCardColor = Color(0xFFFFD8EE)



    )

    object Night: ThemeColor(
        background = Color.Black,
        surface = Color(0xFF121212),
        surfaceVariant = Color(0xFF1A1A1A),
        primary = Color(0xFF388E3C),
        secondary = Color(0xFF43A047),
        primaryText = Color.White,
        descriptionText = Color.White.copy(0.8f),
        error = Color.Red,
        floatingActionButton = Color(0xFF034273),
        alternativeCardColor = Color(0xFF633B48)


    )
}