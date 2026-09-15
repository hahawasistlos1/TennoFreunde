package com.example.tennofreunde

import com.example.tennofreunde.api.BaroResponse
import com.example.tennofreunde.screens.baroIsActive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LiveReliabilityTest {
    @Test
    fun baroActivityIsDerivedWhenApiOmitsActiveField() {
        val baro = BaroResponse(
            activation = "2026-09-04T13:00:00Z",
            expiry = "2026-09-06T13:00:00Z",
            active = null
        )

        assertTrue(baroIsActive(baro, Instant.parse("2026-09-05T12:00:00Z")))
        assertFalse(baroIsActive(baro, Instant.parse("2026-09-07T12:00:00Z")))
    }

    @Test
    fun scannerDownsamplesLargeScreenshotsBeforeOcr() {
        assertEquals(2, ocrSampleSize(1080, 2400))
        assertEquals(4, ocrSampleSize(4320, 7680))
        assertEquals(1, ocrSampleSize(1080, 1920))
    }
}
