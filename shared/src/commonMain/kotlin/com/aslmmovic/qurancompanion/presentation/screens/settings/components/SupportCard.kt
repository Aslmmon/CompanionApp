package com.aslmmovic.qurancompanion.presentation.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_star
import qurancompanion.shared.generated.resources.settings_patron_active_desc
import qurancompanion.shared.generated.resources.settings_patron_badge
import qurancompanion.shared.generated.resources.settings_supporter_badge
import qurancompanion.shared.generated.resources.settings_support_card_action
import qurancompanion.shared.generated.resources.settings_support_card_subtitle
import qurancompanion.shared.generated.resources.settings_support_card_title

@Composable
fun SupportCard(
    isPatron: Boolean,
    isSupporter: Boolean,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPatron) {
        // Golden Star Patron Status Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(SahabaWarmGold, Color(0xFFF0D58C), SahabaWarmGold)
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag("settings_patron_status_card")
                .clickable(role = Role.Button, onClick = onOpenPaywall)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SahabaWarmGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_star),
                            contentDescription = null,
                            tint = SahabaWarmGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.settings_patron_badge),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_patron_active_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = SahabaTextMuted
                    )
                }
            }
        }
    } else {
        // Gilded Invitation Card (Free or Supporter tier)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(SahabaWarmGold.copy(alpha = 0.8f), MaterialTheme.colorScheme.outline)
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag("settings_support_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SahabaWarmGold.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_star),
                                contentDescription = null,
                                tint = SahabaWarmGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.settings_support_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSupporter) {
                            Text(
                                text = stringResource(Res.string.settings_supporter_badge),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SahabaWarmGold
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(Res.string.settings_support_card_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SahabaTextMuted,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onOpenPaywall,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SahabaWarmGold,
                        contentColor = Color(0xFF1E1400)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_support_card_button")
                ) {
                    Text(
                        text = stringResource(Res.string.settings_support_card_action),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
