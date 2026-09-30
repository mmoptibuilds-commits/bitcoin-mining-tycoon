package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.content.Miners
import com.antigravity.bitcoinminingtycoon.content.Upgrades
import com.antigravity.bitcoinminingtycoon.model.GameState
import java.math.BigDecimal
import java.math.MathContext

/**
 * Test-only reference player that exercises the real gameplay transitions. Its policy and
 * deterministic tie break are deliberately explicit so pacing results can be reproduced.
 */
internal object ReferencePolicySimulator {
    const val STEP_SECONDS = 5
    const val FIRST_RUN_LIMIT_SECONDS = 90
    const val BURST_END_SECONDS = 600
    const val MAX_RUN_SECONDS = 7_200
    private const val START_WALL_MILLIS = 1_800_000_000_000L
    private const val SIMULATION_HORIZON_SECONDS = MAX_RUN_SECONDS.toDouble()
    private val HASH_TO_BTC = BigDecimal(BalanceConfig.BTC_PER_HASH)

    data class Candidate(
        val id: String,
        val cost: BigDecimal,
        val after: GameState,
        val expectedGain: BigDecimal,
        val score: BigDecimal
    )

    data class Result(
        val seed: Long,
        val balanceHash: String,
        val durationSeconds: Int,
        val firstSaleSeconds: Int?,
        val firstMachineSeconds: Int?,
        val firstGpuSeconds: Int?,
        val infrastructureMattersSeconds: Int?,
        val firstAsicSeconds: Int?,
        val firstPrestigeSeconds: Int?,
        val manualBtcAtMinuteFive: BigDecimal,
        val passiveBtcAtMinuteFive: BigDecimal,
        val manualBtc: BigDecimal,
        val passiveBtc: BigDecimal,
        val simulatedPrestigeCycles: Int,
        val finalState: GameState,
        val purchases: List<String>,
        val salePrices: List<String>
    ) {
        fun trace(): String = buildString {
            appendLine("seed=$seed balanceHash=$balanceHash duration=${durationSeconds}s")
            appendLine("sale=$firstSaleSeconds machine=$firstMachineSeconds gpu=$firstGpuSeconds infra=$infrastructureMattersSeconds asic=$firstAsicSeconds prestige=$firstPrestigeSeconds")
            appendLine("manualBtc=$manualBtc passiveBtc=$passiveBtc")
            appendLine("simulatedPrestigeCycles=$simulatedPrestigeCycles")
            appendLine("lifetimeBtc=${finalState.stats.lifetimeBtcMined} prestigeBaseline=${finalState.stats.prestigePointsBaselineV2} pointsEarnedSinceV2=${finalState.stats.prestigePointsEarnedSinceV2} totalPrestiges=${finalState.stats.totalPrestiges}")
            appendLine("minute5ManualBtc=$manualBtcAtMinuteFive minute5PassiveBtc=$passiveBtcAtMinuteFive")
            purchases.forEach(::appendLine)
        }
    }

