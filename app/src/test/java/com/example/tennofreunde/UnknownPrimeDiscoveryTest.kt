package com.example.tennofreunde

import com.example.tennofreunde.data.discoverUnknownPrimeItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnknownPrimeDiscoveryTest {
    @Test
    fun unknownWarframeIsPlacedAndCompletedFromNeuroptics() {
        val result = discoverUnknownPrimeItems("TAU PRIME NEUROPTIK BLAUPAUSE", emptyList())

        assertEquals(1, result.size)
        assertEquals("Tau Prime", result.single().name)
        assertEquals("warframe", result.single().type)
        assertEquals("Tenno", result.single().tabName)
        assertEquals("Prime Warframe", result.single().subTabName)
        assertEquals(setOf("Blueprint", "Chassis", "Neuroptics", "Systems"), result.single().components.map { it.name }.toSet())
        assertTrue(result.single().components.first { it.name == "Neuroptics" }.checked)
    }

    @Test
    fun unknownMeleeWeaponIsPlacedByItsBlade() {
        val result = discoverUnknownPrimeItems("VESPER PRIME KLINGE", emptyList())

        assertEquals("weapon", result.single().type)
        assertEquals("Waffen", result.single().tabName)
        assertEquals("Prime Nahkampf", result.single().subTabName)
        assertEquals("Blade", result.single().components.single().name)
    }

    @Test
    fun knownOrNearlyIdenticalNameIsNotCreatedAgain() {
        val result = discoverUnknownPrimeItems("WISP PRIME CHASSIS", listOf("Wisp Prime"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun completelyNewNonPrimeWarframeCanBeDiscoveredFromAComponentLine() {
        val result = discoverUnknownPrimeItems("SOLARA CHASSIS", emptyList())

        assertEquals("Solara", result.single().name)
        assertEquals("warframe", result.single().type)
        assertEquals("Warframe", result.single().subTabName)
        assertTrue(result.single().components.first { it.name == "Chassis" }.checked)
    }
}
