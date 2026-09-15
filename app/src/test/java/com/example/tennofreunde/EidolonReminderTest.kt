package com.example.tennofreunde

import com.example.tennofreunde.system.eidolonNightMessage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class EidolonReminderTest {
    @Test
    fun messageShowsPreciseRemainingTimeAndLocalStartTime() {
        val now = Instant.parse("2026-09-14T18:00:00Z").toEpochMilli()
        val start = now + 8 * 60_000L + 30_000L

        assertEquals(
            "Eidolon-Nacht beginnt in 8 Min 30 Sek (um 20:08 Uhr).",
            eidolonNightMessage(now, start, ZoneId.of("Europe/Berlin"))
        )
    }

    @Test
    fun messageRoundsPartialSecondUp() {
        assertEquals(
            "Eidolon-Nacht beginnt in 1 Sek (um 00:00 Uhr).",
            eidolonNightMessage(0L, 1L, ZoneId.of("UTC"))
        )
    }
}
