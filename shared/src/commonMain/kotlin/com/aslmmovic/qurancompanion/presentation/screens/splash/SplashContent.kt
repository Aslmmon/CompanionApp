package com.aslmmovic.qurancompanion.presentation.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.theme.ElMessiriFontFamily
import com.aslmmovic.qurancompanion.ui.theme.OutfitFontFamily
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_splash_dome
import qurancompanion.shared.generated.resources.ic_splash_watermark
import qurancompanion.shared.generated.resources.splash_companions
import qurancompanion.shared.generated.resources.splash_title
import qurancompanion.shared.generated.resources.splash_slogan
import qurancompanion.shared.generated.resources.splash_tagline_arabic

@Composable
fun SplashContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(vertical = 40.dp, horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashEmblemBadge()
            SplashBrandWordmark()

        }
    }
}

@Composable
fun SplashWatermarkBackground(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Res.drawable.ic_splash_watermark),
        contentDescription = null,
        modifier = modifier
            .size(width = 390.dp, height = 580.dp)
            .alpha(0.06f),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun SplashEmblemBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient golden glow
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x26D4AF37),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Squircle emblem badge
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(
                    width = 1.dp,
                    color = Color(0x66D4AF37),
                    shape = RoundedCornerShape(26.dp)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1F5E43),
                            Color(0xFF164E3A),
                            Color(0xFF0F3826)
                        )
                    )
                )
                .padding(1.dp)
                .border(
                    width = 0.5.dp,
                    color = Color(0x33D4AF37),
                    shape = RoundedCornerShape(25.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_splash_dome),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun SplashBrandWordmark(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sahaba",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            letterSpacing = (-0.75).sp,
            color = Color(0xFF1F5E43),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.splash_companions),
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 3.sp,
            color = Color(0xFFC59B27),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

