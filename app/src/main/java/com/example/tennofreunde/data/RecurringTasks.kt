package com.example.tennofreunde.data

import java.time.LocalDate
import java.time.temporal.WeekFields

enum class TaskCadence { DAILY, WEEKLY }

data class RecurringTask(
    val id: String,
    val germanName: String,
    val englishName: String,
    val cadence: TaskCadence
) {
    fun name(german: Boolean): String = if (german) germanName else englishName
}

fun defaultRecurringTasks(): List<RecurringTask> = listOf(
    RecurringTask("sortie", "Tägliche Sortie", "Daily sortie", TaskCadence.DAILY),
    RecurringTask("nightwave", "Nightwave", "Nightwave", TaskCadence.WEEKLY),
    RecurringTask("archon", "Archon-Jagd", "Archon hunt", TaskCadence.WEEKLY),
    RecurringTask("steel_path", "Stahlpfad", "Steel Path", TaskCadence.DAILY),
    RecurringTask("relics", "Relikte öffnen", "Open relics", TaskCadence.DAILY)
)

internal fun taskPeriodKey(cadence: TaskCadence, date: LocalDate): String = when (cadence) {
    TaskCadence.DAILY -> date.toString()
    TaskCadence.WEEKLY -> {
        val fields = WeekFields.ISO
        "${date.get(fields.weekBasedYear())}-W${date.get(fields.weekOfWeekBasedYear()).toString().padStart(2, '0')}"
    }
}

internal fun encodeTaskCompletion(task: RecurringTask, date: LocalDate): String =
    "${task.id}|${task.cadence.name}|${taskPeriodKey(task.cadence, date)}"

internal fun activeTaskIds(
    stored: Set<String>,
    tasks: List<RecurringTask>,
    date: LocalDate
): Set<String> = tasks.mapNotNullTo(mutableSetOf()) { task ->
    task.id.takeIf { encodeTaskCompletion(task, date) in stored }
}
