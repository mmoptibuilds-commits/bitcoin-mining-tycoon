package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

/** Shows only useful facility conditions, with the Upgrade destination beside each remedy. */
@Composable
fun EnvironmentalPanel(
    powerDemandKw: Double,
    powerCapacityKw: Double,
    powerFactor: Double,
    equilibriumTemp: Double,
    thermalFactor: Double,
    onNavigateToUpgrades: () -> Unit,
    modifier: Modifier = Modifier
) {
    val powerLimited = powerFactor < 0.999
    val coolingLimited = thermalFactor < 0.999
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Facility health", color = AppColors.TextHigh, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = "Power ${"%.2f".format(powerDemandKw)} / ${"%.1f".format(powerCapacityKw)} kW · Cooling ${"%.1f".format(equilibriumTemp)} °C",
                color = AppColors.TextMedium,
                fontSize = 12.sp,
                modifier = Modifier.semantics { heading() }
            )
            if (powerLimited) {
                RemedyRow(
                    message = "Power is reducing mining speed. Increase Power capacity or efficiency.",
                    button = "Upgrade Power",
                    onClick = onNavigateToUpgrades
                )
            }
            if (coolingLimited) {
                RemedyRow(
                    message = "Heat is reducing mining speed. Improve Cooling to bring output back up.",
                    button = "Upgrade Cooling",
                    onClick = onNavigateToUpgrades
                )
            }
            if (!powerLimited && !coolingLimited) {
                Text("Power and Cooling are keeping the machines at full output.", color = AppColors.PositiveGreen, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RemedyRow(message: String, button: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, modifier = Modifier.weight(1f), color = AppColors.WarningAmber, fontSize = 12.sp, lineHeight = 17.sp)
        TycoonButton(text = button, onClick = onClick, style = ButtonStyle.SECONDARY)
    }
}
