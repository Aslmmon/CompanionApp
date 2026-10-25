package com.aslmmovic.qurancompanion.presentation.screens.journey.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.aslmmovic.qurancompanion.ui.theme.SahabaEmeraldHighlight
import com.aslmmovic.qurancompanion.ui.theme.SahabaEvergreen
import com.aslmmovic.qurancompanion.ui.theme.SahabaMintContainer
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val initialAngleRad: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean,
    val flutterFrequency: Float,
    val flutterAmplitude: Float
)

@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 48,
    originYFraction: Float = 0.16f
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
    }

    val palette = remember {
        listOf(
            SahabaWarmGold,
            SahabaEvergreen,
            SahabaEmeraldHighlight,
            SahabaMintContainer
        )
    }

    val particles = remember {
        val random = Random(42)
        List(particleCount) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 350f + random.nextFloat() * 450f
            val color = palette[random.nextInt(palette.size)]
            val size = 12f + random.nextFloat() * 14f
            val rotationSpeed = (random.nextFloat() - 0.5f) * 720f
            val isCircle = random.nextBoolean()
            val flutterFrequency = 4f + random.nextFloat() * 6f
            val flutterAmplitude = 15f + random.nextFloat() * 25f
            ConfettiParticle(
                initialAngleRad = angle,
                speed = speed,
                color = color,
                size = size,
                rotationSpeed = rotationSpeed,
                isCircle = isCircle,
                flutterFrequency = flutterFrequency,
                flutterAmplitude = flutterAmplitude
            )
        }
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    val alpha = ((1f - currentProgress) / 0.5f).coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * originYFraction
        val timeSeconds = currentProgress * 2.5f
        val gravity = 500f // px/s^2 downward acceleration

        particles.forEach { p ->
            val vx = cos(p.initialAngleRad) * p.speed
            val vy = sin(p.initialAngleRad) * p.speed - 150f
            val x = centerX + vx * timeSeconds + sin(timeSeconds * p.flutterFrequency) * p.flutterAmplitude
            val y = centerY + vy * timeSeconds + 0.5f * gravity * timeSeconds * timeSeconds

            val particleColor = p.color.copy(alpha = (p.color.alpha * alpha).coerceIn(0f, 1f))

            rotate(
                degrees = timeSeconds * p.rotationSpeed,
                pivot = Offset(x, y)
            ) {
                if (p.isCircle) {
                    drawCircle(
                        color = particleColor,
                        radius = p.size / 2f,
                        center = Offset(x, y)
                    )
                } else {
                    drawRect(
                        color = particleColor,
                        topLeft = Offset(x - p.size / 2f, y - (p.size * 0.6f) / 2f),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
