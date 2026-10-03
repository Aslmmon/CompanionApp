package com.aslmmovic.qurancompanion.presentation.screens.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.category_all
import qurancompanion.shared.generated.resources.category_ansar
import qurancompanion.shared.generated.resources.category_mothers_of_believers
import qurancompanion.shared.generated.resources.category_muhajirun
import qurancompanion.shared.generated.resources.category_ten_promised

@Composable
fun CategoryFilterChips(
    selectedCategory: SahabaCategory,
    onCategorySelected: (SahabaCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = SahabaCategory.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                label = "chipContainer_${category.name}"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else SahabaTextMuted,
                label = "chipContent_${category.name}"
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = containerColor,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        role = Role.RadioButton,
                        onClick = { onCategorySelected(category) }
                    )
            ) {
                Text(
                    text = stringResource(category.toTitleResource()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

fun SahabaCategory.toTitleResource(): StringResource = when (this) {
    SahabaCategory.ALL -> Res.string.category_all
    SahabaCategory.TEN_PROMISED -> Res.string.category_ten_promised
    SahabaCategory.MOTHERS_OF_BELIEVERS -> Res.string.category_mothers_of_believers
    SahabaCategory.ANSAR -> Res.string.category_ansar
    SahabaCategory.MUHAJIRUN -> Res.string.category_muhajirun
}
