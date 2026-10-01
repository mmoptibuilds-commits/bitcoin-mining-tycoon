package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.screens.hardware.HardwareScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HardwareFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun everyExistingHardwareTierRemainsReachableInTheCatalogue() {
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                HardwareScreen(
                    uiState = GameUiState(),
                    bulkMode = BulkMode.X1,
                    onBulkModeSelected = {},
                    onBuyMiner = {},
                    onOpenUpgrades = {},
                    onOpenMine = {}
                )
            }
        }

        Miners.ALL.forEach { miner ->
            composeTestRule.onNodeWithText(miner.name.uppercase()).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun powerOrCoolingLimitExplainsRemedyAndOpensInfrastructureUpgrades() {
        var openedUpgradeRoute = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                HardwareScreen(
                    uiState = GameUiState(
                        powerDemandKw = 12.0,
                        powerCapacityKw = 10.0,
                        powerFactor = 0.83,
                        thermalFactor = 0.75,
                        gameState = GameState()
                    ),
                    bulkMode = BulkMode.X1,
                    onBulkModeSelected = {},
                    onBuyMiner = {},
                    onOpenUpgrades = { openedUpgradeRoute++ },
                    onOpenMine = {}
                )
            }
        }

        composeTestRule.onNodeWithText("INFRASTRUCTURE LIMITING OUTPUT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open infrastructure upgrades").performScrollTo().performClick()
        assertEquals(1, openedUpgradeRoute)
    }

    @Test
    fun hardwareCashShortfallOffersMineSaleRoute() {
        var openedMineRoute = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                HardwareScreen(
                    uiState = GameUiState(gameState = GameState(btc = "0.001")),
                    bulkMode = BulkMode.X1,
                    onBulkModeSelected = {},
                    onBuyMiner = {},
                    onOpenUpgrades = {},
                    onOpenMine = { openedMineRoute++ }
                )
            }
        }

        composeTestRule.onNodeWithText("TURN BITCOIN INTO HARDWARE CASH").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Mine to sell Bitcoin").performClick()
        assertEquals(1, openedMineRoute)
    }
}
