package com.antigravity.bitcoinminingtycoon.ui.screens.upgrades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.Infrastructure
import com.antigravity.bitcoinminingtycoon.content.UpgradeCategory
import com.antigravity.bitcoinminingtycoon.content.UpgradeDefinition
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.engine.PowerEngine
import com.antigravity.bitcoinminingtycoon.engine.ThermalEngine
import com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.MetricTile
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState

/**
 * Upgrades and Infrastructure screen managing category filters, dedicated Power/Cooling
 * infrastructure stages (Decision D012), data-driven upgrades, and the Satoshi Tree entry portal.
 */
@Composable
fun UpgradesScreen(
    uiState: GameUiState,
    onBuyUpgrade: (String) -> Unit,
    onUpgradePowerGrid: () -> Unit,
    onUpgradeCooling: () -> Unit,
    onNavigateToSatoshiTree: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(UpgradeCategory.ALL) }
    val gameState = uiState.gameState

    val filteredUpgrades = remember(selectedCategory, gameState.purchasedUpgrades) {
        if (selectedCategory == UpgradeCategory.ALL) {
            Upgrades.ALL
        } else {
            Upgrades.ALL.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Balance Summary Card
        item(key = "header_hud") {
            TycoonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricTile(
                        label = "Cash Reserve",
                        value = uiState.usdFormatted,
                        valueColor = AppColors.TextHigh,
                        subValue = "Upgrades Owned: ${gameState.purchasedUpgrades.size}",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricTile(
                        label = "Satoshi Points",
                        value = "${gameState.satoshiPoints} SP",
                        valueColor = AppColors.PrimaryCopper,
                        subValue = "Permanent Multiplier: +${gameState.satoshiPoints}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Dedicated Infrastructure Upgrade Tracks (Decision D012)
        item(key = "infrastructure_power") {
            PowerGridInfrastructureCard(
                gameState = gameState,
                onUpgrade = onUpgradePowerGrid
            )
        }

        item(key = "infrastructure_cooling") {
            CoolingInfrastructureCard(
                gameState = gameState,
                onUpgrade = onUpgradeCooling
            )
        }

        // 3. Satoshi Prestige Portal Card (Decision D020)
        item(key = "satoshi_portal") {
            SatoshiPortalCard(
                satoshiPoints = gameState.satoshiPoints,
                onNavigate = onNavigateToSatoshiTree
            )
        }

        // 4. Upgrade Category Filter Chips
        item(key = "category_filters") {
            UpgradeCategoryFilterRow(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }

        // 5. Data-driven Upgrades List
        items(
            items = filteredUpgrades,
            key = { it.id }
        ) { upgrade ->
            UpgradeItemCard(
                upgrade = upgrade,
                gameState = gameState,
                onBuy = { onBuyUpgrade(upgrade.id) }
            )
        }
    }
}

@Composable
private fun PowerGridInfrastructureCard(
    gameState: GameState,
    onUpgrade: () -> Unit
) {
    val currentStage = Infrastructure.getPowerStage(gameState.powerGridTier)
    val nextStage = PowerEngine.getNextPowerStage(gameState)
    val canUpgrade = nextStage != null
    val isAffordable = nextStage != null && gameState.usdBigDecimal >= nextStage.costUsd
    val costFormatted = if (nextStage != null) NumberFormatter.formatUsd(nextStage.costUsd, gameState.settings.numberFormat) else ""

    TycoonCard(borderColor = AppColors.PrimaryCopperDark) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "POWER GRID INFRASTRUCTURE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    Text(
                        text = "Stage ${currentStage.tier}/10: ${currentStage.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextHigh
                    )
                }
                Text(
                    text = "${currentStage.capacityKw} kW",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PositiveGreen
                )
            }

            if (nextStage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: ${nextStage.name} (${nextStage.capacityKw} kW)",
                        fontSize = 11.sp,
                        color = AppColors.TextMedium
                    )
                }

                val buttonText = if (isAffordable) {
                    "Upgrade Grid ($costFormatted)"
                } else {
                    val deficit = nextStage.costUsd.subtract(gameState.usdBigDecimal, GameNumber.MATH_CONTEXT)
                    "Need ${NumberFormatter.formatUsd(deficit, gameState.settings.numberFormat)}"
                }

                TycoonButton(
                    text = buttonText,
                    onClick = onUpgrade,
                    enabled = isAffordable,
                    style = if (isAffordable) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                    contentDescriptionText = "Upgrade power grid to stage ${nextStage.tier} for $costFormatted"
                )
            } else {
                Text(
                    text = "MAXIMUM POWER GRID STAGE REACHED",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PositiveGreen
                )
            }
        }
    }
}

