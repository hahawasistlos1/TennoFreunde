package com.example.tennofreunde.data

internal fun nextProfileCopyName(base: String, existing: Collection<String>, german: Boolean): String {
    val suffix = if (german) "Kopie" else "Copy"
    val normalized = existing.map { it.trim().lowercase() }.toSet()
    var number = 1
    while (true) {
        val candidate = if (number == 1) "$base $suffix" else "$base $suffix $number"
        if (candidate.lowercase() !in normalized) return candidate
        number++
    }
}
