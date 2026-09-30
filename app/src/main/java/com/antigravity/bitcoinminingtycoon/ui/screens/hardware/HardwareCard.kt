package com.antigravity.bitcoinminingtycoon.ui.screens.hardware

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.MinerDefinition
import com.antigravity.bitcoinminingtycoon.engine.BulkMode
import com.antigravity.bitcoinminingtycoon.engine.FleetEngine
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.GameNumber
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import java.math.BigDecimal

/**
 * Hardware tier card displaying specs, fleet contribution, environmental impact, and dynamic bulk purchase action.
 * Strict anti-AI-slop rule: Industrial graphite surface, vector schematic motif, no stock crypto images or emoji.
 */
@Composable
fun HardwareCard(
    miner: MinerDefinition,
    ownedCount: Long,
    bulkMode: BulkMode,
    availableUsd: BigDecimal,
    isUnlocked: Boolean,
    numberFormat: NumberFormatPreference,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (!isUnlocked) AppColors.BorderSubtle else if (ownedCount > 0) AppColors.BorderFocus else AppColors.BorderSubtle
    val cardBackground = if (!isUnlocked) AppColors.SurfaceLow else AppColors.Surface

    TycoonCard(
        backgroundColor = cardBackground,
        borderColor = borderColor,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Header: Icon motif, Title, Owned Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinerSchematicIcon(minerId = miner.id, isUnlocked = isUnlocked)

                    Column {
                        Text(
                            text = miner.name.uppercase(),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) AppColors.TextHigh else AppColors.TextMedium
                        )
                        Text(
                            text = miner.description,
                            fontSize = 11.sp,
                            color = AppColors.TextMedium,
                            maxLines = 2
                        )
                    }
                }

                // Owned badge
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (ownedCount > 0) AppColors.PrimaryCopperDark else AppColors.SurfaceLow)
                            .border(1.dp, if (ownedCount > 0) AppColors.PrimaryCopper else AppColors.BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "x$ownedCount",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (ownedCount > 0) AppColors.PrimaryCopperHover else AppColors.TextDisabled
                        )
                    }
                }
            }

            // 2. Metrics & Telemetry Row
            if (isUnlocked) {
                val totalOutput = miner.baseHashrate.multiply(BigDecimal(ownedCount.coerceAtLeast(1L)), GameNumber.MATH_CONTEXT)
                val totalHashrateFormatted = NumberFormatter.formatHashrate(totalOutput)
                val baseHashrateFormatted = NumberFormatter.formatHashrate(miner.baseHashrate)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "UNIT HASHRATE", fontSize = 10.sp, color = AppColors.TextMedium)
                        Text(
                            text = "$baseHashrateFormatted/s",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextHigh
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "POWER / HEAT", fontSize = 10.sp, color = AppColors.TextMedium)
                        Text(
                            text = "${miner.powerDrawKw} kW | +${miner.heatLoad}°C",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AppColors.WarningAmber
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "FLEET SHARE", fontSize = 10.sp, color = AppColors.TextMedium)
                        Text(
                            text = totalHashrateFormatted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PositiveGreen
                        )
                    }
                }

                // 3. Purchase Button & Affordability Feedback
                val (countToBuy, totalCost) = FleetEngine.calculatePurchase(miner, ownedCount, bulkMode, availableUsd)
                val isAffordable = availableUsd >= totalCost && countToBuy > 0L
                val costFormatted = NumberFormatter.formatUsd(totalCost, numberFormat)

                val buttonText = when {
                    isAffordable -> "Buy $countToBuy ($costFormatted)"
                    countToBuy > 0L -> {
                        val deficit = totalCost.subtract(availableUsd, GameNumber.MATH_CONTEXT)
                        val deficitFormatted = NumberFormatter.formatUsd(deficit, numberFormat)
                        "Buy $countToBuy (Need $deficitFormatted)"
                    }
                    else -> {
                        val deficit = totalCost.subtract(availableUsd, GameNumber.MATH_CONTEXT)
                        val deficitFormatted = NumberFormatter.formatUsd(deficit, numberFormat)
                        "Need $deficitFormatted"
                    }
                }

                TycoonButton(
                    text = buttonText,
                    onClick = onBuyClick,
                    enabled = isAffordable,
                    style = if (isAffordable) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                    contentDescriptionText = if (isAffordable) {
                        "Purchase $countToBuy ${miner.name} for $costFormatted"
                    } else {
                        "Cannot afford $countToBuy ${miner.name}. Requires $costFormatted"
                    }
                )
            } else {
                // Locked State UI
                val unlockBtcFormatted = NumberFormatter.formatBtc(miner.unlockLifetimeBtc, numberFormat)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.SurfaceLow)
                        .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOCKED — UNLOCKS AT $unlockBtcFormatted LIFETIME MINED",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextDisabled
                    )
                }
            }
        }
    }
}

/**
 * Procedural Canvas drawing of industrial hardware components.
 */
@Composable
private fun MinerSchematicIcon(
    minerId: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isUnlocked) AppColors.PrimaryCopper else AppColors.TextDisabled
    val surfaceColor = if (isUnlocked) AppColors.SurfaceHigh else AppColors.SurfaceLow

    Canvas(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(surfaceColor)
            .border(1.dp, if (isUnlocked) AppColors.BorderFocus else AppColors.BorderSubtle, RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        val w = size.width
        val h = size.height

        when {
            minerId.contains("cpu") -> {
                // Central silicon die with corner pins
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.25f, h * 0.25f),
                    size = Size(w * 0.5f, h * 0.5f),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(color = accentColor, radius = 1.5.dp.toPx(), center = Offset(w * 0.15f, h * 0.15f))
                drawCircle(color = accentColor, radius = 1.5.dp.toPx(), center = Offset(w * 0.85f, h * 0.15f))
                drawCircle(color = accentColor, radius = 1.5.dp.toPx(), center = Offset(w * 0.15f, h * 0.85f))
                drawCircle(color = accentColor, radius = 1.5.dp.toPx(), center = Offset(w * 0.85f, h * 0.85f))
            }
            minerId.contains("gpu") -> {
                // Dual cooling fan circles
                drawCircle(color = accentColor, radius = w * 0.22f, center = Offset(w * 0.32f, h * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
                drawCircle(color = accentColor, radius = w * 0.22f, center = Offset(w * 0.68f, h * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
            }
            minerId.contains("asic") -> {
                // Server rack hashblade lines
                drawLine(color = accentColor, start = Offset(w * 0.15f, h * 0.3f), end = Offset(w * 0.85f, h * 0.3f), strokeWidth = 2.dp.toPx())
                drawLine(color = accentColor, start = Offset(w * 0.15f, h * 0.5f), end = Offset(w * 0.85f, h * 0.5f), strokeWidth = 2.dp.toPx())
                drawLine(color = accentColor, start = Offset(w * 0.15f, h * 0.7f), end = Offset(w * 0.85f, h * 0.7f), strokeWidth = 2.dp.toPx())
            }
            else -> {
                // Industrial facility chevron motif
                drawLine(color = accentColor, start = Offset(w * 0.2f, h * 0.7f), end = Offset(w * 0.5f, h * 0.3f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = accentColor, start = Offset(w * 0.5f, h * 0.3f), end = Offset(w * 0.8f, h * 0.7f), strokeWidth = 1.5.dp.toPx())
                drawCircle(color = accentColor, radius = 2.dp.toPx(), center = Offset(w * 0.5f, h * 0.55f))
            }
        }
    }
}
