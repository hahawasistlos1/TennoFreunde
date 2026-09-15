package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.screens.masteryXpForItem
import org.junit.Assert.assertEquals
import org.junit.Test

class MasteryEstimateTest {
    private fun item(type: String) = WarframeItem(
        name = type,
        type = type,
        tabName = "",
        subTabName = "",
        infoFields = mutableListOf(),
        components = mutableStateListOf()
    )

    @Test
    fun masteryUsesItemTypeAndIgnoresNonMasteryCollections() {
        assertEquals(6_000, masteryXpForItem(item("warframe")))
        assertEquals(6_000, masteryXpForItem(item("companion")))
        assertEquals(3_000, masteryXpForItem(item("weapon")))
        assertEquals(0, masteryXpForItem(item("mod")))
        assertEquals(0, masteryXpForItem(item("resource")))
    }
}
