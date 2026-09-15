package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.security.MessageDigest

enum class ScannerQueueState { PENDING, PROCESSING, OCR_READY, COMPLETED, FAILED }

data class ScannerQueueEntry(
    val id: String,
    val uri: String,
    val folderKey: String = "",
    val fromFolder: Boolean = false,
    val state: ScannerQueueState = ScannerQueueState.PENDING,
    val attempts: Int = 0,
    val lastError: String = "",
    val recognizedTextFile: String = "",
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

    @Synchronized
    fun load(context: Context): List<ScannerQueueEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ENTRIES, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ScannerQueueEntry>>() {}.type
        return runCatching { gson.fromJson<List<ScannerQueueEntry>>(raw, type).orEmpty() }
            .getOrDefault(emptyList())
    }

    @Synchronized
    fun save(context: Context, entries: List<ScannerQueueEntry>) {
        deleteUnusedTextFiles(context, load(context), entries)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, gson.toJson(entries.takeLast(500)))
            .apply()
    }

    @Synchronized
    fun update(
        context: Context,
        transform: (List<ScannerQueueEntry>) -> List<ScannerQueueEntry>
    ): List<ScannerQueueEntry> {
        val before = load(context)
        val updated = transform(before).takeLast(500)
        deleteUnusedTextFiles(context, before, updated)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ENTRIES, gson.toJson(updated))
            .commit()
        return updated
    }

    @Synchronized
    fun saveRecognizedText(context: Context, id: String, text: String): String {
        val directory = File(context.filesDir, "scanner_ocr").apply { mkdirs() }
        val fileKey = MessageDigest.getInstance("SHA-256")
            .digest(id.toByteArray(Charsets.UTF_8))
            .take(16)
            .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        val file = File(directory, "$fileKey.txt")
        file.writeText(text.take(50_000), Charsets.UTF_8)
        return file.absolutePath
    }

    fun readRecognizedText(entry: ScannerQueueEntry): String {
        if (entry.recognizedTextFile.isBlank()) return ""
        return runCatching { File(entry.recognizedTextFile).readText(Charsets.UTF_8) }.getOrDefault("")
    }

    fun isPaused(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PAUSED, false)

    fun setPaused(context: Context, paused: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(PAUSED, paused).apply()
    }

    fun isForegroundActive(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).let { preferences ->
            preferences.getBoolean("foreground_active", false) &&
                System.currentTimeMillis() - preferences.getLong("foreground_active_at", 0L) < 60_000L
        }

    fun setForegroundActive(context: Context, active: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("foreground_active", active)
            .putLong("foreground_active_at", if (active) System.currentTimeMillis() else 0L)
            .commit()
    }

    private fun deleteUnusedTextFiles(
        context: Context,
        before: List<ScannerQueueEntry>,
        after: List<ScannerQueueEntry>
    ) {
        val retained = after.mapTo(hashSetOf()) { it.recognizedTextFile }.filter { it.isNotBlank() }.toSet()
        before.map { it.recognizedTextFile }
            .filter { it.isNotBlank() && it !in retained }
            .forEach { path ->
                runCatching {
                    val file = File(path)
                    if (file.parentFile == File(context.filesDir, "scanner_ocr")) file.delete()
                }
            }
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
