package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun mineScreen_displaysDashboardAndHandlesTap() {
        var tapCount = 0
        var sellPercent = 0

        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                MineScreen(
                    uiState = GameUiState(
                        btcFormatted = "0.00000000 BTC",
                        usdFormatted = "$ 0.00",
                        hashrateFormatted = "0.00 H/s",
                        btcPerSecFormatted = "+0.00000000 BTC/s",
                        gameState = GameState(manualHashStrength = "10")
                    ),
                    onMineClick = { tapCount++ },
                    onQuickSell = { sellPercent = it }
                )
            }
        }

        // Verify balance headers
        composeTestRule.onNodeWithText("BTC BALANCE").assertIsDisplayed()
        composeTestRule.onNodeWithText("CASH RESERVE").assertIsDisplayed()

        // Verify MINE button is present and clickable
        val mineNode = composeTestRule.onNodeWithContentDescription("Mine Bitcoin manually. Generates +10 H")
        mineNode.assertIsDisplayed()
        mineNode.assertHasClickAction()
        mineNode.performClick()
        assertEquals(1, tapCount)

        // Verify Quick Sell buttons are accessible
        val sellMaxNode = composeTestRule.onNodeWithText("Sell MAX")
        sellMaxNode.assertIsDisplayed()
        sellMaxNode.performClick()
        assertEquals(100, sellPercent)

        // Verify Market telemetry & sparkline
        composeTestRule.onNodeWithText("MARKET TELEMETRY").assertIsDisplayed()
        composeTestRule.onNodeWithText("SIMULATED BTC SPOT").assertIsDisplayed()
        composeTestRule.onNodeWithText("AUTO-SELL: OFF").assertIsDisplayed()

        // Verify Environmental telemetry
        composeTestRule.onNodeWithText("ENVIRONMENTAL TELEMETRY").assertIsDisplayed()
        composeTestRule.onNodeWithText("GRID LOAD").assertIsDisplayed()
        composeTestRule.onNodeWithText("CORE TEMP").assertIsDisplayed()
    }
}
