package com.example.tennofreunde.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import org.json.JSONArray

data class PrimeCatalogItem(
    val item: WarframeItem,
    val searchableParts: List<String>
)

object PrimeCatalog {
    private val frameParts = setOf("blueprint", "bp", "chassis", "systems", "system", "neuroptics", "neuroptik")
    private val sentinelNames = setOf("carrier", "dethcube", "helios", "nautilus", "shade", "wyrm")
    private val companionAccessoryNames = setOf("kavasa")
    private val archwingNames = setOf("odonata")

    fun load(context: Context): List<PrimeCatalogItem> {
        val frames = read(context, "relic_data.json", true)
        val weapons = read(context, "weapon_relic_data.json", false)
        return (frames + weapons).distinctBy { it.item.name.lowercase() }
    }

    private fun read(context: Context, asset: String, frames: Boolean): List<PrimeCatalogItem> {
        val array = JSONArray(context.assets.open(asset).bufferedReader().use { it.readText() })
        val parts = buildList {
            for (index in 0 until array.length()) {
                val row = array.getJSONObject(index)
                add(
                    CatalogPart(
                        raw = row.optString("part"),
                        relic = row.optString("relic"),
                        rotation = row.optString("rotation"),
                        farmLocation = row.optString("farmLocation")
                    )
                )
            }
        }.filter { it.raw.isNotBlank() }.distinctBy { it.raw.lowercase() }

        return parts.groupBy { itemName(it.raw, frames) }.filterKeys { it.isNotBlank() }.map { (name, itemParts) ->
            val baseName = name.lowercase().removeSuffix(" prime")
            val type = when {
                frames -> "warframe"
                baseName in sentinelNames || baseName in companionAccessoryNames -> "companion"
                baseName in archwingNames -> "archwing"
                else -> "weapon"
            }
            val defaultTab = when (type) {
                "warframe", "archwing" -> "Tenno"
                "companion" -> if (baseName in sentinelNames) "Wächter" else "Begleiter"
                else -> "Waffen"
            }
            val defaultSubTab = when (type) {
                "warframe" -> "Prime Warframe"
                "companion" -> if (baseName in sentinelNames) "Prime Wächter" else "Kubrow"
                "archwing" -> "Archwing"
                else -> "Prime Waffen"
            }
            val placement = CollectionPlacementRules.forItem(
                type = type,
                name = name,
                tabName = defaultTab,
                subTabName = defaultSubTab
            )
            val loadedComponents = itemParts.map { part ->
                ComponentItem(
                    name = componentName(part.raw, name),
                    farmLocation = part.farmLocation,
                    relic = part.relic,
                    rotation = part.rotation
                )
            }
            val components = (
                loadedComponents + if (baseName in sentinelNames) {
                    listOf("Blueprint", "Carapace", "Cerebrum", "Systems").map(::ComponentItem)
                } else {
                    emptyList()
                }
            ).distinctBy { it.name.lowercase() }
            PrimeCatalogItem(
                item = WarframeItem(
                    name = name,
                    type = type,
                    tabName = placement.tabName,
                    subTabName = placement.subTabName,
                    infoFields = mutableListOf(),
                    components = mutableStateListOf<ComponentItem>().also { it.addAll(components) },
                    isNew = true
                ),
                searchableParts = itemParts.map { it.raw }
            )
        }
    }

    private fun itemName(raw: String, frames: Boolean): String {
        val words = raw.lowercase().replace(Regex("[^a-z0-9 ]"), " ").split(Regex("\\s+")).filter { it.isNotBlank() }
        val nameWords = if (frames) {
            words.filterNot { it == "prime" || it in frameParts }
        } else {
            val componentIndex = words.indexOfFirst { it in weaponParts }
            val base = if (componentIndex > 0) words.subList(0, componentIndex) else words
            base.filterNot { it == "prime" }
        }
        val canonicalWords = when (nameWords.joinToString(" ")) {
            "skyla" -> listOf("nami", "skyla")
            "kamas" -> listOf("dual", "kamas")
            "decurion" -> listOf("dual", "decurion")
            "zoren" -> listOf("dual", "zoren")
            else -> nameWords
        }
        return canonicalWords.joinToString(" ") { it.replaceFirstChar(Char::uppercase) } + if (canonicalWords.isNotEmpty()) " Prime" else ""
    }

    private fun componentName(raw: String, itemName: String): String {
        val itemWords = itemName.lowercase().removeSuffix(" prime").split(" ").toSet()
        val words = raw.lowercase().split(Regex("\\s+")).filterNot { it == "prime" || it in itemWords }
        return words.joinToString(" ") {
            when (it) {
                "bp" -> "Blueprint"
                "system" -> "Systems"
                "neuroptik" -> "Neuroptics"
                else -> it.replaceFirstChar(Char::uppercase)
            }
        }.ifBlank { "Blueprint" }
    }

    private data class CatalogPart(val raw: String, val relic: String, val rotation: String, val farmLocation: String)

    private val weaponParts = setOf(
        "bp", "blueprint", "barrel", "receiver", "stock", "blade", "handle", "disc", "grip", "link", "string",
        "ornament", "guard", "hilt", "stars", "pouch", "limb", "lower", "upper", "head", "gauntlet", "boot",
        "cerebrum", "carapace", "systems", "system", "harness", "wings", "buckle", "band"
    )
}
