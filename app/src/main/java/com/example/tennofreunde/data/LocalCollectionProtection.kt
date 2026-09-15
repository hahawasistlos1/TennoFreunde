package com.example.tennofreunde.data

import com.example.tennofreunde.models.WarframeItem

fun preserveLocalCollection(
    remoteItems: MutableList<WarframeItem>,
    localItems: List<WarframeItem>
): Boolean {
    var changed = false
    localItems.forEach { localItem ->
        val remoteItem = remoteItems.firstOrNull { it.name.equals(localItem.name, ignoreCase = true) }
        if (remoteItem == null) {
            remoteItems.add(localItem)
            changed = true
        } else {
            localItem.components.forEach { localComponent ->
                if (remoteItem.components.none { it.name.equals(localComponent.name, ignoreCase = true) }) {
                    remoteItem.components.add(localComponent)
                    changed = true
                }
            }
        }
    }
    return changed
}