    fun simulate(
        seed: Long,
        initialState: GameState = GameState(rngSeed = seed),
        randomMarket: Boolean = false,
        randomEvents: Boolean = false,
        claimWindfalls: Boolean = false,
        claimFirstDailyReward: Boolean = false,
        stopAtFirstPrestige: Boolean = true,
        maxDurationSeconds: Int = MAX_RUN_SECONDS,
        tapIntervalHalfSeconds: Int = 1,
        sellIntervalSeconds: Int = 15,
        marketHolderMinimumUsd: BigDecimal? = null,
        infrastructureAware: Boolean = false,
        prestigeCyclesToSimulate: Int = 1
    ): Result {
        var state = initialState.copy(
            usd = initialState.usd,
            marketPrice = BalanceConfig.MARKET_INITIAL_USD,
            marketHistory = listOf(BalanceConfig.MARKET_INITIAL_USD),
            eventTimerSeconds = if (randomEvents) BalanceConfig.EVENT_INITIAL_TIMER_SECONDS else 1_000_000.0,
            rngSeed = seed
        )
        var firstSale: Int? = null
        var firstMachine: Int? = null
        var firstGpu: Int? = null
        var infrastructureMatters: Int? = null
        var firstAsic: Int? = null
        var firstPrestige: Int? = null
        var manualBtcAtMinuteFive = BigDecimal.ZERO
        var passiveBtcAtMinuteFive = BigDecimal.ZERO
        val purchases = mutableListOf<String>()
        val salePrices = mutableListOf<String>()
        var elapsed = 0
        var simulatedPrestigeCycles = 0
        var cycleStartSeconds = 0

        while (elapsed < maxDurationSeconds) {
            val next = elapsed + STEP_SECONDS
            val cycleElapsed = elapsed - cycleStartSeconds
            val cycleNext = next - cycleStartSeconds
            val wall = START_WALL_MILLIS + next * 1_000L
            state = GameEngine.tick(state, STEP_SECONDS.toDouble(), wall)
            if (!randomMarket) state = state.copy(marketPrice = BalanceConfig.MARKET_INITIAL_USD)

            tapCountInWindow(cycleElapsed, cycleNext, tapIntervalHalfSeconds).let { count -> repeat(count) { state = GameEngine.performManualTap(state) } }
            if (claimFirstDailyReward && next >= 30 && state.lastDailyClaimWallMillis == 0L && state.miners.values.any { it > 0L }) {
                state = com.antigravity.bitcoinminingtycoon.content.DailyRewards.claim(state, wall).first
            }
            if (claimWindfalls) {
                state.activeEvents.filter { it.isWindfall }.map { it.eventId }.forEach { eventId ->
                    state = EventEngine.claimWindfall(state, eventId, wall).first
                }
            }

            val shouldSave = shouldSaveForHigherValuePurchase(state, cycleNext, tapIntervalHalfSeconds, infrastructureAware)
            val marketAllowsSale = marketHolderMinimumUsd == null || state.marketPriceBigDecimal >= marketHolderMinimumUsd
            if (cycleNext % sellIntervalSeconds.coerceAtLeast(STEP_SECONDS) == 0 && marketAllowsSale && state.btcBigDecimal.signum() > 0) {
                val investmentTarget = if (shouldSave) {
                    bestInvestment(state, cycleNext, infrastructureAware, tapIntervalHalfSeconds)
                } else {
                    bestCandidate(state, cycleNext, infrastructureAware, tapIntervalHalfSeconds)
                }
                if (investmentTarget != null && state.usdBigDecimal < investmentTarget.cost) {
                    val cashPlusBtc = state.usdBigDecimal.add(
                        state.btcBigDecimal.multiply(state.marketPriceBigDecimal, MathContext.DECIMAL128),
                        MathContext.DECIMAL128
                    )
                    if (cashPlusBtc >= investmentTarget.cost) {
                        val beforeSold = state.stats.totalBtcSoldBigDecimal
                        val salePrice = state.marketPrice
                        state = MarketEngine.sellBtc(state, 100)
                        if (state.stats.totalBtcSoldBigDecimal > beforeSold) {
                            if (firstSale == null) firstSale = next
                            salePrices += salePrice
                        }
                    }
                }
            }

            val selected = if (shouldSaveForHigherValuePurchase(state, cycleNext, tapIntervalHalfSeconds, infrastructureAware)) {
                null
            } else {
                bestCandidate(state, cycleNext, infrastructureAware, tapIntervalHalfSeconds)
            }
            if (selected != null) {
                val beforePower = PowerEngine.calculatePowerFactor(state)
                val beforeThermal = EconomyEngine.calculateThermalState(state).second
                state = when {
                    selected.id.startsWith("miner:") -> FleetEngine.buyMiner(
                        state, selected.id.removePrefix("miner:"), BulkMode.X1
                    )
                    selected.id.startsWith("upgrade:") -> UpgradeEngine.buyUpgrade(
                        state, selected.id.removePrefix("upgrade:")
                    )
                    selected.id.startsWith("power:") -> PowerEngine.upgradePowerGrid(state)
                    selected.id.startsWith("cooling:") -> ThermalEngine.upgradeCooling(state)
                    else -> state
                }
                val ownedIds = state.miners.filterValues { it > 0L }.keys
                if (firstMachine == null && ownedIds.isNotEmpty()) firstMachine = next
                if (firstGpu == null && "gaming_gpu" in ownedIds) firstGpu = next
                if (firstAsic == null && ownedIds.any { id ->
                        id in setOf("entry_asic", "industrial_asic", "asic_rack")
                    }) firstAsic = next
                if (infrastructureMatters == null &&
                    ((selected.id.startsWith("power:") && PowerEngine.calculatePowerFactor(state) > beforePower) ||
                        (selected.id.startsWith("cooling:") && EconomyEngine.calculateThermalState(state).second > beforeThermal))) {
                    infrastructureMatters = next
                }
                val detail = when {
                    selected.id.startsWith("miner:") -> selected.id.removePrefix("miner:")
                    selected.id.startsWith("upgrade:") -> selected.id.removePrefix("upgrade:")
                    else -> selected.id
                }
                purchases += "%04ds BUY %s cost=$%s rate=%s".format(
                    next, detail, selected.cost.stripTrailingZeros().toPlainString(),
                    EconomyEngine.calculateEffectiveHashrate(state).toPlainString()
                )
            }

            elapsed = next
            if (elapsed == 300) {
                manualBtcAtMinuteFive = BigDecimal(state.stats.manualBtc)
                passiveBtcAtMinuteFive = BigDecimal(state.stats.foregroundPassiveBtc)
            }
            if (PrestigeEngine.calculateEarnablePoints(state) > 0L) {
                if (firstPrestige == null) firstPrestige = elapsed
                if (prestigeCyclesToSimulate <= 1) {
                    if (stopAtFirstPrestige) break
                } else {
                    state = PrestigeEngine.applyPrestige(state)
                    simulatedPrestigeCycles++
                    if (simulatedPrestigeCycles >= prestigeCyclesToSimulate) break
                    state = PrestigeEngine.buyPrestigeNode(state, "efficient_silicon")
                    cycleStartSeconds = next
                }
            }
        }

        return Result(
            seed = seed,
            balanceHash = BalanceConfig.stableHash(),
            durationSeconds = elapsed,
            firstSaleSeconds = firstSale,
            firstMachineSeconds = firstMachine,
            firstGpuSeconds = firstGpu,
            infrastructureMattersSeconds = infrastructureMatters,
            firstAsicSeconds = firstAsic,
            firstPrestigeSeconds = firstPrestige,
            manualBtcAtMinuteFive = manualBtcAtMinuteFive,
            passiveBtcAtMinuteFive = passiveBtcAtMinuteFive,
            manualBtc = BigDecimal(state.stats.manualBtc),
            passiveBtc = BigDecimal(state.stats.foregroundPassiveBtc),
            simulatedPrestigeCycles = simulatedPrestigeCycles,
            finalState = state,
            purchases = purchases,
            salePrices = salePrices
        )
    }

