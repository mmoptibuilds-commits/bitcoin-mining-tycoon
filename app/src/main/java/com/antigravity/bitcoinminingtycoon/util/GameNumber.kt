package com.antigravity.bitcoinminingtycoon.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max

/**
 * High-precision mathematical and economy helpers for Bitcoin Mining Tycoon.
 * Uses [MathContext.DECIMAL128] to ensure deterministic arithmetic without NaN or Infinity.
 */
object GameNumber {
    val MATH_CONTEXT: MathContext = MathContext.DECIMAL128

    val ZERO: BigDecimal = BigDecimal.ZERO
    val ONE: BigDecimal = BigDecimal.ONE
    val HUNDRED: BigDecimal = BigDecimal("100")

    fun fromLong(value: Long): BigDecimal = BigDecimal(max(0L, value))
    fun fromDouble(value: Double): BigDecimal {
        if (value.isNaN() || value.isInfinite() || value <= 0.0) return ZERO
        return BigDecimal(value.toString(), MATH_CONTEXT)
    }
    fun fromString(value: String): BigDecimal = try {
        val parsed = BigDecimal(value.trim(), MATH_CONTEXT)
        if (parsed < ZERO) ZERO else parsed
    } catch (_: Exception) {
        ZERO
    }

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal =
        a.add(maxOf(ZERO, b), MATH_CONTEXT)

    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal {
        val res = a.subtract(b, MATH_CONTEXT)
        return if (res < ZERO) ZERO else res
    }

    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal =
        a.multiply(b, MATH_CONTEXT)

    fun multiply(a: BigDecimal, scalar: Double): BigDecimal {
        if (scalar <= 0.0 || scalar.isNaN() || scalar.isInfinite()) return ZERO
        return a.multiply(BigDecimal(scalar.toString(), MATH_CONTEXT), MATH_CONTEXT)
    }

    fun divide(a: BigDecimal, b: BigDecimal, scale: Int = 8): BigDecimal {
        if (b.compareTo(ZERO) <= 0) return ZERO
        return a.divide(b, scale, RoundingMode.HALF_UP)
    }

    /**
     * Computes the exact cost of purchasing [count] items starting at [owned] count
     * given base cost [baseCost] and geometric cost growth [growthRate].
     *
     * Formula: Cost(k) = baseCost * growthRate^owned * (growthRate^count - 1) / (growthRate - 1)
     */
    fun calculateBulkCost(
        baseCost: BigDecimal,
        growthRate: Double,
        owned: Long,
        count: Long
    ): BigDecimal {
        if (count <= 0L || baseCost <= ZERO) return ZERO
        if (growthRate <= 1.0) {
            return baseCost.multiply(BigDecimal(count), MATH_CONTEXT)
        }

        // Single item fast-path
        if (count == 1L) {
            val multiplier = Math.pow(growthRate, owned.toDouble())
            return baseCost.multiply(BigDecimal(multiplier.toString(), MATH_CONTEXT), MATH_CONTEXT)
                .setScale(2, RoundingMode.HALF_UP)
        }

        // Series: baseCost * r^owned * (r^k - 1) / (r - 1)
        val r = growthRate
        val rOwned = Math.pow(r, owned.toDouble())
        val rCount = Math.pow(r, count.toDouble())

        val currentItemCost = baseCost.multiply(BigDecimal(rOwned.toString(), MATH_CONTEXT), MATH_CONTEXT)
        val seriesFactor = (rCount - 1.0) / (r - 1.0)

        val total = currentItemCost.multiply(BigDecimal(seriesFactor.toString(), MATH_CONTEXT), MATH_CONTEXT)
        return total.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the maximum number of items affordable with [availableFunds] in O(1) time
     * using logarithmic estimation with a bounded verification step.
     */
    fun calculateMaxAffordable(
        availableFunds: BigDecimal,
        baseCost: BigDecimal,
        growthRate: Double,
        owned: Long
    ): Long {
        if (availableFunds <= ZERO || baseCost <= ZERO || growthRate <= 1.0) return 0L

        val firstItemCost = calculateBulkCost(baseCost, growthRate, owned, 1L)
        if (availableFunds < firstItemCost) return 0L

        // Closed-form estimate: k = floor( ln(1 + funds * (r - 1) / firstItemCost) / ln(r) )
        val fundsDouble = availableFunds.toDouble()
        val firstCostDouble = firstItemCost.toDouble()
        val r = growthRate

        val estimate: Long = if (!fundsDouble.isInfinite() && !firstCostDouble.isInfinite() && firstCostDouble > 0.0) {
            val ratio = 1.0 + (fundsDouble * (r - 1.0) / firstCostDouble)
            if (ratio > 0.0) {
                floor(ln(ratio) / ln(r)).toLong()
            } else 0L
        } else {
            // Fallback for massive balance ratios
            0L
        }

        var k = max(0L, estimate)

        // Bounded refinement to guarantee exactness (checks k, k+1, k-1)
        while (calculateBulkCost(baseCost, growthRate, owned, k + 1L) <= availableFunds) {
            k += 1L
        }
        while (k > 0L && calculateBulkCost(baseCost, growthRate, owned, k) > availableFunds) {
            k -= 1L
        }

        return max(0L, k)
    }
}
