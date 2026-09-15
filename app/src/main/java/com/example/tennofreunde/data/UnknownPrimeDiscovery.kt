package com.example.tennofreunde.data

import java.text.Normalizer
import java.util.Locale

private val primeNamePattern = Regex("([A-Za-zÀ-ž0-9][A-Za-zÀ-ž0-9 '\\-]{1,45}?)\\s+Prime\\b", RegexOption.IGNORE_CASE)
private val componentAtLineEndPattern = Regex(
    "^([A-Za-zÀ-ž0-9][A-Za-zÀ-ž0-9 '\\-]{1,50}?)\\s+" +
        "(neuroptik(?:s)?|chassis|systeme?|systems?|cerebrum|carapace|geh[aä]use|receiver|lauf|barrel|schaft|stock|klinge|blade|verbindung|link|oberteil|upper limb|unterteil|lower limb|sehne|string|harness|fl[uü]gel|wings|griff|handle|grip|blaupause|blueprint)$",
    RegexOption.IGNORE_CASE
)

fun discoverUnknownPrimeItems(text: String, knownNames: Collection<String>): List<ScannerAddedItem> {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val known = knownNames.map(::discoveryKey)
    val found = linkedMapOf<String, MutableSet<String>>()

    lines.forEachIndexed { index, line ->
        primeNamePattern.findAll(line).forEach matchLoop@{ match ->
            val rawName = match.groupValues[1].trim().replace(Regex("\\s+"), " ") + " Prime"
            val name = rawName.split(" ").joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar(Char::uppercase)
            }
            val key = discoveryKey(name)
            if (known.any { similarity(it, key) >= 0.88 }) return@matchLoop
            val nearby = buildString {
                append(line.substring(match.range.first))
                lines.getOrNull(index + 1)?.takeUnless { primeNamePattern.containsMatchIn(it) }?.let { append(' ').append(it) }
                lines.getOrNull(index + 2)?.takeUnless { primeNamePattern.containsMatchIn(it) }?.let { append(' ').append(it) }
            }
            detectedComponent(nearby)?.let { found.getOrPut(name) { linkedSetOf() }.add(it) }
        }
    }

    lines.forEach { line ->
        if (primeNamePattern.containsMatchIn(line)) return@forEach
        val match = componentAtLineEndPattern.find(line) ?: return@forEach
        val rawName = match.groupValues[1].trim().replace(Regex("\\s+"), " ")
        if (rawName.length < 3 || rawName.contains("komponent", ignoreCase = true)) return@forEach
        val name = rawName.split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.ROOT).replaceFirstChar(Char::uppercase)
        }
        val key = discoveryKey(name)
        if (known.any { similarity(it, key) >= 0.88 }) return@forEach
        detectedComponent(match.groupValues[2])?.let { found.getOrPut(name) { linkedSetOf() }.add(it) }
    }

    return found.map { (name, detected) -> createDiscoveredItem(name, detected) }
}

private fun createDiscoveredItem(name: String, detected: Set<String>): ScannerAddedItem {
    val isPrime = name.endsWith(" Prime", ignoreCase = true)
    val type = when {
        detected.any { it in setOf("Neuroptics", "Chassis") } -> "warframe"
        detected.any { it in setOf("Cerebrum", "Carapace") } -> "companion"
        detected.any { it in setOf("Harness", "Wings") } -> "archwing"
        else -> "weapon"
    }
    val placement = when (type) {
        "warframe" -> CollectionPlacement("Tenno", if (isPrime) "Prime Warframe" else "Warframe")
        "companion" -> CollectionPlacement("Wächter", if (isPrime) "Prime Wächter" else "Wächter")
        "archwing" -> CollectionPlacement("Tenno", "Archwing")
        else -> {
            val subTab = when {
                detected.any { it in setOf("Blade", "Handle", "Guard", "Hilt", "Upper Limb", "Lower Limb", "String") } -> if (isPrime) "Prime Nahkampf" else "Nahkampf"
                "Link" in detected -> if (isPrime) "Prime Sekundär" else "Sekundär"
                else -> if (isPrime) "Primär Prime" else "Primär"
            }
            CollectionPlacement("Waffen", subTab)
        }
    }
    val standard = when (type) {
        "warframe" -> listOf("Blueprint", "Chassis", "Neuroptics", "Systems")
        "companion" -> listOf("Blueprint", "Carapace", "Cerebrum", "Systems")
        "archwing" -> listOf("Blueprint", "Harness", "Systems", "Wings")
        else -> detected.toList()
    }.distinct()
    return ScannerAddedItem(
        name = name,
        type = type,
        tabName = placement.tabName,
        subTabName = placement.subTabName,
        components = standard.map { ScannerAddedComponent(it, it in detected, "", "", "") }
    )
}

private fun detectedComponent(value: String): String? {
    val normalized = discoveryKey(value)
    val aliases = listOf(
        "neuroptik" to "Neuroptics", "neuroptics" to "Neuroptics", "chassis" to "Chassis",
        "systeme" to "Systems", "systems" to "Systems", "cerebrum" to "Cerebrum", "carapace" to "Carapace",
        "gehause" to "Receiver", "receiver" to "Receiver", "lauf" to "Barrel", "barrel" to "Barrel",
        "schaft" to "Stock", "stock" to "Stock", "klinge" to "Blade", "blade" to "Blade",
        "verbindung" to "Link", "link" to "Link", "oberteil" to "Upper Limb", "upper limb" to "Upper Limb",
        "unterteil" to "Lower Limb", "lower limb" to "Lower Limb", "sehne" to "String", "string" to "String",
        "harness" to "Harness", "flugel" to "Wings", "wings" to "Wings", "griff" to "Handle",
        "handle" to "Handle", "grip" to "Grip", "blaupause" to "Blueprint", "blueprint" to "Blueprint"
    )
    return aliases.firstOrNull { (alias) -> Regex("\\b${Regex.escape(alias)}\\b").containsMatchIn(normalized) }?.second
}

private fun discoveryKey(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .replace(Regex("[^a-z0-9]+"), " ")
    .trim()
    .replace(Regex("\\s+"), " ")

private fun similarity(first: String, second: String): Double {
    val previous = IntArray(second.length + 1) { it }
    for (i in first.indices) {
        var diagonal = previous[0]
        previous[0] = i + 1
        for (j in second.indices) {
            val old = previous[j + 1]
            previous[j + 1] = minOf(previous[j + 1] + 1, previous[j] + 1, diagonal + if (first[i] == second[j]) 0 else 1)
            diagonal = old
        }
    }
    return 1.0 - previous[second.length].toDouble() / maxOf(first.length, second.length).coerceAtLeast(1)
}
