package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.screens.upgrades.UpgradesScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpgradeGroupsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun investmentTracksFilterAndKeepPowerAndCoolingSubgroupsDistinct() {
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                UpgradesScreen(
                    uiState = GameUiState(gameState = GameState(usd = "1000000000000")),
                    onBuyUpgrade = {},
                    onUpgradePowerGrid = {},
                    onUpgradeCooling = {},
                    onNavigateToSatoshiTree = {},
                    onNavigateToMine = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Show Tapping upgrade track").performClick()
        composeTestRule.onNodeWithText("COPPER FINGERS").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("CPU BIOS VOLTAGE MOD").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Show Infrastructure upgrade track").performClick()
        composeTestRule.onNodeWithText("PRECISION UNDERVOLTING").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("CARBON NANOTUBE THERMAL PASTE").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Show Automation upgrade track").performClick()
        composeTestRule.onNodeWithText("SIMULATED MARKET TICKER").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("COPPER FINGERS").assertDoesNotExist()
    }

    @Test
    fun upgradeFundingShortfallOffersBitcoinSaleRoute() {
        var mineRouteCount = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                UpgradesScreen(
                    uiState = GameUiState(gameState = GameState(btc = "0.01")),
                    onBuyUpgrade = {},
                    onUpgradePowerGrid = {},
                    onUpgradeCooling = {},
                    onNavigateToSatoshiTree = {},
                    onNavigateToMine = { mineRouteCount++ }
                )
            }
        }

        composeTestRule.onNodeWithText("TURN BITCOIN INTO INVESTMENT CASH").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Mine to sell Bitcoin").performClick()
        assertEquals(1, mineRouteCount)
    }
}
