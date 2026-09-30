package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.antigravity.bitcoinminingtycoon.engine.MarketEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.CanvasSparkline
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import java.math.BigDecimal

/**
 * Industrial market control panel for simulated Bitcoin trading and liquidation.
 * Anti-AI-slop rule: Dense tabular data, 8dp/10dp radii, clear hierarchy, zero emoji.
 */
@Composable
fun MarketCard(
    gameState: GameState,
    onQuickSell: (Int) -> Unit,
    onToggleAutoSell: () -> Unit,
    onSetAutoSellThreshold: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val prices = remember(gameState.marketHistory) {
        gameState.marketHistory.mapNotNull { it.toDoubleOrNull() }.ifEmpty { listOf(50000.0) }
    }

    val priceFormatted = remember(gameState.marketPrice, gameState.settings.numberFormat) {
        NumberFormatter.formatUsd(gameState.marketPriceBigDecimal, gameState.settings.numberFormat)
    }

    val btcAmount = gameState.btcBigDecimal
    val proceeds10 = remember(btcAmount, gameState.marketPriceBigDecimal) {
        MarketEngine.calculateProceeds(btcAmount, gameState.marketPriceBigDecimal, 10)
    }
    val proceeds10Formatted = NumberFormatter.formatUsd(proceeds10, gameState.settings.numberFormat)

    val proceeds50 = remember(btcAmount, gameState.marketPriceBigDecimal) {
        MarketEngine.calculateProceeds(btcAmount, gameState.marketPriceBigDecimal, 50)
    }
    val proceeds50Formatted = NumberFormatter.formatUsd(proceeds50, gameState.settings.numberFormat)

    val maxProceeds = remember(btcAmount, gameState.marketPriceBigDecimal) {
        MarketEngine.calculateProceeds(btcAmount, gameState.marketPriceBigDecimal, 100)
    }
    val maxProceedsFormatted = NumberFormatter.formatUsd(maxProceeds, gameState.settings.numberFormat)

    TycoonCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Current Price & Market Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIMULATED BTC SPOT",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextMedium
                    )
                    Text(
                        text = "$priceFormatted / BTC",
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextHigh
                    )
                }

                // Auto-sell status chip
                val autoSellBorderColor = if (gameState.autoSellEnabled) AppColors.PrimaryCopper else AppColors.BorderSubtle
                val autoSellBgColor = if (gameState.autoSellEnabled) AppColors.PrimaryCopperDark else AppColors.SurfaceLow
                val autoSellTextColor = if (gameState.autoSellEnabled) AppColors.PrimaryCopperHover else AppColors.TextMedium

                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(autoSellBgColor)
                        .border(1.dp, autoSellBorderColor, RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Switch,
                            onClickLabel = "Toggle Auto-Sell"
                        ) {
                            onToggleAutoSell()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (gameState.autoSellEnabled) "AUTO-SELL: ON" else "AUTO-SELL: OFF",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = autoSellTextColor
                        )
                        Text(
                            text = "≥ $${gameState.autoSellThresholdUsd}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AppColors.TextMedium
                        )
                    }
                }
            }

            // 2. Rolling 30-sample Canvas Sparkline
            CanvasSparkline(
                prices = prices,
                trend = gameState.marketTrend,
                currentPriceText = priceFormatted
            )

            // 3. Quick Sell Liquidation Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TycoonButton(
                    text = if (proceeds10 > BigDecimal.ZERO) "10% ($proceeds10Formatted)" else "Sell 10%",
                    onClick = { onQuickSell(10) },
                    style = ButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    contentDescriptionText = "Sell 10 percent of mined Bitcoin for estimated $proceeds10Formatted"
                )
                TycoonButton(
                    text = if (proceeds50 > BigDecimal.ZERO) "50% ($proceeds50Formatted)" else "Sell 50%",
                    onClick = { onQuickSell(50) },
                    style = ButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    contentDescriptionText = "Sell 50 percent of mined Bitcoin for estimated $proceeds50Formatted"
                )
                TycoonButton(
                    text = if (maxProceeds > BigDecimal.ZERO) "MAX ($maxProceedsFormatted)" else "Sell MAX",
                    onClick = { onQuickSell(100) },
                    style = ButtonStyle.PRIMARY,
                    modifier = Modifier.weight(1.3f),
                    contentDescriptionText = "Sell all available Bitcoin for estimated $maxProceedsFormatted"
                )
            }
        }
    }
}
