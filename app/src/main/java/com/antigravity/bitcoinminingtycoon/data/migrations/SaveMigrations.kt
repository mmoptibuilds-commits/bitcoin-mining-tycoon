package com.antigravity.bitcoinminingtycoon.data.migrations

import com.antigravity.bitcoinminingtycoon.data.GameSave
import com.antigravity.bitcoinminingtycoon.data.SaveMigrationResult
import com.antigravity.bitcoinminingtycoon.content.BalanceConfig
import com.antigravity.bitcoinminingtycoon.content.FacilityStageCatalog
import com.antigravity.bitcoinminingtycoon.engine.PrestigeEngine
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.model.GameState
import com.antigravity.bitcoinminingtycoon.model.MarketTrend
import com.antigravity.bitcoinminingtycoon.model.PendingOfflineSummary
import com.antigravity.bitcoinminingtycoon.model.PowerEnergySample
import com.antigravity.bitcoinminingtycoon.model.SettingsState
import com.antigravity.bitcoinminingtycoon.model.StatsState
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.math.BigDecimal

/** Pure, field-by-field migrations. A bad payload is never converted to a fresh save. */
object SaveMigrations {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun migrate(rawJson: String): SaveMigrationResult {
        if (rawJson.isBlank()) return SaveMigrationResult.RecoveryRequired("Save payload is empty")

        val root = try {
            json.parseToJsonElement(rawJson) as? JsonObject
                ?: return SaveMigrationResult.RecoveryRequired("Save root is not an object")
        } catch (_: Exception) {
            return SaveMigrationResult.RecoveryRequired("Save payload is malformed")
        }

        val version = when (val element = root["schemaVersion"]) {
            null -> 1 // The original v1 writer omitted this field in a few early saves.
            is JsonPrimitive -> element.intOrNull
                ?: return SaveMigrationResult.RecoveryRequired("Save schema version is malformed")
            else -> return SaveMigrationResult.RecoveryRequired("Save schema version is malformed")
        }
        if (version <= 0) {
            return SaveMigrationResult.RecoveryRequired("Save schema version is invalid", version)
        }
        if (version > GameSave.CURRENT_SCHEMA_VERSION) {
            return SaveMigrationResult.RecoveryRequired(
                reason = "Save schema $version is newer than supported schema ${GameSave.CURRENT_SCHEMA_VERSION}",
                schemaVersion = version
            )
        }
        if (version !in 1..GameSave.CURRENT_SCHEMA_VERSION) {
            return SaveMigrationResult.RecoveryRequired("Save schema version is invalid", version)
        }

        return try {
            val save = decodeSupported(root, wasSchemaOne = version == 1)
            SaveMigrationResult.Ready(
                save = save,
                migratedFromVersion = if (version < GameSave.CURRENT_SCHEMA_VERSION) version else null,
                recoveryWarnings = validationWarnings(root, version == 1)
            )
        } catch (_: Exception) {
            // A syntactically valid but structurally unsafe document must keep its raw checkpoint.
            SaveMigrationResult.RecoveryRequired("Save fields could not be migrated safely", version)
        }
    }

