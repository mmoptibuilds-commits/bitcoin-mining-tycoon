package com.antigravity.bitcoinminingtycoon.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.AchievementCategory
import com.antigravity.bitcoinminingtycoon.content.AchievementDefinition
import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import java.util.concurrent.TimeUnit

@Composable
fun StatsScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val stats = uiState.gameState.stats
    val unlockedSet = uiState.gameState.achievements

    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

    val filteredAchievements = remember(selectedCategory) {
        if (selectedCategory == null) {
            Achievements.ALL
        } else {
            Achievements.ALL.filter { it.category == selectedCategory }
        }
    }

    val playtimeHours = TimeUnit.SECONDS.toHours(stats.totalPlaytimeSeconds)
    val playtimeMins = TimeUnit.SECONDS.toMinutes(stats.totalPlaytimeSeconds) % 60
    val playtimeText = "${playtimeHours}h ${playtimeMins}m"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Operational Telemetry Section
        item {
            Text(
                text = "FACILITY TELEMETRY",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper
            )
        }

        item {
            TycoonCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Lifetime Bitcoin Mined", NumberFormatter.formatBtc(stats.lifetimeBtcBigDecimal, uiState.gameState.settings.numberFormat))
                    StatRow("Lifetime Revenue", NumberFormatter.formatUsd(stats.lifetimeUsdBigDecimal, uiState.gameState.settings.numberFormat))
                    StatRow("Peak Hashrate", NumberFormatter.formatHashrate(stats.peakHashrateBigDecimal))
                    StatRow("Manual MINE Taps", "${stats.totalManualTaps}")
                    StatRow("Hardware Units Deployed", "${stats.totalMinersPurchased}")
                    StatRow("Technologies Installed", "${stats.totalUpgradesPurchased}")
                    StatRow("Bitcoin Liquidated", NumberFormatter.formatBtc(stats.totalBtcSoldBigDecimal, uiState.gameState.settings.numberFormat))
                    StatRow("Highest Spot Price", NumberFormatter.formatUsd(stats.highestPriceBigDecimal, uiState.gameState.settings.numberFormat))
                    StatRow("Lowest Spot Price", NumberFormatter.formatUsd(stats.lowestPriceBigDecimal, uiState.gameState.settings.numberFormat))
                    StatRow("Events Encountered", "${stats.totalEventsTriggered}")
                    StatRow("Total Prestiges Executed", "${stats.totalPrestiges}")
                    StatRow("Lifetime Satoshi Points", "${stats.lifetimeSatoshiPointsEarned} SP")
                    StatRow("Total Facility Uptime", playtimeText)
                }
            }
        }

        // Achievements Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACHIEVEMENTS ARCHIVE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryCopper
                )
                Text(
                    text = "${unlockedSet.size} / ${Achievements.ALL.size} UNLOCKED",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextHigh
                )
            }
        }

        // Filter Chips
        item {
            val chipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "ALL (${Achievements.ALL.size})",
                    isSelected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
                AchievementCategory.entries.forEach { category ->
                    val count = Achievements.ALL.count { it.category == category }
                    FilterChipItem(
                        label = "${category.name} ($count)",
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        // Achievement Items
        items(filteredAchievements, key = { it.id }) { achievement ->
            val isUnlocked = achievement.id in unlockedSet
            AchievementItemCard(achievement = achievement, isUnlocked = isUnlocked)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = AppColors.TextMedium)
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextHigh
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AppColors.PrimaryCopper else AppColors.SurfaceLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AppColors.PrimaryCopperHover else AppColors.BorderSubtle
        )
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AppColors.Background else AppColors.TextMedium
            )
        }
    }
}

@Composable
private fun AchievementItemCard(
    achievement: AchievementDefinition,
    isUnlocked: Boolean
) {
    val borderColor = if (isUnlocked) AppColors.PositiveGreen else AppColors.BorderSubtle

    TycoonCard(
        borderColor = borderColor,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "${achievement.title}, ${achievement.description}, ${if (isUnlocked) "Unlocked" else "Locked"}"
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) AppColors.TextHigh else AppColors.TextDisabled
                )
                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = if (isUnlocked) AppColors.TextMedium else AppColors.TextDisabled,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = if (isUnlocked) "[UNLOCKED]" else "[LOCKED]",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) AppColors.PositiveGreen else AppColors.TextDisabled,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
