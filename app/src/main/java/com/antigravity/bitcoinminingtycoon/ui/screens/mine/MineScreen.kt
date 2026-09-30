package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.CoreMineButton
import com.antigravity.bitcoinminingtycoon.ui.components.EventBanner
import com.antigravity.bitcoinminingtycoon.ui.components.MetricTile
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.components.WindfallChip
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState

@Composable
fun MineScreen(
    uiState: GameUiState,
    onMineClick: () -> Unit,
    onQuickSell: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onToggleAutoSell: () -> Unit = {},
    onSetAutoSellThreshold: (java.math.BigDecimal) -> Unit = {},
    onClaimWindfall: (String) -> Unit = {},
    onDismissAchievement: () -> Unit = {},
    onCollectOfflineReward: () -> Unit = {},
    onShowDailyRewardSheet: () -> Unit = {},
    onDismissDailyRewardSheet: () -> Unit = {},
    onClaimDailyReward: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dual Balance HUD Card
        TycoonCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricTile(
                    label = "BTC Balance",
                    value = uiState.btcFormatted,
                    valueColor = AppColors.PrimaryCopper,
                    subValue = uiState.btcPerSecFormatted,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MetricTile(
                    label = "Cash Reserve",
                    value = uiState.usdFormatted,
                    valueColor = AppColors.TextHigh,
                    subValue = "Fleet: ${uiState.hashrateFormatted}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 1b. Daily Allocation Entry Button
        TycoonButton(
            text = if (uiState.canClaimDailyReward) "★ DAILY ALLOCATION [CLAIM READY]" else "DAILY ALLOCATION [DAY ${uiState.gameState.dailyRewardDay}]",
            onClick = onShowDailyRewardSheet,
            style = if (uiState.canClaimDailyReward) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Dynamic Operations Directive (Guides player on next high-value action)
        val directiveText = when {
            uiState.gameState.stats.totalMinersPurchased == 0L && uiState.gameState.usdBigDecimal < java.math.BigDecimal("10.00") && uiState.gameState.btcBigDecimal <= java.math.BigDecimal.ZERO ->
                "DIRECTIVE 01: Tap the central MINE reticle to calculate hashes & earn simulated BTC."
            uiState.gameState.stats.totalMinersPurchased == 0L && uiState.gameState.btcBigDecimal > java.math.BigDecimal.ZERO && uiState.gameState.usdBigDecimal < java.math.BigDecimal("10.00") ->
                "DIRECTIVE 02: Sell your mined BTC on the SIMULATED BTC SPOT card below for Cash."
            uiState.gameState.stats.totalMinersPurchased == 0L && uiState.gameState.usdBigDecimal >= java.math.BigDecimal("10.00") ->
                "DIRECTIVE 03: Cash ready! Go to HARDWARE tab to deploy your first automated Ancient CPU."
            uiState.powerFactor < 0.99 ->
                "WARNING: Grid deficit detected! Upgrade power capacity in UPGRADES tab."
            uiState.thermalFactor < 0.99 ->
                "WARNING: High thermal load! Upgrade datacenter cooling in UPGRADES tab."
            else ->
                "SYSTEM STATUS: Fleet mining at ${uiState.hashrateFormatted}. Scale compute & tech in HARDWARE/UPGRADES."
        }

        TycoonCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (uiState.powerFactor < 0.99 || uiState.thermalFactor < 0.99) AppColors.WarningAmber else AppColors.BorderSubtle
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "▶",
                    fontSize = 12.sp,
                    color = AppColors.PrimaryCopper,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = directiveText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextHigh
                )
            }
        }

        // 3. Central Interactive Mining Core (Stable position; never shifts)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CoreMineButton(
                onMineClick = onMineClick,
                manualHashrateText = "+${uiState.gameState.manualHashStrength} H/tap",
                reducedMotion = uiState.gameState.settings.reducedMotion
            )
        }

        // 4. Active Ambient Event & Interactive Windfall Section (Positioned below MINE button)
        if (uiState.activeAmbientEvent != null || uiState.activeWindfallEvent != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.activeAmbientEvent != null) {
                    EventBanner(
                        activeEvent = uiState.activeAmbientEvent,
                        currentWallMillis = uiState.currentWallMillis
                    )
                }
                if (uiState.activeWindfallEvent != null) {
                    WindfallChip(
                        windfallEvent = uiState.activeWindfallEvent,
                        currentWallMillis = uiState.currentWallMillis,
                        onClaim = onClaimWindfall
                    )
                }
            }
        }

        // 5. Market Spot & Quick Sell Controls
        MarketCard(
            gameState = uiState.gameState,
            onQuickSell = onQuickSell,
            onToggleAutoSell = onToggleAutoSell,
            onSetAutoSellThreshold = onSetAutoSellThreshold
        )

        // 6. Environmental Subsystem Telemetry Panel
        EnvironmentalPanel(
            powerDemandKw = uiState.powerDemandKw,
            powerCapacityKw = uiState.powerCapacityKw,
            powerFactor = uiState.powerFactor,
            equilibriumTemp = uiState.equilibriumTemp,
            thermalFactor = uiState.thermalFactor
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal Sheets
    if (uiState.offlineReport != null) {
        OfflineReturnSheet(
            report = uiState.offlineReport,
            onCollect = onCollectOfflineReward
        )
    }

    if (uiState.dailyRewardSheetVisible) {
        DailyRewardSheet(
            currentDay = uiState.gameState.dailyRewardDay,
            canClaim = uiState.canClaimDailyReward,
            millisUntilNextClaim = uiState.dailyRewardCooldownMillis,
            onClaim = onClaimDailyReward,
            onDismiss = onDismissDailyRewardSheet
        )
    }
}

