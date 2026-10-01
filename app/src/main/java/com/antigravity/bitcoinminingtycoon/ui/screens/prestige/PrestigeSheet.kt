package com.antigravity.bitcoinminingtycoon.ui.screens.prestige

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.engine.PrestigePreview
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestigeSheet(
    preview: PrestigePreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        contentColor = AppColors.TextHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = if (preview.isPrestigeAvailable) {
                        "Satoshi Prestige Confirmation: reset economy to earn ${preview.earnablePoints} permanent Satoshi Points"
                    } else {
                        "Satoshi Prestige unavailable: mine more Bitcoin to qualify for prestige points"
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "SATOSHI PRESTIGE PORTAL",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper
            )

            Text(
                text = "Full Economy Reset",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextHigh
            )

            // Reward Highlight Card
            TycoonCard(
                borderColor = if (preview.isPrestigeAvailable) AppColors.PrimaryCopper else AppColors.BorderSubtle
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PRESTIGE AWARD",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    Text(
                        text = "+${preview.earnablePoints} SATOSHI POINTS",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (preview.isPrestigeAvailable) AppColors.PrimaryCopperHover else AppColors.TextDisabled
                    )
                    Text(
                        text = "Total after reset: ${preview.newSatoshiPointsTotal} SP (+${preview.newSatoshiPointsTotal}% Hashrate)",
                        fontSize = 12.sp,
                        color = AppColors.TextMedium
                    )
                }
            }

            // Reset vs Preserved Sections
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Reset Column
                TycoonCard(
                    borderColor = AppColors.CriticalRed
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "RESET TO BASE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.CriticalRed
                        )
                        Text(text = "• BTC & USD Reserves", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• ${preview.minersCountToLose} Hardware Rigs", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• ${preview.upgradesCountToLose} Tech Upgrades", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• Substation & Cooling", fontSize = 11.sp, color = AppColors.TextMedium)
                    }
                }

                // Preserved Column
                TycoonCard(
                    borderColor = AppColors.PositiveGreen
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "PERMANENT",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PositiveGreen
                        )
                        Text(text = "• All Satoshi Points", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• Unlocked Legacy Tree", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• Lifetime Stats", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• All Achievements", fontSize = 11.sp, color = AppColors.TextMedium)
                        Text(text = "• Daily Reward Streak", fontSize = 11.sp, color = AppColors.TextMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TycoonButton(
                text = if (preview.isPrestigeAvailable) "CONFIRM PRESTIGE RESET" else "MORE BITCOIN REQUIRED",
                onClick = onConfirm,
                enabled = preview.isPrestigeAvailable,
                style = ButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            TycoonButton(
                text = "CANCEL",
                onClick = onDismiss,
                style = ButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
