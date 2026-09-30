package com.antigravity.bitcoinminingtycoon.ui.navigation

import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.antigravity.bitcoinminingtycoon.data.SaveReadiness
import com.antigravity.bitcoinminingtycoon.ui.components.AchievementBanner
import com.antigravity.bitcoinminingtycoon.ui.screens.settings.AppVersionInfo
import com.antigravity.bitcoinminingtycoon.ui.screens.SaveReadinessScreen
import com.antigravity.bitcoinminingtycoon.ui.screens.mine.MineScreen
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.viewmodel.GameUiState
import com.antigravity.bitcoinminingtycoon.viewmodel.GameViewModel

@Composable
fun AppNavHost(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersionInfo = remember(context) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        AppVersionInfo(
            packageName = context.packageName,
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = packageInfo.longVersionCode,
            minimumAndroidApi = appInfo.minSdkVersion,
            buildVariant = if (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) "debug" else "release"
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val prestigeSheetVisible by viewModel.prestigeSheetVisible.collectAsState()
    if (uiState.saveReadiness != SaveReadiness.Ready) {
        SaveReadinessScreen(
            readiness = uiState.saveReadiness,
            onStartNewSave = { viewModel.onStartNewSaveFromCheckpoint() },
            onRetry = { viewModel.onRetrySaveLoading() }
        )
        return
    }

    val backStack: NavBackStack<NavKey> = rememberNavBackStack(AppDestination.Home)
    val entryStateHolder = rememberSaveableStateHolder()
    val entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>(entryStateHolder))

    Box(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryDecorators = entryDecorators,
            entryProvider = entryProvider {
                entry<AppDestination.Home> {
                    HomeDestination(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenStats = { backStack.add(AppDestination.Stats) },
                        onOpenSettings = { backStack.add(AppDestination.Settings) },
                        onOpenSatoshiTree = { backStack.add(AppDestination.SatoshiTree) }
                    )
                }
                entry<AppDestination.Stats> {
                    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
                        DetailTopBar(title = "Stats", onBack = { popBackStack(backStack) })
                        com.antigravity.bitcoinminingtycoon.ui.screens.stats.StatsScreen(
                            uiState = uiState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                entry<AppDestination.Settings> {
                    com.antigravity.bitcoinminingtycoon.ui.screens.settings.SettingsScreen(
                        settings = uiState.gameState.settings,
                        batteryFriendlyAnimations = uiState.gameState.batteryFriendlyAnimations,
                        onBackClick = { popBackStack(backStack) },
                        onUpdateSettings = viewModel::onUpdateSettings,
                        onBatteryFriendlyAnimationsChanged = viewModel::onSetBatteryFriendlyAnimations,
                        onOpenAbout = { backStack.add(AppDestination.About) },
                        onFactoryReset = {
                            viewModel.onFactoryReset()
                            while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        }
                    )
                }
                entry<AppDestination.About> {
                    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
                        DetailTopBar(title = "About", onBack = { popBackStack(backStack) })
                        com.antigravity.bitcoinminingtycoon.ui.screens.settings.AboutScreen(
                            versionInfo = appVersionInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                entry<AppDestination.SatoshiTree> {
                    com.antigravity.bitcoinminingtycoon.ui.screens.prestige.SatoshiTreeScreen(
                        gameState = uiState.gameState,
                        onBackClick = { popBackStack(backStack) },
                        onBuyNode = viewModel::onBuyPrestigeNode,
                        onOpenPrestigeSheet = viewModel::onShowPrestigeSheet
                    )
                }
            }
        )

        AchievementBanner(
            achievement = uiState.unlockedAchievement,
            onDismiss = viewModel::onDismissAchievement,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 64.dp, start = 16.dp, end = 16.dp)
        )

        if (prestigeSheetVisible && uiState.prestigePreview != null) {
            com.antigravity.bitcoinminingtycoon.ui.screens.prestige.PrestigeSheet(
                preview = uiState.prestigePreview!!,
                onConfirm = viewModel::onConfirmPrestige,
                onDismiss = viewModel::onDismissPrestigeSheet
            )
        }
    }
}

@Composable
private fun HomeDestination(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSatoshiTree: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(RootTab.MINE) }
    val tabStateHolder = rememberSaveableStateHolder()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = AppColors.Background,
        topBar = {
            AppTopBar(onStatsClick = onOpenStats, onSettingsClick = onOpenSettings)
        },
        bottomBar = {
            AppBottomNavBar(selectedTab = activeTab, onTabSelected = { activeTab = it })
        }
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            tabStateHolder.SaveableStateProvider(activeTab.name) {
                when (activeTab) {
                    RootTab.MINE -> MineScreen(
                        uiState = uiState,
                        onMineClick = viewModel::onManualMineTap,
                        onQuickSell = viewModel::onQuickSell,
                        onToggleAutoSell = viewModel::onToggleAutoSell,
                        onSetAutoSellThreshold = viewModel::onSetAutoSellThreshold,
                        onClaimWindfall = viewModel::onClaimWindfall,
                        onDismissAchievement = viewModel::onDismissAchievement,
                        onCollectOfflineReward = viewModel::onCollectOfflineReward,
                        onShowDailyRewardSheet = viewModel::onShowDailyRewardSheet,
                        onDismissDailyRewardSheet = viewModel::onDismissDailyRewardSheet,
                        onClaimDailyReward = viewModel::onClaimDailyReward,
                        onCompleteTeachingCue = viewModel::onCompleteTeachingCue,
                        onNavigateToHardware = { activeTab = RootTab.HARDWARE },
                        onNavigateToUpgrades = { activeTab = RootTab.UPGRADES },
                        feedbackEvents = viewModel.gameplayFeedback
                    )
                    RootTab.HARDWARE -> com.antigravity.bitcoinminingtycoon.ui.screens.hardware.HardwareScreen(
                        uiState = uiState,
                        bulkMode = uiState.bulkMode,
                        onBulkModeSelected = viewModel::onSetBulkMode,
                        onBuyMiner = viewModel::onBuyMiner,
                        onOpenUpgrades = { activeTab = RootTab.UPGRADES },
                        onOpenMine = { activeTab = RootTab.MINE }
                    )
                    RootTab.UPGRADES -> com.antigravity.bitcoinminingtycoon.ui.screens.upgrades.UpgradesScreen(
                        uiState = uiState,
                        onBuyUpgrade = viewModel::onBuyUpgrade,
                        onUpgradePowerGrid = viewModel::onUpgradePowerGrid,
                        onUpgradeCooling = viewModel::onUpgradeCooling,
                        onNavigateToSatoshiTree = onOpenSatoshiTree,
                        onNavigateToMine = { activeTab = RootTab.MINE }
                    )
                }
            }
        }
    }
}

