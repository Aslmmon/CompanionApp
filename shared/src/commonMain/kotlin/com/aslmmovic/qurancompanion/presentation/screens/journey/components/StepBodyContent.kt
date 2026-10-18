package com.aslmmovic.qurancompanion.presentation.screens.journey.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.step_type_action
import qurancompanion.shared.generated.resources.step_type_intro
import qurancompanion.shared.generated.resources.step_type_key_lessons
import qurancompanion.shared.generated.resources.step_type_references
import qurancompanion.shared.generated.resources.step_type_reflection
import qurancompanion.shared.generated.resources.step_type_story

@Composable
fun StepBodyContent(
    step: JourneyStep,
    companionName: String?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = SahabaWarmGold

    val highlightedContent = remember(step.content, companionName, primaryColor, goldColor) {
        buildHighlightedContent(
            text = step.content,
            companionName = companionName,
            primaryColor = primaryColor,
            accentColor = goldColor
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        // Step Category Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = getStepLabel(step.type).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step Title
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Clean Step Text with Smart Highlighting
        Text(
            text = highlightedContent,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
            lineHeight = 28.sp,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun getStepLabel(type: StepType): String {
    return when (type) {
        StepType.INTRO       -> stringResource(Res.string.step_type_intro)
        StepType.STORY       -> stringResource(Res.string.step_type_story)
        StepType.KEY_LESSONS -> stringResource(Res.string.step_type_key_lessons)
        StepType.REFLECTION  -> stringResource(Res.string.step_type_reflection)
        StepType.ACTION      -> stringResource(Res.string.step_type_action)
        StepType.REFERENCES  -> stringResource(Res.string.step_type_references)
    }
}

private data class HighlightTarget(
    val range: IntRange,
    val style: SpanStyle
)

private fun buildHighlightedContent(
    text: String,
    companionName: String?,
    primaryColor: Color,
    accentColor: Color
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    val targets = mutableListOf<HighlightTarget>()

    // 1. Prophet's honorific ﷺ
    val prophetKeywords = listOf("ﷺ", "صلى الله عليه وسلم", "(pbuh)", "(PBUH)")
    for (kw in prophetKeywords) {
        var start = text.indexOf(kw, ignoreCase = true)
        while (start >= 0) {
            targets.add(
                HighlightTarget(
                    range = start until (start + kw.length),
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = accentColor)
                )
            )
            start = text.indexOf(kw, start + kw.length, ignoreCase = true)
        }
    }

    // 2. Companion honorifics (RA)
    val raKeywords = listOf("(RA)", "(ra)", "رضي الله عنه", "رضي الله عنها")
    for (kw in raKeywords) {
        var start = text.indexOf(kw, ignoreCase = true)
        while (start >= 0) {
            targets.add(
                HighlightTarget(
                    range = start until (start + kw.length),
                    style = SpanStyle(fontWeight = FontWeight.SemiBold, color = primaryColor)
                )
            )
            start = text.indexOf(kw, start + kw.length, ignoreCase = true)
        }
    }

    // 3. Companion name if specified
    if (!companionName.isNullOrBlank() && companionName.length >= 3) {
        var start = text.indexOf(companionName, ignoreCase = true)
        while (start >= 0) {
            targets.add(
                HighlightTarget(
                    range = start until (start + companionName.length),
                    style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)
                )
            )
            start = text.indexOf(companionName, start + companionName.length, ignoreCase = true)
        }
    }

    // Resolve non-overlapping targets sorted by start
    val sortedTargets = targets
        .sortedBy { it.range.first }
        .fold(mutableListOf<HighlightTarget>()) { acc, target ->
            val last = acc.lastOrNull()
            if (last == null || target.range.first >= last.range.last + 1) {
                acc.add(target)
            }
            acc
        }

    return buildAnnotatedString {
        var currentIndex = 0
        for (target in sortedTargets) {
            if (target.range.first > currentIndex) {
                append(text.substring(currentIndex, target.range.first))
            }
            pushStyle(target.style)
            append(text.substring(target.range.first, target.range.last + 1))
            pop()
            currentIndex = target.range.last + 1
        }
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
