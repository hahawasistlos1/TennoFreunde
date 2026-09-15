package com.example.tennofreunde.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.WarframeItem

/** A complete offline baseline built from assets shipped inside the APK. */
object AssetCollectionCatalog {
    private val warframes = setOf(
        "ash", "atlas", "banshee", "baruuk", "caliban", "chroma", "citrine", "cyte_09",
        "dagath", "dante", "ember", "equinox", "excalibur", "excalibur_umbra", "frost",
        "gara", "garuda", "gauss", "grendel", "gyre", "harrow", "hildryn", "hydroid",
        "inaros", "ivara", "jade", "khora", "koumei", "kullervo", "limbo", "loki", "mag",
        "mesa", "mirage", "nekros", "nezha", "nidus", "nova", "nyx", "oberon", "octavia",
        "oraxia", "protea", "qorvex", "revenant", "rhino", "saryn", "sevagoth", "styanax",
        "titania", "trinity", "valkyr", "vauban", "volt", "voruna", "wisp", "wukong", "xaku",
        "yareli", "zephyr"
    )

    private val protoframes = setOf(
        "amir", "aoi", "arthur", "eleanor", "lettie", "quincy"
    )

    fun load(context: Context): List<WarframeItem> = buildList {
        val rootImages = context.assets.list("").orEmpty()
            .filter { it.endsWith(".png", ignoreCase = true) }
            .map { it.substringBeforeLast('.') }

        rootImages.filter { it in warframes }.forEach { assetName ->
            add(frame(assetName, "Warframe"))
        }
        rootImages.filter { it in protoframes }.forEach { assetName ->
            add(frame(assetName, "Protoframes"))
        }

        addAll(weapons(context, "waffen/primär", "warframe_primaerwaffen_", "Primär"))
        addAll(weapons(context, "waffen/sekundär", "warframe_sekundaerwaffen_", "Sekundär"))
        addAll(weapons(context, "waffen/nahkapf", "warframe_nahkampfwaffen_", "Nahkampf"))

        addAll(companions(context, "begleiter/Kavat", "Kavat", "Begleiter"))
        addAll(companions(context, "begleiter/Kubrow", "Kubrow", "Begleiter"))
        addAll(companions(context, "begleiter/Predasite", "Predasite", "Begleiter"))
        addAll(companions(context, "begleiter/Yulpaphyla", "Vulpaphyla", "Begleiter"))
        addAll(companions(context, "begleiter/wächter", "Wächter", "Wächter"))
    }.distinctBy { it.name.lowercase() }

    private fun frame(assetName: String, subTab: String): WarframeItem {
        val name = humanize(assetName)
        val components = if (assetName == "excalibur_umbra") {
            listOf("Warframe")
        } else {
            listOf("Blueprint", "Chassis", "Neuroptics", "Systems")
        }
        return item(name, "warframe", "Tenno", subTab, components)
    }

    private fun weapons(context: Context, path: String, prefix: String, normalSubTab: String): List<WarframeItem> {
        return context.assets.list(path).orEmpty()
            .filter { it.endsWith(".png", ignoreCase = true) }
            .map { file -> file.substringBeforeLast('.').removePrefix(prefix).replace(Regex("\\d+$"), "") }
            .filter { it.isNotBlank() }
            .distinct()
            .map { assetName ->
                val name = humanize(assetName)
                val subTab = if (assetName.contains("prime", ignoreCase = true)) {
                    when (normalSubTab) {
                        "Primär" -> "Primär Prime"
                        "Sekundär" -> "Prime Sekundär"
                        else -> "Prime Nahkampf"
                    }
                } else normalSubTab
                item(name, "weapon", "Waffen", subTab, listOf("Gebaut"))
            }
    }

    private fun companions(context: Context, path: String, subTab: String, tab: String): List<WarframeItem> {
        return context.assets.list(path).orEmpty()
            .filter { it.endsWith(".png", ignoreCase = true) }
            .map { it.substringBeforeLast('.') }
            .distinct()
            .map { assetName ->
                val resolvedSubTab = if (subTab == "Wächter" && assetName.endsWith("Prime")) "Prime Wächter" else subTab
                item(humanize(assetName), "companion", tab, resolvedSubTab, listOf("Vorhanden"))
            }
    }

    private fun item(
        name: String,
        type: String,
        tab: String,
        subTab: String,
        components: List<String>
    ) = WarframeItem(
        name = name,
        type = type,
        tabName = tab,
        subTabName = subTab,
        infoFields = mutableListOf(InfoField("Quelle", "In der App enthaltener Grundkatalog")),
        components = mutableStateListOf<ComponentItem>().also { list ->
            list.addAll(components.map { ComponentItem(it) })
        },
        isNew = false,
        catalogSource = "bundled"
    )

    private fun humanize(value: String): String {
        val separated = value
            .replace('_', ' ')
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .trim()
        return separated.split(Regex("\\s+"))
            .joinToString(" ") { word ->
                when (word.lowercase()) {
                    "prime" -> "Prime"
                    "mk1" -> "MK1"
                    "cyte", "09" -> word.uppercase()
                    else -> word.lowercase().replaceFirstChar(Char::uppercase)
                }
            }
    }
}