    private fun validationWarnings(root: JsonObject, wasSchemaOne: Boolean): List<String> {
        val warnings = linkedSetOf<String>()
        fun decimalField(owner: JsonObject, key: String, label: String = key) {
            val value = owner[key] ?: return
            if (value !is JsonPrimitive || value.contentOrNull?.let(::validDecimal) == null) warnings += "Invalid $label"
        }
        fun nonNegativeLong(owner: JsonObject, key: String) {
            val value = owner[key] ?: return
            if ((value as? JsonPrimitive)?.longOrNull?.let { it >= 0L } != true) warnings += "Invalid $key"
        }

        listOf("btc", "usd", "manualHashStrength", "marketPrice", "autoSellThresholdUsd", "autoSellPendingBtc")
            .forEach { decimalField(root, it) }
        root["marketPrice"]?.let { value ->
            val price = (value as? JsonPrimitive)?.contentOrNull?.let(::validMarketPrice)
            if (price == null) warnings += "Invalid marketPrice"
        }
        root["marketHistory"]?.let { value ->
            val history = value as? JsonArray
            if (history == null || history.any { sample ->
                    (sample as? JsonPrimitive)?.contentOrNull?.let(::validMarketPrice) == null
                }
            ) {
                warnings += "Invalid marketHistory"
            }
        }
        root["marketTrend"]?.let { if (enumOrNull<MarketTrend>(root, "marketTrend") == null) warnings += "Invalid marketTrend" }
        listOf(
            "marketTimerSeconds" to BalanceConfig.MAX_OFFLINE_SECONDS,
            "eventTimerSeconds" to BalanceConfig.MAX_OFFLINE_SECONDS
        ).forEach { (key, max) ->
            val value = root[key] ?: return@forEach
            val number = (value as? JsonPrimitive)?.doubleOrNull
            if (number == null || !number.isFinite() || number !in 0.0..max) warnings += "Invalid $key"
        }
        listOf("powerGridTier" to 10, "coolingTier" to 7, "dailyRewardDay" to 7).forEach { (key, max) ->
            val value = root[key] ?: return@forEach
            val number = (value as? JsonPrimitive)?.intOrNull
            val min = if (key == "dailyRewardDay" || key.endsWith("Tier")) 1 else 0
            if (number == null || number !in min..max) warnings += "Invalid $key"
        }
        listOf("autoSellEnabled", "onboardingCompleted").forEach { key ->
            val value = root[key] ?: return@forEach
            if ((value as? JsonPrimitive)?.booleanOrNull == null) warnings += "Invalid $key"
        }
        listOf("satoshiPoints", "lastDailyClaimWallMillis", "lastSaveWallMillis").forEach { nonNegativeLong(root, it) }
        listOf("balanceRulesVersion").forEach { key ->
            val value = root[key] ?: return@forEach
            if ((value as? JsonPrimitive)?.intOrNull?.let { it > 0 } != true) warnings += "Invalid $key"
        }
        if (!wasSchemaOne) {
            listOf("highestDiscoveredFacilityStage").forEach { key ->
                val value = root[key] ?: return@forEach
                if ((value as? JsonPrimitive)?.intOrNull?.let { it in 0..9 } != true) warnings += "Invalid $key"
            }
            root["batteryFriendlyAnimations"]?.let {
                if ((it as? JsonPrimitive)?.booleanOrNull == null) warnings += "Invalid batteryFriendlyAnimations"
            }
            listOf("completedTeachingCueIds").forEach { key ->
                val value = root[key] ?: return@forEach
                val array = value as? JsonArray
                if (array == null || array.any { (it as? JsonPrimitive)?.takeIf(JsonPrimitive::isString)?.content?.length?.let { n -> n <= 256 } != true }) {
                    warnings += "Invalid $key"
                }
            }
            root["pendingOfflineSummary"]?.let { item ->
                val summary = item as? JsonObject
                val duration = (summary?.get("durationSeconds") as? JsonPrimitive)?.doubleOrNull
                val creditedBtc = summary?.string("creditedBtc")?.let(::validDecimal)
                val creditedAt = (summary?.get("creditedAtWallMillis") as? JsonPrimitive)?.longOrNull
                if (summary == null || duration == null || !duration.isFinite() ||
                    duration !in 0.0..BalanceConfig.MAX_OFFLINE_SECONDS || creditedBtc == null || creditedAt == null || creditedAt < 0L) {
                    warnings += "Invalid pending offline summary"
                }
            }
        }
        listOf("miners").forEach { key ->
            val owner = root[key] ?: return@forEach
            val map = owner as? JsonObject
            if (map == null || map.any { (id, count) ->
                    id.length > 128 || (count as? JsonPrimitive)?.longOrNull?.let { it >= 0L } != true
                }) warnings += "Invalid miner ownership entry"
        }
        listOf("marketHistory", "purchasedUpgrades", "achievements", "purchasedPrestigeNodes").forEach { key ->
            val value = root[key] ?: return@forEach
            val array = value as? JsonArray
            if (array == null || array.any { (it as? JsonPrimitive)?.takeIf(JsonPrimitive::isString)?.content?.length?.let { n -> n <= 256 } != true }) {
                warnings += "Invalid $key"
            } else if (key == "marketHistory" && array.any { (it as? JsonPrimitive)?.contentOrNull?.let(::validDecimal) == null }) {
                warnings += "Invalid marketHistory entry"
            }
        }
        val events = root["activeEvents"]
        if (events != null) {
            val array = events as? JsonArray
            if (array == null || array.any { item ->
                    val event = item as? JsonObject ?: return@any true
                    val id = event.string("eventId")
                    val expiry = (event["expiresAtWallMillis"] as? JsonPrimitive)?.longOrNull
                    id == null || id.length > 128 || expiry == null || expiry < 0L ||
                        listOf("multiplier", "powerModifier", "heatModifier").any { key ->
                            event[key]?.let { e ->
                                val n = (e as? JsonPrimitive)?.doubleOrNull
                                n == null || !n.isFinite() || n !in 0.0..100.0
                            } == true
                        } || event["isWindfall"]?.let { (it as? JsonPrimitive)?.booleanOrNull == null } == true
                }) warnings += "Invalid active event entry"
        }
        val settings = root["settings"]
        if (settings != null) {
            val obj = settings as? JsonObject
            if (obj == null || listOf("soundEnabled", "hapticsEnabled", "reducedMotion").any { key ->
                    obj[key]?.let { (it as? JsonPrimitive)?.booleanOrNull } == null && key in obj
                }) warnings += "Invalid settings"
            if (obj != null && obj["numberFormat"] != null && enumOrNull<NumberFormatPreference>(obj, "numberFormat") == null) {
                warnings += "Invalid number format setting"
            }
        }
        val stats = root["stats"]
        if (stats != null) {
            val obj = stats as? JsonObject
            if (obj == null) warnings += "Invalid stats object" else {
                listOf(
                    "lifetimeBtcMined", "lifetimeUsdEarned", "peakHashrate", "totalBtcSold",
                    "highestPriceObserved", "lowestPriceObserved"
                ).forEach { decimalField(obj, it, "stats.$it") }
                if (!wasSchemaOne) listOf(
                    "manualBtc", "foregroundPassiveBtc", "offlineBtc", "dailyRewardBtc",
                    "windfallBtc", "achievementRewardBtc", "playtimeFractionalSeconds"
                ).forEach { decimalField(obj, it, "stats.$it") }
                listOf(
                    "totalManualTaps", "totalMinersPurchased", "totalUpgradesPurchased", "totalPrestiges",
                    "lifetimeSatoshiPointsEarned", "totalPlaytimeSeconds", "totalEventsTriggered"
                ).forEach { nonNegativeLong(obj, it) }
                if (!wasSchemaOne) listOf("prestigePointsBaselineV2", "prestigePointsEarnedSinceV2", "dailyPointsEarnedSinceV2")
                    .forEach { nonNegativeLong(obj, it) }
                obj["sourceBreakdownTrackedSinceV12"]?.let {
                    if ((it as? JsonPrimitive)?.booleanOrNull == null) warnings += "Invalid stats.sourceBreakdownTrackedSinceV12"
                }
                if (!wasSchemaOne) {
                    val fraction = obj.string("playtimeFractionalSeconds")?.let { runCatching { BigDecimal(it) }.getOrNull() }
                    if (fraction != null && (fraction.signum() < 0 || fraction >= BigDecimal.ONE)) warnings += "Invalid stats.playtimeFractionalSeconds"
                    val peak = (obj["peakTemperatureC"] as? JsonPrimitive)?.doubleOrNull
                    if (obj["peakTemperatureC"] != null && (peak == null || !peak.isFinite() || peak !in -100.0..10_000.0)) {
                        warnings += "Invalid stats.peakTemperatureC"
                    }
                    obj["powerEnergyHistory"]?.let { history ->
                        val samples = history as? JsonArray
                        if (samples == null || samples.any { sample ->
                                val row = sample as? JsonObject ?: return@any true
                                (row["elapsedSeconds"] as? JsonPrimitive)?.longOrNull?.let { it >= 0L } != true ||
                                    listOf("demandKw", "capacityKw", "energyKwh").any { key ->
                                        row.string(key)?.let(::validDecimal) == null
                                    }
                            }) warnings += "Invalid stats.powerEnergyHistory"
                    }
                }
            }
        }
        return warnings.toList()
    }

