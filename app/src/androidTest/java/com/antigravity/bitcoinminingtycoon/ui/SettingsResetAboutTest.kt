package com.antigravity.bitcoinminingtycoon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.ui.navigation.AppNavHost
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsResetAboutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun batteryPreferencePersistsAndAboutReadsInstalledPackageMetadata() {
        val harness = UiGameHarness()
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme { AppNavHost(viewModel = harness.viewModel) }
        }
        composeTestRule.waitUntil(10_000) {
            harness.viewModel.uiState.value.saveReadiness == SaveReadiness.Ready
        }

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithContentDescription(
            "Battery-friendly scene. Pause decorative scene motion and mining particles"
        ).performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.batteryFriendlyAnimations
        }
        assertTrue(harness.repository.gameState.value.batteryFriendlyAnimations)
        assertEquals(SettingsState(), harness.repository.gameState.value.settings)

        composeTestRule.onNodeWithContentDescription("About Bitcoin Mining Tycoon").performClick()
        composeTestRule.onNodeWithText("Native offline facility-management simulation.", substring = true)
            .assertIsDisplayed()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        composeTestRule.onNodeWithText(context.packageName).assertIsDisplayed()
        composeTestRule.onNodeWithText("${packageInfo.versionName} (${packageInfo.longVersionCode})")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            if (packageInfo.versionName?.startsWith("1.2") == true) "V1.2 RELEASE NOTES" else "FEATURES IN THIS BUILD"
        ).assertIsDisplayed()
    }

    @Test
    fun resetExplainsScopeCancelPreservesAndConfirmationClearsProgression() {
        val startingSettings = SettingsState(soundEnabled = false, hapticsEnabled = false)
        val harness = UiGameHarness(
            GameSave(
                btc = "0.1",
                usd = "50",
                miners = mapOf("ancient_cpu" to 1L),
                onboardingCompleted = true,
                settings = startingSettings
            )
        )
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme { AppNavHost(viewModel = harness.viewModel) }
        }
        composeTestRule.waitUntil(10_000) {
            harness.viewModel.uiState.value.saveReadiness == SaveReadiness.Ready
        }
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        composeTestRule.onNodeWithText("FACTORY RESET FACILITY").performClick()
        composeTestRule.onNodeWithText("balances, machines, upgrades", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("CANCEL").performClick()
        assertEquals("0.1", harness.repository.gameState.value.btc)
        assertEquals(1L, harness.repository.gameState.value.miners["ancient_cpu"])

        composeTestRule.onNodeWithText("FACTORY RESET FACILITY").performClick()
        composeTestRule.onNodeWithText("YES, WIPE").performClick()
        composeTestRule.waitUntil(10_000) {
            harness.repository.gameState.value.btcBigDecimal.signum() == 0 &&
                harness.repository.gameState.value.miners.isEmpty()
        }
        assertEquals(SettingsState(), harness.repository.gameState.value.settings)
        assertEquals(false, harness.repository.gameState.value.onboardingCompleted)
    }
}