    private fun bestCandidate(
        state: GameState,
        elapsedSeconds: Int,
        infrastructureAware: Boolean = false,
        tapIntervalHalfSeconds: Int = 1
    ): Candidate? {
        val affordable = investmentCandidates(state, elapsedSeconds, tapIntervalHalfSeconds).filter { it.cost <= state.usdBigDecimal }
        if (infrastructureAware) {
            val powerIsLimiting = PowerEngine.calculatePowerFactor(state) < 0.999
            val coolingIsLimiting = EconomyEngine.calculateThermalState(state).second < 0.999
            affordable.firstOrNull {
                (powerIsLimiting && it.id.startsWith("power:")) ||
                    (coolingIsLimiting && it.id.startsWith("cooling:"))
            }?.let { return it }
        }
        return affordable.firstOrNull()
    }

    private fun bestInvestment(
        state: GameState,
        elapsedSeconds: Int,
        infrastructureAware: Boolean = false,
        tapIntervalHalfSeconds: Int = 1
    ): Candidate? {
        val candidates = investmentCandidates(state, elapsedSeconds, tapIntervalHalfSeconds)
        if (infrastructureAware) {
            val powerIsLimiting = PowerEngine.calculatePowerFactor(state) < 0.999
            val coolingIsLimiting = EconomyEngine.calculateThermalState(state).second < 0.999
            candidates.firstOrNull {
                (powerIsLimiting && it.id.startsWith("power:")) ||
                    (coolingIsLimiting && it.id.startsWith("cooling:"))
            }?.let { return it }
        }
        return candidates.firstOrNull()
    }

