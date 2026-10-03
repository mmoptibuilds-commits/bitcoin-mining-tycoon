package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.engine.OfflineReport
import com.antigravity.bitcoinminingtycoon.engine.PrestigePreview
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.ui.components.EventBanner
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.DailyRewardSheet
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.OfflineReturnSheet
import com.antigravity.bitcoinminingtycoon.ui.screens.prestige.PrestigeSheet
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardsEventsPrestigeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun eventBannerExplainsItsEffectAndTimeUntilExpiry() {
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                EventBanner(
                    activeEvent = ActiveEventState(
                        eventId = "asic_breakthrough",
                        expiresAtWallMillis = 1_040_000L,
                        multiplier = 1.35
                    ),
                    currentWallMillis = 1_000_000L
                )
            }
        }

        composeTestRule.onNodeWithText("ASIC Breakthrough").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hashrate +35%", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Active operational event", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("40 seconds remaining", substring = true).assertIsDisplayed()
    }

    @Test
    fun dailyRewardSheetShowsCycleAndClaimAction() {
        var claims = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                DailyRewardSheet(
                    currentDay = 1,
                    canClaim = true,
                    millisUntilNextClaim = 0L,
                    onClaim = { claims++ },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("7-Day Continuous Cycle").assertIsDisplayed()
        composeTestRule.onNodeWithText("CLAIM DAY 1 REWARD").performScrollTo().performClick()
        assertEquals(1, claims)
    }

    @Test
    fun offlineSummaryNamesTheCreditedBitcoinAndCollectAction() {
        var collections = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                OfflineReturnSheet(
                    report = OfflineReport(3600.0, BigDecimal("1000000"), BigDecimal("0.0001")),
                    onCollect = { collections++ }
                )
            }
        }

        composeTestRule.onNodeWithText("FACILITY OFFLINE REPORT").assertIsDisplayed()
        composeTestRule.onNodeWithText("+0.00010000 BTC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Average Effective Hashrate").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.00 MH/s").assertIsDisplayed()
        composeTestRule.onNodeWithText("COLLECT OFFLINE PRODUCTION").performClick()
        assertEquals(1, collections)
    }

    @Test
    fun olderOfflineSummaryShowsRateWasNotRecorded() {
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                OfflineReturnSheet(
                    report = OfflineReport(3600.0, null, BigDecimal("0.0001")),
                    onCollect = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Not recorded").assertIsDisplayed()
    }

    @Test
    fun prestigeListsResetAndPreservedProgressionBeforeConfirmation() {
        var confirmations = 0
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                PrestigeSheet(
                    preview = PrestigePreview(
                        earnablePoints = 2,
                        isPrestigeAvailable = true,
                        currentBtcToLose = "2",
                        currentUsdToLose = "50",
                        minersCountToLose = 3,
                        upgradesCountToLose = 4,
                        currentSatoshiPoints = 1,
                        newSatoshiPointsTotal = 3
                    ),
                    onConfirm = { confirmations++ },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("RESET TO BASE").assertIsDisplayed()
        composeTestRule.onNodeWithText("PERMANENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("CONFIRM PRESTIGE RESET").performScrollTo().performClick()
        assertEquals(1, confirmations)
    }
}
