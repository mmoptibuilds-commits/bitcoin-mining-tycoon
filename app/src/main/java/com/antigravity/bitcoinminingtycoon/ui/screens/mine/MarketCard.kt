package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.engine.MarketEngine
import com.antigravity.bitcoinminingtycoon.engine.UpgradeEngine
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.CanvasSparkline
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Compact sale controls live on Mine; the real history and automation controls expand on demand. */
@Composable
fun MarketCard(
    gameState: GameState,
    onQuickSell: (Int) -> Unit,
    onToggleAutoSell: () -> Unit,
    onSetAutoSellThreshold: suspend (BigDecimal) -> Boolean,
    modifier: Modifier = Modifier,
    onNavigateToUpgrades: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showThresholdEditor by remember { mutableStateOf(false) }
    var thresholdDraft by remember { mutableStateOf(gameState.autoSellThresholdUsd) }
    var thresholdError by remember { mutableStateOf<String?>(null) }
    var thresholdSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val format = gameState.settings.numberFormat
    val priceFormatted = NumberFormatter.formatUsd(gameState.marketPriceBigDecimal, format)
    val proceeds10 = MarketEngine.previewProceeds(gameState, 10)
    val proceeds50 = MarketEngine.previewProceeds(gameState, 50)
    val maxProceeds = MarketEngine.previewProceeds(gameState, 100)
    val proceeds10Text = NumberFormatter.formatUsd(proceeds10, format)
    val proceeds50Text = NumberFormatter.formatUsd(proceeds50, format)
    val maxProceedsText = NumberFormatter.formatUsd(maxProceeds, format)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Bitcoin market", color = AppColors.TextHigh, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Simulated price · $priceFormatted per Bitcoin", color = AppColors.TextMedium, fontSize = 12.sp)
                }
                Text(
                    text = if (expanded) "Less" else "Details",
                    color = AppColors.PrimaryCopper,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics {
                            contentDescription = if (expanded) "Hide market details" else "Show market details"
                        }
                        .clickable(role = Role.Button) { expanded = !expanded }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SaleAction(10, proceeds10Text, proceeds10, onQuickSell, Modifier.weight(1f))
                SaleAction(50, proceeds50Text, proceeds50, onQuickSell, Modifier.weight(1f))
                SaleAction(100, maxProceedsText, maxProceeds, onQuickSell, Modifier.weight(1.1f))
            }

            if (expanded) {
                val prices = remember(gameState.marketHistory) {
                    gameState.marketHistory.mapNotNull { it.toDoubleOrNull() }.ifEmpty { listOf(MarketEngine.INITIAL_PRICE.toDouble()) }
                }
                CanvasSparkline(
                    prices = prices,
                    trend = gameState.marketTrend,
                    currentPriceText = priceFormatted
                )

                Text(
                    text = "Price history from this save · ${gameState.marketTrend.name.lowercase().replace('_', ' ')} market",
                    color = AppColors.TextMedium,
                    fontSize = 12.sp
                )
                if (UpgradeEngine.canEnableAutoSell(gameState)) {
                    TycoonButton(
                        text = if (gameState.autoSellEnabled) "Turn off Auto-Sell" else "Turn on Auto-Sell",
                        onClick = onToggleAutoSell,
                        style = if (gameState.autoSellEnabled) ButtonStyle.SECONDARY else ButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        contentDescriptionText = if (gameState.autoSellEnabled) {
                            "Turn off Auto-Sell at ${gameState.autoSellThresholdUsd} dollars per Bitcoin"
                        } else {
                            "Turn on Auto-Sell at ${gameState.autoSellThresholdUsd} dollars per Bitcoin"
                        }
                    )
                    Text(
                        text = "Auto-Sell converts new mining output when Bitcoin reaches this price per Bitcoin.",
                        color = AppColors.TextMedium,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Current threshold: ${NumberFormatter.formatUsd(gameState.autoSellThresholdBigDecimal, format)} per Bitcoin",
                        color = AppColors.TextMedium,
                        fontSize = 12.sp
                    )
                    if (gameState.autoSellPendingBtcBigDecimal.signum() > 0) {
                        Text(
                            text = "Auto-Sell is holding mined Bitcoin until a sale reaches \$0.01. Turning Auto-Sell off returns it to your Bitcoin balance.",
                            color = AppColors.TextMedium,
                            fontSize = 12.sp
                        )
                    }
                    TycoonButton(
                        text = "Change threshold",
                        onClick = {
                            thresholdDraft = gameState.autoSellThresholdUsd
                            thresholdError = null
                            showThresholdEditor = true
                        },
                        style = ButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        contentDescriptionText = "Change Auto-Sell price threshold. Current threshold ${gameState.autoSellThresholdUsd} dollars per Bitcoin"
                    )
                } else {
                    Text(
                        text = "Auto-Sell converts new mining output at a price you choose.",
                        color = AppColors.TextMedium,
                        fontSize = 12.sp
                    )
                    TycoonButton(
                        text = "Unlock Auto-Sell in Upgrades",
                        onClick = onNavigateToUpgrades,
                        style = ButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showThresholdEditor) {
        AlertDialog(
            onDismissRequest = { if (!thresholdSaving) showThresholdEditor = false },
            title = { Text("Set Auto-Sell threshold") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Automatically sell new mined Bitcoin when its simulated price reaches your USD threshold.")
                    OutlinedTextField(
                        value = thresholdDraft,
                        onValueChange = { value ->
                            if (value.length <= MAX_THRESHOLD_INPUT_LENGTH) {
                                thresholdDraft = value
                                thresholdError = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(AUTO_SELL_THRESHOLD_INPUT_TAG),
                        label = { Text("USD per Bitcoin") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = thresholdError != null,
                        supportingText = {
                            Text(thresholdError ?: "Use a non-negative amount with up to 34 significant digits.")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !thresholdSaving,
                    onClick = {
                        val threshold = parseAutoSellThresholdInput(thresholdDraft)
                        if (threshold == null) {
                            thresholdError = "Enter a valid non-negative USD amount with up to 34 significant digits."
                        } else {
                            scope.launch {
                                thresholdSaving = true
                                val saved = try {
                                    onSetAutoSellThreshold(threshold)
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (_: Exception) {
                                    false
                                }
                                thresholdSaving = false
                                if (saved) {
                                    showThresholdEditor = false
                                } else {
                                    thresholdError = "Auto-Sell is locked or the threshold could not be saved. Try again."
                                }
                            }
                        }
                    }
                ) {
                    Text(if (thresholdSaving) "Saving…" else "Save threshold")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !thresholdSaving,
                    onClick = { showThresholdEditor = false }
                ) { Text("Cancel") }
            }
        )
    }
}

internal const val MAX_THRESHOLD_INPUT_LENGTH = 64
internal const val AUTO_SELL_THRESHOLD_INPUT_TAG = "autoSellThresholdInput"

private fun parseAutoSellThresholdInput(value: String): BigDecimal? {
    val input = value.trim()
    if (input.isEmpty() || input.length > MAX_THRESHOLD_INPUT_LENGTH) return null
    if (!AUTO_SELL_THRESHOLD_NUMBER.matches(input)) return null
    return runCatching { BigDecimal(input) }
        .getOrNull()
        ?.takeIf { it.signum() >= 0 && it.precision() <= 34 }
}

private val AUTO_SELL_THRESHOLD_NUMBER = Regex("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")

@Composable
private fun SaleAction(
    percentage: Int,
    proceedsText: String,
    proceeds: BigDecimal,
    onQuickSell: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TycoonButton(
            text = if (percentage >= 100) "Sell MAX" else "Sell $percentage%",
            onClick = { onQuickSell(percentage) },
            style = if (percentage >= 100) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
            enabled = proceeds.signum() > 0,
            modifier = Modifier.fillMaxWidth(),
            contentDescriptionText = "Sell ${if (percentage >= 100) "all" else "$percentage percent"} of mined Bitcoin for $proceedsText Cash"
        )
        Text(
            text = proceedsText,
            color = AppColors.TextMedium,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