    private inline fun <reified T : Enum<T>> enumOrNull(owner: JsonObject, key: String): T? =
        owner[key]?.let { runCatching { json.decodeFromJsonElement<T>(it) }.getOrNull() }

    private fun decodeSupported(root: JsonObject, wasSchemaOne: Boolean): GameSave {
        val default = GameSave()
        val stats = decodeStats(root["stats"], wasSchemaOne)
        val miners = readLongMap(root["miners"])
        val activeEvents = readEvents(root["activeEvents"])
        val marketPrice = root.string("marketPrice")?.let(::validMarketPrice) ?: default.marketPrice
        val marketHistory = readMarketHistory(root["marketHistory"]).takeLast(30)
        val derivedStage = highestStage(miners)

        return GameSave(
            schemaVersion = GameSave.CURRENT_SCHEMA_VERSION,
            btc = decimal(root, "btc", "0"),
            usd = decimal(root, "usd", "0"),
            // Reconcile only the exact defaults written by older balance rules. Keep every
            // non-default value byte-for-byte so player-specific saves remain intact.
            manualHashStrength = migratedManualHash(root, wasSchemaOne, default.manualHashStrength),
            miners = miners,
            purchasedUpgrades = readStringSet(root["purchasedUpgrades"]),
            powerGridTier = root.int("powerGridTier", 1).coerceIn(1, 10),
            coolingTier = root.int("coolingTier", 1).coerceIn(1, 7),
            marketPrice = marketPrice,
            marketTrend = root.enum("marketTrend", MarketTrend.NEUTRAL),
            marketHistory = marketHistory.ifEmpty { listOf(marketPrice) },
            marketTimerSeconds = root.finiteDouble("marketTimerSeconds", BalanceConfig.MARKET_INITIAL_TIMER_SECONDS, 0.0, BalanceConfig.MAX_OFFLINE_SECONDS),
            eventTimerSeconds = root.finiteDouble("eventTimerSeconds", BalanceConfig.EVENT_INITIAL_TIMER_SECONDS, 0.0, BalanceConfig.MAX_OFFLINE_SECONDS),
            autoSellEnabled = root.bool("autoSellEnabled", false),
            autoSellThresholdUsd = decimal(root, "autoSellThresholdUsd", default.autoSellThresholdUsd),
            autoSellPendingBtc = decimal(root, "autoSellPendingBtc", default.autoSellPendingBtc),
            activeEvents = activeEvents,
            achievements = readStringSet(root["achievements"]),
            satoshiPoints = root.nonNegativeLong("satoshiPoints", 0L),
            purchasedPrestigeNodes = readStringSet(root["purchasedPrestigeNodes"]),
            dailyRewardDay = root.int("dailyRewardDay", 1).coerceIn(1, 7),
            lastDailyClaimWallMillis = root.nonNegativeLong("lastDailyClaimWallMillis", 0L),
            stats = stats,
            onboardingCompleted = root.bool("onboardingCompleted", false),
            settings = decodeSettings(root["settings"]),
            lastSaveWallMillis = root.nonNegativeLong("lastSaveWallMillis", 0L),
            rngSeed = root.long("rngSeed", 1337L),
            balanceRulesVersion = BalanceConfig.BALANCE_RULES_VERSION,
            completedTeachingCueIds = if (wasSchemaOne) emptySet() else readStringSet(root["completedTeachingCueIds"]),
            highestDiscoveredFacilityStage = root.intOrNull("highestDiscoveredFacilityStage")
                ?.takeIf { !wasSchemaOne && it in 0..9 } ?: derivedStage,
            batteryFriendlyAnimations = if (wasSchemaOne) false else root.bool("batteryFriendlyAnimations", false),
            pendingOfflineSummary = if (wasSchemaOne) null else decodePendingOfflineSummary(root["pendingOfflineSummary"])
        )
    }

