package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.PowerEnergySample
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.ui.screens.stats.StatsScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatsCoverageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun legacyCoverageCurrentAndLifetimeStatsAndAchievementGoalsAreVisible() {
        val gameState = GameState(
            btc = "0.25",
            usd = "12.50",
            stats = StatsState(
                lifetimeBtcMined = "5",
                lifetimeUsdEarned = "1000",
                totalManualTaps = 40,
                totalPlaytimeSeconds = 3661,
                playtimeFractionalSeconds = "0.25",
                sourceBreakdownTrackedSinceV12 = false,
                powerEnergyHistory = listOf(PowerEnergySample(60, "1.5", "5", "0.125"))
            )
        )
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                StatsScreen(uiState = GameUiState(gameState = gameState))
            }
        }

        composeTestRule.scrollUntilTextDisplayed("Current Bitcoin")
        composeTestRule.scrollUntilTextDisplayed("Lifetime Bitcoin mined")
        composeTestRule.scrollUntilTextDisplayed("Partial; tracked since v1.2, earlier lifetime totals are unassigned")
        composeTestRule.scrollUntilTextDisplayed("1h 1m 1.25s")
        composeTestRule.scrollUntilTextDisplayed("Genesis Entropy")
        composeTestRule.onNodeWithContentDescription("progress 40 / 1 taps", substring = true)
            .fetchSemanticsNode()
    }
}
