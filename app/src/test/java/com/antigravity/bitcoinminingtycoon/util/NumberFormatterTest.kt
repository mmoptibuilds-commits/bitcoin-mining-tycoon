package com.antigravity.bitcoinminingtycoon.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class NumberFormatterTest {

    @Test
    fun formatBtc_formatsEarlyGameWith8Decimals() {
        val btc = BigDecimal("0.00041285")
        val formatted = NumberFormatter.formatBtc(btc)
        assertEquals("0.00041285 BTC", formatted)
    }

    @Test
    fun formatBtc_formatsLateGameWithCompactSuffix() {
        val btc = BigDecimal("15420.50")
        val formatted = NumberFormatter.formatBtc(btc)
        assertEquals("15.42K BTC", formatted)
    }

    @Test
    fun formatUsd_formatsNormalAndCompact() {
        val small = BigDecimal("248.50")
        assertEquals("$ 248.50", NumberFormatter.formatUsd(small))

        val millions = BigDecimal("12450000.00")
        assertEquals("$ 12.45M", NumberFormatter.formatUsd(millions))
    }

    @Test
    fun formatHashrate_formatsStandardUnits() {
        assertEquals("500.00 H/s", NumberFormatter.formatHashrate(BigDecimal("500")))
        assertEquals("12.50 KH/s", NumberFormatter.formatHashrate(BigDecimal("12500")))
        assertEquals("1.68 GH/s", NumberFormatter.formatHashrate(BigDecimal("1680000000")))
        assertEquals("25.00 PH/s", NumberFormatter.formatHashrate(BigDecimal("25000000000000000")))
    }

    @Test
    fun formatCompact_supportsScientificNotation() {
        val billion = BigDecimal("5800000000")
        val scientific = NumberFormatter.formatCompact(billion, NumberFormatPreference.SCIENTIFIC)
        assertEquals("5.80e9", scientific)
    }
}
