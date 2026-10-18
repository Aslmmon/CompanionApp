package com.aslmmovic.qurancompanion.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rub-el-Hizb (۞) 8-Pointed Star Badge Component
 * Renders two overlapping squares rotated at 45 degrees with centered text/number.
 */
@Composable
fun RubElHizbBadge(
    number: String,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 38.dp,
    starColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(badgeSize)
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val sideLength = size.minDimension * 0.72f
                val rectSize = Size(sideLength, sideLength)
                val topLeft = Offset(center.x - sideLength / 2f, center.y - sideLength / 2f)
                // Cached per size change — not allocated every frame
                val borderStroke = Stroke(width = 1.5.dp.toPx())
                onDrawBehind {
                    // Fill 1st Square
                    drawRect(
                        color = starColor,
                        topLeft = topLeft,
                        size = rectSize
                    )

                    // Fill 2nd Square rotated by 45 degrees
                    rotate(degrees = 45f, pivot = center) {
                        drawRect(
                            color = starColor,
                            topLeft = topLeft,
                            size = rectSize
                        )
                    }

                    // Draw subtle gold outer accent border
                    drawRect(
                        color = starColor.copy(alpha = 0.8f),
                        topLeft = topLeft,
                        size = rectSize,
                        style = borderStroke
                    )
                }
            }
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

/**
 * Canvas-drawn subtle Islamic geometric lattice overlay background.
 * Rendered at low opacity (3% - 5%) to create an authentic Mushaf papyrus depth effect.
 */
@Composable
fun IslamicBackgroundLattice(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
) {
    Box(
        modifier = modifier.fillMaxSize().drawWithCache {
            val patternSize = 60.dp.toPx()
            // Cached per size change — not allocated every loop iteration
            val stroke = Stroke(width = 1.dp.toPx())
            val numCols = (size.width / patternSize).toInt() + 2
            val numRows = (size.height / patternSize).toInt() + 2
            onDrawBehind {
                for (row in -1..numRows) {
                    for (col in -1..numCols) {
                        val cx = col * patternSize
                        val cy = row * patternSize
                        val radius = patternSize * 0.35f
                        // Draw 8-pointed lattice guide lines
                        drawIslamicStarOutline(Offset(cx, cy), radius, color, stroke)
                    }
                }
            }
        }
    )
}

private fun DrawScope.drawIslamicStarOutline(
    center: Offset,
    radius: Float,
    color: Color,
    stroke: Stroke // accepts pre-cached Stroke instead of allocating per call
) {
    val side = radius * 1.2f
    val halfSide = side / 2f
    val topLeft = Offset(center.x - halfSide, center.y - halfSide)
    val rectSize = Size(side, side)

    drawRect(
        color = color,
        topLeft = topLeft,
        size = rectSize,
        style = stroke
    )

    rotate(degrees = 45f, pivot = center) {
        drawRect(
            color = color,
            topLeft = topLeft,
            size = rectSize,
            style = stroke
        )
    }
}
