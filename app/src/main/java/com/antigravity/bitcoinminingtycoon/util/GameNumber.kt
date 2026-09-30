package com.antigravity.bitcoinminingtycoon.util

import java.math.BigDecimal
import java.math.BigInteger
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
    private val SATURATED_COST = BigDecimal("1E+100000")

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

    /** Exact floor square root for nonnegative economic values, without a Double conversion. */
    fun floorSquareRoot(value: BigDecimal): BigInteger {
        val integer = value.toBigInteger()
        if (integer.signum() <= 0) return BigInteger.ZERO

        // Newton iteration uses only long-supported BigInteger operations (minSdk 31).
        var estimate = BigInteger.ONE.shiftLeft((integer.bitLength() + 1) / 2)
        while (true) {
            val next = estimate.add(integer.divide(estimate)).shiftRight(1)
            if (next >= estimate) return estimate
            estimate = next
        }
    }

    /** Converts a nonnegative integer counter safely when the mathematical result can exceed Long. */
    fun saturatingLong(value: BigInteger): Long = when {
        value.signum() <= 0 -> 0L
        value >= BigInteger.valueOf(Long.MAX_VALUE) -> Long.MAX_VALUE
        else -> value.toLong()
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
            if (!multiplier.isFinite()) return SATURATED_COST
            return baseCost.multiply(BigDecimal(multiplier.toString(), MATH_CONTEXT), MATH_CONTEXT)
                .setScale(2, RoundingMode.HALF_UP)
        }

        // Series: baseCost * r^owned * (r^k - 1) / (r - 1)
        val r = growthRate
        val rOwned = Math.pow(r, owned.toDouble())
        val rCount = Math.pow(r, count.toDouble())
        if (!rOwned.isFinite() || !rCount.isFinite()) return SATURATED_COST

        val currentItemCost = baseCost.multiply(BigDecimal(rOwned.toString(), MATH_CONTEXT), MATH_CONTEXT)
        val seriesFactor = (rCount - 1.0) / (r - 1.0)

        val total = currentItemCost.multiply(BigDecimal(seriesFactor.toString(), MATH_CONTEXT), MATH_CONTEXT)
        if (!total.toDouble().isFinite()) return SATURATED_COST
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
        if (firstItemCost >= SATURATED_COST || availableFunds < firstItemCost) return 0L
        fun affordable(count: Long): Boolean {
            val cost = calculateBulkCost(baseCost, growthRate, owned, count)
            return cost < SATURATED_COST && cost <= availableFunds
        }

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

        // A few local checks retain the fast logarithmic path for ordinary balances.
        repeat(8) {
            when {
                k < Long.MAX_VALUE && affordable(k + 1L) -> k++
                k > 0L && !affordable(k) -> k--
                else -> return k
            }
        }

        // Extreme decimal balances can overflow Double during the estimate. Finish with a
        // 63-step integer search rather than allowing an unbounded correction loop.
        var low = 0L
        var high = Long.MAX_VALUE
        while (low < high) {
            val middle = low + (high - low) / 2L + 1L
            if (affordable(middle)) low = middle
            else high = middle - 1L
        }
        return max(0L, low)
    }
}
