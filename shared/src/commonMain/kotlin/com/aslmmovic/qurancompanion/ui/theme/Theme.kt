package com.aslmmovic.qurancompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

fun getThemeColorScheme(darkTheme: Boolean, themeName: String?): ColorScheme {
    if (themeName == null) return if (darkTheme) DarkColorScheme else LightColorScheme

    return when (themeName.lowercase()) {
        "desert" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = DesertPrimaryDark,
                    onPrimary = DesertBackgroundDark,
                    secondary = DesertSecondaryDark,
                    onSecondary = DesertBackgroundDark,
                    background = DesertBackgroundDark,
                    onBackground = DesertOnBackgroundDark,
                    surface = DesertSurfaceDark,
                    onSurface = DesertOnBackgroundDark,
                    primaryContainer = DesertSurfaceDark,
                    onPrimaryContainer = DesertPrimaryDark
                )
            } else {
                lightColorScheme(
                    primary = DesertPrimaryLight,
                    onPrimary = DesertSurfaceLight,
                    secondary = DesertSecondaryLight,
                    onSecondary = DesertOnBackgroundLight,
                    background = DesertBackgroundLight,
                    onBackground = DesertOnBackgroundLight,
                    surface = DesertSurfaceLight,
                    onSurface = DesertOnBackgroundLight,
                    primaryContainer = DesertSurfaceLight,
                    onPrimaryContainer = DesertPrimaryLight
                )
            }
        }

        "emerald" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = EmeraldPrimaryDark,
                    onPrimary = EmeraldBackgroundDark,
                    secondary = EmeraldSecondaryDark,
                    onSecondary = EmeraldBackgroundDark,
                    background = EmeraldBackgroundDark,
                    onBackground = EmeraldOnBackgroundDark,
                    surface = EmeraldSurfaceDark,
                    onSurface = EmeraldOnBackgroundDark,
                    primaryContainer = EmeraldSurfaceDark,
                    onPrimaryContainer = EmeraldPrimaryDark
                )
            } else {
                lightColorScheme(
                    primary = EmeraldPrimaryLight,
                    onPrimary = EmeraldSurfaceLight,
                    secondary = EmeraldSecondaryLight,
                    onSecondary = EmeraldSurfaceLight,
                    background = EmeraldBackgroundLight,
                    onBackground = EmeraldOnBackgroundLight,
                    surface = EmeraldSurfaceLight,
                    onSurface = EmeraldOnBackgroundLight,
                    primaryContainer = EmeraldSurfaceLight,
                    onPrimaryContainer = EmeraldPrimaryLight
                )
            }
        }

        "ocean" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = OceanPrimaryDark,
                    onPrimary = OceanBackgroundDark,
                    secondary = OceanSecondaryDark,
                    onSecondary = OceanBackgroundDark,
                    background = OceanBackgroundDark,
                    onBackground = OceanOnBackgroundDark,
                    surface = OceanSurfaceDark,
                    onSurface = OceanOnBackgroundDark,
                    primaryContainer = OceanSurfaceDark,
                    onPrimaryContainer = OceanPrimaryDark
                )
            } else {
                lightColorScheme(
                    primary = OceanPrimaryLight,
                    onPrimary = OceanSurfaceLight,
                    secondary = OceanSecondaryLight,
                    onSecondary = OceanSurfaceLight,
                    background = OceanBackgroundLight,
                    onBackground = OceanOnBackgroundLight,
                    surface = OceanSurfaceLight,
                    onSurface = OceanOnBackgroundLight,
                    primaryContainer = OceanSurfaceLight,
                    onPrimaryContainer = OceanPrimaryLight
                )
            }
        }

        "night" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = NightPrimaryDark,
                    onPrimary = NightBackgroundDark,
                    secondary = NightSecondaryDark,
                    onSecondary = NightBackgroundDark,
                    background = NightBackgroundDark,
                    onBackground = NightOnBackgroundDark,
                    surface = NightSurfaceDark,
                    onSurface = NightOnBackgroundDark,
                    primaryContainer = NightSurfaceDark,
                    onPrimaryContainer = NightPrimaryDark
                )
            } else {
                lightColorScheme(
                    primary = NightPrimaryLight,
                    onPrimary = NightSurfaceLight,
                    secondary = NightSecondaryLight,
                    onSecondary = NightSurfaceLight,
                    background = NightBackgroundLight,
                    onBackground = NightOnBackgroundLight,
                    surface = NightSurfaceLight,
                    onSurface = NightOnBackgroundLight,
                    primaryContainer = NightSurfaceLight,
                    onPrimaryContainer = NightPrimaryLight
                )
            }
        }

        "gold" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = GoldPrimaryDark,
                    onPrimary = GoldBackgroundDark,
                    secondary = GoldSecondaryDark,
                    onSecondary = GoldBackgroundDark,
                    background = GoldBackgroundDark,
                    onBackground = GoldOnBackgroundDark,
                    surface = GoldSurfaceDark,
                    onSurface = GoldOnBackgroundDark,
                    primaryContainer = GoldSurfaceDark,
                    onPrimaryContainer = GoldPrimaryDark
                )
            } else {
                lightColorScheme(
                    primary = GoldPrimaryLight,
                    onPrimary = GoldSurfaceLight,
                    secondary = GoldSecondaryLight,
                    onSecondary = GoldOnBackgroundLight,
                    background = GoldBackgroundLight,
                    onBackground = GoldOnBackgroundLight,
                    surface = GoldSurfaceLight,
                    onSurface = GoldOnBackgroundLight,
                    primaryContainer = GoldSurfaceLight,
                    onPrimaryContainer = GoldPrimaryLight
                )
            }
        }

        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
}

@Composable
fun QuranCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String? = null,
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(darkTheme, themeName)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getQuranCompanionTypography(isArabic),
        content = content
    )
}
