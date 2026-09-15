package com.example.tennofreunde

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.data.ScannerAddedItem
import com.example.tennofreunde.data.TennoBackup
import com.example.tennofreunde.data.TennoBackupCodec
import com.example.tennofreunde.data.PortablePreferences
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.WarframeItem
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TennoBackupCodecTest {
    private val gson = Gson()

    @Test
    fun fullBackupKeepsProfilesTabsFavoritesAndProgress() {
        val item = WarframeItem(
            name = "Ash",
            tabName = "Tenno",
            subTabName = "Warframe",
            infoFields = mutableListOf(InfoField("Fundort", "Manics")),
            components = mutableStateListOf(ComponentItem("Chassis", checked = true))
        )
        val backup = TennoBackup(
            appVersion = "10.4",
            exportedAt = 1234L,
            activeProfile = "Marcel",
            profiles = listOf("Marcel", "Tester"),
            items = listOf(item),
            tabs = listOf(TabItem("Tenno")),
            subTabs = listOf(SubTabItem("Warframe", "Tenno")),
            progressByProfile = mapOf("Marcel" to mapOf("Ash_Chassis" to true)),
            favorites = listOf("Ash"),
            scannerAddedItems = listOf(ScannerAddedItem.from(item)),
            preferences = PortablePreferences(
                hubStrings = mapOf("foundry_timers_v1" to "timers"),
                hubStringSets = mapOf("tasks_v2_Marcel" to listOf("sortie|DAILY|2026-09-14")),
                settingsBooleans = mapOf("backup_reminder" to true)
            )
        )

        val decoded = TennoBackupCodec.decode(gson, TennoBackupCodec.encode(gson, backup))

        assertFalse(decoded.isLegacy)
        assertEquals("Marcel", decoded.activeProfile)
        assertEquals(listOf("Marcel", "Tester"), decoded.profiles)
        assertEquals("Tenno", decoded.tabs.single().name)
        assertEquals("Warframe", decoded.subTabs.single().name)
        assertEquals(listOf("Ash"), decoded.favorites)
        assertTrue(decoded.progressByProfile.getValue("Marcel").getValue("Ash_Chassis"))
        assertTrue(decoded.items.single().components.single().checked)
        assertEquals(1, decoded.scannerAddedItems.size)
        assertEquals("timers", decoded.preferences.hubStrings["foundry_timers_v1"])
        assertEquals(true, decoded.preferences.settingsBooleans["backup_reminder"])
    }

    @Test
    fun version73ArrayBackupRemainsImportable() {
        val legacyJson = """[{"name":"Rhino","type":"warframe","tabName":"Tenno","subTabName":"Warframe","infoFields":[],"components":[{"name":"Systems","checked":true}],"isNew":false}]"""

        val decoded = TennoBackupCodec.decode(gson, legacyJson)

        assertTrue(decoded.isLegacy)
        assertEquals("Rhino", decoded.items.single().name)
        assertTrue(decoded.items.single().components.single().checked)
    }
}
