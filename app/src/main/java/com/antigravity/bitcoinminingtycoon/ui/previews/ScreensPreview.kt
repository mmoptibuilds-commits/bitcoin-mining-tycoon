package com.antigravity.bitcoinminingtycoon.ui.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.components.CoreMineButton
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.MineScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState

@Preview(name = "Mine Dashboard Early Game", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun MineScreenEarlyGamePreview() {
    BitcoinMiningTycoonTheme {
        MineScreen(
            uiState = GameUiState(
                btcFormatted = "0.00041285 BTC",
                usdFormatted = "$ 248.50",
                hashrateFormatted = "120.00 MH/s",
                btcPerSecFormatted = "+0.00000012 BTC/s",
                powerDemandKw = 1.2,
                powerCapacityKw = 10.0,
                powerFactor = 1.0,
                equilibriumTemp = 32.5,
                thermalFactor = 1.0,
                activeEventTitle = "CHEAP ELECTRICITY",
                activeEventSecondsRemaining = 45L,
                gameState = GameState(manualHashStrength = "15")
            ),
            onMineClick = {},
            onQuickSell = {}
        )
    }
}

@Preview(name = "Core Mine Button", showBackground = true)
@Composable
fun CoreMineButtonPreview() {
    BitcoinMiningTycoonTheme {
        CoreMineButton(
            onMineClick = {},
            manualHashrateText = "+10 H"
        )
    }
}

@Preview(name = "Market Card Bullish", showBackground = true, widthDp = 380)
@Composable
fun MarketCardPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.mine.MarketCard(
            gameState = GameState(
                btc = "0.01500000",
                marketPrice = "64280.50",
                marketTrend = com.antigravity.bitcoinminingtycoon.model.MarketTrend.BULL,
                marketHistory = listOf(
                    "61000", "61500", "61200", "62000", "62400", "62100", "62800", "63200", "63500", "64280.50"
                ),
                autoSellEnabled = true,
                autoSellThresholdUsd = "60000"
            ),
            onQuickSell = {},
            onToggleAutoSell = {},
            onSetAutoSellThreshold = { true }
        )
    }
}

@Preview(name = "Hardware Card Unlocked", showBackground = true, widthDp = 380)
@Composable
fun HardwareCardPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.hardware.HardwareCard(
            miner = com.antigravity.bitcoinminingtycoon.content.Miners.ALL[2], // Gaming GPU
            ownedCount = 4,
            bulkMode = com.antigravity.bitcoinminingtycoon.engine.BulkMode.X10,
            availableUsd = java.math.BigDecimal("5000.00"),
            isUnlocked = true,
            numberFormat = com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference.COMPACT_SUFFIX,
            gameState = com.antigravity.bitcoinminingtycoon.model.GameState(miners = mapOf("gaming_gpu" to 4L)),
            onBuyClick = {}
        )
    }
}

@Preview(name = "Events & Achievements Preview", showBackground = true, widthDp = 380)
@Composable
fun EventAndAchievementPreview() {
    BitcoinMiningTycoonTheme {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .background(com.antigravity.bitcoinminingtycoon.ui.theme.AppColors.Background)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            com.antigravity.bitcoinminingtycoon.ui.components.AchievementBanner(
                achievement = com.antigravity.bitcoinminingtycoon.content.Achievements.FIRST_HASH,
                onDismiss = {}
            )
            com.antigravity.bitcoinminingtycoon.ui.components.EventBanner(
                activeEvent = com.antigravity.bitcoinminingtycoon.model.ActiveEventState(
                    eventId = "bull_run",
                    expiresAtWallMillis = System.currentTimeMillis() + 38_000L
                ),
                currentWallMillis = System.currentTimeMillis()
            )
            com.antigravity.bitcoinminingtycoon.ui.components.WindfallChip(
                windfallEvent = com.antigravity.bitcoinminingtycoon.model.ActiveEventState(
                    eventId = "lucky_block",
                    expiresAtWallMillis = System.currentTimeMillis() + 18_000L,
                    isWindfall = true
                ),
                currentWallMillis = System.currentTimeMillis(),
                onClaim = {}
            )
        }
    }
}

@Preview(name = "Upgrades Screen", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun UpgradesScreenPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.upgrades.UpgradesScreen(
            uiState = GameUiState(
                gameState = GameState(
                    usd = "12500.00",
                    purchasedUpgrades = setOf("copper_heatpipe"),
                    powerGridTier = 2,
                    coolingTier = 2
                )
            ),
            onBuyUpgrade = {},
            onUpgradePowerGrid = {},
                    onUpgradeCooling = {},
                    onNavigateToSatoshiTree = {},
                    onNavigateToMine = {}
        )
    }
}

@Preview(name = "Stats & Achievements Screen", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun StatsScreenPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.stats.StatsScreen(
            uiState = GameUiState(
                gameState = GameState(
                    achievements = setOf("first_hash", "first_rig", "cold_intake")
                )
            )
        )
    }
}

@Preview(name = "Settings Screen", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun SettingsScreenPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.settings.SettingsScreen(
            settings = com.antigravity.bitcoinminingtycoon.model.SettingsState(),
            batteryFriendlyAnimations = false,
            onBackClick = {},
            onUpdateSettings = {},
            onBatteryFriendlyAnimationsChanged = {},
            onOpenAbout = {},
            onFactoryReset = {}
        )
    }
}

@Preview(name = "Satoshi Legacy Tree Screen", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun SatoshiTreeScreenPreview() {
    BitcoinMiningTycoonTheme {
        com.antigravity.bitcoinminingtycoon.ui.screens.prestige.SatoshiTreeScreen(
            gameState = GameState(
                satoshiPoints = 12L,
                purchasedPrestigeNodes = setOf("efficient_silicon")
            ),
            onBackClick = {},
            onBuyNode = {},
            onOpenPrestigeSheet = {}
        )
    }
}
