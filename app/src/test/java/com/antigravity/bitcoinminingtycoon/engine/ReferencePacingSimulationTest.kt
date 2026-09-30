package com.antigravity.bitcoinminingtycoon.engine

import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.data.SaveMigrationResult
import com.antigravity.bitcoinminingtycoon.data.migrations.SaveMigrations
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.content.PrestigeNodes
import java.io.File
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferencePacingSimulationTest {

    @Test
    fun fixedPriceReferencePolicyMeetsFirstRunTargetsAndShowsFasterEfficientSiliconCycle() {
        val first = ReferencePolicySimulator.simulate(seed = 1337L)
        val trace = first.trace()
        assertTrue("First sale outside 30–90 seconds\n$trace", first.firstSaleSeconds?.let { it in 30..90 } == true)
        assertTrue("First machine outside 30–90 seconds\n$trace", first.firstMachineSeconds?.let { it in 30..90 } == true)
        assertTrue("GPU outside 3–6 minutes\n$trace", first.firstGpuSeconds?.let { it in 180..360 } == true)
        assertTrue("Infrastructure outside 7–12 minutes\n$trace", first.infrastructureMattersSeconds?.let { it in 420..720 } == true)
        assertTrue("ASIC outside 12–20 minutes\n$trace", first.firstAsicSeconds?.let { it in 720..1_200 } == true)
        assertTrue(
            "Passive BTC must exceed manual BTC by minute 5\n$trace",
            first.passiveBtcAtMinuteFive > first.manualBtcAtMinuteFive
        )
        assertTrue("First prestige outside 25–35 minutes\n$trace", first.firstPrestigeSeconds?.let { it in 1_500..2_100 } == true)
        assertEquals(BalanceConfig.stableHash(), first.balanceHash)

        val reset = PrestigeEngine.applyPrestige(first.finalState)
        val efficient = PrestigeEngine.buyPrestigeNode(reset, PrestigeNodes.EFFICIENT_SILICON.id)
        val next = ReferencePolicySimulator.simulate(
            seed = 1337L,
            initialState = efficient,
            maxDurationSeconds = 7_200
        )
        assertNotNull("The next run must reach the same prestige milestone\n${next.trace()}", next.firstPrestigeSeconds)
        assertTrue(
            "Efficient Silicon next run should take at most 75% of the first run (${first.firstPrestigeSeconds}s)\n${next.trace()}",
            next.firstPrestigeSeconds!! <= first.firstPrestigeSeconds!! * BalanceConfig.REFERENCE_NEXT_RUN_MAX_RATIO
        )
        writeArtifact(
            "reference-policy.txt",
            buildString {
                appendLine("policy=fixed $50,000 market; no random events, windfalls, daily rewards, or critical tap")
                appendLine("taps=2/sec through 90 sec; five-second bursts each minute through 10 min")
                appendLine("sales=15-second opportunities only when they fund the next selected purchase")
                appendLine("choice=best marginal effective BTC per USD; id lexical tie break; save window <=300 sec")
                appendLine("balanceConfigSha256=${BalanceConfig.stableHash()}")
                appendLine(first.trace())
                appendLine("nextRunEfficientSiliconMultiplier=${BalanceConfig.EFFICIENT_SILICON_HASHRATE_MULTIPLIER}")
                appendLine(next.trace())
            }
        )
    }

    @Test
    fun fiveMarketAndEventSeedsRecordRepeatableTimingVariance() {
        val seeds = listOf(7L, 42L, 1337L, 2026L, 8_675_309L)
        val runs = seeds.map { seed ->
            ReferencePolicySimulator.simulate(
                seed = seed,
                randomMarket = true,
                randomEvents = true,
                claimWindfalls = true,
                claimFirstDailyReward = false,
                maxDurationSeconds = 7_200
            )
        }
        val report = runs.joinToString("\n---\n") { it.trace() }
        assertTrue("Every seeded run should reach first prestige\n$report", runs.all { it.firstPrestigeSeconds != null })
        assertTrue("Seed report needs observable timing variance", runs.map { it.firstPrestigeSeconds }.distinct().size > 1)
        assertTrue("All live runs must use the same balance inputs", runs.all { it.balanceHash == BalanceConfig.stableHash() })
        val prestigeTimes = runs.mapNotNull { it.firstPrestigeSeconds }.sorted()
        writeArtifact(
            "seeded-market-event-policy.txt",
            buildString {
                appendLine("policy=real market + random events, claim windfalls, no daily rewards")
                appendLine("balanceConfigSha256=${BalanceConfig.stableHash()}")
                appendLine("firstPrestigeSeconds min=${prestigeTimes.first()} max=${prestigeTimes.last()} median=${prestigeTimes[prestigeTimes.size / 2]}")
                appendLine("varianceSeconds=${prestigeTimes.last() - prestigeTimes.first()}")
                runs.forEach { appendLine(it.trace()) }
            }
        )
    }

    @Test
    fun alternatePlayerPoliciesAndLegacyStartsRemainPlayable() {
        val lowTap = ReferencePolicySimulator.simulate(seed = 1337L, tapIntervalHalfSeconds = 2)
        val frequentSeller = ReferencePolicySimulator.simulate(seed = 1337L, sellIntervalSeconds = 5)
        val dailyClaim = ReferencePolicySimulator.simulate(seed = 1337L, claimFirstDailyReward = true)
        val marketHolder = ReferencePolicySimulator.simulate(
            seed = 42L,
            randomMarket = true,
            marketHolderMinimumUsd = BigDecimal("52000.00")
        )
        val infrastructureAware = ReferencePolicySimulator.simulate(
            seed = 1337L,
            initialState = GameState(
                usd = "100000",
                miners = mapOf("gaming_gpu" to 10L),
                stats = StatsState(lifetimeBtcMined = "0.001")
            ),
            infrastructureAware = true,
            maxDurationSeconds = 60
        )
        val first = ReferencePolicySimulator.simulate(seed = 1337L)
        val delayedPrestige = ReferencePolicySimulator.simulate(
            seed = 1337L,
            stopAtFirstPrestige = false,
            maxDurationSeconds = first.firstPrestigeSeconds!! + 300
        )
        val multiPrestige = ReferencePolicySimulator.simulate(seed = 1337L, prestigeCyclesToSimulate = 2)

        assertTrue("low-tap start should still reach its first machine\n${lowTap.trace()}", lowTap.firstMachineSeconds != null)
        assertTrue("frequent seller must liquidate for a real purchase\n${frequentSeller.trace()}", frequentSeller.firstSaleSeconds != null)
        assertTrue("daily claim remains available after the first machine", dailyClaim.finalState.lastDailyClaimWallMillis > 0L)
        assertTrue("market-holder policy should eventually sell\n${marketHolder.trace()}", marketHolder.firstSaleSeconds != null)
        assertTrue(
            "market-holder only sells at or above its target",
            marketHolder.salePrices.all { BigDecimal(it) >= BigDecimal("52000.00") }
        )
        assertTrue("infrastructure-aware policy repairs its power deficit first", infrastructureAware.purchases.first().contains("BUY power:2"))
        assertEquals(first.firstPrestigeSeconds, delayedPrestige.firstPrestigeSeconds)
        assertEquals(0L, delayedPrestige.finalState.stats.totalPrestiges)
        assertTrue(delayedPrestige.finalState.stats.lifetimeBtcBigDecimal > first.finalState.stats.lifetimeBtcBigDecimal)
        assertEquals("Multi-prestige trace:\n${multiPrestige.trace()}", 2, multiPrestige.simulatedPrestigeCycles)
        assertEquals(2L, multiPrestige.finalState.stats.totalPrestiges)
        assertTrue("Efficient Silicon is bought between the prestige cycles", "efficient_silicon" in multiPrestige.finalState.purchasedPrestigeNodes)

        val legacy = listOf("early.json", "mid.json", "late.json").map { name ->
            val payload = File("src/test/resources/saves/v1/$name").readText()
            val migrated = SaveMigrations.migrate(payload) as SaveMigrationResult.Ready
            name to migrated.save.toGameState()
        }
        assertEquals("50000", legacy.first().second.manualHashStrength)
        assertEquals(1L, legacy.first().second.miners["ancient_cpu"])
        val report = buildString {
            appendLine("configSha256=${BalanceConfig.stableHash()}")
            appendLine("low-tap\n${lowTap.trace()}")
            appendLine("frequent-seller\n${frequentSeller.trace()}")
            appendLine("day-one-reward\n${dailyClaim.trace()}")
            appendLine("market-holder threshold=52000.00 salePrices=${marketHolder.salePrices}\n${marketHolder.trace()}")
            appendLine("infrastructure-aware\n${infrastructureAware.trace()}")
            appendLine("delayed-prestige\n${delayedPrestige.trace()}")
            appendLine("two-prestige-cycles\n${multiPrestige.trace()}")
            legacy.forEach { (name, state) ->
                appendLine("legacy=$name manual=${state.manualHashStrength} btc=${state.btc} miners=${state.miners} upgrades=${state.purchasedUpgrades.size} points=${state.satoshiPoints}")
            }
            appendLine("offline=12h capped at ${OfflineEngine.MAX_OFFLINE_SECONDS.toLong()}s; expiry, reward idempotence and offline-buffer coverage are in OfflineDailyTest")
        }
        writeArtifact("alternate-policies.txt", report)
    }

    private fun writeArtifact(name: String, contents: String) {
        val directory = File("../artifacts/m2")
        directory.mkdirs()
        File(directory, name).writeText(contents)
    }
}
