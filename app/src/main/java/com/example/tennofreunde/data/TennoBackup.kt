package com.example.tennofreunde.data

import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.WarframeItem
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

data class PortablePreferences(
    val hubStrings: Map<String, String> = emptyMap(),
    val hubStringSets: Map<String, List<String>> = emptyMap(),
    val hubBooleans: Map<String, Boolean> = emptyMap(),
    val settingsStrings: Map<String, String> = emptyMap(),
    val settingsBooleans: Map<String, Boolean> = emptyMap()
)

data class TennoBackup(
    val formatVersion: Int = 3,
    val appVersion: String,
    val exportedAt: Long,
    val activeProfile: String,
    val profiles: List<String>,
    val items: List<WarframeItem>,
    val tabs: List<TabItem>,
    val subTabs: List<SubTabItem>,
    val progressByProfile: Map<String, Map<String, Boolean>>,
    val favorites: List<String>,
    val scannerAddedItems: List<ScannerAddedItem>,
    val preferences: PortablePreferences? = null
)

data class ParsedTennoBackup(
    val formatVersion: Int,
    val activeProfile: String,
    val profiles: List<String>,
    val items: List<WarframeItem>,
    val tabs: List<TabItem>,
    val subTabs: List<SubTabItem>,
    val progressByProfile: Map<String, Map<String, Boolean>>,
    val favorites: List<String>,
    val scannerAddedItems: List<ScannerAddedItem>,
    val preferences: PortablePreferences = PortablePreferences()
) {
    val isLegacy: Boolean get() = formatVersion == 1
}

object TennoBackupCodec {
    fun encode(gson: Gson, backup: TennoBackup): String = gson.toJson(backup)

    fun decode(gson: Gson, json: String): ParsedTennoBackup {
        val root = JsonParser.parseString(json)
        if (root.isJsonArray) {
            val itemType = object : TypeToken<List<WarframeItem>>() {}.type
            val items: List<WarframeItem> = gson.fromJson(root, itemType)
            return ParsedTennoBackup(1, "Tenno", listOf("Tenno"), items, emptyList(), emptyList(), emptyMap(), emptyList(), emptyList(), PortablePreferences())
        }
        require(root.isJsonObject) { "Unsupported backup format" }
        val backup = gson.fromJson(root, TennoBackup::class.java)
        require(backup.formatVersion in 2..3) { "Unsupported backup version" }
        return ParsedTennoBackup(
            backup.formatVersion,
            backup.activeProfile.ifBlank { "Tenno" },
            backup.profiles.filter { it.isNotBlank() }.ifEmpty { listOf("Tenno") },
            backup.items,
            backup.tabs,
            backup.subTabs,
            backup.progressByProfile,
            backup.favorites,
            backup.scannerAddedItems,
            backup.preferences ?: PortablePreferences()
        )
    }
}
