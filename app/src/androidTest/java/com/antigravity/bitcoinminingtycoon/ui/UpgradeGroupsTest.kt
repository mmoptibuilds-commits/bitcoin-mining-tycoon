package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
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
        composeTestRule.scrollUntilTextDisplayed("COPPER FINGERS")
        composeTestRule.onNodeWithText("CPU BIOS VOLTAGE MOD").assertDoesNotExist()

        composeTestRule.scrollToTop()
        composeTestRule.onNodeWithContentDescription("Show Infrastructure upgrade track").performClick()
        composeTestRule.scrollUntilTextDisplayed("PRECISION UNDERVOLTING")
        composeTestRule.scrollUntilTextDisplayed("CARBON NANOTUBE THERMAL PASTE")

        composeTestRule.scrollToTop()
        composeTestRule.onNodeWithContentDescription("Show Infrastructure upgrade track")
            .performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithContentDescription("Show Automation upgrade track").performClick()
        composeTestRule.scrollUntilTextDisplayed("SIMULATED MARKET TICKER")
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
