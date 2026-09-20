package com.aslmmovic.qurancompanion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SahabaModernDesignShowcase(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ShowcaseHeader()
            ColorSwatchesSection()
            TypographySection()
            CategoryChipsSection()
            DailyJourneyHeroCardMockup()
            ActiveHabitItemMockup()
        }
    }
}

@Composable
private fun ShowcaseHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Sahaba Modern Design System",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Canonical color tokens, shapes & component mockups",
            style = MaterialTheme.typography.bodyMedium,
            color = SahabaTextMuted
        )
    }
}

@Composable
private fun ColorSwatchesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Color Palette",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorSwatchCard(
                name = "Evergreen",
                hex = "#1B4332",
                color = SahabaEvergreen,
                textColor = SahabaOnEvergreen,
                modifier = Modifier.weight(1f)
            )
            ColorSwatchCard(
                name = "Warm Gold",
                hex = "#D4AF37",
                color = SahabaWarmGold,
                textColor = SahabaOnWarmGold,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorSwatchCard(
                name = "Ivory",
                hex = "#F9F8F3",
                color = SahabaIvoryBackground,
                textColor = SahabaDarkText,
                borderColor = SahabaCardBorder,
                modifier = Modifier.weight(1f)
            )
            ColorSwatchCard(
                name = "Card Border",
                hex = "#EBE8DF",
                color = SahabaCardBorder,
                textColor = SahabaDarkText,
                borderColor = SahabaCardBorder,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ColorSwatchCard(
    name: String,
    hex: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null
) {
    Card(
        modifier = modifier
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, SahabaShapes.medium)
                } else {
                    Modifier
                }
            ),
        shape = SahabaShapes.medium,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hex,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun CategoryChipsSection() {
    val categories = remember { listOf("All", "Ten Promised", "Mothers of Believers") }
    var selectedCategory by remember { mutableStateOf("All") }
    val chipScrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Category Filter Chips",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SahabaPillBackground,
                        labelColor = SahabaDarkText,
                        selectedContainerColor = SahabaEvergreen,
                        selectedLabelColor = SahabaOnEvergreen
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SahabaCardBorder,
                        selectedBorderColor = SahabaEvergreen,
                        borderWidth = 1.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun DailyJourneyHeroCardMockup() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Daily Journey Hero Card",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape = SahabaShapes.large,
            colors = CardDefaults.cardColors(containerColor = SahabaIvoryBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SahabaCardBorder, SahabaShapes.large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Reading duration tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(SahabaMintContainer, SahabaShapes.small)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BIOGRAPHY • 6 min read",
                            style = MaterialTheme.typography.labelSmall,
                            color = SahabaOnMintContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(SahabaWarmGold.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .border(1.dp, SahabaWarmGold.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+2k reading today",
                            style = MaterialTheme.typography.labelSmall,
                            color = SahabaWarmGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Abu Bakr As-Siddiq",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SahabaDarkText
                    )
                    Text(
                        text = "\"The one who believed when others doubted.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SahabaTextMuted
                    )
                }

                Button(
                    onClick = {},
                    shape = SahabaShapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SahabaEvergreen,
                        contentColor = SahabaOnEvergreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Begin Journey →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveHabitItemMockup() {
    var isChecked by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Active Habit Item",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape = SahabaShapes.medium,
            colors = CardDefaults.cardColors(containerColor = SahabaCardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SahabaCardBorder, SahabaShapes.medium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SahabaEvergreen,
                        checkmarkColor = SahabaOnEvergreen,
                        uncheckedColor = SahabaTextMuted
                    )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Duha Prayer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SahabaDarkText
                    )
                    Text(
                        text = "The prayer of the repentant",
                        style = MaterialTheme.typography.bodySmall,
                        color = SahabaTextMuted
                    )
                }

                // Flame streak badge
                Box(
                    modifier = Modifier
                        .background(SahabaWarmGold.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .border(1.dp, SahabaWarmGold.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 12d",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SahabaWarmGold
                    )
                }
            }
        }
    }
}

@Composable
private fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Typography Scale & Styles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape = SahabaShapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SahabaCardBorder, SahabaShapes.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Screen Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Screen Title (headlineLarge)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "Library & Discover",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Section Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Section Title (titleLarge)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "Curated Collections",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Card Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Card Title (titleMedium)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "Morning Adhkar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Story Body
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Story Body (bodyLarge)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "Narrative story reading with relaxed line-height for comfort...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Badge
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Badge (labelMedium)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Box(
                        modifier = Modifier
                            .background(SahabaWarmGold.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .border(1.dp, SahabaWarmGold.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "12 JOURNEYS • STEP 2 OF 5",
                            style = MaterialTheme.typography.labelMedium,
                            color = SahabaWarmGold
                        )
                    }
                }

                // Tagline
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Tagline (SahabaTaglineStyle)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "JOURNEY INTO EXCELLENCE",
                        style = SahabaTaglineStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quran Arabic Verse
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Quran Arabic Verse (QuranArabicTextStyle)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahabaTextMuted
                    )
                    Text(
                        text = "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
                        style = QuranArabicTextStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SahabaModernDesignShowcaseLightPreview() {
    SahabaCompanionTheme(darkTheme = false) {
        SahabaModernDesignShowcase()
    }
}

@Preview
@Composable
fun SahabaModernDesignShowcaseDarkPreview() {
    SahabaCompanionTheme(darkTheme = true) {
        SahabaModernDesignShowcase()
    }
}
