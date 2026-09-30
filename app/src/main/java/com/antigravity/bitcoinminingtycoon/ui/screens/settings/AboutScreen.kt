package com.antigravity.bitcoinminingtycoon.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@Composable
fun AboutScreen(
    versionInfo: AppVersionInfo,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        "Version" to versionInfo.versionLabel,
        "Package" to versionInfo.packageName,
        "Minimum Android API" to versionInfo.minimumAndroidApi.toString(),
        "Build variant" to versionInfo.buildVariant
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().background(AppColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TycoonCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "BITCOIN MINING TYCOON",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    Text(
                        text = "Native offline facility-management simulation. All Bitcoin balances, market prices, rewards and mining output are fictional game data stored on this device.",
                        fontSize = 13.sp,
                        color = AppColors.TextMedium
                    )
                }
            }
        }
        item {
            TycoonCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (versionInfo.versionName.startsWith("1.2")) "V1.2 RELEASE NOTES" else "FEATURES IN THIS BUILD",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper
                    )
                    listOf(
                        "Mine Bitcoin, sell it into a simulated market, and reinvest cash in hardware.",
                        "Grow from desktop components through industrial, orbital, lunar and Dyson facilities.",
                        "Power, cooling and 56 grouped upgrades shape effective production.",
                        "Events, offline summaries, cumulative daily rewards and prestige are saved locally."
                    ).forEach { note ->
                        Text("• $note", fontSize = 12.sp, color = AppColors.TextMedium)
                    }
                }
            }
        }
        items(rows, key = { it.first }) { (label, value) ->
            TycoonCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(label, fontSize = 11.sp, color = AppColors.TextMedium)
                    Text(value, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = AppColors.TextHigh)
                }
            }
        }
    }
}
