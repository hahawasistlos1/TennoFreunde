package com.example.tennofreunde

import com.example.tennofreunde.data.nextProfileCopyName
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileToolsTest {
    @Test
    fun copyNameNeverCreatesDuplicateIgnoringCase() {
        assertEquals("Tenno Kopie", nextProfileCopyName("Tenno", listOf("Tenno"), true))
        assertEquals(
            "Tenno Kopie 3",
            nextProfileCopyName("Tenno", listOf("Tenno", "tenno kopie", "Tenno Kopie 2"), true)
        )
        assertEquals("Player Copy", nextProfileCopyName("Player", listOf("Player"), false))
    }
}
