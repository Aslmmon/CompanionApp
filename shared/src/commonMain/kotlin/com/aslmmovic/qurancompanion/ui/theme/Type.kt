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

// Define base typography with Outfit for clean English styling
val QuranCompanionTypography: Typography
    @Composable
    get() {
        val outfit = OutfitFontFamily
        val defaultTypography = Typography()
        return Typography(
            displayLarge = defaultTypography.displayLarge.copy(fontFamily = outfit),
            displayMedium = defaultTypography.displayMedium.copy(fontFamily = outfit),
            displaySmall = defaultTypography.displaySmall.copy(fontFamily = outfit),
            headlineLarge = defaultTypography.headlineLarge.copy(
                fontFamily = outfit,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
            headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = outfit),
            headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = outfit),
            titleLarge = defaultTypography.titleLarge.copy(
                fontFamily = outfit,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = defaultTypography.titleMedium.copy(fontFamily = outfit),
            titleSmall = defaultTypography.titleSmall.copy(fontFamily = outfit),
            bodyLarge = defaultTypography.bodyLarge.copy(
                fontFamily = outfit,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = defaultTypography.bodyMedium.copy(
                fontFamily = outfit,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = defaultTypography.bodySmall.copy(fontFamily = outfit),
            labelLarge = defaultTypography.labelLarge.copy(fontFamily = outfit),
            labelMedium = defaultTypography.labelMedium.copy(
                fontFamily = outfit,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = defaultTypography.labelSmall.copy(fontFamily = outfit)
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
