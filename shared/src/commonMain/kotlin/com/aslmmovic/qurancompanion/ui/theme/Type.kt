package com.aslmmovic.qurancompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.cairo
import qurancompanion.shared.generated.resources.outfit

// Define custom font families using resources inside Composable context
val CairoFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.cairo, FontWeight.Normal),
        Font(Res.font.cairo, FontWeight.Medium),
        Font(Res.font.cairo, FontWeight.SemiBold),
        Font(Res.font.cairo, FontWeight.Bold)
    )

val OutfitFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.outfit, FontWeight.Normal),
        Font(Res.font.outfit, FontWeight.Medium),
        Font(Res.font.outfit, FontWeight.SemiBold),
        Font(Res.font.outfit, FontWeight.Bold)
    )

// Define base typography with dynamic font selection based on active language
@Composable
fun getQuranCompanionTypography(isArabic: Boolean): Typography {
    val fontFamily = if (isArabic) CairoFontFamily else OutfitFontFamily
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
    )
}

// Custom text style specifically designed for Quranic / Arabic verses using Cairo font
val QuranArabicTextStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 44.sp // High line-height to accommodate Arabic vowel markings (tashkeel)
    )
