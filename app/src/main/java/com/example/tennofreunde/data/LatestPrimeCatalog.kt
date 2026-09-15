package com.example.tennofreunde.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.WarframeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LatestPrimeCatalog {
    private const val BASE = "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json"
    private const val PREFS = "latest_prime_catalog"
    private const val DATA = "items_v3"
    private const val UPDATED = "updated_at"
    private val maxAge = TimeUnit.HOURS.toMillis(24)
    private val sources = listOf(
        CatalogSource("Warframes.json", "warframe", "Tenno", "Warframe", "Prime Warframe", ComponentMode.FRAME_PARTS),
        CatalogSource("Primary.json", "weapon", "Waffen", "Primär", "Primär Prime", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("Secondary.json", "weapon", "Waffen", "Sekundär", "Prime Sekundär", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("Melee.json", "weapon", "Waffen", "Nahkampf", "Prime Nahkampf", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("Arch-Gun.json", "weapon", "Waffen", "Arch-Gun", "Arch-Gun Prime", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("Arch-Melee.json", "weapon", "Waffen", "Arch-Nahkampf", "Arch-Nahkampf Prime", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("SentinelWeapons.json", "weapon", "Waffen", "Wächterwaffen", "Wächterwaffen Prime", ComponentMode.PRIME_OR_BUILT),
        CatalogSource("Sentinels.json", "companion", "Wächter", "Wächter", "Prime Wächter", ComponentMode.PRIME_OR_OWNED),
        CatalogSource("Pets.json", "companion", "Begleiter", "Begleiter", "Prime Begleiter", ComponentMode.OWNED, masterableOnly = true),
        CatalogSource("Archwing.json", "archwing", "Tenno", "Archwing", "Archwing", ComponentMode.FRAME_PARTS)
    )
    private val componentNames = setOf(
        "blueprint", "chassis", "neuroptics", "systems", "barrel", "receiver", "stock",
        "blade", "handle", "grip", "link", "string", "upper limb", "lower limb", "disc",
        "ornament", "guard", "hilt", "stars", "pouch", "head", "gauntlet", "boot",
        "carapace", "cerebrum", "harness", "wings", "buckle", "band"
    )

    fun load(
        context: Context,
        client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    ): List<PrimeCatalogItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cached = decode(prefs.getString(DATA, null))
        if (cached.isNotEmpty() && System.currentTimeMillis() - prefs.getLong(UPDATED, 0L) < maxAge) return cached

        val refreshed = sources.flatMap { source -> fetch(client, "$BASE/${source.file}", source) }
            .distinctBy { it.item.name.lowercase() }
        if (refreshed.isNotEmpty()) {
            prefs.edit()
                .putString(DATA, Gson().toJson(refreshed.map { ScannerAddedItem.from(it.item) }))
                .putLong(UPDATED, System.currentTimeMillis())
                .apply()
            return refreshed
        }
        return cached
    }

    private fun fetch(client: OkHttpClient, url: String, source: CatalogSource): List<PrimeCatalogItem> {
        val body = runCatching {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (response.isSuccessful) response.body?.string().orEmpty() else ""
            }
        }.getOrDefault("")
        if (body.isBlank()) return emptyList()
        val array = runCatching { JSONArray(body) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val row = array.optJSONObject(index) ?: continue
                val name = row.optString("name").trim()
                if (name.isBlank() || source.masterableOnly && !row.optBoolean("masterable", false)) continue
                val components = catalogComponents(row, source, name)
                if (components.isEmpty()) continue
                val requestedSubTab = when (source.file) {
                    "Pets.json" -> petCategory(name, row)
                    else -> if (name.endsWith(" Prime", ignoreCase = true)) source.primeSubTab else source.normalSubTab
                }
                val resolved = CollectionPlacementRules.forItem(source.type, name, source.tab, requestedSubTab)
                val item = WarframeItem(
                    name = name,
                    type = source.type,
                    tabName = resolved.tabName,
                    subTabName = resolved.subTabName,
                    infoFields = mutableListOf(InfoField("Quelle", "Aktueller WFCD-Katalog")),
                    components = mutableStateListOf<ComponentItem>().also { it.addAll(components) },
                    imageName = row.optString("imageName"),
                    isNew = true,
                    catalogSource = "wfcd_latest"
                )
                add(PrimeCatalogItem(item, components.map { "$name ${it.name}" }))
            }
        }
    }

    private fun catalogComponents(row: JSONObject, source: CatalogSource, itemName: String): List<ComponentItem> {
        val isPrime = itemName.endsWith(" Prime", ignoreCase = true)
        when (source.componentMode) {
            ComponentMode.OWNED -> return listOf(ComponentItem("Vorhanden"))
            ComponentMode.PRIME_OR_BUILT -> if (!isPrime) return listOf(ComponentItem("Gebaut"))
            ComponentMode.PRIME_OR_OWNED -> if (!isPrime) return listOf(ComponentItem("Vorhanden"))
            ComponentMode.FRAME_PARTS -> Unit
        }
        val array = row.optJSONArray("components") ?: JSONArray()
        val found = buildList {
            for (index in 0 until array.length()) {
                val component = array.optJSONObject(index) ?: continue
                val rawName = component.optString("name").trim()
                val name = componentNames.firstOrNull { candidate ->
                    rawName.equals(candidate, ignoreCase = true) ||
                        rawName.endsWith(" $candidate", ignoreCase = true)
                } ?: continue
                add(ComponentItem(name.split(" ").joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }))
            }
        }.toMutableList()
        val standardParts = when (source.type) {
            "warframe" -> listOf("Blueprint", "Chassis", "Neuroptics", "Systems")
            "companion" -> listOf("Blueprint", "Carapace", "Cerebrum", "Systems")
            "archwing" -> listOf("Blueprint", "Harness", "Systems", "Wings")
            else -> emptyList()
        }
        if (standardParts.isNotEmpty()) {
            standardParts.forEach { expected ->
                if (found.none { it.name.equals(expected, true) }) found += ComponentItem(expected)
            }
        }
        return found.distinctBy { it.name.lowercase() }
    }

    private fun petCategory(name: String, row: JSONObject): String {
        val text = "$name ${row.optString("type")} ${row.optString("description")}".lowercase()
        return when {
            "vulpaphyla" in text -> "Vulpaphyla"
            "predasite" in text -> "Predasite"
            "kavat" in text -> "Kavat"
            "kubrow" in text -> "Kubrow"
            "hound" in text -> "Hound"
            "moa" in text -> "MOA"
            else -> "Begleiter"
        }
    }

    private fun decode(raw: String?): List<PrimeCatalogItem> {
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ScannerAddedItem>>() {}.type
        val saved = runCatching { Gson().fromJson<List<ScannerAddedItem>>(raw, type) }.getOrDefault(emptyList())
        return saved.map { entry ->
            val item = entry.toWarframeItem().apply { catalogSource = "wfcd_latest" }
            PrimeCatalogItem(item, item.components.map { "${item.name} ${it.name}" })
        }
    }

    private enum class ComponentMode { FRAME_PARTS, PRIME_OR_BUILT, PRIME_OR_OWNED, OWNED }

    private data class CatalogSource(
        val file: String,
        val type: String,
        val tab: String,
        val normalSubTab: String,
        val primeSubTab: String,
        val componentMode: ComponentMode,
        val masterableOnly: Boolean = false
    )
}
