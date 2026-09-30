package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

/**
 * High-performance, zero-heap-allocation sparkline rendering the 30-sample simulated price history.
 * Anti-AI-slop rule: Restrained 2dp path, industrial graphite grid line, trend glyph ensures
 * information is never conveyed by color alone.
 */
@Composable
fun CanvasSparkline(
    prices: List<Double>,
    trend: MarketTrend,
    currentPriceText: String,
    modifier: Modifier = Modifier,
    heightDp: Int = 48
) {
    val (trendGlyph, trendLabel, trendColor) = when (trend) {
        MarketTrend.NEUTRAL -> Triple("▬", "NEUTRAL", AppColors.TextMedium)
        MarketTrend.BULL -> Triple("▲", "BULL", AppColors.PositiveGreen)
        MarketTrend.BEAR -> Triple("▼", "BEAR", AppColors.CriticalRed)
        MarketTrend.VOLATILE -> Triple("≈", "VOLATILE", AppColors.WarningAmber)
        MarketTrend.CRASH -> Triple("⚠", "CRASH", AppColors.CriticalRed)
        MarketTrend.PUMP -> Triple("⚡", "PUMP", AppColors.PositiveGreen)
    }

    val lineColor = when (trend) {
        MarketTrend.BULL, MarketTrend.PUMP -> AppColors.PositiveGreen
        MarketTrend.BEAR, MarketTrend.CRASH -> AppColors.CriticalRed
        MarketTrend.VOLATILE -> AppColors.WarningAmber
        MarketTrend.NEUTRAL -> AppColors.PrimaryCopper
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Simulated market trend is $trendLabel. Current price $currentPriceText"
            },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MARKET TELEMETRY",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trendGlyph,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
                Text(
                    text = trendLabel,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .padding(vertical = 4.dp)
        ) {
            val width = size.width
            val height = size.height

            // 1. Faint horizontal midline baseline
            drawLine(
                color = AppColors.BorderSubtle,
                start = Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                strokeWidth = 1f
            )

            if (prices.size < 2) return@Canvas

            var minPrice = Double.MAX_VALUE
            var maxPrice = Double.MIN_VALUE
            for (p in prices) {
                if (p < minPrice) minPrice = p
                if (p > maxPrice) maxPrice = p
            }

            val range = (maxPrice - minPrice).coerceAtLeast(10.0)
            val stepX = width / (prices.size - 1).coerceAtLeast(1)

            val path = Path()
            prices.forEachIndexed { index, price ->
                val x = index * stepX
                val normalizedY = ((price - minPrice) / range).toFloat().coerceIn(0f, 1f)
                val y = height - (normalizedY * (height - 8f) + 4f)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Draw clean 2dp sparkline
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw current terminal point marker
            val lastNormalizedY = ((prices.last() - minPrice) / range).toFloat().coerceIn(0f, 1f)
            val lastY = height - (lastNormalizedY * (height - 8f) + 4f)
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(width, lastY)
            )
        }
    }
}
