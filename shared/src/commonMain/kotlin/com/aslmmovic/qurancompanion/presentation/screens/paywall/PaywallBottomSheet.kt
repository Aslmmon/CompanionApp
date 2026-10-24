package com.aslmmovic.qurancompanion.presentation.screens.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.ui.theme.SahabaEmeraldHighlight
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_star
import qurancompanion.shared.generated.resources.paywall_annual_subtitle
import qurancompanion.shared.generated.resources.paywall_annual_title
import qurancompanion.shared.generated.resources.paywall_best_value
import qurancompanion.shared.generated.resources.paywall_free_trial_tag
import qurancompanion.shared.generated.resources.paywall_header_ornament
import qurancompanion.shared.generated.resources.paywall_monthly_subtitle
import qurancompanion.shared.generated.resources.paywall_monthly_title
import qurancompanion.shared.generated.resources.paywall_not_now
import qurancompanion.shared.generated.resources.paywall_processing
import qurancompanion.shared.generated.resources.paywall_restore
import qurancompanion.shared.generated.resources.paywall_subtitle
import qurancompanion.shared.generated.resources.paywall_support_button
import qurancompanion.shared.generated.resources.paywall_tip_button
import qurancompanion.shared.generated.resources.paywall_tip_subtitle
import qurancompanion.shared.generated.resources.paywall_tip_title
import qurancompanion.shared.generated.resources.paywall_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    uiState: PaywallUiState,
    onSelectProduct: (String) -> Unit,
    onPurchaseClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("paywall_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Classical Islamic ornament
            Text(
                text = stringResource(Res.string.paywall_header_ornament),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SahabaWarmGold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = stringResource(Res.string.paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(Res.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SahabaTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Products List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.products.forEach { product ->
                    val isSelected = product.id == uiState.selectedProductId
                    PaywallProductOptionCard(
                        product = product,
                        isSelected = isSelected,
                        onClick = { onSelectProduct(product.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Purchase Button
            val selectedProduct = uiState.products.firstOrNull { it.id == uiState.selectedProductId }
            val buttonTextRes = if (selectedProduct?.tier == ProductTier.TIP) {
                Res.string.paywall_tip_button
            } else {
                Res.string.paywall_support_button
            }

            Button(
                onClick = onPurchaseClicked,
                enabled = !uiState.isPurchasing && !uiState.isRestoring && uiState.selectedProductId != null,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SahabaWarmGold,
                    contentColor = Color(0xFF1E1400)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("paywall_purchase_button")
            ) {
                if (uiState.isPurchasing) {
                    CircularProgressIndicator(
                        color = Color(0xFF1E1400),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(Res.string.paywall_processing), fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = stringResource(buttonTextRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Purchases & Not Now buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRestoreClicked,
                    enabled = !uiState.isPurchasing && !uiState.isRestoring,
                    modifier = Modifier.testTag("paywall_restore_button")
                ) {
                    Text(
                        text = stringResource(Res.string.paywall_restore),
                        style = MaterialTheme.typography.bodySmall,
                        color = SahabaTextMuted
                    )
                }

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("paywall_dismiss_button")
                ) {
                    Text(
                        text = stringResource(Res.string.paywall_not_now),
                        style = MaterialTheme.typography.bodySmall,
                        color = SahabaTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallProductOptionCard(
    product: SubscriptionProduct,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        SahabaWarmGold
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = if (isSelected) {
        SahabaWarmGold.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val title = when (product.tier) {
        ProductTier.ANNUAL -> stringResource(Res.string.paywall_annual_title)
        ProductTier.MONTHLY -> stringResource(Res.string.paywall_monthly_title)
        ProductTier.TIP -> stringResource(Res.string.paywall_tip_title)
    }

    val subtitle = when (product.tier) {
        ProductTier.ANNUAL -> stringResource(Res.string.paywall_annual_subtitle, product.effectiveMonthlyPrice ?: "")
        ProductTier.MONTHLY -> stringResource(Res.string.paywall_monthly_subtitle)
        ProductTier.TIP -> stringResource(Res.string.paywall_tip_subtitle)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("paywall_product_${product.id}")
            .clickable(role = Role.RadioButton, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Golden dot or check for selected
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) SahabaWarmGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Best Value or Free Trial Tag
                if (product.tier == ProductTier.ANNUAL) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SahabaWarmGold
                    ) {
                        Text(
                            text = stringResource(Res.string.paywall_best_value),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1400),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (product.hasFreeTrial) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SahabaEmeraldHighlight.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(Res.string.paywall_free_trial_tag),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SahabaEmeraldHighlight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SahabaTextMuted,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = product.formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
