package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.MineScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun facilityHomeExplainsBitcoinSaleAndMarketDetailsProgressively() {
        var tapCount = 0
        var sellPercent = 0
        var upgradeNavigationCount = 0

        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                MineScreen(
                    uiState = GameUiState(
                        btcFormatted = "1.00000000 BTC",
                        usdFormatted = "$ 0.00",
                        hashrateFormatted = "2.50 kH/s",
                        btcPerSecFormatted = "+0.00000000 BTC/s",
                        powerFactor = 0.5,
                        powerDemandKw = 2.0,
                        powerCapacityKw = 0.5,
                        gameState = GameState(btc = "1.00000000", manualHashStrength = "10")
                    ),
                    onMineClick = { tapCount++ },
                    onQuickSell = { sellPercent = it },
                    onNavigateToUpgrades = { upgradeNavigationCount++ }
                )
            }
        }

        composeTestRule.onNodeWithText("Your facility").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cash").assertIsDisplayed()
        composeTestRule.onNodeWithText("Turn Bitcoin into Cash").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually", substring = true)
            .assertHasClickAction()
            .performClick()
        assertEquals(1, tapCount)

        composeTestRule.onNodeWithText("Sell MAX").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(100, sellPercent)

        composeTestRule.onNodeWithContentDescription("Show market details").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Price history from this save", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlock Auto-Sell in Upgrades").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, upgradeNavigationCount)

        composeTestRule.onNodeWithText("Upgrade Power").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(2, upgradeNavigationCount)
        composeTestRule.onNodeWithText("MARKET TELEMETRY").assertDoesNotExist()
        composeTestRule.onNodeWithText("ENVIRONMENTAL TELEMETRY").assertDoesNotExist()
    }
}
