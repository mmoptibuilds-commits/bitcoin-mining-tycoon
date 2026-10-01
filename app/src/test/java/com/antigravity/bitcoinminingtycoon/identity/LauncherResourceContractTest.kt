package com.antigravity.bitcoinminingtycoon.identity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LauncherResourceContractTest {

    private val androidNamespace = "http://schemas.android.com/apk/res/android"

    @Test
    fun adaptiveAndRoundIconsUseDedicatedSingleColorMonochromeArtwork() {
        listOf("ic_launcher.xml", "ic_launcher_round.xml").forEach { fileName ->
            val document = parse(File("src/main/res/mipmap-anydpi-v26/$fileName"))
            val icon = document.documentElement
            val monochrome = icon.getElementsByTagName("monochrome").item(0) as? Element

            assertNotNull("$fileName must define themed icon artwork", monochrome)
            assertEquals(
                "@drawable/ic_launcher_monochrome",
                monochrome?.getAttributeNS(androidNamespace, "drawable")
            )
        }

        val monochrome = parse(File("src/main/res/drawable/ic_launcher_monochrome.xml"))
        val paths = monochrome.getElementsByTagName("path")
        assertTrue("Monochrome art must have visible paths", paths.length > 0)
        val visibleColors = mutableSetOf<String>()
        for (index in 0 until paths.length) {
            val path = paths.item(index) as Element
            listOf("fillColor", "strokeColor").forEach { attribute ->
                path.getAttributeNS(androidNamespace, attribute)
                    .takeUnless { it.isBlank() || it.equals("#00000000", ignoreCase = true) }
                    ?.let(visibleColors::add)
            }
        }
        assertEquals("Monochrome paths use one opaque alpha mask", setOf("#FFFFFFFF"), visibleColors)
    }

    @Test
    fun android12SystemSplashUsesGraphiteAndReturnsToTheAppTheme() {
        val document = parse(File("src/main/res/values-v31/themes.xml"))
        val styles = document.getElementsByTagName("style")
        var theme: Element? = null
        for (index in 0 until styles.length) {
            val style = styles.item(index) as Element
            if (style.getAttribute("name") == "Theme.BitcoinMiningTycoon") theme = style
        }
        assertNotNull("API 31+ must override the existing app theme", theme)

        val items = theme!!.getElementsByTagName("item")
        val values = mutableMapOf<String, String>()
        for (index in 0 until items.length) {
            val item = items.item(index) as Element
            values[item.getAttribute("name")] = item.textContent.trim()
        }
        assertEquals("#172228", values["android:windowSplashScreenBackground"])
        assertEquals("@drawable/ic_launcher_foreground", values["android:windowSplashScreenAnimatedIcon"])
    }

    private fun parse(file: File) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse(file)
}
