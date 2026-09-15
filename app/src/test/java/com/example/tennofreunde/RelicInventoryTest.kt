package com.example.tennofreunde

import com.example.tennofreunde.data.parseRelicInventory
import com.example.tennofreunde.data.updateRelicCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicInventoryTest {
    @Test
    fun parsesCommonRelicLinesAndMergesDuplicates() {
        val parsed = parseRelicInventory("Lith A1 x3\nlith a1: 2\nAxi G7 4\nNotiz")

        assertEquals(2, parsed.size)
        assertEquals(5, parsed.first { it.name.equals("Lith A1", true) }.count)
        assertEquals(4, parsed.first { it.name.equals("Axi G7", true) }.count)
    }

    @Test
    fun plusMinusNeverKeepsZeroEntries() {
        val added = updateRelicCount(emptyList(), "Neo N21", 2)
        assertEquals(2, added.single().count)
        assertTrue(updateRelicCount(added, "neo n21", -2).isEmpty())
    }
}
