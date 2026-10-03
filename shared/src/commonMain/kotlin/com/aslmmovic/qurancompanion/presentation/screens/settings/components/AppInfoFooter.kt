package com.aslmmovic.qurancompanion.presentation.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.settings_app_info_brand
import qurancompanion.shared.generated.resources.settings_app_info_version

@Composable
fun AppInfoFooter(
    modifier: Modifier = Modifier,
    versionName: String = "1.0.0",
    buildNumber: Long = 1L,
    buildType: String = "Release"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(Res.string.settings_app_info_brand),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = SahabaTextMuted
        )
        Text(
            text = stringResource(Res.string.settings_app_info_version, versionName, buildNumber, buildType),
            style = MaterialTheme.typography.bodySmall,
            color = SahabaTextMuted
        )
    }
}
