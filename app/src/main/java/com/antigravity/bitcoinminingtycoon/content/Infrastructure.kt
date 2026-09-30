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
    val POWER_STAGES: List<PowerGridStage> = listOf(
        PowerGridStage(1, "House Outlet", 0.5, BigDecimal.ZERO),
        PowerGridStage(2, "Commercial Grid", 10.0, BigDecimal("200.00")),
        PowerGridStage(3, "Industrial Grid", 100.0, BigDecimal("2000.00")),
        PowerGridStage(4, "Dedicated Substation", 1000.0, BigDecimal("25000.00")),
        PowerGridStage(5, "Solar Farm Array", 10000.0, BigDecimal("300000.00")),
        PowerGridStage(6, "Hydro Generator", 100000.0, BigDecimal("5000000.00")),
        PowerGridStage(7, "Geothermal Tap", 1000000.0, BigDecimal("100000000.00")),
        PowerGridStage(8, "Nuclear Fission Plant", 10000000.0, BigDecimal("2500000000.00")),
        PowerGridStage(9, "Fusion Core Network", 100000000.0, BigDecimal("80000000000.00")),
        PowerGridStage(10, "Dyson Collector Ring", 1000000000.0, BigDecimal("5000000000000.00"))
    )

    val COOLING_STAGES: List<CoolingStage> = listOf(
        CoolingStage(1, "Desk Fan", 0.5, BigDecimal.ZERO),
        CoolingStage(2, "Commercial AC", 5.0, BigDecimal("150.00")),
        CoolingStage(3, "Industrial HVAC", 50.0, BigDecimal("1500.00")),
        CoolingStage(4, "Closed-Loop Liquid", 500.0, BigDecimal("18000.00")),
        CoolingStage(5, "Dielectric Immersion", 5000.0, BigDecimal("250000.00")),
        CoolingStage(6, "Cryogenic Chiller", 50000.0, BigDecimal("4000000.00")),
        CoolingStage(7, "Quantum Thermodynamic", 500000.0, BigDecimal("80000000.00"))
    )

    fun getPowerStage(tier: Int): PowerGridStage =
        POWER_STAGES.getOrElse(tier - 1) { POWER_STAGES.last() }

    fun getCoolingStage(tier: Int): CoolingStage =
        COOLING_STAGES.getOrElse(tier - 1) { COOLING_STAGES.last() }
}