private fun popBackStack(backStack: NavBackStack<NavKey>) {
    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
}

@Composable
private fun AppTopBar(onStatsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Surface(color = AppColors.SurfaceLow, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(58.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Bitcoin Mining Tycoon", color = AppColors.TextHigh, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Offline mining simulation", color = AppColors.TextMedium, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                HeaderAction("Stats", onStatsClick)
                HeaderAction("Settings", onSettingsClick)
            }
        }
    }
}

@Composable
private fun DetailTopBar(title: String, onBack: () -> Unit) {
    Surface(color = AppColors.SurfaceLow, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(58.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderAction("Back", onBack)
            Text(title, color = AppColors.TextHigh, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

@Composable
private fun HeaderAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(AppColors.SurfaceHigh, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = AppColors.TextHigh, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AppBottomNavBar(selectedTab: RootTab, onTabSelected: (RootTab) -> Unit) {
    Surface(color = AppColors.SurfaceLow, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RootTab.values().forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize().defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .background(if (selected) AppColors.PrimaryCopperDark else AppColors.SurfaceLow)
                        .clickable(role = Role.Tab, onClick = { onTabSelected(tab) })
                        .semantics {
                            contentDescription = "${tab.title} tab"
                            this.selected = selected
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab.title, color = if (selected) AppColors.PrimaryCopper else AppColors.TextMedium, fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}
