package com.antigravity.bitcoinminingtycoon.ui.screens.hardware

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import com.antigravity.bitcoinminingtycoon.ui.components.BulkPurchaseToggle
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.MetricTile
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState

/**
 * Hardware Fleet catalogue screen displaying all 20 miner tiers, bulk toggle, and live fleet stats.
 */
@Composable
fun HardwareScreen(
    uiState: GameUiState,
    bulkMode: BulkMode,
    onBulkModeSelected: (BulkMode) -> Unit,
    onBuyMiner: (String) -> Unit,
    onOpenUpgrades: () -> Unit,
    onOpenMine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infrastructureLimited = uiState.powerFactor < 0.999999 || uiState.thermalFactor < 0.999999
    val firstUnaffordablePurchase = Miners.ALL.asSequence()
        .filter { FleetEngine.isUnlocked(it, uiState.gameState) }
        .map { miner ->
            val cost = FleetEngine.calculatePurchase(
                miner,
                uiState.gameState.miners[miner.id] ?: 0L,
                bulkMode,
                uiState.gameState.usdBigDecimal
            ).second
            miner to cost
        }
        .firstOrNull { (_, cost) -> uiState.gameState.usdBigDecimal < cost }
    val sellForHardware = uiState.gameState.btcBigDecimal.signum() > 0 &&
        firstUnaffordablePurchase != null
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Sticky Fleet Telemetry & Cash HUD
        TycoonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricTile(
                    label = "Cash Reserve",
                    value = uiState.usdFormatted,
                    valueColor = AppColors.TextHigh,
                    subValue = "Mined: ${uiState.btcFormatted}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MetricTile(
                    label = "Fleet Hashrate",
                    value = uiState.hashrateFormatted,
                    valueColor = AppColors.PrimaryCopper,
                    subValue = "Load: ${String.format("%.2f kW", uiState.powerDemandKw)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (sellForHardware) {
            val (miner, requiredCash) = firstUnaffordablePurchase
            val deficit = requiredCash.subtract(uiState.gameState.usdBigDecimal, GameNumber.MATH_CONTEXT)
            TycoonCard(borderColor = AppColors.BorderFocus) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TURN BITCOIN INTO HARDWARE CASH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    Text(
                        text = "${miner.name} in ${bulkMode.label} mode needs ${NumberFormatter.formatUsd(requiredCash, uiState.gameState.settings.numberFormat)}. You are short by ${NumberFormatter.formatUsd(deficit, uiState.gameState.settings.numberFormat)}; sell mined Bitcoin on Mine or choose a smaller bulk amount.",
                        fontSize = 12.sp,
                        color = AppColors.TextMedium
                    )
                    TycoonButton(
                        text = "Open Mine to sell Bitcoin",
                        onClick = onOpenMine,
                        style = ButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        contentDescriptionText = "Open Mine to sell Bitcoin for hardware cash"
                    )
                }
            }
        }

        if (infrastructureLimited) {
            TycoonCard(borderColor = AppColors.WarningAmber) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INFRASTRUCTURE LIMITING OUTPUT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarningAmber
                    )
                    Text(
                        text = "Power ${String.format(java.util.Locale.US, "%.2f", uiState.powerDemandKw)} / ${String.format(java.util.Locale.US, "%.2f", uiState.powerCapacityKw)} kW · cooling ${String.format(java.util.Locale.US, "%.0f%%", uiState.thermalFactor * 100.0)}. Increase grid capacity or cooling to restore full mining speed.",
                        fontSize = 12.sp,
                        color = AppColors.TextMedium
                    )
                    TycoonButton(
                        text = "Open infrastructure upgrades",
                        onClick = onOpenUpgrades,
                        style = ButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        contentDescriptionText = "Open upgrades to improve power capacity or cooling"
                    )
                }
            }
        }

        // 2. Bulk Purchase Mode Selector
        BulkPurchaseToggle(
            selectedMode = bulkMode,
            onModeSelected = onBulkModeSelected
        )

        // 3. Catalogue List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = Miners.ALL,
                key = { it.id }
            ) { miner ->
                val owned = uiState.gameState.miners[miner.id] ?: 0L
                val isUnlocked = FleetEngine.isUnlocked(miner, uiState.gameState)

                HardwareCard(
                    miner = miner,
                    ownedCount = owned,
                    bulkMode = bulkMode,
                    availableUsd = uiState.gameState.usdBigDecimal,
                    isUnlocked = isUnlocked,
                    numberFormat = uiState.gameState.settings.numberFormat,
                    gameState = uiState.gameState,
                    onBuyClick = { onBuyMiner(miner.id) }
                )
            }
        }
    }
}
