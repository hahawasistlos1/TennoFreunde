package com.example.tennofreunde

import com.example.tennofreunde.components.swapCollectionItems
import com.example.tennofreunde.models.WarframeItem
import androidx.compose.runtime.mutableStateListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionOrderingTest {
    @Test
    fun draggedItemsSwapInPersistentBackingList() {
        val first = item("Ash")
        val second = item("Banshee")
        val items = mutableListOf(first, second)
        assertTrue(swapCollectionItems(items, first, second))
        assertEquals(listOf("Banshee", "Ash"), items.map { it.name })
        assertFalse(swapCollectionItems(items, first, item("Missing")))
    }

    private fun item(name: String) = WarframeItem(
        name = name,
        tabName = "Warframes",
        subTabName = "Prime",
        infoFields = mutableListOf(),
        components = mutableStateListOf()
    )
}
