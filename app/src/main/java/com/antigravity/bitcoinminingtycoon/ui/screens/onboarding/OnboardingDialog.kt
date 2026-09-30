package com.antigravity.bitcoinminingtycoon.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = {}, // Non-cancellable without explicit initialization acknowledgment
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.Background,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.PrimaryCopper)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Simulation Initialization Protocol. This application is a strictly fictional single-player offline simulation. No real cryptocurrency, mining, or blockchain operations occur."
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "INITIALIZATION PROTOCOL",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryCopper
                )

                Text(
                    text = "Welcome to Bitcoin Mining Tycoon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextHigh
                )

                TycoonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SIMULATION NOTICE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.WarningAmber
                        )
                        Text(
                            text = "• This application is an entirely offline, fictional clicker game simulation.",
                            fontSize = 12.sp,
                            color = AppColors.TextMedium
                        )
                        Text(
                            text = "• No real cryptocurrencies, blockchain networks, private keys, or actual hardware mining are used.",
                            fontSize = 12.sp,
                            color = AppColors.TextMedium
                        )
                        Text(
                            text = "• Zero internet connectivity, tracking, analytics, or external ads are required or included.",
                            fontSize = 12.sp,
                            color = AppColors.TextMedium
                        )
                    }
                }

                TycoonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "3-STEP OPERATIONS LOOP",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryCopper
                        )
                        Text(
                            text = "1. TAP CORE — Hit the central MINE button to generate hashes and accumulate BTC.",
                            fontSize = 12.sp,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = "2. SELL ON SPOT — Liquidate BTC on the simulated market card for USD Cash.",
                            fontSize = 12.sp,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = "3. DEPLOY RIGS — Buy automated Hardware & Tech Upgrades to mine 24/7 passively.",
                            fontSize = 12.sp,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = "★ Starter Seed Grant: $15.00 USD credited to your Cash Reserve.",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryCopperHover
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TycoonButton(
                    text = "INITIALIZE DATACENTER",
                    onClick = onDismiss,
                    style = ButtonStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
