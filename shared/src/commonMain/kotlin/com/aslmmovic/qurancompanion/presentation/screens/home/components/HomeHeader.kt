package com.aslmmovic.qurancompanion.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_dark_mode
import qurancompanion.shared.generated.resources.ic_light_mode
import qurancompanion.shared.generated.resources.ic_menu
import qurancompanion.shared.generated.resources.ic_settings
import qurancompanion.shared.generated.resources.menu_content_description
import qurancompanion.shared.generated.resources.settings_title
import qurancompanion.shared.generated.resources.theme_dark_mode
import qurancompanion.shared.generated.resources.theme_light_mode
import qurancompanion.shared.generated.resources.welcome_title

@Composable
fun HomeHeader(
    isDarkMode: Boolean,
    onThemeToggleClick: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_menu),
                    contentDescription = stringResource(Res.string.menu_content_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(if (isDarkMode) Res.string.theme_light_mode else Res.string.theme_dark_mode)) },
                    onClick = {
                        showMenu = false
                        onThemeToggleClick(!isDarkMode)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(if (isDarkMode) Res.drawable.ic_light_mode else Res.drawable.ic_dark_mode),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.settings_title)) },
                    onClick = {
                        showMenu = false
                        onSettingsClick()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_settings),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }

        Text(
            text = stringResource(Res.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Spacer matches the size of the menu button (40.dp) to keep title centered.
        Spacer(modifier = Modifier.size(40.dp))
    }
}
