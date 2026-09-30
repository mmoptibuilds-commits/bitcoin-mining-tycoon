package com.antigravity.bitcoinminingtycoon.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class NumberFormatPreference {
    COMPACT_SUFFIX,
    SCIENTIFIC
}

/**
 * Deterministic number formatting utility ensuring stable tabular output
 * across normal and astronomical late-game idle magnitudes.
 */
object NumberFormatter {

    private val SUFFIXES = arrayOf(
        "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No",
        "Dc", "Ud", "Dd", "Td", "Qad", "Qid", "Sxd", "Spd", "Ocd", "Nod", "Vg"
    )

    private val HASH_UNITS = arrayOf("H/s", "KH/s", "MH/s", "GH/s", "TH/s", "PH/s", "EH/s", "ZH/s", "YH/s")

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat2 = DecimalFormat("#,##0.00", symbols)
    private val decimalFormat8 = DecimalFormat("0.00000000", symbols)

    /**
     * Formats BTC balance. In early game shows 8 decimal places.
     * In late game (>= 1,000 BTC) switches to compact suffix.
     */
    fun formatBtc(amount: BigDecimal, preference: NumberFormatPreference = NumberFormatPreference.COMPACT_SUFFIX): String {
        if (amount <= BigDecimal.ZERO) return "0.00000000 BTC"
        if (amount < BigDecimal("1000")) {
            return "${decimalFormat8.format(amount)} BTC"
        }
        return "${formatCompact(amount, preference)} BTC"
    }

    /**
     * Formats USD currency values.
     */
    fun formatUsd(amount: BigDecimal, preference: NumberFormatPreference = NumberFormatPreference.COMPACT_SUFFIX): String {
        if (amount <= BigDecimal.ZERO) return "$ 0.00"
        if (amount < BigDecimal("1000")) {
            return "$ ${decimalFormat2.format(amount)}"
        }
        return "$ ${formatCompact(amount, preference)}"
    }

    /**
     * Formats hashrate values in standard computing engineering units (H/s -> EH/s).
     */
    fun formatHashrate(hashesPerSec: BigDecimal): String {
        if (hashesPerSec <= BigDecimal.ZERO) return "0.00 H/s"

        var unitIndex = 0
        var value = hashesPerSec
        val thousand = BigDecimal("1000")

        while (value >= thousand && unitIndex < HASH_UNITS.lastIndex) {
            value = value.divide(thousand, 2, RoundingMode.HALF_UP)
            unitIndex++
        }

        return "${decimalFormat2.format(value)} ${HASH_UNITS[unitIndex]}"
    }

    /**
     * Formats arbitrary large economy magnitudes according to player preference.
     */
    fun formatCompact(
        amount: BigDecimal,
        preference: NumberFormatPreference = NumberFormatPreference.COMPACT_SUFFIX
    ): String {
        if (amount <= BigDecimal.ZERO) return "0.00"

        if (preference == NumberFormatPreference.SCIENTIFIC) {
            val unscaled = amount.stripTrailingZeros()
            val exponent = unscaled.precision() - unscaled.scale() - 1
            if (exponent < 3) return decimalFormat2.format(amount)
            val mantissa = amount.divide(BigDecimal.TEN.pow(exponent), 2, RoundingMode.HALF_UP)
            return "${decimalFormat2.format(mantissa)}e$exponent"
        }

        var unscaled = amount
        var suffixIndex = 0
        val thousand = BigDecimal("1000")

        while (unscaled >= thousand && suffixIndex < SUFFIXES.lastIndex) {
            unscaled = unscaled.divide(thousand, 2, RoundingMode.HALF_UP)
            suffixIndex++
        }

        return "${decimalFormat2.format(unscaled)}${SUFFIXES[suffixIndex]}"
    }
}
