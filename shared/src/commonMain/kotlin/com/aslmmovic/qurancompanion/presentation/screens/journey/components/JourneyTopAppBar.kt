package com.aslmmovic.qurancompanion.presentation.screens.journey.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.theme.ElMessiriFontFamily
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.action_back
import qurancompanion.shared.generated.resources.ic_arrow_back
import qurancompanion.shared.generated.resources.ic_bookmark
import qurancompanion.shared.generated.resources.ic_bookmark_border
import qurancompanion.shared.generated.resources.journey_bookmarked
import qurancompanion.shared.generated.resources.journey_default_arabic_verse

@Composable
fun JourneyTopAppBar(
    title: String,
    arabicVerse: String?,
    isBookmarked: Boolean,
    onBackPressed: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.action_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            val verseText = arabicVerse ?: stringResource(Res.string.journey_default_arabic_verse)
            Text(
                text = verseText,
                fontFamily = ElMessiriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier.size(44.dp)
        ) {
            Crossfade(
                targetState = isBookmarked,
                animationSpec = tween(durationMillis = 200),
                label = "bookmark_crossfade"
            ) { bookmarked ->
                if (bookmarked) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_bookmark),
                        contentDescription = stringResource(Res.string.journey_bookmarked),
                        tint = SahabaWarmGold,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_bookmark_border),
                        contentDescription = stringResource(Res.string.journey_bookmarked),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
