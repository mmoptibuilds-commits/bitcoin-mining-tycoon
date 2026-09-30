package com.antigravity.bitcoinminingtycoon.content

import java.math.BigDecimal

data class PowerGridStage(
    val tier: Int,
    val name: String,
    val capacityKw: Double,
    val costUsd: BigDecimal
)

data class CoolingStage(
    val tier: Int,
    val name: String,
    val dissipationRating: Double,
    val costUsd: BigDecimal
)

object Infrastructure {
    private val powerNames = listOf(
        "House Outlet", "Commercial Grid", "Industrial Grid", "Dedicated Substation", "Solar Farm Array",
        "Hydro Generator", "Geothermal Tap", "Nuclear Fission Plant", "Fusion Core Network", "Dyson Collector Ring"
    )
    private val coolingNames = listOf(
        "Desk Fan", "Commercial AC", "Industrial HVAC", "Closed-Loop Liquid", "Dielectric Immersion",
        "Cryogenic Chiller", "Quantum Thermodynamic"
    )

    val POWER_STAGES: List<PowerGridStage> = powerNames.mapIndexed { index, name ->
        val tuning = BalanceConfig.POWER_STAGES[index]
        PowerGridStage(index + 1, name, tuning.magnitude, BigDecimal(tuning.costUsd))
    }

    val COOLING_STAGES: List<CoolingStage> = coolingNames.mapIndexed { index, name ->
        val tuning = BalanceConfig.COOLING_STAGES[index]
        CoolingStage(index + 1, name, tuning.magnitude, BigDecimal(tuning.costUsd))
    }

    fun getPowerStage(tier: Int): PowerGridStage =
        POWER_STAGES.getOrElse(tier - 1) { POWER_STAGES.last() }

    fun getCoolingStage(tier: Int): CoolingStage =
        COOLING_STAGES.getOrElse(tier - 1) { COOLING_STAGES.last() }
}
