package com.antigravity.bitcoinminingtycoon.ui.facility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.theme.BitcoinMiningTycoonTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FacilitySceneTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allTenOwnedStagesRenderTheirNameAndOnlyTheNextStagePreview() {
        var scene by mutableStateOf(FacilityPresentation.present(GameState()))
        composeTestRule.setContent {
            BitcoinMiningTycoonTheme {
                FacilityScene(model = scene, reducedMotion = true, batteryFriendly = true)
            }
        }

        FacilityStage.entries.forEachIndexed { index, stage ->
            val owned = stage.minerIds.associateWith { 1L }
            composeTestRule.runOnIdle {
                scene = FacilityPresentation.present(
                    GameState(miners = owned, highestDiscoveredFacilityStage = stage.stageIndex)
                )
            }
            composeTestRule.onNodeWithText(stage.title).assertIsDisplayed()
            FacilityStage.entries.getOrNull(index + 1)?.let { next ->
                composeTestRule.onNodeWithText("Preview: ${next.title}").assertIsDisplayed()
            }
        }
    }
}
