package com.example.tennofreunde.data

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem

data class ScannerAddedItem(
    val name: String,
    val type: String,
    val tabName: String,
    val subTabName: String,
    val components: List<ScannerAddedComponent>,
    val catalogSource: String = "scanner_discovered"
) {
    fun toWarframeItem(): WarframeItem {
        val placement = CollectionPlacementRules.forItem(type, name, tabName, subTabName)

        return WarframeItem(
            name = name,
            type = type,
            tabName = placement.tabName,
            subTabName = placement.subTabName,
            infoFields = mutableListOf(),
            components = mutableStateListOf<ComponentItem>().also { list ->
                list.addAll(components.map { it.toComponentItem() })
            },
            isNew = true,
            catalogSource = catalogSource
        )
    }

    companion object {
        fun from(item: WarframeItem) = ScannerAddedItem(
            item.name, item.type, item.tabName, item.subTabName,
            item.components.map { ScannerAddedComponent(it.name, it.checked, it.farmLocation, it.relic, it.rotation) },
            item.catalogSource
        )
    }
}

data class ScannerAddedComponent(
    val name: String,
    val checked: Boolean,
    val farmLocation: String,
    val relic: String,
    val rotation: String
) {
    fun toComponentItem() = ComponentItem(name, checked, farmLocation, relic, rotation)
}
