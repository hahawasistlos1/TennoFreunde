package com.example.tennofreunde

import com.example.tennofreunde.data.TaskCadence
import com.example.tennofreunde.data.activeTaskIds
import com.example.tennofreunde.data.defaultRecurringTasks
import com.example.tennofreunde.data.encodeTaskCompletion
import com.example.tennofreunde.data.taskPeriodKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecurringTasksTest {
    @Test
    fun dailyTaskExpiresNextDayWhileWeeklyTaskStaysInSameWeek() {
        val monday = LocalDate.of(2026, 9, 14)
        val tuesday = monday.plusDays(1)
        val tasks = defaultRecurringTasks()
        val sortie = tasks.first { it.id == "sortie" }
        val nightwave = tasks.first { it.id == "nightwave" }
        val stored = setOf(
            encodeTaskCompletion(sortie, monday),
            encodeTaskCompletion(nightwave, monday)
        )

        val activeTuesday = activeTaskIds(stored, tasks, tuesday)

        assertFalse("sortie" in activeTuesday)
        assertTrue("nightwave" in activeTuesday)
    }

    @Test
    fun weeklyTaskExpiresOnMondayAndIsoYearBoundaryIsStable() {
        val sunday = LocalDate.of(2027, 1, 3)
        val monday = sunday.plusDays(1)
        assertEquals("2026-W53", taskPeriodKey(TaskCadence.WEEKLY, sunday))
        assertEquals("2027-W01", taskPeriodKey(TaskCadence.WEEKLY, monday))
    }
}
