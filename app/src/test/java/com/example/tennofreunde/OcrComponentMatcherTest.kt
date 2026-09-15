package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.data.PrimeCatalogItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrComponentMatcherTest {
    @Test
    fun landscapeInventoryCropKeepsThePrimeGridAndDropsTheSidePanel() {
        assertEquals(OcrCrop(0, 172, 2918, 1901), inventoryOcrCrop(3840, 2160))
        assertEquals(OcrCrop(0, 86, 1778, 950), inventoryOcrCrop(2340, 1080))
        assertEquals(null, inventoryOcrCrop(1080, 2340))
    }

    @Test
    fun germanConsoleWeaponPartsMatchEnglishCatalogParts() {
        val item = item(
            "Paris Prime",
            ComponentItem("Blueprint"),
            ComponentItem("Grip"),
            ComponentItem("Upper Limb"),
            ComponentItem("String"),
            ComponentItem("Lower Limb")
        )

        val result = reconcileOcrComponents(
            "Paris Prime Blaupause Paris Prime Griff Paris Prime Oberteil Paris Prime Sehne Paris Prime Unterteil",
            listOf(item)
        )

        assertEquals(5, result.size)
        assertTrue(item.components.all { it.checked })
    }

    @Test
    fun androidAndPlayStationGermanLabelsMatchReceiversBarrelsStocksAndLinks() {
        val items = listOf(
            item("Magnus Prime", ComponentItem("Receiver")),
            item("Nagantaka Prime", ComponentItem("Barrel")),
            item("Rubico Prime", ComponentItem("Stock")),
            item("Akarius Prime", ComponentItem("Link")),
            item("Nami Skyla Prime", ComponentItem("Blade"), ComponentItem("Handle"))
        )

        val result = reconcileOcrComponents(
            "Magnus Prime Gehäuse Nagantaka Prime Lauf Rubico Prime Schaft " +
                "Akarius Prime Verbindung Nami Skyla Prime Klinge Nami Skyla Prime Griff",
            items
        )

        assertEquals(6, result.size)
        assertTrue(items.all { it.components.all { component -> component.checked } })
    }

    @Test
    fun similarPrimeNamesDoNotCreateFalseMatches() {
        val items = listOf(
            item("Oberon Prime", ComponentItem("Blueprint")),
            item("Tiberon Prime", ComponentItem("Blueprint")),
            item("Garuda Prime", ComponentItem("Chassis")),
            item("Gara Prime", ComponentItem("Chassis")),
            item("Magnus Prime", ComponentItem("Blueprint")),
            item("Akmagnus Prime", ComponentItem("Blueprint"))
        )

        val result = reconcileOcrComponents(
            "Oberon Prime Blaupause Garuda Prime Chassis Magnus Prime Blaupause",
            items
        )

        assertEquals(setOf("Oberon Prime", "Garuda Prime", "Magnus Prime"), result.map { it.itemName }.toSet())
        assertFalse(items[1].components.single().checked)
        assertFalse(items[3].components.single().checked)
        assertFalse(items[5].components.single().checked)
    }

    @Test
    fun germanScreenshotChecksOnlyTheMatchingComponent() {
        val systems = ComponentItem("Ash Prime Systems Blueprint")
        val chassis = ComponentItem("Ash Prime Chassis Blueprint")
        val item = item("Ash Prime", systems, chassis)

        val result = reconcileOcrComponents("ASH PRIME SYSTEME BLAUPAUSE", listOf(item))

        assertEquals(1, result.size)
        assertEquals(OcrMatchState.NEWLY_CHECKED, result.single().state)
        assertTrue(item.components[0].checked)
        assertFalse(item.components[1].checked)
    }

    @Test
    fun neuropticsBlueprintTextDoesNotCheckTheMainBlueprint() {
        val blueprint = ComponentItem("Mag Prime Blueprint")
        val neuroptics = ComponentItem("Mag Prime Neuroptics")
        val item = item("Mag Prime", blueprint, neuroptics)

        val result = reconcileOcrComponents("Mag Prime Neuroptik Blaupause", listOf(item))

        assertEquals(listOf("Mag Prime Neuroptics"), result.map { it.componentName })
        assertFalse(item.components[0].checked)
        assertTrue(item.components[1].checked)
    }

    @Test
    fun visibleBlueprintDoesNotInventAnotherPartForTheSameWeapon() {
        val blueprint = ComponentItem("Afuris Prime Blueprint")
        val link = ComponentItem("Afuris Prime Link")
        val item = item("Afuris Prime", blueprint, link)

        val result = reconcileOcrComponents(
            "Afuris Prime Blaupause Akbronco Prime Verbindung",
            listOf(item)
        )

        assertEquals(listOf("Afuris Prime Blueprint"), result.map { it.componentName })
        assertTrue(item.components[0].checked)
        assertFalse(item.components[1].checked)
    }

    @Test
    fun visibleWeaponPartsDoNotInventItsMainBlueprint() {
        val blueprint = ComponentItem("Akarius Prime Blueprint")
        val barrel = ComponentItem("Akarius Prime Barrel")
        val link = ComponentItem("Akarius Prime Link")
        val item = item("Akarius Prime", blueprint, barrel, link)

        val result = reconcileOcrComponents(
            "Akarius Prime Lauf Akarius Prime Verbindung Bronco Prime Blaupause",
            listOf(item)
        )

        assertEquals(setOf("Akarius Prime Barrel", "Akarius Prime Link"), result.map { it.componentName }.toSet())
        assertFalse(item.components[0].checked)
        assertTrue(item.components[1].checked)
        assertTrue(item.components[2].checked)
    }

    @Test
    fun missingSpaceBeforePrimeIsRepaired() {
        val chassis = ComponentItem("Banshee Prime Chassis")

        val result = reconcileOcrComponents("BansheePrime Chassis", listOf(item("Banshee Prime", chassis)))

        assertEquals(1, result.size)
        assertTrue(result.single().componentName.contains("Chassis"))
    }

    @Test
    fun alreadyCheckedComponentIsNotChangedAgain() {
        val component = ComponentItem("Ash Prime Neuroptics Blueprint", checked = true)

        val result = reconcileOcrComponents("Ash Prime Neuroptik Blaupause", listOf(item("Ash Prime", component)))

        assertEquals(OcrMatchState.ALREADY_CHECKED, result.single().state)
        assertTrue(component.checked)
    }

    @Test
    fun matchedComponentIsReplacedInStateListForComposeUpdates() {
        val original = ComponentItem("Ash Prime Chassis Blueprint")
        val item = item("Ash Prime", original)

        reconcileOcrComponents("ASH PRIME CHASSIS BLUEPRINT", listOf(item))

        assertTrue(item.components.single().checked)
        assertFalse(original.checked)
    }

    @Test
    fun confirmedPreviewMatchChecksCollectionComponent() {
        val item = item("Ash Prime", ComponentItem("Ash Prime Chassis Blueprint"))
        val preview = listOf(
            OcrComponentMatch("Ash Prime", "Ash Prime Chassis Blueprint", OcrMatchState.NEWLY_CHECKED)
        )

        val applied = applyOcrMatchesToCollection(preview, listOf(item))

        assertEquals(1, applied.size)
        assertEquals(OcrMatchState.NEWLY_CHECKED, applied.single().state)
        assertTrue(item.components.single().checked)
    }

    @Test
    fun confirmedPreviewMatchChecksShortCollectionComponentName() {
        val item = item("Ash Prime", ComponentItem("Chassis"))
        val preview = listOf(
            OcrComponentMatch("Ash Prime", "Ash Prime Chassis Blueprint", OcrMatchState.NEWLY_CHECKED)
        )

        val applied = applyOcrMatchesToCollection(preview, listOf(item))

        assertEquals(1, applied.size)
        assertEquals("Chassis", applied.single().componentName)
        assertTrue(item.components.single().checked)
    }

    @Test
    fun collectionScreenshotWithShortComponentLabelsCreatesMatches() {
        val item = item(
            "Dante",
            ComponentItem("BP"),
            ComponentItem("Neuroptik"),
            ComponentItem("System"),
            ComponentItem("Chassis")
        )

        val preview = previewVisibleCollectionCardComponents(
            "Sammlung Dante 0 / 4 Komponenten BP Neuroptik System Chassis",
            listOf(item)
        )
        val applied = applyOcrMatchesToCollection(preview, listOf(item))

        assertEquals(4, preview.size)
        assertEquals(4, applied.size)
        assertTrue(item.components.all { it.checked })
    }

    @Test
    fun warframeBpMeansMainBlueprint() {
        val item = item("Dante", ComponentItem("BP"), ComponentItem("System"))

        val preview = previewVisibleCollectionCardComponents(
            "Sammlung Dante 0 / 4 Komponenten BP",
            listOf(item)
        )
        val applied = applyOcrMatchesToCollection(preview, listOf(item))

        assertEquals(1, applied.size)
        assertEquals("BP", applied.single().componentName)
        assertTrue(item.components[0].checked)
        assertFalse(item.components[1].checked)
    }

    @Test
    fun warframeMainBlueprintLabelChecksBpComponent() {
        val item = item("Dante", ComponentItem("BP"), ComponentItem("Neuroptik"))

        val applied = applyOcrMatchesToCollection(
            listOf(OcrComponentMatch("Dante", "Hauptblaupause", OcrMatchState.NEWLY_CHECKED)),
            listOf(item)
        )

        assertEquals(1, applied.size)
        assertEquals("BP", applied.single().componentName)
        assertTrue(item.components[0].checked)
        assertFalse(item.components[1].checked)
    }

    @Test
    fun detectedOnlyScanRowsDoNotCheckCollectionComponents() {
        val item = item("Rhino Prime", ComponentItem("Rhino Prime Systems Blueprint"))
        val applied = applyOcrMatchesToCollection(
            listOf(OcrComponentMatch("Rhino Prime", "Rhino Prime Systems Blueprint", OcrMatchState.DETECTED_ONLY)),
            listOf(item)
        )

        assertTrue(applied.isEmpty())
        assertFalse(item.components.single().checked)
    }

    @Test
    fun alreadyCheckedPreviewStillChecksCollectionWhenStoredPartIsMissing() {
        val item = item("Rhino Prime", ComponentItem("Rhino Prime Systems Blueprint"))
        val applied = applyOcrMatchesToCollection(
            listOf(OcrComponentMatch("Rhino Prime", "Rhino Prime Systems Blueprint", OcrMatchState.ALREADY_CHECKED)),
            listOf(item)
        )

        assertEquals(1, applied.size)
        assertEquals(OcrMatchState.NEWLY_CHECKED, applied.single().state)
        assertTrue(item.components.single().checked)
    }

    @Test
    fun confirmedSystemsMatchChecksExistingCollectionPart() {
        val item = item(
            "Rhino Prime",
            ComponentItem("Rhino Prime Blueprint"),
            ComponentItem("Rhino Prime Systems Blueprint")
        )
        val applied = applyOcrMatchesToCollection(
            listOf(OcrComponentMatch("Rhino Prime", "Rhino Prime Systems Blueprint", OcrMatchState.NEWLY_CHECKED)),
            listOf(item)
        )

        assertEquals(1, applied.size)
        assertFalse(item.components[0].checked)
        assertTrue(item.components[1].checked)
    }

    @Test
    fun genericOrUnknownTextNeverChecksAComponent() {
        val component = ComponentItem("Ash Prime Systems Blueprint")

        val result = reconcileOcrComponents("Systeme Blaupause 1", listOf(item("Ash Prime", component)))

        assertTrue(result.isEmpty())
        assertFalse(component.checked)
    }

    @Test
    fun missingCatalogItemIsAddedCompletelyAndMatchedPartIsChecked() {
        val blueprint = ComponentItem("Blueprint")
        val barrel = ComponentItem("Barrel")
        val catalogItem = item("Sagek Prime", blueprint, barrel).apply {
            type = "weapon"
            tabName = "Waffen"
            subTabName = "Prime Waffen"
        }
        val collection = mutableListOf<WarframeItem>()

        val result = addMissingCatalogItems(
            "SAGEK PRIME BLUEPRINT",
            collection,
            listOf(PrimeCatalogItem(catalogItem, listOf("prime sagek bp")))
        )

        assertEquals(1, collection.size)
        assertEquals(2, collection.single().components.size)
        assertTrue(collection.single().components[0].checked)
        assertFalse(collection.single().components[1].checked)
        assertEquals(OcrMatchState.ADDED_TO_COLLECTION, result.single().state)
        assertEquals("Prime Waffen", collection.single().subTabName)
    }

    @Test
    fun existingBasicCatalogItemReceivesAllRealPartsDuringScan() {
        val existing = item("Shade Prime", ComponentItem("Vorhanden"))
        val catalogItem = item(
            "Shade Prime",
            ComponentItem("Blueprint"),
            ComponentItem("Carapace"),
            ComponentItem("Cerebrum"),
            ComponentItem("Systems")
        )
        val collection = mutableListOf(existing)

        val result = addMissingCatalogItems(
            "Shade Prime Cerebrum",
            collection,
            listOf(PrimeCatalogItem(catalogItem, emptyList()))
        )

        assertEquals(5, existing.components.size)
        assertEquals("Cerebrum", result.single().componentName)
        assertTrue(existing.components.first { it.name == "Cerebrum" }.checked)
        assertFalse(existing.components.first { it.name == "Systems" }.checked)
    }

    @Test
    fun normalWeaponNameChecksItsBuiltState() {
        val acceltra = item("Acceltra", ComponentItem("Gebaut"))

        val result = reconcileOcrComponents("ACCELTRA", listOf(acceltra))

        assertEquals(1, result.size)
        assertTrue(acceltra.components.single().checked)
    }

    @Test
    fun primeVariantDoesNotAlsoCheckNormalWeapon() {
        val braton = item("Braton", ComponentItem("Gebaut"))
        val bratonPrime = item("Braton Prime", ComponentItem("Vorhanden"))

        val result = reconcileOcrComponents("BRATON PRIME", listOf(braton, bratonPrime))

        assertEquals(listOf("Braton Prime"), result.map { it.itemName })
        assertFalse(braton.components.single().checked)
        assertTrue(bratonPrime.components.single().checked)
    }

    @Test
    fun builtWarframeLineChecksItsWholeCollectionEntry() {
        val dante = item(
            "Dante",
            ComponentItem("Blueprint"),
            ComponentItem("Chassis"),
            ComponentItem("Neuroptics"),
            ComponentItem("Systems")
        ).apply { type = "warframe" }

        val result = reconcileOcrComponents("DANTE RANG 30", listOf(dante))

        assertEquals(4, result.size)
        assertTrue(dante.components.all { it.checked })
    }

    @Test
    fun splitWarframeComponentLineStillChecksOnlyThatComponent() {
        val dante = item(
            "Dante",
            ComponentItem("Blueprint"),
            ComponentItem("Chassis"),
            ComponentItem("Neuroptics"),
            ComponentItem("Systems")
        ).apply { type = "warframe" }

        val result = reconcileOcrComponents("DANTE\nCHASSIS BLAUPAUSE", listOf(dante))

        assertEquals(listOf("Chassis"), result.map { it.componentName })
        assertFalse(dante.components.first { it.name == "Blueprint" }.checked)
        assertTrue(dante.components.first { it.name == "Chassis" }.checked)
    }

    private fun item(name: String, vararg components: ComponentItem) = WarframeItem(
        name = name,
        tabName = "Prime Warframes",
        subTabName = "",
        infoFields = mutableListOf(),
        components = mutableStateListOf(*components)
    )
}
