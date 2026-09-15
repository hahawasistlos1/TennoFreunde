package com.example.tennofreunde

import com.example.tennofreunde.data.ScannerAddedComponent
import com.example.tennofreunde.data.SharedCollectionItem
import com.example.tennofreunde.data.decodeSharedCollectionItems
import com.example.tennofreunde.data.encodeSharedCollectionItems
import com.example.tennofreunde.data.mergeSharedCollectionItems
import com.example.tennofreunde.data.sharedCatalogShard
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedCollectionCatalogTest {
    @Test
    fun compressedShardRoundTripKeepsCollectionData() {
        val original = listOf(
            SharedCollectionItem(
                "Solara", "warframe", "Tenno", "Warframe", "solara.png", "wfcd_latest",
                listOf(ScannerAddedComponent("Chassis", false, "Mission", "", ""))
            )
        )

        assertEquals(original, decodeSharedCollectionItems(Gson(), encodeSharedCollectionItems(Gson(), original)))
    }

    @Test
    fun mergingARepeatedItemAddsOnlyMissingComponents() {
        val old = SharedCollectionItem(
            "Solara", "warframe", "Tenno", "Warframe",
            components = listOf(ScannerAddedComponent("Blueprint", false, "", "", ""))
        )
        val fresh = old.copy(
            catalogSource = "wfcd_latest",
            components = listOf(ScannerAddedComponent("Chassis", false, "", "", ""))
        )

        val result = mergeSharedCollectionItems(listOf(old), listOf(fresh))

        assertEquals(1, result.size)
        assertEquals(setOf("Blueprint", "Chassis"), result.single().components.map { it.name }.toSet())
        assertEquals("wfcd_latest", result.single().catalogSource)
    }

    @Test
    fun shardAssignmentIsStableAndBounded() {
        val first = sharedCatalogShard("Wisp Prime")
        assertEquals(first, sharedCatalogShard("wisp prime"))
        assertTrue(first.removePrefix("shard_").toInt() in 0..31)
    }
}
