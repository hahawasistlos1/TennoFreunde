package com.example.tennofreunde

import com.example.tennofreunde.data.ProgressionGoal
import com.example.tennofreunde.data.adjustProgressionGoal
import com.example.tennofreunde.data.progressionCategoryProgress
import com.example.tennofreunde.data.upsertProgressionGoal
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionGoalsTest {
    @Test
    fun progressIsClampedBetweenZeroAndTarget() {
        val goal = ProgressionGoal("earth", "Erde", "star_chart", 2, 3)
        val increased = adjustProgressionGoal(listOf(goal), "earth", 9).single()
        val decreased = adjustProgressionGoal(listOf(goal), "earth", -9).single()

        assertEquals(3, increased.current)
        assertEquals(0, decreased.current)
    }

    @Test
    fun categoryProgressCombinesAllTargets() {
        val goals = listOf(
            ProgressionGoal("earth", "Erde", "star_chart", 3, 5),
            ProgressionGoal("venus", "Venus", "star_chart", 1, 5),
            ProgressionGoal("quest", "Quest", "quests", 1, 1)
        )

        assertEquals(0.4f, progressionCategoryProgress(goals, "star_chart"), 0.001f)
    }

    @Test
    fun upsertReplacesGoalWithSameId() {
        val old = ProgressionGoal("earth", "Erde", "star_chart", 1, 5)
        val updated = old.copy(current = 4)

        assertEquals(listOf(updated), upsertProgressionGoal(listOf(old), updated))
    }
}