    private fun migratedManualHash(root: JsonObject, wasSchemaOne: Boolean, currentDefault: String): String {
        val value = decimal(root, "manualHashStrength", if (wasSchemaOne) "10" else currentDefault)
        val priorRules = wasSchemaOne || root.int("balanceRulesVersion", 1) < BalanceConfig.BALANCE_RULES_VERSION
        if (!priorRules) return value

        val parsed = runCatching { BigDecimal(value) }.getOrNull() ?: return value
        val knownOldDefaults = listOf(BigDecimal("10"), BigDecimal(currentDefault))
        return if (knownOldDefaults.any { parsed.compareTo(it) == 0 }) currentDefault else value
    }

    private fun decodeStats(element: JsonElement?, wasSchemaOne: Boolean): StatsState {
        val root = element as? JsonObject ?: JsonObject(emptyMap())
        val defaults = StatsState()
        val oldLifetimeBtc = decimal(root, "lifetimeBtcMined", defaults.lifetimeBtcMined)
        val oldLifetimePoints = root.nonNegativeLong("lifetimeSatoshiPointsEarned", 0L)
        val baseline = minOf(
            oldLifetimePoints,
            PrestigeEngine.calculateTotalPointsFromLifetimeBtc(BigDecimal(oldLifetimeBtc))
        )

        return StatsState(
            lifetimeBtcMined = oldLifetimeBtc,
            lifetimeUsdEarned = decimal(root, "lifetimeUsdEarned", defaults.lifetimeUsdEarned),
            peakHashrate = decimal(root, "peakHashrate", defaults.peakHashrate),
            totalManualTaps = root.nonNegativeLong("totalManualTaps", 0L),
            totalMinersPurchased = root.nonNegativeLong("totalMinersPurchased", 0L),
            totalUpgradesPurchased = root.nonNegativeLong("totalUpgradesPurchased", 0L),
            totalBtcSold = decimal(root, "totalBtcSold", defaults.totalBtcSold),
            highestPriceObserved = decimal(root, "highestPriceObserved", defaults.highestPriceObserved),
            lowestPriceObserved = decimal(root, "lowestPriceObserved", defaults.lowestPriceObserved),
            totalPrestiges = root.nonNegativeLong("totalPrestiges", 0L),
            lifetimeSatoshiPointsEarned = oldLifetimePoints,
            totalPlaytimeSeconds = root.nonNegativeLong("totalPlaytimeSeconds", 0L),
            totalEventsTriggered = root.nonNegativeLong("totalEventsTriggered", 0L),
            manualBtc = if (wasSchemaOne) "0" else decimal(root, "manualBtc", "0"),
            foregroundPassiveBtc = if (wasSchemaOne) "0" else decimal(root, "foregroundPassiveBtc", "0"),
            offlineBtc = if (wasSchemaOne) "0" else decimal(root, "offlineBtc", "0"),
            dailyRewardBtc = if (wasSchemaOne) "0" else decimal(root, "dailyRewardBtc", "0"),
            windfallBtc = if (wasSchemaOne) "0" else decimal(root, "windfallBtc", "0"),
            achievementRewardBtc = if (wasSchemaOne) "0" else decimal(root, "achievementRewardBtc", "0"),
            sourceBreakdownTrackedSinceV12 = if (wasSchemaOne) false else root.bool("sourceBreakdownTrackedSinceV12", false),
            playtimeFractionalSeconds = if (wasSchemaOne) "0" else fractional(root),
            powerEnergyHistory = if (wasSchemaOne) emptyList() else readPowerHistory(root["powerEnergyHistory"]).takeLast(288),
            peakTemperatureC = if (wasSchemaOne) 25.0 else root.finiteDouble("peakTemperatureC", 25.0, -100.0, 10_000.0),
            prestigePointsBaselineV2 = if (wasSchemaOne) baseline else root.nonNegativeLong("prestigePointsBaselineV2", baseline),
            prestigePointsEarnedSinceV2 = if (wasSchemaOne) 0L else root.nonNegativeLong("prestigePointsEarnedSinceV2", 0L),
            dailyPointsEarnedSinceV2 = if (wasSchemaOne) 0L else root.nonNegativeLong("dailyPointsEarnedSinceV2", 0L)
        )
    }

