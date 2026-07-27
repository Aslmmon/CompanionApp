package com.aslmmovic.qurancompanion.presentation.screens.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.aslmmovic.qurancompanion.ui.theme.QuranArabicTextStyle
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.bismillah

@Composable
fun BismillahHeader(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(Res.string.bismillah),
        style = QuranArabicTextStyle,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}
