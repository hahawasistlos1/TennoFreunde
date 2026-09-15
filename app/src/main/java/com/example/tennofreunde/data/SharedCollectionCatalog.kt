package com.example.tennofreunde.data

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class SharedCollectionItem(
    val name: String,
    val type: String,
    val tabName: String,
    val subTabName: String,
    val imageName: String = "",
    val catalogSource: String = "shared_catalog",
    val components: List<ScannerAddedComponent> = emptyList()
) {
    fun toWarframeItem() = WarframeItem(
        name = name,
        type = type,
        tabName = tabName,
        subTabName = subTabName,
        infoFields = mutableListOf(),
        components = mutableStateListOf<ComponentItem>().also { list ->
            list.addAll(components.map { it.toComponentItem().copy(checked = false) })
        },
        isNew = true,
        imageName = imageName,
        catalogSource = "shared_catalog"
    )

    companion object {
        fun from(item: WarframeItem) = SharedCollectionItem(
            name = item.name,
            type = item.type,
            tabName = item.tabName,
            subTabName = item.subTabName,
            imageName = item.imageName,
            catalogSource = item.catalogSource,
            components = item.components.map {
                ScannerAddedComponent(it.name, false, it.farmLocation, it.relic, it.rotation)
            }
        )
    }
}

fun sharedCatalogShard(name: String, shardCount: Int = 32): String {
    val normalized = CollectionPlacementRules.normalizedKey(name)
    val shard = (normalized.hashCode().toLong() and 0x7fffffffL) % shardCount
    return "shard_${shard.toString().padStart(2, '0')}"
}

fun mergeSharedCollectionItems(
    existing: List<SharedCollectionItem>,
    incoming: List<SharedCollectionItem>
): List<SharedCollectionItem> {
    val merged = linkedMapOf<String, SharedCollectionItem>()
    (existing + incoming).forEach { candidate ->
        if (candidate.name.isBlank()) return@forEach
        val key = CollectionPlacementRules.normalizedKey(candidate.name)
        val current = merged[key]
        if (current == null) {
            merged[key] = candidate.copy(components = candidate.components.distinctBy(::componentKey))
        } else {
            val preferred = if (candidate.catalogSource in setOf("wfcd", "wfcd_latest", "scanner_discovered")) candidate else current
            merged[key] = preferred.copy(
                imageName = preferred.imageName.ifBlank { current.imageName.ifBlank { candidate.imageName } },
                components = (current.components + candidate.components).distinctBy(::componentKey)
            )
        }
    }
    return merged.values.sortedBy { it.name.lowercase(Locale.ROOT) }
}

fun encodeSharedCollectionItems(gson: Gson, items: List<SharedCollectionItem>): String {
    val raw = gson.toJson(items).toByteArray(Charsets.UTF_8)
    val compressed = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(raw) }
        output.toByteArray()
    }
    return Base64.getEncoder().encodeToString(compressed)
}

fun decodeSharedCollectionItems(gson: Gson, encoded: String?): List<SharedCollectionItem> {
    if (encoded.isNullOrBlank()) return emptyList()
    return runCatching {
        val json = GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(encoded)))
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val type = object : TypeToken<List<SharedCollectionItem>>() {}.type
        gson.fromJson<List<SharedCollectionItem>>(json, type).orEmpty()
    }.getOrDefault(emptyList())
}

private fun componentKey(component: ScannerAddedComponent): String =
    CollectionPlacementRules.normalizedKey(component.name)
