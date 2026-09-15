package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.data.analyzeCloudProgress
import com.example.tennofreunde.data.applyCloudProgress
import com.example.tennofreunde.data.cloudProgressFromDocument
import com.example.tennofreunde.data.CloudRestoreExtras
import com.example.tennofreunde.data.ScannerAddedComponent
import com.example.tennofreunde.data.ScannerAddedItem
import com.example.tennofreunde.data.mergeCloudExtras
import com.example.tennofreunde.data.encodeCloudExtras
import com.example.tennofreunde.data.decodeCloudExtras
import com.google.gson.Gson
import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudProgressMergeTest {

    @Test
    fun compressedCloudExtrasRoundTripAllProfiles() {
        val extras = CloudRestoreExtras(
            formatVersion = 3,
            profiles = listOf("Tenno", "Test"),
            favorites = listOf("Wisp Prime"),
            tabs = listOf(TabItem("Tenno")),
            subTabs = listOf(SubTabItem("Prime Warframes", "Tenno")),
            progressByProfile = mapOf(
                "Tenno" to mapOf("Wisp Prime_Blueprint" to true),
                "Test" to mapOf("Wisp Prime_Blueprint" to false)
            )
        )

        val encoded = encodeCloudExtras(Gson(), extras)
        val restored = decodeCloudExtras(Gson(), encoded)

        assertEquals(extras, restored)
        assertEquals(null, decodeCloudExtras(Gson(), "not-a-backup"))
    }
    @Test
    fun fullCloudExtrasMergeIsIdempotentAndCanonicalizesOldTabs() {
        val items = mutableListOf<WarframeItem>()
        val tabs = mutableListOf(TabItem("Tenno"))
        val subTabs = mutableListOf(SubTabItem("Prime Warframe", "Tenno"))
        val extras = CloudRestoreExtras(
            formatVersion = 2,
            profiles = listOf("Tenno", "Tester"),
            favorites = listOf("Ash Prime"),
            tabs = listOf(TabItem("Prime Warframes"), TabItem("Waffen")),
            subTabs = listOf(
                SubTabItem("Prime Warframes", "Prime Warframes"),
                SubTabItem("Sekundär", "Waffen")
            ),
            scannerAddedItems = listOf(
                ScannerAddedItem(
                    "Eigene Waffe", "weapon", "Waffen", "Sekundär",
                    listOf(ScannerAddedComponent("Gebaut", true, "", "", ""))
                )
            )
        )

        val first = mergeCloudExtras(items, tabs, subTabs, setOf("Tenno"), emptySet(), extras)
        val second = mergeCloudExtras(items, tabs, subTabs, first.profiles, first.favorites, extras)

        assertEquals(1, first.addedTabs)
        assertEquals(1, first.addedSubTabs)
        assertEquals(1, first.addedItems)
        assertEquals(0, second.addedTabs)
        assertEquals(0, second.addedSubTabs)
        assertEquals(0, second.addedItems)
        assertEquals(setOf("Tenno", "Tester"), second.profiles)
        assertEquals(setOf("Ash Prime"), second.favorites)
        assertEquals(2, tabs.size)
        assertEquals(2, subTabs.size)
        assertEquals(1, items.size)
    }

    @Test
    fun readsLegacyFlatCloudDocument() {
        val progress = cloudProgressFromDocument(
            mapOf("Ash_Chassis" to true, "Ash_Systems" to false)
        )

        assertEquals(mapOf("Ash_Chassis" to true, "Ash_Systems" to false), progress)
    }

    @Test
    fun cloudScannerItemCompletesExistingItemWithoutCreatingDuplicate() {
        val existing = WarframeItem(
            name = "Tau Prime",
            type = "warframe",
            tabName = "Tenno",
            subTabName = "Prime Warframe",
            infoFields = mutableListOf(),
            components = mutableStateListOf(ComponentItem("Blueprint", checked = false)),
            isNew = true
        )
        val items = mutableListOf(existing)
        val extras = CloudRestoreExtras(
            scannerAddedItems = listOf(
                ScannerAddedItem(
                    "Tau Prime", "warframe", "Tenno", "Prime Warframe",
                    listOf(
                        ScannerAddedComponent("Blueprint", true, "", "", ""),
                        ScannerAddedComponent("Chassis", true, "", "", "")
                    )
                )
            )
        )

        val result = mergeCloudExtras(items, mutableListOf(), mutableListOf(), emptySet(), emptySet(), extras)

        assertEquals(0, result.addedItems)
        assertEquals(1, items.size)
        assertEquals(2, items.single().components.size)
        assertTrue(items.single().components.first { it.name == "Blueprint" }.checked)
        assertTrue(items.single().components.first { it.name == "Chassis" }.checked)
    }

    @Test
    fun readsProgressFromFullCloudDocumentWithoutTreatingMetadataAsProgress() {
        val progress = cloudProgressFromDocument(
            mapOf(
                "formatVersion" to 2,
                "appVersion" to "10.5",
                "progress" to mapOf("Rhino_Blueprint" to true)
            )
        )

        assertEquals(mapOf("Rhino_Blueprint" to true), progress)
    }

    @Test
    fun previewAndRestoreOnlyTouchKnownComponents() {
        val item = WarframeItem(
            name = "Ash Prime",
            tabName = "Warframes",
            subTabName = "Prime",
            infoFields = mutableListOf(),
            components = mutableStateListOf(
                ComponentItem("Blaupause", checked = false),
                ComponentItem("Systeme", checked = true)
            )
        )
        val cloud = mapOf(
            "Ash Prime_Blaupause" to true,
            "Ash Prime_Systeme" to true,
            "Unbekannt_Teil" to true
        )

        val summary = analyzeCloudProgress(listOf(item), cloud)
        assertEquals(2, summary.matchingComponents)
        assertEquals(1, summary.changedComponents)
        assertEquals(1, summary.unknownCloudEntries)

        assertEquals(1, applyCloudProgress(listOf(item), cloud))
        assertTrue(item.components[0].checked)
        assertTrue(item.components[1].checked)
        assertEquals(2, item.components.size)
    }
}
