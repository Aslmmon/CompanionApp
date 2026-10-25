package com.aslmmovic.qurancompanion.presentation.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.AppTheme
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_lock
import qurancompanion.shared.generated.resources.settings_theme_title
import qurancompanion.shared.generated.resources.theme_andalus
import qurancompanion.shared.generated.resources.theme_classic
import qurancompanion.shared.generated.resources.theme_oled
import qurancompanion.shared.generated.resources.theme_parchment

@Composable
fun ThemeSelectorRow(
    selectedThemeId: String?,
    isPatron: Boolean,
    onSelectTheme: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = AppTheme.fromId(selectedThemeId)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.settings_theme_title),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = SahabaTextMuted,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = AppTheme.entries,
                    key = { it.id },
                    contentType = { "ThemePill" }
                ) { theme ->
                    val isSelected = theme == currentTheme
                    val isLocked = theme.isPremium && !isPatron

                    val titleRes = when (theme) {
                        AppTheme.CLASSIC -> Res.string.theme_classic
                        AppTheme.MUSHAF_PARCHMENT -> Res.string.theme_parchment
                        AppTheme.ANDALUS_EMERALD -> Res.string.theme_andalus
                        AppTheme.DARK_OLED -> Res.string.theme_oled
                    }

                    val (swatchBg, swatchAccent) = when (theme) {
                        AppTheme.CLASSIC -> Color(0xFF1B4332) to Color(0xFFD4AF37)
                        AppTheme.MUSHAF_PARCHMENT -> Color(0xFFFBF7EE) to Color(0xFFB8860B)
                        AppTheme.ANDALUS_EMERALD -> Color(0xFF0F2B20) to Color(0xFFD4AF37)
                        AppTheme.DARK_OLED -> Color(0xFF000000) to Color(0xFFE5C158)
                    }

                    val borderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .testTag("theme_selector_${theme.id}")
                            .clickable(
                                role = Role.RadioButton,
                                onClick = { onSelectTheme(theme) }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            // Swatch preview circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatchBg)
                                    .border(1.dp, swatchAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(swatchAccent)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(titleRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    painter = painterResource(Res.drawable.ic_lock),
                                    contentDescription = "Locked",
                                    tint = SahabaWarmGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
