package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.CoreMineButton
import com.antigravity.bitcoinminingtycoon.ui.components.EventBanner
import com.antigravity.bitcoinminingtycoon.ui.components.TeachingCue
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.WindfallChip
import com.antigravity.bitcoinminingtycoon.ui.presentation.DiscoveryPresentation
import com.antigravity.bitcoinminingtycoon.ui.presentation.DiscoveryCue
import com.antigravity.bitcoinminingtycoon.ui.presentation.DiscoveryGoal
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import java.math.BigDecimal

@Composable
fun MineScreen(
    uiState: GameUiState,
    onMineClick: () -> Unit,
    onQuickSell: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onToggleAutoSell: () -> Unit = {},
    onSetAutoSellThreshold: (BigDecimal) -> Unit = {},
    onClaimWindfall: (String) -> Unit = {},
    onDismissAchievement: () -> Unit = {},
    onCollectOfflineReward: () -> Unit = {},
    onShowDailyRewardSheet: () -> Unit = {},
    onDismissDailyRewardSheet: () -> Unit = {},
    onClaimDailyReward: () -> Unit = {},
    onCompleteTeachingCue: (String) -> Unit = {},
    onNavigateToHardware: () -> Unit = {},
    onNavigateToUpgrades: () -> Unit = {}
) {
    val gameState = uiState.gameState
    val cue = DiscoveryPresentation.nextCue(gameState)
    val goal = DiscoveryPresentation.nextGoal(gameState)
    val hasMachine = gameState.miners.values.any { it > 0L }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Your facility", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = AppColors.TextHigh)
            Text(
                text = if (hasMachine) "Machines running · offline simulation" else "No machines yet · offline simulation",
                color = AppColors.TextMedium,
                fontSize = 12.sp
            )
        }

        BalanceStrip(uiState = uiState)

        Surface(color = AppColors.SurfaceLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Mining speed", color = AppColors.TextMedium, fontSize = 13.sp)
                Text(uiState.hashrateFormatted, color = AppColors.PrimaryCopper, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Box(modifier = Modifier.padding(top = 4.dp), contentAlignment = Alignment.Center) {
                    CoreMineButton(
                        onMineClick = onMineClick,
                        manualHashrateText = "+${gameState.manualHashStrength} hashes per tap",
                        reducedMotion = gameState.settings.reducedMotion
                    )
                }
                Text("Tap Mine to add Bitcoin", color = AppColors.TextMedium, fontSize = 12.sp)
            }
        }

        if (cue != null) {
            TeachingCue(
                cue = cue,
                onAction = { runCueAction(cue, onMineClick, onQuickSell, onNavigateToHardware, onCompleteTeachingCue) },
                onDismiss = { onCompleteTeachingCue(cue.id) }
            )
        } else {
            NextGoal(goal = goal, onMineClick = onMineClick, onQuickSell = onQuickSell,
                onNavigateToHardware = onNavigateToHardware, onNavigateToUpgrades = onNavigateToUpgrades)
        }

        if (hasMachine) {
            TycoonButton(
                text = if (uiState.canClaimDailyReward) "Collect today's reward" else "Daily rewards · Day ${gameState.dailyRewardDay}",
                onClick = onShowDailyRewardSheet,
                style = if (uiState.canClaimDailyReward) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.activeAmbientEvent != null || uiState.activeWindfallEvent != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.activeAmbientEvent?.let { EventBanner(it, uiState.currentWallMillis) }
                uiState.activeWindfallEvent?.let { WindfallChip(it, uiState.currentWallMillis, onClaimWindfall) }
            }
        }

        MarketCard(
            gameState = gameState,
            onQuickSell = onQuickSell,
            onToggleAutoSell = onToggleAutoSell,
            onSetAutoSellThreshold = onSetAutoSellThreshold,
            onNavigateToUpgrades = onNavigateToUpgrades
        )

        if (uiState.powerFactor < 0.999 || uiState.thermalFactor < 0.999) {
            EnvironmentalPanel(
                powerDemandKw = uiState.powerDemandKw,
                powerCapacityKw = uiState.powerCapacityKw,
                powerFactor = uiState.powerFactor,
                equilibriumTemp = uiState.equilibriumTemp,
                thermalFactor = uiState.thermalFactor,
                onNavigateToUpgrades = onNavigateToUpgrades
            )
        }

        Text(
            text = "Fictional offline simulation · no real Bitcoin is mined or traded.",
            color = AppColors.TextMedium,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
    }

    if (uiState.offlineReport != null) {
        OfflineReturnSheet(report = uiState.offlineReport, onCollect = onCollectOfflineReward)
    }
    if (uiState.dailyRewardSheetVisible) {
        DailyRewardSheet(
            currentDay = gameState.dailyRewardDay,
            canClaim = uiState.canClaimDailyReward && hasMachine,
            millisUntilNextClaim = uiState.dailyRewardCooldownMillis,
            onClaim = onClaimDailyReward,
            onDismiss = onDismissDailyRewardSheet
        )
    }
}

@Composable
private fun BalanceStrip(uiState: GameUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = AppColors.SurfaceLow,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Bitcoin", color = AppColors.TextMedium, fontSize = 12.sp)
                Text(uiState.btcFormatted, color = AppColors.PrimaryCopper, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(uiState.btcPerSecFormatted, color = AppColors.TextMedium, fontSize = 11.sp)
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = AppColors.SurfaceLow,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Cash", color = AppColors.TextMedium, fontSize = 12.sp)
                Text(uiState.usdFormatted, color = AppColors.TextHigh, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Buy machines and upgrades", color = AppColors.TextMedium, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun NextGoal(
    goal: DiscoveryGoal,
    onMineClick: () -> Unit,
    onQuickSell: (Int) -> Unit,
    onNavigateToHardware: () -> Unit,
    onNavigateToUpgrades: () -> Unit
) {
    Surface(color = AppColors.SurfaceLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Next goal", color = AppColors.PrimaryCopper, fontSize = 11.sp, modifier = Modifier.semantics { heading() })
                Text(goal.title, color = AppColors.TextHigh, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(goal.body, color = AppColors.TextMedium, fontSize = 12.sp)
            }
            TycoonButton(
                text = goal.actionLabel,
                onClick = {
                    when (goal.actionLabel) {
                        "Mine" -> onMineClick()
                        "Sell MAX" -> onQuickSell(100)
                        "Upgrades" -> onNavigateToUpgrades()
                        else -> onNavigateToHardware()
                    }
                },
                style = ButtonStyle.SECONDARY,
                contentDescriptionText = "${goal.actionLabel}. Next goal: ${goal.title}"
            )
        }
    }
}

private fun runCueAction(
    cue: DiscoveryCue,
    onMine: () -> Unit,
    onSell: (Int) -> Unit,
    onHardware: () -> Unit,
    onComplete: (String) -> Unit
) {
    when (cue.id) {
        DiscoveryPresentation.MINE_BITCOIN -> onMine()
        DiscoveryPresentation.SELL_BITCOIN -> onSell(100)
        DiscoveryPresentation.BUY_FIRST_MACHINE -> onHardware()
        else -> onComplete(cue.id)
    }
}
