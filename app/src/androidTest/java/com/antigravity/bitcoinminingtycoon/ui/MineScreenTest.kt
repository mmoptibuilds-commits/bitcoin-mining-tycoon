package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.MineScreen
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.AUTO_SELL_THRESHOLD_INPUT_TAG
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

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

        composeTestRule.onNodeWithContentDescription("Show market details")
            .assertHeightIsAtLeast(48.dp)
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText("Price history from this save", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlock Auto-Sell in Upgrades").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, upgradeNavigationCount)

        composeTestRule.onNodeWithText("Upgrade Power").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(2, upgradeNavigationCount)
        composeTestRule.onNodeWithText("MARKET TELEMETRY").assertDoesNotExist()
        composeTestRule.onNodeWithText("ENVIRONMENTAL TELEMETRY").assertDoesNotExist()
    }

    @Test
    fun unlockedAutoSellThresholdCanBeEditedAndSavedFromMarketDetails() {
        var savedThreshold: BigDecimal? = null
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                MineScreen(
                    uiState = GameUiState(
                        gameState = GameState(
                            purchasedUpgrades = setOf("auto_sell_controller"),
                            autoSellThresholdUsd = "50000"
                        )
                    ),
                    onMineClick = {},
                    onQuickSell = {},
                    onSetAutoSellThreshold = { threshold ->
                        savedThreshold = threshold
                        true
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Show market details").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Change threshold").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Set Auto-Sell threshold").assertIsDisplayed()
        val thresholdField = composeTestRule.onNodeWithTag(AUTO_SELL_THRESHOLD_INPUT_TAG)
        thresholdField.performTextClearance()
        thresholdField.performTextInput("62000.75")
        composeTestRule.onNodeWithText("Save threshold").performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, BigDecimal("62000.75").compareTo(savedThreshold))
        composeTestRule.onNodeWithText("Set Auto-Sell threshold").assertDoesNotExist()
    }

    @Test
    fun invalidAutoSellThresholdShowsErrorWithoutSaving() {
        var saveAttempted = false
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                MineScreen(
                    uiState = GameUiState(
                        gameState = GameState(purchasedUpgrades = setOf("auto_sell_controller"))
                    ),
                    onMineClick = {},
                    onQuickSell = {},
                    onSetAutoSellThreshold = {
                        saveAttempted = true
                        true
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Show market details").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Change threshold").performScrollTo().performClick()
        val thresholdField = composeTestRule.onNodeWithTag(AUTO_SELL_THRESHOLD_INPUT_TAG)
        thresholdField.performTextClearance()
        thresholdField.performTextInput("-12")
        composeTestRule.onNodeWithText("Save threshold").performClick()

        composeTestRule.onNodeWithText("Enter a valid non-negative USD amount", substring = true).assertIsDisplayed()
        assertEquals(false, saveAttempted)
    }
}
