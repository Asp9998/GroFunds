package com.aryanspatel.grofunds.ui.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeColor(
    // Core backgrounds
    val background: Color,         // App background
    val surface: Color,            // Default surfaces (cards, sheets, menus)
    val surfaceVariant: Color,     // Alternative surface (chips, outlined cards)

    // Brand
    val brand: Color,
    val blueBrand: Color,
    val primary: Color,            // Main brand color (buttons, highlights)
    val secondary: Color,          // second brand color
    val final: Color,

    // Text
    val primaryText: Color,          // Text on background
    val descriptionText: Color,


    val error: Color,

    // expense / income main container color
    val expenseScreenCont: Color,
    val incomeScreenCont: Color,
    val savingsScreenCont: Color,

    val menuOptionsBackground: Color,

    val mainIcon: Color,
    val mainBorder: Color,
    val mainContainerBackground1: Color,
    val mainContainerBackground2: Color,
    val mainContainerSurface: Color,

    val redIcon: Color,
    val redBorder: Color,
    val redContainerBackground1: Color,
    val redContainerBackground2: Color,
    val redContainerSurface: Color,

    val blueIcon: Color,
    val blueBorder: Color,
    val blueContainerBackground1: Color,
    val blueContainerBackground2: Color,
    val blueContainerSurface: Color,

    val childContainerBackground: Color,
    val childContainerBorder: Color,
){

    object Day: ThemeColor(
        background = Color.White,
        surface = Color(0xFFEFF3F8),

//        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF3F4F6),

//        brand = Color(0xFF27C767),
        brand = Color(0xFF27C767),
        primary = Color(0xFF27C767),
        secondary = Color(0xFF2ECC71),
        final= Color(0xFF673AB7),
        primaryText = Color.Black,
        descriptionText = Color.Black.copy(0.6f),
        error = Color.Red,

//        floatingActionButton = Color(0xFF2196F3),
//        alternativeCardColor = Color(0xFFFFD8EE),

        expenseScreenCont = Color(0xFFFFCDD2),
        incomeScreenCont = Color(0xFFC8E6C9),
        savingsScreenCont = Color(0xFFBBDEFB),
        menuOptionsBackground = Color(0xFFEFF6FB),

        // border and container background colors
        mainIcon = Color(0xFF27C767),
        mainBorder = Color(0xFF6EE7B7).copy(alpha = 0.4f),
        mainContainerBackground1 = Color(0xFFA7F3D0).copy(alpha = 0.4f),
        mainContainerBackground2 = Color(0xFFA7F3D0).copy(alpha = 0.1f),
        mainContainerSurface = Color(0xFF34D399).copy(alpha = 0.2f),

        // Light theme — BLUE variant
        blueBrand = Color(0xFF1976D2),
        blueIcon = Color(0xFF3B82F6),                      // ~blue-500
        blueBorder = Color(0xFF93C5FD).copy(alpha = 0.4f), // ~blue-300 @40%
        blueContainerBackground1 = Color(0xFFBFDBFE).copy(alpha = 0.4f), // ~blue-200 @40%
        blueContainerBackground2 = Color(0xFFBFDBFE).copy(alpha = 0.1f), // ~blue-200 @10%
        blueContainerSurface = Color(0xFF60A5FA).copy(alpha = 0.2f),     // ~blue-400 @20%

        redIcon = Color(0xFFEF4444),                     // ~red-500
        redBorder = Color(0xFFFCA5A5).copy(alpha = 0.4f),// ~red-300
        redContainerBackground1 = Color(0xFFFECACA).copy(alpha = 0.4f), // ~red-200
        redContainerBackground2 = Color(0xFFFECACA).copy(alpha = 0.1f), // ~red-200 @10%
        redContainerSurface = Color(0xFFF87171).copy(alpha = 0.2f),      // ~red-400 @20%


        childContainerBackground = Color.Black.copy(alpha = 0.04f),
        childContainerBorder = Color.Black.copy(alpha = 0.06f),

    )

    object Night: ThemeColor(
//        background = Color.Black,
        background = Color(0xFF0C1116),

        surface = Color(0xFF1B2732),         // + subtle lift for cards/sheets
        surfaceVariant = Color(0xFF10161C),

//        surface = Color(0xFF121110),
//        surfaceVariant = Color(0xFF2E2E2E),

        brand = Color(0xFF388E3C),
        primary = Color(0xFF0B1A0C),
        secondary = Color(0xFF102515),
        final = Color(0xFF673AB7),
        primaryText = Color.White,
        descriptionText = Color.White.copy(0.6f),
        error = Color.Red,

//        floatingActionButton = Color(0xFF034273),
//        alternativeCardColor = Color(0xFF633B48),

        expenseScreenCont = Color(0xFF2B1212),
        incomeScreenCont = Color(0xFF0E1F12),
        savingsScreenCont = Color(0xFF0F1E2F),

        menuOptionsBackground = Color(0xFF0F1B25),

        mainIcon = Color(0xFF6EE7B7),
        mainBorder = Color(0xFF34D399).copy(alpha = 0.2f) ,
        mainContainerBackground2 = Color(0xFF34D399).copy(alpha = 0.05f),
        mainContainerBackground1 = Color(0xFF34D399).copy(alpha = 0.1f),
        mainContainerSurface = Color(0xFF34D399).copy(alpha = 0.2f),

        blueBrand = Color(0xFF1976D2),
        blueIcon = Color(0xFF93C5FD),                       // ~blue-300
        blueBorder = Color(0xFF60A5FA).copy(alpha = 0.2f),  // ~blue-400 @20%
        blueContainerBackground2 = Color(0xFF60A5FA).copy(alpha = 0.05f), // @5%
        blueContainerBackground1 = Color(0xFF60A5FA).copy(alpha = 0.1f),  // @10%
        blueContainerSurface = Color(0xFF60A5FA).copy(alpha = 0.2f),       // @20%

        redIcon = Color(0xFFFCA5A5),                                     // ~red-300
        redBorder = Color(0xFFF87171).copy(alpha = 0.2f),                // ~red-400 @20%
        redContainerBackground2 = Color(0xFFF87171).copy(alpha = 0.05f), // @5%
        redContainerBackground1 = Color(0xFFF87171).copy(alpha = 0.1f),  // @10%
        redContainerSurface = Color(0xFFF87171).copy(alpha = 0.2f),       // @20%

        childContainerBackground = Color.White.copy(alpha = 0.04f),
        childContainerBorder = Color.White.copy(alpha = 0.06f),
    )
}