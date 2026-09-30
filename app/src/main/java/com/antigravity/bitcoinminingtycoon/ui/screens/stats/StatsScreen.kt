package com.antigravity.bitcoinminingtycoon.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.AchievementCategory
import com.antigravity.bitcoinminingtycoon.content.AchievementDefinition
import com.antigravity.bitcoinminingtycoon.content.Achievements
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun StatsScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val unlockedSet = uiState.gameState.achievements

    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

    val filteredAchievements = remember(selectedCategory) {
        if (selectedCategory == null) {
            Achievements.ALL
        } else {
            Achievements.ALL.filter { it.category == selectedCategory }
        }
    }

    val statistics = remember(uiState.gameState) {
        StatsBreakdown.sections(uiState.gameState)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "FACILITY TELEMETRY",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper
            )
        }

        items(statistics, key = { "stats_${it.title}" }) { section ->
            TycoonCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = section.title.uppercase(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    section.rows.forEach { row -> StatRow(row.label, row.value) }
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
            val progress = StatsBreakdown.achievementProgress(achievement.id, uiState.gameState)
            AchievementItemCard(
                achievement = achievement,
                isUnlocked = isUnlocked,
                progressText = progress?.displayValue(uiState.gameState.settings.numberFormat) ?: "Progress unavailable",
                progressFraction = progress?.let {
                    if (it.target <= BigDecimal.ZERO) 1f
                    else it.current.divide(it.target, 4, RoundingMode.HALF_UP).min(BigDecimal.ONE).toFloat()
                } ?: 0f
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.TextMedium,
            modifier = Modifier.weight(0.44f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextHigh,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.56f)
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
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = label
            },
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
    isUnlocked: Boolean,
    progressText: String,
    progressFraction: Float
) {
    val borderColor = if (isUnlocked) AppColors.PositiveGreen else AppColors.BorderSubtle

    TycoonCard(
        borderColor = borderColor,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "${achievement.title}, ${achievement.description}, ${if (isUnlocked) "Unlocked" else "Locked"}, progress $progressText"
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
                Text(
                    text = "Progress: $progressText",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isUnlocked) AppColors.PositiveGreen else AppColors.TextMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    color = if (isUnlocked) AppColors.PositiveGreen else AppColors.PrimaryCopper,
                    trackColor = AppColors.SurfaceLow
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
