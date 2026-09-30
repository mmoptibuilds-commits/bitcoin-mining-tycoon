package com.antigravity.bitcoinminingtycoon.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.components.AchievementBanner
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.MineScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel

@Composable
fun AppNavHost(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val prestigeSheetVisible by viewModel.prestigeSheetVisible.collectAsState()

    var activeTab by remember { mutableStateOf(RootTab.MINE) }
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.MainTabs) }

    // Predictive back handler for full-screen destinations
    BackHandler(enabled = currentDestination != AppDestination.MainTabs) {
        currentDestination = AppDestination.MainTabs
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentDestination) {
            AppDestination.MainTabs -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = AppColors.Background,
                    topBar = {
                        AppTopBar(
                            onSettingsClick = { currentDestination = AppDestination.Settings }
                        )
                    },
                    bottomBar = {
                        AppBottomNavBar(
                            selectedTab = activeTab,
                            onTabSelected = { activeTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            RootTab.MINE -> {
                                MineScreen(
                                    uiState = uiState,
                                    onMineClick = { viewModel.onManualMineTap() },
                                    onQuickSell = { percent -> viewModel.onQuickSell(percent) },
                                    onToggleAutoSell = { viewModel.onToggleAutoSell() },
                                    onSetAutoSellThreshold = { threshold -> viewModel.onSetAutoSellThreshold(threshold) },
                                    onClaimWindfall = { eventId -> viewModel.onClaimWindfall(eventId) },
                                    onDismissAchievement = { viewModel.onDismissAchievement() },
                                    onCollectOfflineReward = { viewModel.onCollectOfflineReward() },
                                    onShowDailyRewardSheet = { viewModel.onShowDailyRewardSheet() },
                                    onDismissDailyRewardSheet = { viewModel.onDismissDailyRewardSheet() },
                                    onClaimDailyReward = { viewModel.onClaimDailyReward() }
                                )
                            }
                            RootTab.HARDWARE -> {
                                com.antigravity.bitcoinminingtycoon.ui.screens.hardware.HardwareScreen(
                                    uiState = uiState,
                                    bulkMode = uiState.bulkMode,
                                    onBulkModeSelected = { mode -> viewModel.onSetBulkMode(mode) },
                                    onBuyMiner = { minerId -> viewModel.onBuyMiner(minerId) }
                                )
                            }
                            RootTab.UPGRADES -> {
                                com.antigravity.bitcoinminingtycoon.ui.screens.upgrades.UpgradesScreen(
                                    uiState = uiState,
                                    onBuyUpgrade = { id -> viewModel.onBuyUpgrade(id) },
                                    onUpgradePowerGrid = { viewModel.onUpgradePowerGrid() },
                                    onUpgradeCooling = { viewModel.onUpgradeCooling() },
                                    onNavigateToSatoshiTree = { currentDestination = AppDestination.SatoshiTree }
                                )
                            }
                            RootTab.STATS -> {
                                com.antigravity.bitcoinminingtycoon.ui.screens.stats.StatsScreen(
                                    uiState = uiState
                                )
                            }
                        }
                    }
                }
            }
            AppDestination.Settings -> {
                com.antigravity.bitcoinminingtycoon.ui.screens.settings.SettingsScreen(
                    settings = uiState.gameState.settings,
                    onBackClick = { currentDestination = AppDestination.MainTabs },
                    onUpdateSettings = { newSettings -> viewModel.onUpdateSettings(newSettings) },
                    onFactoryReset = {
                        viewModel.onFactoryReset()
                        currentDestination = AppDestination.MainTabs
                    }
                )
            }
            AppDestination.SatoshiTree -> {
                com.antigravity.bitcoinminingtycoon.ui.screens.prestige.SatoshiTreeScreen(
                    gameState = uiState.gameState,
                    onBackClick = { currentDestination = AppDestination.MainTabs },
                    onBuyNode = { nodeId -> viewModel.onBuyPrestigeNode(nodeId) },
                    onOpenPrestigeSheet = { viewModel.onShowPrestigeSheet() }
                )
            }
        }

        // Floating heads-up achievement notification overlay at top
        AchievementBanner(
            achievement = uiState.unlockedAchievement,
            onDismiss = { viewModel.onDismissAchievement() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        )

        if (prestigeSheetVisible && uiState.prestigePreview != null) {
            com.antigravity.bitcoinminingtycoon.ui.screens.prestige.PrestigeSheet(
                preview = uiState.prestigePreview!!,
                onConfirm = { viewModel.onConfirmPrestige() },
                onDismiss = { viewModel.onDismissPrestigeSheet() }
            )
        }

        if (!uiState.gameState.onboardingCompleted) {
            com.antigravity.bitcoinminingtycoon.ui.screens.onboarding.OnboardingDialog(
                onDismiss = { viewModel.onCompleteOnboarding() }
            )
        }
    }
}

@Composable
private fun AppTopBar(
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BITCOIN MINING TYCOON",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryCopper,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "OPERATIONS CONSOLE // V1.0",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextMedium
                )
            }

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.SurfaceHigh)
                    .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = onSettingsClick
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .semantics { contentDescription = "Open Settings and Facility Protocols" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙ PROTOCOLS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextHigh
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavBar(
    selectedTab: RootTab,
    onTabSelected: (RootTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RootTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val textColor = if (isSelected) AppColors.PrimaryCopper else AppColors.TextDisabled
                val bgColor = if (isSelected) AppColors.PrimaryCopperDark else Color.Transparent

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .background(bgColor)
                        .clickable(
                            role = Role.Tab,
                            onClick = { onTabSelected(tab) }
                        )
                        .semantics {
                            contentDescription = "${tab.title} tab, ${if (isSelected) "selected" else "not selected"}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title.uppercase(),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Subsystem online. Loading telemetry...",
                fontSize = 13.sp,
                color = AppColors.TextMedium
            )
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "< RETURN TO DASHBOARD",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextHigh
                    )
                }
            }
        }
    }
}
