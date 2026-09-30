package com.antigravity.bitcoinminingtycoon.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class GameNumberTest {

    @Test
    fun arithmetic_neverProducesNegativeOrNaN() {
        val a = GameNumber.fromLong(100L)
        val b = GameNumber.fromLong(250L)

        // Subtraction floor at zero
        val sub = GameNumber.subtract(a, b)
        assertEquals(BigDecimal.ZERO, sub)

        // Division by zero returns zero
        val divZero = GameNumber.divide(a, BigDecimal.ZERO)
        assertEquals(BigDecimal.ZERO, divZero)

        // Negative input normalization
        val fromNeg = GameNumber.fromDouble(-50.0)
        assertEquals(BigDecimal.ZERO, fromNeg)

        val fromNaN = GameNumber.fromDouble(Double.NaN)
        assertEquals(BigDecimal.ZERO, fromNaN)

        val fromInf = GameNumber.fromDouble(Double.POSITIVE_INFINITY)
        assertEquals(BigDecimal.ZERO, fromInf)
    }

    @Test
    fun calculateBulkCost_matchesIterativeSummation() {
        val baseCost = BigDecimal("100.00")
        val growth = 1.15
        val owned = 5L
        val count = 25L

        val closedForm = GameNumber.calculateBulkCost(baseCost, growth, owned, count)

        // Iterative sum
        var iterative = BigDecimal.ZERO
        for (i in 0 until count) {
            val costForSingle = baseCost.multiply(BigDecimal(Math.pow(growth, (owned + i).toDouble()).toString(), GameNumber.MATH_CONTEXT))
            iterative = iterative.add(costForSingle)
        }
        val iterativeRounded = iterative.setScale(2, java.math.RoundingMode.HALF_UP)

        // Compare with tolerance of pennies due to intermediate rounding
        val diff = closedForm.subtract(iterativeRounded).abs()
        assertTrue("Difference between closed form and loop should be <= 0.05, was $diff", diff <= BigDecimal("0.05"))
    }

    @Test
    fun calculateMaxAffordable_exactAndPerformsInMicroseconds() {
        val baseCost = BigDecimal("10.00")
        val growth = 1.15
        val owned = 0L

        // Give exactly enough for 10 items
        val costFor10 = GameNumber.calculateBulkCost(baseCost, growth, owned, 10L)
        val affordableFor10 = GameNumber.calculateMaxAffordable(costFor10, baseCost, growth, owned)
        assertEquals(10L, affordableFor10)

        // Slightly less than cost for 10
        val slightlyLess = costFor10.subtract(BigDecimal("0.01"))
        val affordableFor9 = GameNumber.calculateMaxAffordable(slightlyLess, baseCost, growth, owned)
        assertEquals(9L, affordableFor9)

        // Huge astronomical balance: $10^30
        val massiveBalance = BigDecimal("1000000000000000000000000000000")
        val startNano = System.nanoTime()
        val maxItems = GameNumber.calculateMaxAffordable(massiveBalance, baseCost, growth, owned)
        val durationMicros = (System.nanoTime() - startNano) / 1000

        assertTrue("Calculation must complete in < 50,000 microseconds (50ms)", durationMicros < 50_000)
        assertTrue("Must be able to afford hundreds of items with massive balance", maxItems > 100L)

        // Verify the result is exactly affordable
        val totalCost = GameNumber.calculateBulkCost(baseCost, growth, owned, maxItems)
        assertTrue("Total cost must be <= massiveBalance", totalCost <= massiveBalance)
    }
}
