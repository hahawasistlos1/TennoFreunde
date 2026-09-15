package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.data.WarframeAcquisitionCatalog
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WarframeAcquisitionCatalogTest {
    @Test
    fun matchesGermanAndEnglishComponentNames() {
        assertEquals("blueprint", WarframeAcquisitionCatalog.componentKey("BP"))
        assertEquals("blueprint", WarframeAcquisitionCatalog.componentKey("Rhino Blueprint"))
        assertEquals("neuroptics", WarframeAcquisitionCatalog.componentKey("Neuroptik"))
        assertEquals("systems", WarframeAcquisitionCatalog.componentKey("Systems"))
        assertEquals("chassis", WarframeAcquisitionCatalog.componentKey("Chassis"))
    }

    @Test
    fun enrichesBlankWarframeComponentSources() {
        val item = warframe("Dante", ComponentItem("BP"), ComponentItem("Neuroptik"))
        val sources = mapOf(
            WarframeAcquisitionCatalog.itemKey("Dante") to mapOf(
                "blueprint" to WarframeAcquisitionCatalog.ComponentSource("Deimos/Armatus, Rotation C | Herstellung: 25.000 Credits"),
                "neuroptics" to WarframeAcquisitionCatalog.ComponentSource("Deimos/Armatus, Rotation C", rotation = "C")
            )
        )

        assertTrue(WarframeAcquisitionCatalog.enrich(item, sources))
        assertEquals("Deimos/Armatus, Rotation C | Herstellung: 25.000 Credits", item.components[0].farmLocation)
        assertEquals("Deimos/Armatus, Rotation C", item.components[1].farmLocation)
        assertEquals("C", item.components[1].rotation)
    }

    @Test
    fun keepsUserEditedFarmLocation() {
        val item = warframe("Rhino", ComponentItem("Chassis", farmLocation = "Mein eigener Ort"))
        val sources = mapOf(
            WarframeAcquisitionCatalog.itemKey("Rhino") to mapOf(
                "chassis" to WarframeAcquisitionCatalog.ComponentSource("Venus/Fossa")
            )
        )

        assertFalse(WarframeAcquisitionCatalog.enrich(item, sources))
        assertEquals("Mein eigener Ort", item.components[0].farmLocation)
    }

    private fun warframe(name: String, vararg components: ComponentItem) = WarframeItem(
        name = name,
        type = "warframe",
        tabName = "Tenno",
        subTabName = "Warframe",
        infoFields = mutableListOf(),
        components = mutableStateListOf(*components)
    )
}
