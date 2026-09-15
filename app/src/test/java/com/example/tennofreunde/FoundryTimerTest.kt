package com.example.tennofreunde

import com.example.tennofreunde.data.FoundryTimer
import com.example.tennofreunde.data.FoundryTimerStore
import com.example.tennofreunde.data.foundryRemainingLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundryTimerTest {
    @Test
    fun timersRoundTripSortedWithoutDuplicates() {
        val timers = listOf(
            FoundryTimer("two", "Forma", 1_000L, 7_200_000L),
            FoundryTimer("one", "Warframe", 1_000L, 3_600_000L),
            FoundryTimer("one", "Duplicate", 1_000L, 4_000_000L)
        )

        val restored = FoundryTimerStore.decode(FoundryTimerStore.encode(timers))

        assertEquals(2, restored.size)
        assertEquals("one", restored.first().id)
        assertTrue(FoundryTimerStore.decode("broken").isEmpty())
    }

    @Test
    fun countdownRoundsUpAndMarksReady() {
        assertEquals("Fertig", foundryRemainingLabel(0L, true))
        assertEquals("Ready", foundryRemainingLabel(-1L, false))
        assertEquals("1m", foundryRemainingLabel(1L, true))
        assertEquals("1d 2h 1m", foundryRemainingLabel(93_660_000L, true))
    }
}
