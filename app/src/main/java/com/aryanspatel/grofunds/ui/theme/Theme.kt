package com.aryanspatel.grofunds.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.aryanspatel.grofunds.ui.theme.ThemeColor

private val DarkColorScheme = darkColorScheme(

    primary = ThemeColor.Night.primary,                      // Main brand color
    secondary = ThemeColor.Night.secondary,                  // second brand color
    onPrimary = ThemeColor.Night.primaryText,                // Text on primary buttons

    onSecondary = ThemeColor.Night.descriptionText,         // Text/icons on secondary

    background = ThemeColor.Night.background,               // App background
//    onBackground = ThemeColor.Day.titleText,              // Text on background
//
    surface = ThemeColor.Night.surface,                     // Card, surfaces
//    onSurface = ThemeColor.Day.titleText,                 // Text/icons on surface
//
//    surfaceVariant = ThemeColor.Day.surfaceVariant,       // Alternative surface (chips, etc.)
//    onSurfaceVariant = ThemeColor.Day.bodyText,

    error = ThemeColor.Night.error,

    primaryContainer = ThemeColor.Night.floatingActionButton,
    secondaryContainer = ThemeColor.Night.alternativeCardColor,

)

private val LightColorScheme = lightColorScheme(

    primary = ThemeColor.Day.primary,                      // Main brand color
    secondary = ThemeColor.Day.secondary,                  // secondary brand color
    onPrimary = ThemeColor.Day.primaryText,                  // Text on primary buttons

    onSecondary = ThemeColor.Day.descriptionText,           // Text/icons on secondary
//
//    tertiary = ThemeColor.Day.selectedIcon,               // Accent (e.g. selected icon)
//    onTertiary = ThemeColor.Day.fadeIcon,                 // Disabled/unselected icon
//
    background = ThemeColor.Day.background,               // App background
//    onBackground = ThemeColor.Day.titleText,              // Text on background
//
    surface = ThemeColor.Day.surface,                     // Card, surfaces
//    onSurface = ThemeColor.Day.titleText,                 // Text/icons on surface
//
//    surfaceVariant = ThemeColor.Day.surfaceVariant,       // Alternative surface (chips, etc.)
//    onSurfaceVariant = ThemeColor.Day.bodyText,
//
    primaryContainer = ThemeColor.Day.floatingActionButton,
    secondaryContainer = ThemeColor.Day.alternativeCardColor,
//    secondaryContainer = ThemeColor.Day.goalCardChild,    // Goal card sub-items
//    tertiaryContainer = ThemeColor.Day.arcBackground,     // Progress arcs
//
    error = ThemeColor.Day.error,                  // Error / High priority
//    onError = ThemeColor.Day.bodyText,
//
//    surfaceTint = ThemeColor.Day.goalProgressBar,                 // Tint for progress bars
//    inverseSurface = ThemeColor.Day.goalAnimationBackground,      // Animated backgrounds

)

@Composable
fun GroFundsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}