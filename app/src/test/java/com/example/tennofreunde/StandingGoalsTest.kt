package com.example.tennofreunde

import com.example.tennofreunde.data.StandingGoal
import com.example.tennofreunde.data.applyStandingDay
import com.example.tennofreunde.data.standingDaysRemaining
import com.example.tennofreunde.data.upsertStandingGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StandingGoalsTest {
    @Test
    fun remainingDaysRoundUpAndDailyProgressStopsAtTarget() {
        val goal = StandingGoal("entrati", "Entrati", 12_000, 50_000, 16_000)
        assertEquals(3, standingDaysRemaining(goal))

        val afterOneDay = applyStandingDay(listOf(goal), goal.id).single()
        assertEquals(28_000, afterOneDay.current)
        val finished = applyStandingDay(listOf(afterOneDay.copy(current = 49_000)), goal.id).single()
        assertEquals(50_000, finished.current)
        assertEquals(0, standingDaysRemaining(finished))
    }

    @Test
    fun zeroDailyGainHasNoFalseCompletionDateAndValuesAreClamped() {
        val goal = StandingGoal("one", "Goal", 90_000, 50_000, -20)
        val saved = upsertStandingGoal(emptyList(), goal).single()

        assertEquals(50_000, saved.current)
        assertEquals(0, saved.dailyGain)
        assertEquals(0, standingDaysRemaining(saved))
        assertNull(standingDaysRemaining(saved.copy(current = 10_000)))
    }
}
