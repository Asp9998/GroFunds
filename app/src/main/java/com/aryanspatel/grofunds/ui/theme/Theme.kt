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

    background = ThemeColor.Night.background,
    primaryFixed = ThemeColor.Night.brand,
    surface = ThemeColor.Night.surface,
    surfaceVariant = ThemeColor.Night.surfaceVariant,

    primary = ThemeColor.Night.primary,
    secondary = ThemeColor.Night.secondary,
    tertiary = ThemeColor.Night.final,

    onPrimary = ThemeColor.Night.primaryText,
    onSecondary = ThemeColor.Night.descriptionText,


    error = ThemeColor.Night.error,


    // green variant
    surfaceTint = ThemeColor.Night.mainIcon,
    onBackground = ThemeColor.Night.mainBorder,
    primaryContainer = ThemeColor.Night.mainContainerBackground1,
    secondaryContainer = ThemeColor.Night.mainContainerBackground2,
    surfaceContainer = ThemeColor.Night.mainContainerSurface,

    // red variant
    surfaceDim = ThemeColor.Night.redIcon,
    onSurface = ThemeColor.Night.redBorder,
    onPrimaryContainer = ThemeColor.Night.redContainerBackground1,
    onSecondaryContainer = ThemeColor.Night.redContainerBackground2,
    onSurfaceVariant = ThemeColor.Night.redContainerSurface ,

    // blue variant
    onPrimaryFixed = ThemeColor.Night.blueBrand,
    surfaceBright = ThemeColor.Night.blueIcon,
    onTertiary = ThemeColor.Night.blueBorder,
    tertiaryContainer = ThemeColor.Night.blueContainerBackground1,
    onTertiaryContainer = ThemeColor.Night.blueContainerBackground2,
    onTertiaryFixed = ThemeColor.Night.blueContainerSurface,

    surfaceContainerLow = ThemeColor.Night.incomeScreenCont,
    surfaceContainerLowest = ThemeColor.Night.expenseScreenCont,
    surfaceContainerHigh = ThemeColor.Night.savingsScreenCont,

    // Regular container
    inverseSurface = ThemeColor.Night.childContainerBackground,
    inverseOnSurface = ThemeColor.Night.childContainerBorder
)

private val LightColorScheme = lightColorScheme(

    primaryFixed = ThemeColor.Day.brand,

    primary = ThemeColor.Day.primary,
    secondary = ThemeColor.Day.secondary,
    tertiary = ThemeColor.Day.final,

    onPrimary = ThemeColor.Day.primaryText,
    onSecondary = ThemeColor.Day.descriptionText,

    background = ThemeColor.Day.background,
    surface = ThemeColor.Day.surface,
    surfaceVariant = ThemeColor.Day.surfaceVariant,


    // main green variant
    surfaceTint = ThemeColor.Day.mainIcon,
    onBackground = ThemeColor.Day.mainBorder,
    primaryContainer = ThemeColor.Day.mainContainerBackground1,
    secondaryContainer = ThemeColor.Day.mainContainerBackground2,
    surfaceContainer = ThemeColor.Day.mainContainerSurface,

    // red variant
    surfaceDim = ThemeColor.Day.redIcon,
    onSurface = ThemeColor.Day.redBorder,
    onPrimaryContainer = ThemeColor.Day.redContainerBackground1,
    onSecondaryContainer = ThemeColor.Day.redContainerBackground2,
    onSurfaceVariant = ThemeColor.Day.redContainerSurface,

    // blue variant
    onPrimaryFixed = ThemeColor.Day.blueBrand,
    surfaceBright = ThemeColor.Day.blueIcon,
    onTertiary = ThemeColor.Day.blueBorder,
    tertiaryContainer = ThemeColor.Day.blueContainerBackground1,
    onTertiaryContainer = ThemeColor.Day.blueContainerBackground2,
    onTertiaryFixed = ThemeColor.Day.blueContainerSurface,

    surfaceContainerLow = ThemeColor.Day.incomeScreenCont,
    surfaceContainerLowest = ThemeColor.Day.expenseScreenCont,
    surfaceContainerHigh = ThemeColor.Day.savingsScreenCont,

    // Regular container
    inverseSurface = ThemeColor.Day.childContainerBackground,
    inverseOnSurface = ThemeColor.Day.childContainerBorder,


    error = ThemeColor.Day.error
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