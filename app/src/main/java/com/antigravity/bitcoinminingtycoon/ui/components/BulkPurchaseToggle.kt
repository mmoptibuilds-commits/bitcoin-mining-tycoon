package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

/**
 * Segmented control for switching between bulk purchase multipliers: x1, x10, x25, and MAX.
 * Strict accessibility: Minimum 48dp touch bounds, high-contrast borders, clear semantic states.
 */
@Composable
fun BulkPurchaseToggle(
    selectedMode: BulkMode,
    onModeSelected: (BulkMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.SurfaceLow)
            .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        BulkMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val backgroundColor = if (isSelected) AppColors.PrimaryCopperDark else AppColors.SurfaceLow
            val borderColor = if (isSelected) AppColors.PrimaryCopper else AppColors.SurfaceLow
            val textColor = if (isSelected) AppColors.PrimaryCopperHover else AppColors.TextMedium
            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .clickable(
                        role = Role.RadioButton,
                        onClickLabel = "Select bulk purchase mode ${mode.label}"
                    ) {
                        onModeSelected(mode)
                    }
                    .semantics {
                        contentDescription = "Bulk mode ${mode.label}, ${if (isSelected) "selected" else "not selected"}"
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = fontWeight,
                    color = textColor
                )
            }
        }
    }
}
