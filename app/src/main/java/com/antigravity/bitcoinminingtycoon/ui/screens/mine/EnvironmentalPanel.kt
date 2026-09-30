package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

/**
 * Environmental Telemetry control panel displaying Grid Load and Core Thermal Equilibrium.
 * Anti-AI-slop rule: Restrained gauges, dual status channels (color + text tags), clear tabular metrics.
 */
@Composable
fun EnvironmentalPanel(
    powerDemandKw: Double,
    powerCapacityKw: Double,
    powerFactor: Double,
    equilibriumTemp: Double,
    thermalFactor: Double,
    modifier: Modifier = Modifier
) {
    val powerStatus = if (powerFactor >= 1.0) "[OPTIMAL 100%]" else "[DEFICIT ${(powerFactor * 100).toInt()}%]"
    val powerColor = if (powerFactor >= 1.0) AppColors.PositiveGreen else AppColors.WarningAmber
    val powerFillRatio = if (powerCapacityKw > 0) (powerDemandKw / powerCapacityKw).toFloat().coerceIn(0f, 1f) else 1f

    val (thermalStatus, thermalColor) = when {
        equilibriumTemp < 70.0 -> Pair("[OPTIMAL 100%]", AppColors.PositiveGreen)
        equilibriumTemp < 80.0 -> Pair("[ELEVATED ${(thermalFactor * 100).toInt()}%]", AppColors.WarningAmber)
        equilibriumTemp < 90.0 -> Pair("[HOT ${(thermalFactor * 100).toInt()}%]", AppColors.WarningAmber)
        else -> Pair("[CRITICAL ${(thermalFactor * 100).toInt()}%]", AppColors.CriticalRed)
    }
    val tempFillRatio = ((equilibriumTemp - 25.0) / 75.0).toFloat().coerceIn(0f, 1f)

    TycoonCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Environmental telemetry: Grid load ${String.format("%.2f", powerDemandKw)} of ${String.format("%.1f", powerCapacityKw)} kilowatts, status is $powerStatus. Core temperature is ${String.format("%.1f", equilibriumTemp)} degrees Celsius, status is $thermalStatus."
                },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ENVIRONMENTAL TELEMETRY",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextMedium
            )

            // 1. Grid Power Channel
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "GRID LOAD", fontSize = 11.sp, color = AppColors.TextMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%.2f / %.1f kW", powerDemandKw, powerCapacityKw),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = powerStatus,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = powerColor
                        )
                    }
                }

                // Power Gauge Meter Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppColors.SurfaceLow)
                        .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(powerFillRatio)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(powerColor)
                    )
                }
            }

            // 2. Core Thermal Channel
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CORE TEMP", fontSize = 11.sp, color = AppColors.TextMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%.1f °C", equilibriumTemp),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = thermalStatus,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = thermalColor
                        )
                    }
                }

                // Thermal Gauge Meter Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppColors.SurfaceLow)
                        .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(tempFillRatio)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(thermalColor)
                    )
                }
            }
        }
    }
}
