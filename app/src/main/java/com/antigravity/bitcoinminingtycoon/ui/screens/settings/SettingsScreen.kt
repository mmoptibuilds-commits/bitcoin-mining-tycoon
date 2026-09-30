package com.antigravity.bitcoinminingtycoon.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    batteryFriendlyAnimations: Boolean,
    onBackClick: () -> Unit,
    onUpdateSettings: (SettingsState) -> Unit,
    onBatteryFriendlyAnimationsChanged: (Boolean) -> Unit,
    onOpenAbout: () -> Unit,
    onFactoryReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FACILITY PROTOCOLS",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextHigh
                    )
                },
                navigationIcon = {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable(onClick = onBackClick)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Navigate back to facility dashboard"
                            }
                    ) {
                        Text(
                            text = "← BACK",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryCopper
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.SurfaceLow
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio & Sensory Section
            item {
                SectionHeader("SENSORY FEEDBACK")
            }

            item {
                TycoonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ToggleRow(
                            label = "Procedural Audio Synthesizer",
                            description = "Real-time algorithmic sound effects via AudioTrack",
                            checked = settings.soundEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                        )

                        ToggleRow(
                            label = "Haptic Vibrations",
                            description = "Tactile confirmation for mining taps and trades",
                            checked = settings.hapticsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) }
                        )

                        ToggleRow(
                            label = "Reduced Visual Motion",
                            description = "Minimize animations, transitions, and pulsing",
                            checked = settings.reducedMotion,
                            onCheckedChange = { onUpdateSettings(settings.copy(reducedMotion = it)) }
                        )

                        ToggleRow(
                            label = "Battery-friendly scene",
                            description = "Pause decorative scene motion and mining particles",
                            checked = batteryFriendlyAnimations,
                            onCheckedChange = onBatteryFriendlyAnimationsChanged
                        )
                    }
                }
            }

            // Numeric Display Section
            item {
                SectionHeader("NUMERIC FORMATTING")
            }

            item {
                TycoonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Engineering Unit Representation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextHigh
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isCompact = settings.numberFormat == NumberFormatPreference.COMPACT_SUFFIX
                            TycoonButton(
                                text = "COMPACT (1.25M)",
                                onClick = { onUpdateSettings(settings.copy(numberFormat = NumberFormatPreference.COMPACT_SUFFIX)) },
                                style = if (isCompact) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            TycoonButton(
                                text = "SCIENTIFIC (1.2e6)",
                                onClick = { onUpdateSettings(settings.copy(numberFormat = NumberFormatPreference.SCIENTIFIC)) },
                                style = if (!isCompact) ButtonStyle.PRIMARY else ButtonStyle.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Legal & Simulation Notice
            item {
                SectionHeader("LEGAL & SIMULATION DISCLOSURE")
            }

            item {
                TycoonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Pure Offline Native Android Simulation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextHigh
                        )
                        Text(
                            text = "Bitcoin Mining Tycoon contains no real cryptocurrency, mining algorithms, blockchain network integrations, cryptocurrency wallets, in-app purchases, or advertisements. All balances and market behaviors are simulated locally on this device.",
                            fontSize = 11.sp,
                            color = AppColors.TextMedium,
                            lineHeight = 16.sp
                        )
                        TycoonButton(
                            text = "ABOUT THIS APP",
                            onClick = onOpenAbout,
                            style = ButtonStyle.SECONDARY,
                            modifier = Modifier.fillMaxWidth(),
                            contentDescriptionText = "About Bitcoin Mining Tycoon"
                        )
                    }
                }
            }

            // Factory Reset Danger Zone
            item {
                SectionHeader("DANGER ZONE")
            }

            item {
                TycoonCard(
                    borderColor = AppColors.CriticalRed
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Wipe All Datacenter Save Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.CriticalRed
                        )
                        Text(
                            text = "Irreversibly erases all current and lifetime progression, prestige points, unlocked legacy nodes, and achievements. Returns datacenter to day one.",
                            fontSize = 12.sp,
                            color = AppColors.TextMedium
                        )
                        TycoonButton(
                            text = "FACTORY RESET FACILITY",
                            onClick = { showResetDialog = true },
                            style = ButtonStyle.SECONDARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        BasicAlertDialog(onDismissRequest = { showResetDialog = false }) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = AppColors.Background,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.CriticalRed)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CONFIRM COMPLETE WIPE",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.CriticalRed
                    )
                    Text(
                        text = "This erases balances, machines, upgrades, events, statistics, achievements, prestige points and settings from this device. No real currency or account is involved. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = AppColors.TextHigh
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TycoonButton(
                            text = "CANCEL",
                            onClick = { showResetDialog = false },
                            style = ButtonStyle.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                        TycoonButton(
                            text = "YES, WIPE",
                            onClick = {
                                showResetDialog = false
                                onFactoryReset()
                            },
                            style = ButtonStyle.PRIMARY,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = AppColors.PrimaryCopper,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextHigh
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = AppColors.TextMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                role = Role.Switch
                contentDescription = "$label. $description"
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.Background,
                checkedTrackColor = AppColors.PrimaryCopper,
                uncheckedThumbColor = AppColors.TextDisabled,
                uncheckedTrackColor = AppColors.SurfaceLow
            )
        )
    }
}
