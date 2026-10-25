package com.aslmmovic.qurancompanion.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.ui.theme.SahabaEvergreen
import com.aslmmovic.qurancompanion.ui.theme.SahabaMintContainer
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_begin_journey_btn
import qurancompanion.shared.generated.resources.home_biography_tag
import qurancompanion.shared.generated.resources.home_completed_btn
import qurancompanion.shared.generated.resources.home_faith_of_truth
import qurancompanion.shared.generated.resources.home_honorific_ra
import qurancompanion.shared.generated.resources.ic_bookmark
import qurancompanion.shared.generated.resources.ic_bookmark_border

@Composable
fun CuratedJourneyHeroCard(
    journey: Journey?,
    isCompleted: Boolean,
    isBookmarked: Boolean,
    onBeginClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 0.75.dp,
                color = SahabaWarmGold.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header tag and bookmark action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.home_biography_tag),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isBookmarked) Res.drawable.ic_bookmark else Res.drawable.ic_bookmark_border
                        ),
                        contentDescription = null,
                        tint = SahabaWarmGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Companion identity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calligraphy avatar with rank badge
                Box(
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SahabaEvergreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = extractAvatarMonogram(journey?.person),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = SahabaWarmGold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(SahabaWarmGold)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${journey?.dayNumber ?: 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = SahabaEvergreen
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Identity column matching Figma node 3107:767
                Column(modifier = Modifier.weight(1f)) {
                    // Companion Name
                    Text(
                        text = journey?.person ?: journey?.title ?: "Abu Bakr As-Siddiq",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Honorific
                    Text(
                        text = stringResource(Res.string.home_honorific_ra),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = SahabaWarmGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subtitle / Epithet
                    Text(
                        text = journey?.subtitle ?: "The First Caliph • Companion of the Cave",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SahabaTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reflection quote box with decorative accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = SahabaWarmGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "“",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SahabaWarmGold,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = journey?.heroQuote?.ifBlank { null }
                                ?: journey?.intention
                                ?: "If your faith is true, no darkness can extinguish your light.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(Res.string.home_faith_of_truth),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = SahabaWarmGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Call to Action Button
            Button(
                onClick = onBeginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) SahabaMintContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isCompleted) SahabaEvergreen else Color.White
                )
            ) {
                Text(
                    text = if (isCompleted) {
                        stringResource(Res.string.home_completed_btn)
                    } else {
                        "${stringResource(Res.string.home_begin_journey_btn)}  →"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

private fun extractAvatarMonogram(person: String?): String {
    if (person.isNullOrBlank()) return "أ"
    val trimmed = person.trim()
    if (trimmed.startsWith("أبو") || trimmed.startsWith("ابو") || trimmed.startsWith("Abu")) {
        return "أبو"
    }
    val clean = trimmed
        .removePrefix("ال")
        .removePrefix("Al-")
        .removePrefix("At-")
        .removePrefix("An-")
        .removePrefix("Ad-")
        .removePrefix("As-")
        .removePrefix("Ash-")
    return clean.firstOrNull()?.toString() ?: "أ"
}
