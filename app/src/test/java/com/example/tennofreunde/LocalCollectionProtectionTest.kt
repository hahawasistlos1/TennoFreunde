package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.data.preserveLocalCollection
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCollectionProtectionTest {
    @Test
    fun staleRemoteCollectionCannotRemoveLocalItemsOrComponents() {
        val remote = mutableListOf(item("Dante Prime", "Blaupause"))
        val local = listOf(
            item("Dante Prime", "Blaupause", "Systeme"),
            item("Eigener Eintrag", "Teil")
        )

        assertTrue(preserveLocalCollection(remote, local))
        assertEquals(listOf("Dante Prime", "Eigener Eintrag"), remote.map { it.name })
        assertEquals(listOf("Blaupause", "Systeme"), remote.first().components.map { it.name })
    }

    private fun item(name: String, vararg components: String) = WarframeItem(
        name = name,
        tabName = "Tenno",
        subTabName = "Prime Warframe",
        infoFields = mutableListOf(),
        components = mutableStateListOf(*components.map { ComponentItem(it) }.toTypedArray())
    )
}
