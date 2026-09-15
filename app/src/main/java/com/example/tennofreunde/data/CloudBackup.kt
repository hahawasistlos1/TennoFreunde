package com.example.tennofreunde.data

import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class CloudRestoreExtras(
    val formatVersion: Int = 1,
    val profiles: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val tabs: List<TabItem> = emptyList(),
    val subTabs: List<SubTabItem> = emptyList(),
    val scannerAddedItems: List<ScannerAddedItem> = emptyList(),
    val progressByProfile: Map<String, Map<String, Boolean>> = emptyMap(),
    val preferences: PortablePreferences = PortablePreferences()
) {
    val hasFullBackup: Boolean
        get() = formatVersion >= 2
}

data class CloudExtrasMergeResult(
    val profiles: Set<String>,
    val favorites: Set<String>,
    val addedTabs: Int,
    val addedSubTabs: Int,
    val addedItems: Int
)

fun mergeCloudExtras(
    items: MutableList<com.example.tennofreunde.models.WarframeItem>,
    tabs: MutableList<TabItem>,
    subTabs: MutableList<SubTabItem>,
    localProfiles: Set<String>,
    localFavorites: Set<String>,
    extras: CloudRestoreExtras
): CloudExtrasMergeResult {
    var addedTabs = 0
    var addedSubTabs = 0
    var addedItems = 0

    extras.tabs.forEach { restored ->
        val canonical = CollectionPlacementRules.canonicalTabName(restored.name)
        if (tabs.none { CollectionPlacementRules.sameKey(it.name, canonical) }) {
            tabs.add(TabItem(canonical))
            addedTabs++
        }
    }
    extras.subTabs.forEach { restored ->
        val placement = CollectionPlacementRules.forSubTab(restored.parentTab, restored.name)
        if (subTabs.none {
                CollectionPlacementRules.sameKey(it.parentTab, placement.tabName) &&
                    CollectionPlacementRules.sameKey(it.name, placement.subTabName)
            }
        ) {
            subTabs.add(SubTabItem(placement.subTabName, placement.tabName))
            addedSubTabs++
        }
    }
    extras.scannerAddedItems.forEach { saved ->
        val restored = saved.toWarframeItem()
        val existing = items.firstOrNull { it.name.equals(restored.name, ignoreCase = true) }
        if (existing == null) {
            items.add(restored)
            addedItems++
        } else {
            restored.components.forEach { cloudComponent ->
                val localIndex = existing.components.indexOfFirst {
                    CollectionPlacementRules.sameKey(it.name, cloudComponent.name)
                }
                if (localIndex < 0) {
                    existing.components.add(cloudComponent)
                } else if (cloudComponent.checked && !existing.components[localIndex].checked) {
                    existing.components[localIndex] = existing.components[localIndex].copy(checked = true)
                }
            }
        }
    }

    return CloudExtrasMergeResult(
        profiles = localProfiles + extras.profiles.filter { it.isNotBlank() },
        favorites = localFavorites + extras.favorites.filter { it.isNotBlank() },
        addedTabs = addedTabs,
        addedSubTabs = addedSubTabs,
        addedItems = addedItems
    )
}

fun cloudProgressFromDocument(data: Map<String, Any?>?): Map<String, Boolean> {
    if (data == null) return emptyMap()
    val nested = data["progress"] as? Map<*, *>
    val source = nested ?: data
    return source.entries.mapNotNull { (rawKey, rawValue) ->
        val key = rawKey as? String ?: return@mapNotNull null
        val value = rawValue as? Boolean ?: return@mapNotNull null
        key to value
    }.toMap()
}

/** Keeps a full multi-profile backup well below Firestore's document size limit. */
fun encodeCloudExtras(gson: Gson, extras: CloudRestoreExtras): String {
    val raw = gson.toJson(extras).toByteArray(Charsets.UTF_8)
    val compressed = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(raw) }
        output.toByteArray()
    }
    return Base64.getEncoder().encodeToString(compressed)
}

fun decodeCloudExtras(gson: Gson, encoded: String?): CloudRestoreExtras? {
    if (encoded.isNullOrBlank()) return null
    return runCatching {
        val compressed = Base64.getDecoder().decode(encoded)
        val json = GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
        gson.fromJson(json, CloudRestoreExtras::class.java)
    }.getOrNull()
}
