package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ProgressionGoal(
    val id: String,
    val title: String,
    val category: String,
    val current: Int,
    val target: Int
) {
    val progress: Float get() = if (target <= 0) 0f else current.toFloat() / target
}

object ProgressionGoalStore {
    private fun key(profile: String) = "progression_goals_v1_$profile"

    fun load(context: Context, profile: String): List<ProgressionGoal> {
        val raw = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getString(key(profile), null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ProgressionGoal>>() {}.type
        return normalizeProgressionGoals(runCatching { Gson().fromJson<List<ProgressionGoal>>(raw, type) }.getOrDefault(emptyList()))
    }

    fun save(context: Context, profile: String, goals: List<ProgressionGoal>) {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).edit()
            .putString(key(profile), Gson().toJson(normalizeProgressionGoals(goals)))
            .apply()
    }
}

internal fun upsertProgressionGoal(goals: List<ProgressionGoal>, goal: ProgressionGoal): List<ProgressionGoal> {
    if (goal.id.isBlank() || goal.title.isBlank() || goal.target <= 0) return normalizeProgressionGoals(goals)
    val safe = goal.copy(current = goal.current.coerceIn(0, goal.target))
    return normalizeProgressionGoals(goals.filterNot { it.id == safe.id } + safe)
}

internal fun adjustProgressionGoal(goals: List<ProgressionGoal>, id: String, amount: Int): List<ProgressionGoal> =
    normalizeProgressionGoals(goals.map { goal ->
        if (goal.id == id) goal.copy(current = (goal.current + amount).coerceIn(0, goal.target)) else goal
    })

internal fun progressionCategoryProgress(goals: List<ProgressionGoal>, category: String): Float {
    val selected = goals.filter { it.category == category }
    val total = selected.sumOf { it.target }.coerceAtLeast(1)
    return selected.sumOf { it.current }.toFloat() / total
}

private fun normalizeProgressionGoals(goals: List<ProgressionGoal>): List<ProgressionGoal> = goals
    .filter { it.id.isNotBlank() && it.title.isNotBlank() && it.target > 0 }
    .distinctBy { it.id }
    .sortedWith(compareBy<ProgressionGoal>({ it.category }, { it.title.lowercase() }))