    private fun shouldSaveForHigherValuePurchase(
        state: GameState,
        elapsedSeconds: Int,
        tapIntervalHalfSeconds: Int,
        infrastructureAware: Boolean
    ): Boolean {
        val target = bestInvestment(state, elapsedSeconds, infrastructureAware, tapIntervalHalfSeconds) ?: return false
        if (target.cost <= state.usdBigDecimal) return false
        val affordable = bestCandidate(state, elapsedSeconds, infrastructureAware, tapIntervalHalfSeconds) ?: return true
        if (target.score < affordable.score.multiply(BigDecimal("1.25"), MathContext.DECIMAL128)) return false

        // Saving is bounded: wait only when the target can plausibly be funded within five
        // minutes from current BTC, passive production, and already scheduled reference taps.
        val projectedSeconds = 300
        val passiveBtc = EconomyEngine.calculateMinedBtc(
            EconomyEngine.calculateEffectiveHashrate(state), projectedSeconds.toDouble()
        )
        val scheduledTapBtc = EconomyEngine.calculateManualTapOutput(state)
            .multiply(BigDecimal(tapCountBetween(elapsedSeconds, elapsedSeconds + projectedSeconds, tapIntervalHalfSeconds)), MathContext.DECIMAL128)
        val projectedBtc = state.btcBigDecimal.add(passiveBtc, MathContext.DECIMAL128)
            .add(scheduledTapBtc, MathContext.DECIMAL128)
        val projectedCash = state.usdBigDecimal.add(
            projectedBtc.multiply(state.marketPriceBigDecimal, MathContext.DECIMAL128), MathContext.DECIMAL128
        )
        return projectedCash >= target.cost
    }

