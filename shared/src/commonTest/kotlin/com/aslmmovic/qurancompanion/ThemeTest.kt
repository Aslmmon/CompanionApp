package com.aslmmovic.qurancompanion

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.ui.theme.DarkColorScheme
import com.aslmmovic.qurancompanion.ui.theme.LightColorScheme
import com.aslmmovic.qurancompanion.ui.theme.SahabaCardBorder
import com.aslmmovic.qurancompanion.ui.theme.SahabaEvergreen
import com.aslmmovic.qurancompanion.ui.theme.SahabaGoldAccent
import com.aslmmovic.qurancompanion.ui.theme.SahabaIvoryBackground
import com.aslmmovic.qurancompanion.ui.theme.SahabaMidnightBackground
import com.aslmmovic.qurancompanion.ui.theme.SahabaMidnightBorder
import com.aslmmovic.qurancompanion.ui.theme.SahabaMidnightSurface
import com.aslmmovic.qurancompanion.ui.theme.SahabaShapes
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import com.aslmmovic.qurancompanion.ui.theme.getThemeColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTest {

    @Test
    fun test_AC01_givenLightMode_whenReadingLightColorScheme_thenHasCanonicalSahabaModernTokens() {
        assertEquals(SahabaEvergreen, LightColorScheme.primary)
        assertEquals(Color(0xFF1B4332), LightColorScheme.primary)
        assertEquals(SahabaWarmGold, LightColorScheme.secondary)
        assertEquals(Color(0xFFD4AF37), LightColorScheme.secondary)
        assertEquals(SahabaIvoryBackground, LightColorScheme.background)
        assertEquals(Color(0xFFF9F8F3), LightColorScheme.background)
        assertEquals(Color.White, LightColorScheme.surface)
        assertEquals(Color(0xFFFFFFFF), LightColorScheme.surface)
        assertEquals(SahabaCardBorder, LightColorScheme.outline)
        assertEquals(Color(0xFFEBE8DF), LightColorScheme.outline)
    }

    @Test
    fun test_AC02_givenDarkMode_whenReadingDarkColorScheme_thenHasCanonicalSahabaModernTokens() {
        assertEquals(SahabaGoldAccent, DarkColorScheme.primary)
        assertEquals(Color(0xFFE6C265), DarkColorScheme.primary)
        assertEquals(SahabaMidnightBackground, DarkColorScheme.background)
        assertEquals(Color(0xFF0B1320), DarkColorScheme.background)
        assertEquals(SahabaMidnightSurface, DarkColorScheme.surface)
        assertEquals(Color(0xFF131F33), DarkColorScheme.surface)
        assertEquals(SahabaMidnightBorder, DarkColorScheme.outline)
        assertEquals(Color(0xFF25344D), DarkColorScheme.outline)
    }

    @Test
    fun test_AC03_givenDarkThemeFlag_whenGetThemeColorScheme_thenReturnsMatchingPaletteRegardlessOfThemeName() {
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true))

        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "Sahaba"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "Sahaba"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "desert"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "desert"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "emerald"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "emerald"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "ocean"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "ocean"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "night"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "night"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "gold"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "gold"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "sunrise"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "sunrise"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = null))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = null))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "unknown_theme_123"))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "unknown_theme_123"))
        assertEquals(LightColorScheme, getThemeColorScheme(darkTheme = false, themeName = "  SUNRISE  "))
        assertEquals(DarkColorScheme, getThemeColorScheme(darkTheme = true, themeName = "GoLdEn DuNe"))
    }

    @Test
    fun test_AC04_givenSahabaShapes_thenMatchesDesignTokens() {
        assertEquals(RoundedCornerShape(8.dp), SahabaShapes.small)
        assertEquals(RoundedCornerShape(16.dp), SahabaShapes.medium)
        assertEquals(RoundedCornerShape(20.dp), SahabaShapes.large)
        assertEquals(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), SahabaShapes.extraLarge)
    }
}
