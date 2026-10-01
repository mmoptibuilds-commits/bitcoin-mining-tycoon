package com.antigravity.bitcoinminingtycoon.identity

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReleaseIdentityContractTest {

    @Test
    fun firstV12CandidateAdvancesApplicationVersionWithoutChangingPackage() {
        val gradle = File("build.gradle.kts").readText()

        assertTrue(Regex("applicationId\\s*=\\s*\"com\\.antigravity\\.bitcoinminingtycoon\"")
            .containsMatchIn(gradle))
        assertTrue(Regex("versionName\\s*=\\s*\"1\\.2\\.0\"").containsMatchIn(gradle))
        assertTrue(Regex("versionCode\\s*=\\s*2(?:\\D|$)").containsMatchIn(gradle))
    }
}
