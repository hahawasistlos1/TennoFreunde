package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.ceil

data class StandingGoal(
    val id: String,
    val name: String,
    val current: Int,
    val target: Int,
    val dailyGain: Int
)

object StandingGoalStore {
    private const val KEY = "standing_goals_v1"

    fun load(context: Context): List<StandingGoal> {
        val raw = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getString(KEY, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<StandingGoal>>() {}.type
        return normalizeStandingGoals(runCatching { Gson().fromJson<List<StandingGoal>>(raw, type) }.getOrDefault(emptyList()))
    }

    fun save(context: Context, goals: List<StandingGoal>) {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).edit()
            .putString(KEY, Gson().toJson(normalizeStandingGoals(goals)))
            .apply()
    }
}

internal fun upsertStandingGoal(goals: List<StandingGoal>, goal: StandingGoal): List<StandingGoal> {
    if (goal.id.isBlank() || goal.name.isBlank() || goal.target <= 0) return normalizeStandingGoals(goals)
    val safe = goal.copy(
        current = goal.current.coerceIn(0, goal.target),
        dailyGain = goal.dailyGain.coerceAtLeast(0)
    )
    return normalizeStandingGoals(goals.filterNot { it.id == safe.id } + safe)
}

internal fun standingDaysRemaining(goal: StandingGoal): Int? {
    val missing = (goal.target - goal.current).coerceAtLeast(0)
    if (missing == 0) return 0
    if (goal.dailyGain <= 0) return null
    return ceil(missing.toDouble() / goal.dailyGain).toInt()
}

internal fun applyStandingDay(goals: List<StandingGoal>, id: String): List<StandingGoal> = goals.map { goal ->
    if (goal.id == id) goal.copy(current = (goal.current + goal.dailyGain).coerceAtMost(goal.target)) else goal
}

private fun normalizeStandingGoals(goals: List<StandingGoal>): List<StandingGoal> = goals
    .filter { it.id.isNotBlank() && it.name.isNotBlank() && it.target > 0 }
    .distinctBy { it.id }
    .sortedBy { it.name.lowercase() }
