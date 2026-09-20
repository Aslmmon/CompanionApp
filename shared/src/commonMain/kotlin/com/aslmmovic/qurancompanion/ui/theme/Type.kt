package com.aslmmovic.qurancompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.el_messiri
import qurancompanion.shared.generated.resources.outfit

// Define custom font families using resources inside Composable context
val ElMessiriFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.el_messiri, FontWeight.Normal),
        Font(Res.font.el_messiri, FontWeight.Medium),
        Font(Res.font.el_messiri, FontWeight.SemiBold),
        Font(Res.font.el_messiri, FontWeight.Bold)
    )

val OutfitFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.outfit, FontWeight.Normal),
        Font(Res.font.outfit, FontWeight.Medium),
        Font(Res.font.outfit, FontWeight.SemiBold),
        Font(Res.font.outfit, FontWeight.Bold)
    )

// Build typography token scale for specified font family
fun createSahabaTypography(fontFamily: FontFamily = FontFamily.Default): Typography {
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = defaultTypography.headlineMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = defaultTypography.headlineSmall.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleLarge = defaultTypography.titleLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = defaultTypography.titleSmall.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = defaultTypography.bodyLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodySmall = defaultTypography.bodySmall.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = defaultTypography.labelLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = defaultTypography.labelMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 1.2.sp
        ),
        labelSmall = defaultTypography.labelSmall.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}

// Define base typography with dynamic font selection based on active language
@Composable
fun getSahabaTypography(isArabic: Boolean): Typography {
    val fontFamily = if (isArabic) ElMessiriFontFamily else OutfitFontFamily
    return createSahabaTypography(fontFamily)
}

// Tagline text style helper
fun createSahabaTaglineStyle(fontFamily: FontFamily = FontFamily.Default): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 10.sp,
    letterSpacing = 3.sp,
    lineHeight = 16.sp
)

// Custom text style for taglines / badges
val SahabaTaglineStyle: TextStyle
    @Composable
    get() = createSahabaTaglineStyle(OutfitFontFamily)

// Quran verse style helper
fun createQuranArabicTextStyle(fontFamily: FontFamily = FontFamily.Default): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 48.sp
)

// Custom text style specifically designed for Quranic / Arabic verses using El Messiri font
val QuranArabicTextStyle: TextStyle
    @Composable
    get() = createQuranArabicTextStyle(ElMessiriFontFamily)
