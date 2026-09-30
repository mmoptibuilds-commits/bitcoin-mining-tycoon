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
import com.antigravity.bitcoinminingtycoon.ui.components.BulkPurchaseToggle
import com.antigravity.bitcoinminingtycoon.ui.components.MetricTile
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
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
    modifier: Modifier = Modifier
) {
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
                    onBuyClick = { onBuyMiner(miner.id) }
                )
            }
        }
    }
}
