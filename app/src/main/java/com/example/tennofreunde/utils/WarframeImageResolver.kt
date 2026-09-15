package com.example.tennofreunde.utils

import com.example.tennofreunde.models.WarframeItem

fun warframeItemImageUrl(item: WarframeItem): String {
    val imageName = item.imageName.ifBlank {
        item.infoFields.firstOrNull {
            it.title.equals("Bilddatei", ignoreCase = true) ||
                it.title.equals("imageName", ignoreCase = true)
        }?.value.orEmpty()
    }

    return if (imageName.isNotBlank()) {
        "https://cdn.warframestat.us/img/$imageName"
    } else {
        warframeItemImageUrl(item.name)
    }
}

fun warframeItemImageUrl(name: String): String {
    val imageName = name
        .trim()
        .split(Regex("\\s+"))
        .joinToString("") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
        .replace(Regex("[^A-Za-z0-9]"), "")

    return "https://cdn.warframestat.us/img/$imageName.png"
}
