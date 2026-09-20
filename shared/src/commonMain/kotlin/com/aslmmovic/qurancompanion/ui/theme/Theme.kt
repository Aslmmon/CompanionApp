package com.aslmmovic.qurancompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

internal val LightColorScheme = lightColorScheme(
    primary = SahabaEvergreen,
    onPrimary = SahabaOnEvergreen,
    primaryContainer = SahabaMintContainer,
    onPrimaryContainer = SahabaOnMintContainer,
    secondary = SahabaWarmGold,
    onSecondary = SahabaOnWarmGold,
    background = SahabaIvoryBackground,
    onBackground = SahabaDarkText,
    surface = SahabaCardSurface,
    onSurface = SahabaDarkText,
    surfaceVariant = SahabaPillBackground,
    onSurfaceVariant = SahabaTextMuted,
    outline = SahabaCardBorder,
    outlineVariant = SahabaCardBorder
)

internal val DarkColorScheme = darkColorScheme(
    primary = SahabaGoldAccent,
    onPrimary = SahabaOnGoldAccent,
    primaryContainer = Color(0xFF26362C),
    onPrimaryContainer = Color(0xFFE2F1E8),
    secondary = SahabaEmeraldHighlight,
    onSecondary = Color(0xFF061A10),
    background = SahabaMidnightBackground,
    onBackground = Color(0xFFF5F7FA),
    surface = SahabaMidnightSurface,
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF1B2A44),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = SahabaMidnightBorder,
    outlineVariant = SahabaMidnightBorder
)

val SahabaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
)

fun getThemeColorScheme(darkTheme: Boolean, themeName: String? = null): ColorScheme {
    return if (darkTheme) DarkColorScheme else LightColorScheme
}


@Composable
fun SahabaCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String? = null,
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(darkTheme, themeName)
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = SahabaShapes,
            typography = getSahabaTypography(isArabic),
            content = content
        )
    }
}
