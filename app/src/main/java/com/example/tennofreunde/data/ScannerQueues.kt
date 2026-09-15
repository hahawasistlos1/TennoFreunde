package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class ScannerQueueState { PENDING, PROCESSING, COMPLETED, FAILED }

data class ScannerQueueEntry(
    val id: String,
    val uri: String,
    val folderKey: String = "",
    val fromFolder: Boolean = false,
    val state: ScannerQueueState = ScannerQueueState.PENDING,
    val attempts: Int = 0,
    val lastError: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

data class PendingCatalogSuggestion(
    val id: String,
    val item: ScannerAddedItem,
    val recognizedText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

object ScannerQueueStore {
    private const val PREFS = "scanner_persistent_queue"
    private const val ENTRIES = "entries_v1"
    private const val PAUSED = "paused"
    private val gson = Gson()

    fun load(context: Context): List<ScannerQueueEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ENTRIES, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ScannerQueueEntry>>() {}.type
        return runCatching { gson.fromJson<List<ScannerQueueEntry>>(raw, type).orEmpty() }
            .getOrDefault(emptyList())
            .let(::recoverInterruptedScannerQueue)
    }

    fun save(context: Context, entries: List<ScannerQueueEntry>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, gson.toJson(entries.takeLast(500)))
            .apply()
    }

    fun isPaused(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PAUSED, false)

    fun setPaused(context: Context, paused: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(PAUSED, paused).apply()
    }
}

fun recoverInterruptedScannerQueue(entries: List<ScannerQueueEntry>): List<ScannerQueueEntry> =
    entries.map {
        if (it.state == ScannerQueueState.PROCESSING || it.state == ScannerQueueState.COMPLETED) {
            it.copy(state = ScannerQueueState.PENDING)
        } else it
    }

object PendingCatalogSuggestionStore {
    private const val PREFS = "scanner_catalog_review"
    private const val ENTRIES = "suggestions_v1"
    private val gson = Gson()

    fun load(context: Context): List<PendingCatalogSuggestion> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ENTRIES, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<PendingCatalogSuggestion>>() {}.type
        return runCatching { gson.fromJson<List<PendingCatalogSuggestion>>(raw, type).orEmpty() }
            .getOrDefault(emptyList())
    }

    fun save(context: Context, entries: List<PendingCatalogSuggestion>): List<PendingCatalogSuggestion> {
        val merged = mergePendingCatalogSuggestions(entries)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, gson.toJson(merged.takeLast(200)))
            .apply()
        return merged
    }
}

fun mergePendingCatalogSuggestions(
    entries: List<PendingCatalogSuggestion>
): List<PendingCatalogSuggestion> {
    val merged = linkedMapOf<String, PendingCatalogSuggestion>()
    entries.forEach { suggestion ->
        if (suggestion.item.name.isBlank()) return@forEach
        val key = CollectionPlacementRules.normalizedKey(suggestion.item.name)
        val current = merged[key]
        if (current == null) {
            merged[key] = suggestion
        } else {
            val components = (current.item.components + suggestion.item.components)
                .groupBy { CollectionPlacementRules.normalizedKey(it.name) }
                .map { (_, versions) ->
                    val preferred = versions.firstOrNull { it.checked } ?: versions.first()
                    preferred.copy(checked = versions.any { it.checked })
                }
            merged[key] = current.copy(
                item = current.item.copy(components = components),
                recognizedText = (current.recognizedText + "\n" + suggestion.recognizedText).trim().take(4_000)
            )
        }
    }
    return merged.values.toList()
}
