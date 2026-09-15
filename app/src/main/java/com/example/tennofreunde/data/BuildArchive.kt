package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedBuild(
    val id: String,
    val name: String,
    val target: String,
    val purpose: String,
    val mods: String,
    val arcanes: String,
    val helminth: String,
    val focusSchool: String,
    val favorite: Boolean = false,
    val updatedAt: Long
)

object BuildArchiveStore {
    private const val KEY = "saved_builds_v1"

    fun load(context: Context): List<SavedBuild> {
        val raw = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getString(KEY, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<SavedBuild>>() {}.type
        return normalizeBuilds(runCatching { Gson().fromJson<List<SavedBuild>>(raw, type) }.getOrDefault(emptyList()))
    }

    fun save(context: Context, builds: List<SavedBuild>) {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).edit()
            .putString(KEY, Gson().toJson(normalizeBuilds(builds)))
            .apply()
    }
}

internal fun upsertBuild(builds: List<SavedBuild>, build: SavedBuild): List<SavedBuild> {
    if (build.id.isBlank() || build.name.isBlank() || build.target.isBlank()) return normalizeBuilds(builds)
    return normalizeBuilds(builds.filterNot { it.id == build.id } + build)
}

internal fun duplicateBuild(builds: List<SavedBuild>, source: SavedBuild, now: Long, german: Boolean): List<SavedBuild> {
    val existingNames = builds.map { it.name.lowercase() }.toSet()
    val suffix = if (german) "Kopie" else "Copy"
    var number = 1
    var name: String
    do {
        name = if (number == 1) "${source.name} $suffix" else "${source.name} $suffix $number"
        number++
    } while (name.lowercase() in existingNames)
    return upsertBuild(builds, source.copy(id = "build_$now", name = name, favorite = false, updatedAt = now))
}

internal fun buildCompleteness(build: SavedBuild): Int {
    val checks = listOf(
        build.name.isNotBlank(),
        build.target.isNotBlank(),
        build.purpose.isNotBlank(),
        build.mods.lineSequence().count { it.isNotBlank() } >= 3,
        build.arcanes.isNotBlank() || build.helminth.isNotBlank() || build.focusSchool.isNotBlank()
    )
    return checks.count { it } * 20
}

internal fun buildShareText(build: SavedBuild, german: Boolean): String = buildString {
    appendLine("${build.name} — ${build.target}")
    appendLine(if (german) "Einsatzzweck: ${build.purpose}" else "Purpose: ${build.purpose}")
    if (build.mods.isNotBlank()) appendLine("Mods:\n${build.mods.trim()}")
    if (build.arcanes.isNotBlank()) appendLine("${if (german) "Arkanes" else "Arcanes"}: ${build.arcanes.trim()}")
    if (build.helminth.isNotBlank()) appendLine("Helminth: ${build.helminth.trim()}")
    if (build.focusSchool.isNotBlank()) appendLine("${if (german) "Fokus" else "Focus"}: ${build.focusSchool.trim()}")
}.trim()

private fun normalizeBuilds(builds: List<SavedBuild>): List<SavedBuild> = builds
    .filter { it.id.isNotBlank() && it.name.isNotBlank() && it.target.isNotBlank() }
    .distinctBy { it.id }
    .sortedWith(compareByDescending<SavedBuild> { it.favorite }.thenByDescending { it.updatedAt })