    private fun decodeSettings(element: JsonElement?): SettingsState {
        val root = element as? JsonObject ?: return SettingsState()
        return SettingsState(
            soundEnabled = root.bool("soundEnabled", true),
            hapticsEnabled = root.bool("hapticsEnabled", true),
            reducedMotion = root.bool("reducedMotion", false),
            numberFormat = root.enum("numberFormat", NumberFormatPreference.COMPACT_SUFFIX)
        )
    }

    private fun decodePendingOfflineSummary(element: JsonElement?): PendingOfflineSummary? {
        val root = element as? JsonObject ?: return null
        val duration = root.finiteDouble("durationSeconds", Double.NaN, 0.0, BalanceConfig.MAX_OFFLINE_SECONDS)
        val btc = root.string("creditedBtc")?.let(::validDecimal) ?: return null
        val creditedAt = root.nonNegativeLong("creditedAtWallMillis", -1L)
        if (!duration.isFinite() || duration <= 0.0 || creditedAt < 0L) return null
        return PendingOfflineSummary(duration, btc, creditedAt)
    }

    private fun readEvents(element: JsonElement?): List<ActiveEventState> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            val root = item as? JsonObject ?: return@mapNotNull null
            val id = root.string("eventId")?.takeIf { it.length <= 128 } ?: return@mapNotNull null
            val expiry = root.nonNegativeLong("expiresAtWallMillis", -1L)
            if (expiry < 0L) return@mapNotNull null
            ActiveEventState(
                eventId = id,
                expiresAtWallMillis = expiry,
                multiplier = root.finiteDouble("multiplier", 1.0, 0.0, 100.0),
                powerModifier = root.finiteDouble("powerModifier", 1.0, 0.0, 100.0),
                heatModifier = root.finiteDouble("heatModifier", 1.0, 0.0, 100.0),
                isWindfall = root.bool("isWindfall", false)
            )
        }.take(16)
    }

    private fun readLongMap(element: JsonElement?): Map<String, Long> {
        val root = element as? JsonObject ?: return emptyMap()
        return root.mapNotNull { (key, value) ->
            val count = value.jsonPrimitive.longOrNull ?: return@mapNotNull null
            if (key.length > 128 || count < 0L) null else key to count
        }.toMap()
    }

    private fun readStringSet(element: JsonElement?): Set<String> {
        val array = element as? JsonArray ?: return emptySet()
        return array.mapNotNull { (it as? JsonPrimitive)?.takeIf(JsonPrimitive::isString)?.content }
            .filter { it.length <= 256 }
            .toSet()
    }

    private fun readMarketHistory(element: JsonElement?): List<String> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            (item as? JsonPrimitive)?.contentOrNull?.let(::validMarketPrice)
        }
    }

    private fun readPowerHistory(element: JsonElement?): List<PowerEnergySample> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            val root = item as? JsonObject ?: return@mapNotNull null
            val elapsed = root.nonNegativeLong("elapsedSeconds", -1L)
            val demand = root.string("demandKw")?.let(::validDecimal)
            val capacity = root.string("capacityKw")?.let(::validDecimal)
            val energy = root.string("energyKwh")?.let(::validDecimal)
            if (elapsed < 0L || demand == null || capacity == null || energy == null) null
            else PowerEnergySample(elapsed, demand, capacity, energy)
        }
    }

    private fun highestStage(miners: Map<String, Long>): Int = FacilityStageCatalog.highestOwnedStage(miners)

    private fun fractional(root: JsonObject): String {
        val value = decimal(root, "playtimeFractionalSeconds", "0")
        val parsed = runCatching { BigDecimal(value) }.getOrNull() ?: return "0"
        return if (parsed >= BigDecimal.ZERO && parsed < BigDecimal.ONE) value else "0"
    }

    private fun decimal(root: JsonObject, key: String, default: String): String =
        root.string(key)?.let(::validDecimal) ?: default

    private fun validDecimal(value: String): String? = runCatching {
        if (value.length > 4096) return null
        val parsed = BigDecimal(value)
        if (parsed.signum() < 0 || kotlin.math.abs(parsed.scale()) > 10_000) null else value
    }.getOrNull()

    private fun validMarketPrice(value: String): String? {
        val valid = validDecimal(value) ?: return null
        val price = BigDecimal(valid)
        return valid.takeIf {
            price >= BigDecimal(BalanceConfig.MARKET_MIN_USD) &&
                price <= BigDecimal(BalanceConfig.MARKET_MAX_USD)
        }
    }

    private inline fun <reified T> JsonObject.decode(key: String, default: T): T =
        this[key]?.let { runCatching { json.decodeFromJsonElement<T>(it) }.getOrNull() } ?: default

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.bool(key: String, default: Boolean): Boolean =
        (this[key] as? JsonPrimitive)?.booleanOrNull ?: default

    private fun JsonObject.int(key: String, default: Int): Int =
        (this[key] as? JsonPrimitive)?.intOrNull ?: default

    private fun JsonObject.intOrNull(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.long(key: String, default: Long): Long =
        (this[key] as? JsonPrimitive)?.longOrNull ?: default

    private fun JsonObject.nonNegativeLong(key: String, default: Long): Long =
        (this[key] as? JsonPrimitive)?.longOrNull?.takeIf { it >= 0L } ?: default

    private fun JsonObject.finiteDouble(key: String, default: Double, min: Double, max: Double): Double =
        (this[key] as? JsonPrimitive)?.doubleOrNull?.takeIf { it.isFinite() && it in min..max } ?: default

    private inline fun <reified T : Enum<T>> JsonObject.enum(key: String, default: T): T =
        this[key]?.let { runCatching { json.decodeFromJsonElement<T>(it) }.getOrNull() } ?: default
}
