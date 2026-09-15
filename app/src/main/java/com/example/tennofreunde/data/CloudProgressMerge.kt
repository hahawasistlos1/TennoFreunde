package com.example.tennofreunde.data

import com.example.tennofreunde.models.WarframeItem

data class CloudRestoreSummary(
    val localChecked: Int,
    val cloudChecked: Int,
    val matchingComponents: Int,
    val changedComponents: Int,
    val unknownCloudEntries: Int
)

fun analyzeCloudProgress(
    items: List<WarframeItem>,
    cloudProgress: Map<String, Boolean>
): CloudRestoreSummary {
    val localComponents = items.flatMap { item ->
        item.components.map { component -> "${item.name}_${component.name}" to component.checked }
    }.toMap()
    val matchingKeys = cloudProgress.keys.intersect(localComponents.keys)
    return CloudRestoreSummary(
        localChecked = localComponents.values.count { it },
        cloudChecked = cloudProgress.values.count { it },
        matchingComponents = matchingKeys.size,
        changedComponents = matchingKeys.count { key -> localComponents[key] != cloudProgress[key] },
        unknownCloudEntries = cloudProgress.keys.count { it !in localComponents }
    )
}

fun applyCloudProgress(
    items: List<WarframeItem>,
    cloudProgress: Map<String, Boolean>
): Int {
    var changed = 0
    items.forEach { item ->
        item.components.forEachIndexed { index, component ->
            cloudProgress["${item.name}_${component.name}"]?.let { checked ->
                if (component.checked != checked) {
                    item.components[index] = component.copy(checked = checked)
                    changed++
                }
            }
        }
    }
    return changed
}
