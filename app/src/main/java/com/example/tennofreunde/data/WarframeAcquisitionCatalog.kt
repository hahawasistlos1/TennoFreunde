package com.example.tennofreunde.data

import com.example.tennofreunde.models.WarframeItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

object WarframeAcquisitionCatalog {
    private const val WARFRAMES_URL =
        "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json/Warframes.json"

    data class ComponentSource(
        val farmLocation: String,
        val relic: String = "",
        val rotation: String = ""
    )

    fun load(client: OkHttpClient): Map<String, Map<String, ComponentSource>> {
        val request = Request.Builder().url(WARFRAMES_URL).build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyMap()
            response.body?.string().orEmpty()
        }
        if (body.isBlank()) return emptyMap()

        val array = JSONArray(body)
        val warframes = mutableMapOf<String, Map<String, ComponentSource>>()

        for (index in 0 until array.length()) {
            val row = array.optJSONObject(index) ?: continue
            val name = row.optString("name").trim()
            if (name.isBlank()) continue

            val componentSources = mutableMapOf<String, ComponentSource>()
            val components = row.optJSONArray("components") ?: continue
            for (componentIndex in 0 until components.length()) {
                val component = components.optJSONObject(componentIndex) ?: continue
                val componentKey = componentKey(component.optString("name")) ?: continue
                val source = sourceText(name, row, component, componentKey)
                if (source.isNotBlank()) {
                    componentSources[componentKey] = ComponentSource(
                        farmLocation = source,
                        rotation = rotationFrom(source)
                    )
                }
            }

            if (componentSources.isNotEmpty()) {
                warframes[itemKey(name)] = componentSources
            }
        }

        return warframes
    }

    fun enrich(
        item: WarframeItem,
        sources: Map<String, Map<String, ComponentSource>>
    ): Boolean {
        if (!item.type.equals("warframe", ignoreCase = true)) return false

        val itemSources = sources[itemKey(item.name)]
        var changed = false

        item.components.forEachIndexed { index, component ->
            val key = componentKey(component.name) ?: return@forEachIndexed
            val source = itemSources?.get(key) ?: fallbackSource(item.name, key) ?: return@forEachIndexed
            val needsLocation = component.farmLocation.isBlank() || isPlaceholder(component.farmLocation)
            val needsRelic = component.relic.isBlank() && source.relic.isNotBlank()
            val needsRotation = component.rotation.isBlank() && source.rotation.isNotBlank()
            if (needsLocation || needsRelic || needsRotation) {
                item.components[index] = component.copy(
                    farmLocation = if (needsLocation) source.farmLocation else component.farmLocation,
                    relic = if (needsRelic) source.relic else component.relic,
                    rotation = if (needsRotation) source.rotation else component.rotation
                )
                changed = true
            }
        }

        return changed
    }

    internal fun itemKey(value: String): String =
        value.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")

    internal fun componentKey(value: String): String? {
        val clean = value.lowercase(Locale.ROOT)
            .replace("blueprint", "bp")
            .replace("blaupause", "bp")
            .replace("haupt", "")

        return when {
            Regex("\\bbp\\b|blueprint").containsMatchIn(clean) -> "blueprint"
            clean.contains("neuroptik") || clean.contains("neuroptic") || clean.contains("helmet") -> "neuroptics"
            clean.contains("system") -> "systems"
            clean.contains("chassis") -> "chassis"
            else -> null
        }
    }

    private fun sourceText(
        warframeName: String,
        warframe: JSONObject,
        component: JSONObject,
        componentKey: String
    ): String {
        val dropText = firstDropLocations(component.optJSONArray("drops"))
        val descriptionLocation = locationFromDescription(component.optString("description"))
        val fallback = fallbackSource(warframeName, componentKey)?.farmLocation.orEmpty()
        val base = dropText.ifBlank { descriptionLocation }.ifBlank { fallback }
        if (base.isBlank()) return ""

        val costs = buildList {
            if (componentKey == "blueprint") {
                val buildPrice = warframe.optInt("buildPrice", 0)
                if (buildPrice > 0) add("Herstellung: ${credits(buildPrice)}")
            }
            val componentBuildPrice = component.optInt("buildPrice", 0)
            if (componentBuildPrice > 0) add("Komponente: ${credits(componentBuildPrice)}")
        }

        return (listOf(base) + costs).joinToString(" | ")
    }

    private fun firstDropLocations(drops: JSONArray?): String {
        if (drops == null) return ""
        val locations = buildList {
            for (index in 0 until drops.length()) {
                val drop = drops.optJSONObject(index) ?: continue
                val location = drop.optString("location").trim()
                if (location.isBlank()) continue

                val chance = drop.optDouble("chance", Double.NaN)
                val label = if (!chance.isNaN()) "$location (${chance.formatChance()}%)" else location
                add(label)
                if (size == 3) break
            }
        }
        return locations.distinct().joinToString("; ")
    }

    private fun locationFromDescription(description: String): String {
        val marker = "Location:"
        val index = description.indexOf(marker, ignoreCase = true)
        if (index < 0) return ""
        return description.substring(index + marker.length)
            .lineSequence()
            .firstOrNull()
            ?.trim()
            .orEmpty()
    }

    private fun fallbackSource(warframeName: String, componentKey: String): ComponentSource? {
        val isPrime = warframeName.contains("Prime", ignoreCase = true)
        if (isPrime) {
            return ComponentSource(
                farmLocation = "Void-Relikte / Fissuren; genaue Relikte werden angezeigt, sobald Dropdaten vorhanden sind"
            )
        }

        val name = warframeName.lowercase(Locale.ROOT)
        if (componentKey == "blueprint") {
            val source = when (name) {
                "banshee", "nezha", "volt", "wukong", "zephyr" ->
                    "Clan-Dojo: Tenno-Labor (Credit- und Forschungskosten im Labor)"
                "dagath" ->
                    "Clan-Dojo: Dagaths Höhle (Vainthorn-Kosten im Dojo prüfen)"
                "vauban" ->
                    "Nightwave-Angebote (Nightwave-Credits)"
                "excalibur umbra" ->
                    "Quest: Das Opfer"
                "styanax" ->
                    "Kahl-Garnison / Chipper-Angebote"
                "citrine" ->
                    "Mars/Tyana Pass oder Otak-Angebote"
                "voruna" ->
                    "Lua/Conjunction Survival oder Archimedean Yonta-Angebote"
                "kullervo" ->
                    "Duviri/Kullervos Festung; Kullervos Fluch als Waehrung"
                else ->
                    "Markt: Haupt-Blaupause gegen Credits, wenn der Warframe nicht quest- oder syndikatsgebunden ist"
            }
            return ComponentSource(farmLocation = source)
        }

        return null
    }

    private fun isPlaceholder(value: String): Boolean {
        val clean = value.lowercase(Locale.ROOT)
        return clean.contains("noch kein") ||
            clean.contains("nicht gepflegt") ||
            clean.contains("not set")
    }

    private fun credits(value: Int): String =
        "${NumberFormat.getIntegerInstance(Locale.GERMANY).format(value)} Credits"

    private fun Double.formatChance(): String {
        val rounded = kotlin.math.round(this * 100.0) / 100.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }

    private fun rotationFrom(text: String): String {
        val match = Regex("Rotation\\s+([A-C])", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.getOrNull(1)?.uppercase(Locale.ROOT).orEmpty()
    }
}
