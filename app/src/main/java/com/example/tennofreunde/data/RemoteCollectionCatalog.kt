package com.example.tennofreunde.data

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.WarframeItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

object RemoteCollectionCatalog {
    private const val RESOURCES_URL =
        "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json/Resources.json"
    private const val MODS_URL =
        "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json/Mods.json"

    fun load(client: OkHttpClient): List<WarframeItem> {
        return loadResources(client) + loadMods(client)
    }

    private fun loadResources(client: OkHttpClient): List<WarframeItem> {
        return fetchArray(client, RESOURCES_URL).mapNotNull { row ->
            val name = row.optString("name").trim()
            if (name.isBlank()) return@mapNotNull null

            val location = resourceLocation(row)
            WarframeItem(
                name = name,
                type = "resource",
                tabName = "Ressourcen",
                subTabName = resourceCategory(row, location),
                infoFields = mutableListOf(
                    InfoField("Fundort", location.ifBlank { "Noch kein Fundort in den Daten vorhanden" }),
                    InfoField("Quelle", row.optString("category").ifBlank { row.optString("type") }),
                    InfoField("Bilddatei", row.optString("imageName"))
                ),
                components = mutableStateListOf(
                    ComponentItem(
                        name = "Vorhanden",
                        farmLocation = location
                    )
                ),
                imageName = row.optString("imageName"),
                catalogSource = "wfcd"
            )
        }.distinctBy { it.name.lowercase() }.sortedBy { it.name.lowercase() }
    }

    private fun loadMods(client: OkHttpClient): List<WarframeItem> {
        return fetchArray(client, MODS_URL).mapNotNull { row ->
            val name = row.optString("name").trim()
            if (name.isBlank()) return@mapNotNull null

            val location = modLocation(row)
            WarframeItem(
                name = name,
                type = "mod",
                tabName = "Mods",
                subTabName = modCategory(row),
                infoFields = mutableListOf(
                    InfoField("Fundort", location.ifBlank { "Noch kein Fundort in den Daten vorhanden" }),
                    InfoField("Kategorie", row.optString("category").ifBlank { row.optString("type") }),
                    InfoField("Bilddatei", row.optString("imageName"))
                ),
                components = mutableStateListOf(
                    ComponentItem(
                        name = "Vorhanden",
                        farmLocation = location
                    )
                ),
                imageName = row.optString("imageName"),
                catalogSource = "wfcd"
            )
        }.distinctBy { it.name.lowercase() }.sortedBy { it.name.lowercase() }
    }

    private fun fetchArray(client: OkHttpClient, url: String): List<JSONObject> {
        val request = Request.Builder().url(url).build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            response.body?.string().orEmpty()
        }
        if (body.isBlank()) return emptyList()

        val array = JSONArray(body)
        return buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let(::add)
            }
        }
    }

    private fun resourceLocation(row: JSONObject): String {
        val descriptionLocation = locationFromDescription(row.optString("description"))
        if (descriptionLocation.isNotBlank()) return descriptionLocation

        val drops = row.optJSONArray("drops") ?: return ""
        return firstDropLocations(drops)
    }

    private fun modLocation(row: JSONObject): String {
        val drops = row.optJSONArray("drops")
        if (drops != null) {
            val dropText = firstDropLocations(drops)
            if (dropText.isNotBlank()) return dropText
        }
        return locationFromDescription(row.optString("description"))
    }

    private fun firstDropLocations(drops: JSONArray): String {
        val locations = buildList {
            for (index in 0 until drops.length()) {
                val location = drops.optJSONObject(index)?.optString("location").orEmpty()
                if (location.isNotBlank()) add(location)
                if (size == 4) break
            }
        }
        return locations.distinct().joinToString("; ")
    }

    private fun locationFromDescription(description: String): String {
        val marker = "Location:"
        val index = description.indexOf(marker, ignoreCase = true)
        if (index < 0) return ""
        return description.substring(index + marker.length).lineSequence().firstOrNull()?.trim().orEmpty()
    }

    internal fun resourceCategory(row: JSONObject, location: String): String {
        val text = "${row.optString("name")} ${row.optString("description")} $location"
        return when {
            text.contains("Railjack", ignoreCase = true) -> "Railjack"
            text.contains("Cetus", ignoreCase = true) ||
                text.contains("Orb Vallis", ignoreCase = true) ||
                text.contains("Cambion Drift", ignoreCase = true) ||
                text.contains("Zariman", ignoreCase = true) ||
                text.contains("Duviri", ignoreCase = true) -> "Offene Welten"
            text.contains("Standing", ignoreCase = true) ||
                text.contains("Syndicate", ignoreCase = true) ||
                text.contains("Ticker", ignoreCase = true) ||
                text.contains("Otak", ignoreCase = true) -> "Syndikat"
            row.optString("type").contains("Resource", ignoreCase = true) -> "Planet"
            else -> "Selten"
        }
    }

    internal fun modCategory(row: JSONObject): String {
        val text = "${row.optString("name")} ${row.optString("type")} ${row.optString("category")} ${row.optString("description")}"
        return when {
            text.contains("Riven", ignoreCase = true) -> "Riven"
            text.contains("Galvanized", ignoreCase = true) -> "Galvanized"
            text.contains("Primed", ignoreCase = true) -> "Primed"
            text.contains("Stance", ignoreCase = true) -> "Stance"
            text.contains("Aura", ignoreCase = true) -> "Aura"
            text.contains("Exilus", ignoreCase = true) -> "Exilus"
            text.contains("Companion", ignoreCase = true) ||
                text.contains("Pet", ignoreCase = true) ||
                text.contains("Sentinel", ignoreCase = true) -> "Begleiter"
            text.contains("Archwing", ignoreCase = true) -> "Archwing"
            text.contains("Melee", ignoreCase = true) -> "Nahkampf"
            text.contains("Secondary", ignoreCase = true) ||
                text.contains("Pistol", ignoreCase = true) -> "Sekundär"
            text.contains("Primary", ignoreCase = true) ||
                text.contains("Rifle", ignoreCase = true) ||
                text.contains("Shotgun", ignoreCase = true) -> "Primär"
            else -> "Warframe"
        }
    }
}
