package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

data class RelicInventoryEntry(val id: String, val name: String, val count: Int)

object RelicInventoryStore {
    private const val KEY = "relic_inventory_v2"

    fun load(context: Context): List<RelicInventoryEntry> {
        val raw = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getString(KEY, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<RelicInventoryEntry>>() {}.type
        return normalize(runCatching { Gson().fromJson<List<RelicInventoryEntry>>(raw, type) }.getOrDefault(emptyList()))
    }

    fun save(context: Context, entries: List<RelicInventoryEntry>) {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).edit()
            .putString(KEY, Gson().toJson(normalize(entries)))
            .apply()
    }
}

internal fun parseRelicInventory(text: String): List<RelicInventoryEntry> {
    val pattern = Regex(
        "^\\s*((?:Lith|Meso|Neo|Axi|Requiem)\\s+[A-Za-z0-9-]+)(?:\\s*(?:x|×|:)?\\s*(\\d+))?\\s*$",
        RegexOption.IGNORE_CASE
    )
    return normalize(text.lineSequence().mapNotNull { line ->
        val match = pattern.matchEntire(line) ?: return@mapNotNull null
        val name = match.groupValues[1].trim().split(Regex("\\s+")).joinToString(" ") { part ->
            part.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
        }
        RelicInventoryEntry(relicKey(name), name, match.groupValues[2].toIntOrNull()?.coerceAtLeast(1) ?: 1)
    }.toList())
}

internal fun updateRelicCount(
    entries: List<RelicInventoryEntry>,
    name: String,
    delta: Int
): List<RelicInventoryEntry> {
    val clean = name.trim().replace(Regex("\\s+"), " ")
    if (clean.isBlank()) return normalize(entries)
    val key = relicKey(clean)
    val current = entries.firstOrNull { it.id == key }?.count ?: 0
    val next = current + delta
    val without = entries.filterNot { it.id == key }
    return normalize(if (next > 0) without + RelicInventoryEntry(key, clean, next) else without)
}

private fun normalize(entries: List<RelicInventoryEntry>): List<RelicInventoryEntry> = entries
    .filter { it.name.isNotBlank() && it.count > 0 }
    .groupBy { relicKey(it.name) }
    .map { (key, matches) -> RelicInventoryEntry(key, matches.first().name.trim(), matches.sumOf { it.count }) }
    .sortedWith(compareBy<RelicInventoryEntry>({ relicEraOrder(it.name) }, { it.name.lowercase(Locale.ROOT) }))

private fun relicKey(name: String): String = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

private fun relicEraOrder(name: String): Int = when (name.substringBefore(' ').lowercase(Locale.ROOT)) {
    "lith" -> 0
    "meso" -> 1
    "neo" -> 2
    "axi" -> 3
    "requiem" -> 4
    else -> 5
}