    private fun investmentCandidates(state: GameState, elapsedSeconds: Int, tapIntervalHalfSeconds: Int): List<Candidate> {
        val remainingSeconds = (SIMULATION_HORIZON_SECONDS - elapsedSeconds).coerceAtLeast(1.0)
        val baseRate = EconomyEngine.calculateEffectiveHashrate(state)
        val baseTap = EconomyEngine.calculateManualTapOutput(state)
        val remainingTaps = futureTapCount(elapsedSeconds, tapIntervalHalfSeconds)
        val candidates = mutableListOf<Candidate>()

        Miners.ALL.forEach { miner ->
            if (!FleetEngine.isUnlocked(miner, state)) return@forEach
            val owned = state.miners[miner.id] ?: 0L
            val (_, cost) = FleetEngine.calculatePurchase(miner, owned, BulkMode.X1, state.usdBigDecimal)
            if (cost.signum() <= 0) return@forEach
            val after = FleetEngine.buyMiner(state.copy(usd = cost.toPlainString()), miner.id, BulkMode.X1)
            addRateCandidate(candidates, "miner:${miner.id}", cost, state, after, baseRate, remainingSeconds)
        }

        Upgrades.ALL.forEach { upgrade ->
            if (upgrade.id in state.purchasedUpgrades || !UpgradeEngine.isUnlocked(upgrade, state)) return@forEach
            val after = UpgradeEngine.buyUpgrade(state.copy(usd = upgrade.costUsd.toPlainString()), upgrade.id)
            if (after == state) return@forEach
            if (upgrade.category == com.antigravity.bitcoinminingtycoon.content.UpgradeCategory.TAP) {
                val tapGain = EconomyEngine.calculateManualTapOutput(after).subtract(baseTap)
                    .max(BigDecimal.ZERO).multiply(BigDecimal(remainingTaps), MathContext.DECIMAL128)
                addCandidate(candidates, "upgrade:${upgrade.id}", upgrade.costUsd, after, tapGain)
            } else {
                addRateCandidate(candidates, "upgrade:${upgrade.id}", upgrade.costUsd, state, after, baseRate, remainingSeconds)
            }
        }

        if (PowerEngine.canUpgradePowerGrid(state)) {
            val stage = com.antigravity.bitcoinminingtycoon.content.Infrastructure.getPowerStage(state.powerGridTier + 1)
            val after = PowerEngine.upgradePowerGrid(state.copy(usd = stage.costUsd.toPlainString()))
            addRateCandidate(candidates, "power:${stage.tier}", stage.costUsd, state, after, baseRate, remainingSeconds)
        }
        if (ThermalEngine.canUpgradeCooling(state)) {
            val stage = com.antigravity.bitcoinminingtycoon.content.Infrastructure.getCoolingStage(state.coolingTier + 1)
            val after = ThermalEngine.upgradeCooling(state.copy(usd = stage.costUsd.toPlainString()))
            addRateCandidate(candidates, "cooling:${stage.tier}", stage.costUsd, state, after, baseRate, remainingSeconds)
        }

        return candidates.asSequence()
            .filter { it.expectedGain.signum() > 0 }
            .sortedWith(compareByDescending<Candidate> { it.score }.thenBy { it.id })
            .toList()
    }

    private fun addRateCandidate(
        destination: MutableList<Candidate>,
        id: String,
        cost: BigDecimal,
        before: GameState,
        after: GameState,
        beforeRate: BigDecimal,
        remainingSeconds: Double
    ) {
        val rateGain = EconomyEngine.calculateEffectiveHashrate(after).subtract(beforeRate)
        if (rateGain.signum() <= 0) return
        val expectedGain = rateGain.multiply(BigDecimal(remainingSeconds.toString()), MathContext.DECIMAL128)
            .multiply(HASH_TO_BTC, MathContext.DECIMAL128)
        addCandidate(destination, id, cost, after, expectedGain)
    }

    private fun addCandidate(destination: MutableList<Candidate>, id: String, cost: BigDecimal, after: GameState, gain: BigDecimal) {
        if (cost.signum() <= 0 || gain.signum() <= 0) return
        destination += Candidate(id, cost, after, gain, gain.divide(cost, MathContext.DECIMAL128))
    }

    private fun futureTapCount(elapsedSeconds: Int, tapIntervalHalfSeconds: Int): Long {
        return tapCountBetween(elapsedSeconds, BURST_END_SECONDS, tapIntervalHalfSeconds)
    }

    private fun tapCountInWindow(startSeconds: Int, endSeconds: Int, tapIntervalHalfSeconds: Int): Int =
        tapCountBetween(startSeconds, endSeconds, tapIntervalHalfSeconds).toInt()

    private fun tapCountBetween(startSeconds: Int, endSeconds: Int, tapIntervalHalfSeconds: Int): Long {
        var count = 0L
        var halfSecond = startSeconds * 2
        while (halfSecond < endSeconds * 2) {
            val second = halfSecond / 2.0
            if ((second < FIRST_RUN_LIMIT_SECONDS ||
                    (second >= 120.0 && second < BURST_END_SECONDS && second.toInt() % 60 < 5)) &&
                halfSecond % tapIntervalHalfSeconds.coerceAtLeast(1) == 0) count++
            halfSecond++
        }
        return count
    }
}
