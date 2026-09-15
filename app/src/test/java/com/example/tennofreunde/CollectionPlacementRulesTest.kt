package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.data.CollectionPlacementRules
import com.example.tennofreunde.data.ScannerAddedComponent
import com.example.tennofreunde.data.ScannerAddedItem
import com.example.tennofreunde.models.WarframeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionPlacementRulesTest {
    @Test
    fun primeWarframesUseTennoPrimeWarframeTabs() {
        val item = WarframeItem(
            name = "Dante Prime",
            type = "warframe",
            tabName = "Warframes",
            subTabName = "Prime Warframes",
            infoFields = mutableListOf(),
            components = mutableStateListOf()
        )

        val changed = CollectionPlacementRules.applyTo(item)

        assertTrue(changed)
        assertEquals("Tenno", item.tabName)
        assertEquals("Prime Warframe", item.subTabName)
    }

    @Test
    fun scannerSavedPrimeWeaponUsesCanonicalWeaponCategory() {
        val saved = ScannerAddedItem(
            name = "Aklex Prime",
            type = "weapon",
            tabName = "Waffen",
            subTabName = "Prime Waffen",
            components = listOf(ScannerAddedComponent("Blueprint", true, "", "", ""))
        )

        val restored = saved.toWarframeItem()

        assertEquals("Waffen", restored.tabName)
        assertEquals("Prime Sekundär", restored.subTabName)
    }

    @Test
    fun generatedPrimeWeaponsAreSplitIntoKnownWeaponSubTabs() {
        assertEquals("Primär Prime", CollectionPlacementRules.primeWeaponCategory("Paris Prime"))
        assertEquals("Prime Sekundär", CollectionPlacementRules.primeWeaponCategory("Lex Prime"))
        assertEquals("Prime Nahkampf", CollectionPlacementRules.primeWeaponCategory("Glaive Prime"))
    }

    @Test
    fun weaponSubTabsWinEvenWhenOldItemTypeSaysWarframe() {
        val item = WarframeItem(
            name = "Broken War",
            type = "warframe",
            tabName = "Tenno",
            subTabName = "Nahkampf",
            infoFields = mutableListOf(),
            components = mutableStateListOf()
        )

        val changed = CollectionPlacementRules.applyTo(item)

        assertTrue(changed)
        assertEquals("Waffen", item.tabName)
        assertEquals("Nahkampf", item.subTabName)
    }

    @Test
    fun knownCompanionAndSentinelSubTabsKeepTheirOwnMainTabs() {
        val companion = CollectionPlacementRules.forSubTab("Tenno", "Kavat")
        val sentinel = CollectionPlacementRules.forSubTab("Tenno", "Prime Wachter")

        assertEquals("Begleiter", companion.tabName)
        assertEquals("Kavat", companion.subTabName)
        assertEquals("Wächter", sentinel.tabName)
        assertEquals("Prime Wächter", sentinel.subTabName)
    }

    @Test
    fun tabNamesAreCanonicalizedForDuplicateCleanup() {
        assertEquals("Waffen", CollectionPlacementRules.canonicalTabName(" Waffen "))
        assertEquals("Wächter", CollectionPlacementRules.canonicalTabName("Wachter"))
        assertEquals("Tenno", CollectionPlacementRules.canonicalTabName("Prime Warframe"))
        assertEquals("Tenno", CollectionPlacementRules.canonicalTabName("Prime Warframes"))
        assertEquals("Waffen", CollectionPlacementRules.canonicalTabName("Prime Waffen"))
        assertTrue(CollectionPlacementRules.sameKey("Sekundär", "sekundar"))
    }

    @Test
    fun resourcesAndModsUseTheirOwnMainTabs() {
        val resource = WarframeItem(
            name = "Orokin Cell",
            type = "resource",
            tabName = "Tenno",
            subTabName = "Selten",
            infoFields = mutableListOf(),
            components = mutableStateListOf()
        )
        val mod = WarframeItem(
            name = "Serration",
            type = "mod",
            tabName = "Tenno",
            subTabName = "Primär",
            infoFields = mutableListOf(),
            components = mutableStateListOf()
        )

        CollectionPlacementRules.applyTo(resource)
        CollectionPlacementRules.applyTo(mod)

        assertEquals("Ressourcen", resource.tabName)
        assertEquals("Selten", resource.subTabName)
        assertEquals("Mods", mod.tabName)
        assertEquals("Primär", mod.subTabName)
    }
}
