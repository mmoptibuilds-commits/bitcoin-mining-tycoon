package com.antigravity.bitcoinminingtycoon.ui.screens.prestige

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.antigravity.bitcoinminingtycoon.content.PrestigeNodeDefinition
import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatoshiTreeScreen(
    gameState: GameState,
    onBackClick: () -> Unit,
    onBuyNode: (String) -> Unit,
    onOpenPrestigeSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SATOSHI LEGACY TREE",
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
                                contentDescription = "Navigate back to previous screen"
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
                actions = {
                    Text(
                        text = "${gameState.satoshiPoints} SP",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopperHover,
                        modifier = Modifier.padding(end = 16.dp)
                    )
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
            // Header: Satoshi Points & Reset Entry
            item {
                TycoonCard(
                    borderColor = AppColors.PrimaryCopper
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AVAILABLE SATOSHI POINTS",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryCopper
                                )
                                Text(
                                    text = "${gameState.satoshiPoints} SP",
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextHigh
                                )
                            }
                            Text(
                                text = "Bonus: +${gameState.satoshiPoints}%",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PositiveGreen
                            )
                        }

                        TycoonButton(
                            text = "OPEN PRESTIGE RESET PORTAL",
                            onClick = onOpenPrestigeSheet,
                            style = ButtonStyle.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Group by Tier 1 to 4
            val groupedByTier = PrestigeNodes.ALL.groupBy { it.tier }
            groupedByTier.forEach { (tier, nodes) ->
                val tierName = when (tier) {
                    1 -> "TIER 1: FOUNDATIONS"
                    2 -> "TIER 2: EXPANSION"
                    3 -> "TIER 3: MASTERY"
                    else -> "TIER 4: SINGULARITY"
                }

                item {
                    Text(
                        text = tierName,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryCopper,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(nodes, key = { it.id }) { node ->
                    PrestigeNodeCard(
                        node = node,
                        isOwned = node.id in gameState.purchasedPrestigeNodes,
                        prereqsMet = node.prerequisiteNodeIds.all { it in gameState.purchasedPrestigeNodes },
                        availableSp = gameState.satoshiPoints,
                        onBuy = { onBuyNode(node.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrestigeNodeCard(
    node: PrestigeNodeDefinition,
    isOwned: Boolean,
    prereqsMet: Boolean,
    availableSp: Long,
    onBuy: () -> Unit
) {
    val canAfford = availableSp >= node.costSp
    val canPurchase = !isOwned && prereqsMet && canAfford

    val borderColor = when {
        isOwned -> AppColors.PositiveGreen
        canPurchase -> AppColors.PrimaryCopper
        else -> AppColors.BorderSubtle
    }

    TycoonCard(
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwned) AppColors.TextHigh else if (prereqsMet) AppColors.TextHigh else AppColors.TextDisabled
                )
                Text(
                    text = "${node.costSp} SP",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwned) AppColors.PositiveGreen else AppColors.PrimaryCopper
                )
            }

            Text(
                text = node.description,
                fontSize = 12.sp,
                color = if (isOwned || prereqsMet) AppColors.TextMedium else AppColors.TextDisabled
            )

            if (node.prerequisiteNodeIds.isNotEmpty()) {
                val prereqNames = node.prerequisiteNodeIds.joinToString(", ") {
                    PrestigeNodes.getById(it)?.title ?: it
                }
                Text(
                    text = "Requires: $prereqNames",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (prereqsMet) AppColors.TextDisabled else AppColors.WarningAmber
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            when {
                isOwned -> {
                    Text(
                        text = "[PERMANENTLY UNLOCKED]",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PositiveGreen
                    )
                }
                !prereqsMet -> {
                    Text(
                        text = "[PREREQUISITES UNMET]",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AppColors.TextDisabled
                    )
                }
                else -> {
                    TycoonButton(
                        text = if (canAfford) "UNLOCK FOR ${node.costSp} SP" else "INSUFFICIENT SP (NEED ${node.costSp} SP)",
                        onClick = onBuy,
                        enabled = canAfford,
                        style = ButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