@Composable
private fun CoolingInfrastructureCard(
    gameState: GameState,
    onUpgrade: () -> Unit
) {
    val currentStage = Infrastructure.getCoolingStage(gameState.coolingTier)
    val nextStage = ThermalEngine.getNextCoolingStage(gameState)
    val canUpgrade = nextStage != null
    val isAffordable = nextStage != null && gameState.usdBigDecimal >= nextStage.costUsd
    val costFormatted = if (nextStage != null) NumberFormatter.formatUsd(nextStage.costUsd, gameState.settings.numberFormat) else ""

    TycoonCard(borderColor = AppColors.BorderSubtle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THERMAL COOLING INFRASTRUCTURE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.WarningAmber
                    )
                    Text(
                        text = "Stage ${currentStage.tier}/7: ${currentStage.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextHigh
                    )
                }
                Text(
                    text = "${currentStage.dissipationRating} units",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.WarningAmber
                )
            }

            if (nextStage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: ${nextStage.name} (${nextStage.dissipationRating} units)",
                        fontSize = 11.sp,
                        color = AppColors.TextMedium
                    )
                }

                val buttonText = if (isAffordable) {
                    "Upgrade Cooling ($costFormatted)"
                } else {
                    val deficit = nextStage.costUsd.subtract(gameState.usdBigDecimal, GameNumber.MATH_CONTEXT)
                    "Need ${NumberFormatter.formatUsd(deficit, gameState.settings.numberFormat)}"
                }

                TycoonButton(
                    text = buttonText,
                    onClick = onUpgrade,
                    enabled = isAffordable,
                    style = if (isAffordable) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                    contentDescriptionText = "Upgrade cooling infrastructure to stage ${nextStage.tier} for $costFormatted"
                )
            } else {
                Text(
                    text = "MAXIMUM COOLING STAGE REACHED",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PositiveGreen
                )
            }
        }
    }
}

@Composable
private fun SatoshiPortalCard(
    satoshiPoints: Long,
    onNavigate: () -> Unit
) {
    TycoonCard(
        backgroundColor = AppColors.SurfaceHigh,
        borderColor = AppColors.PrimaryCopper
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SATOSHI PRESTIGE PORTAL",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryCopper
                )
                Text(
                    text = "$satoshiPoints Satoshi Points Available",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextHigh
                )
            }

            TycoonButton(
                text = "Open Tree",
                onClick = onNavigate,
                style = ButtonStyle.PRIMARY,
                contentDescriptionText = "Open Satoshi Prestige Legacy Tree"
            )
        }
    }
}

@Composable
private fun UpgradeCategoryFilterRow(
    selected: UpgradeCategory,
    onSelect: (UpgradeCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(UpgradeCategory.values()) { category ->
            val isSelected = category == selected
            val bg = if (isSelected) AppColors.PrimaryCopperDark else AppColors.SurfaceLow
            val border = if (isSelected) AppColors.PrimaryCopper else AppColors.BorderSubtle
            val text = if (isSelected) AppColors.PrimaryCopperHover else AppColors.TextMedium

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 54.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.RadioButton,
                        onClickLabel = "Filter by ${category.label}"
                    ) {
                        onSelect(category)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.label,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = text
                )
            }
        }
    }
}

@Composable
private fun UpgradeItemCard(
    upgrade: UpgradeDefinition,
    gameState: GameState,
    onBuy: () -> Unit
) {
    val isPurchased = upgrade.id in gameState.purchasedUpgrades
    val isUnlocked = UpgradeEngine.isUnlocked(upgrade, gameState)
    val isAffordable = UpgradeEngine.canAfford(upgrade, gameState)
    val costFormatted = NumberFormatter.formatUsd(upgrade.costUsd, gameState.settings.numberFormat)

    val cardBorder = when {
        isPurchased -> AppColors.PositiveGreen
        !isUnlocked -> AppColors.BorderSubtle
        isAffordable -> AppColors.BorderFocus
        else -> AppColors.BorderSubtle
    }

    TycoonCard(borderColor = cardBorder) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = upgrade.name.uppercase(),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isPurchased) AppColors.PositiveGreen else AppColors.TextHigh
                    )
                    Text(
                        text = upgrade.description,
                        fontSize = 11.sp,
                        color = AppColors.TextMedium,
                        maxLines = 2
                    )
                }

                if (isPurchased) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.SurfaceLow)
                            .border(1.dp, AppColors.PositiveGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PURCHASED",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PositiveGreen
                        )
                    }
                }
            }

            if (!isPurchased) {
                if (isUnlocked) {
                    val buttonText = if (isAffordable) {
                        "Buy for $costFormatted"
                    } else {
                        val deficit = upgrade.costUsd.subtract(gameState.usdBigDecimal, GameNumber.MATH_CONTEXT)
                        "Need ${NumberFormatter.formatUsd(deficit, gameState.settings.numberFormat)}"
                    }

                    TycoonButton(
                        text = buttonText,
                        onClick = onBuy,
                        enabled = isAffordable,
                        style = if (isAffordable) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        contentDescriptionText = "Purchase ${upgrade.name} for $costFormatted"
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.SurfaceLow)
                            .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOCKED — REQUIRES PREREQUISITE UPGRADE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextDisabled
                        )
                    }
                }
            }
        }
    }
}
