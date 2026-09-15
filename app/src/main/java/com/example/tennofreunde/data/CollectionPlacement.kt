package com.example.tennofreunde.data

import com.example.tennofreunde.models.WarframeItem

data class CollectionPlacement(
    val tabName: String,
    val subTabName: String
)

object CollectionPlacementRules {
    val defaultWeaponSubTabs = listOf(
        "Primär",
        "Sekundär",
        "Nahkampf",
        "Primär Prime",
        "Prime Sekundär",
        "Prime Nahkampf"
    )

    val defaultResourceSubTabs = listOf(
        "Selten",
        "Planet",
        "Offene Welten",
        "Syndikat",
        "Railjack"
    )

    val defaultModSubTabs = listOf(
        "Warframe",
        "Primär",
        "Sekundär",
        "Nahkampf",
        "Begleiter",
        "Archwing",
        "Aura",
        "Exilus",
        "Stance",
        "Primed",
        "Galvanized",
        "Riven"
    )

    private val tennoSubTabs = setOf(
        "warframe",
        "prime warframe",
        "prime warframes",
        "protoframes"
    )

    private val weaponSubTabs = setOf(
        "primar",
        "primar prime",
        "primary prime",
        "prime primary",
        "sekundar",
        "prime sekundar",
        "prime secondary",
        "nahkampf",
        "prime nahkampf",
        "prime melee",
        "prime waffen"
    )

    private val companionSubTabs = setOf(
        "vulpaphyla",
        "yulpaphyla",
        "predasite",
        "kavat",
        "kubrow"
    )

    private val sentinelSubTabs = setOf(
        "wachter",
        "prime wachter"
    )

    private val resourceSubTabs = setOf(
        "selten",
        "planet",
        "offene welten",
        "syndikat",
        "railjack"
    )

    private val modSubTabs = setOf(
        "mod warframe",
        "mods warframe",
        "warframe mods",
        "primar mods",
        "primary mods",
        "sekundar mods",
        "secondary mods",
        "nahkampf mods",
        "melee mods",
        "begleiter mods",
        "companion mods",
        "archwing mods",
        "aura mods",
        "exilus mods",
        "stance mods",
        "primed mods",
        "galvanized mods",
        "riven mods"
    )

    private val secondaryWeapons = setOf(
        "afuris", "akarius", "akbolto", "akjagara", "aksomati", "akstiletto",
        "ballistica", "bronco", "epitaph", "euphona", "hystrix", "knell",
        "kompressa", "lex", "magnus", "pandero", "pyrana", "sicarus",
        "vasto", "velox", "zakti", "zylok", "akbronco", "aklex", "akmagnus",
        "akvasto", "hikou"
    )

    private val meleeWeapons = setOf(
        "aegis", "ankyros", "crane", "dakra", "destreza", "fang", "galariak",
        "galatine", "glaive", "gram", "guandao", "gunsen", "kamas", "karyst",
        "keres", "kestrel", "kronen", "masseter", "nikana", "okina", "orthos",
        "pangolin", "quassus", "reaper", "redeemer", "sarofang", "scindo",
        "skyla", "spira", "tatsu", "tekko", "venato", "zoren", "bo", "fragor",
        "kogake", "ninkondi", "tipedo", "venka", "volnus"
    )

    fun forItem(type: String, name: String, tabName: String, subTabName: String): CollectionPlacement {
        return when {
            type.equals("resource", ignoreCase = true) || type.equals("ressource", ignoreCase = true) -> CollectionPlacement(
                tabName = "Ressourcen",
                subTabName = normalizeResourceSubTab(subTabName)
            )
            type.equals("mod", ignoreCase = true) -> CollectionPlacement(
                tabName = "Mods",
                subTabName = normalizeModSubTab(subTabName)
            )
            placementForKnownSubTab(name, subTabName) != null -> placementForKnownSubTab(name, subTabName)!!
            type.equals("warframe", ignoreCase = true) -> CollectionPlacement(
                tabName = "Tenno",
                subTabName = normalizeWarframeSubTab(subTabName)
            )
            type.equals("weapon", ignoreCase = true) -> CollectionPlacement(
                tabName = "Waffen",
                subTabName = normalizeWeaponSubTab(name, subTabName)
            )
            else -> CollectionPlacement(tabName, subTabName)
        }
    }

    fun forSubTab(parentTab: String, subTabName: String): CollectionPlacement {
        when (canonicalTabName(parentTab)) {
            "Ressourcen" -> return CollectionPlacement("Ressourcen", normalizeResourceSubTab(subTabName))
            "Mods" -> return CollectionPlacement("Mods", normalizeModSubTab(subTabName))
        }
        return placementForKnownSubTab("", subTabName) ?: CollectionPlacement(
            canonicalTabName(parentTab),
            subTabName.trim()
        )
    }

    fun canonicalTabName(tabName: String): String {
        val key = normalizedKey(tabName)
        return when {
            key in tennoSubTabs -> "Tenno"
            key in weaponSubTabs -> "Waffen"
            key in companionSubTabs -> "Begleiter"
            key in sentinelSubTabs -> "Wächter"
            key in resourceSubTabs -> "Ressourcen"
            key in modSubTabs -> "Mods"
            else -> when (key) {
            "tenno" -> "Tenno"
            "waffen", "weapon", "weapons" -> "Waffen"
            "begleiter", "companion", "companions" -> "Begleiter"
            "wachter", "sentinel", "sentinels" -> "Wächter"
            "ressourcen", "ressource", "resource", "resources" -> "Ressourcen"
            "mods", "mod" -> "Mods"
            else -> tabName.trim()
            }
        }
    }

    fun sameKey(first: String, second: String): Boolean = normalizedKey(first) == normalizedKey(second)

    fun applyTo(item: WarframeItem): Boolean {
        val placement = forItem(item.type, item.name, item.tabName, item.subTabName)
        val changed = item.tabName != placement.tabName || item.subTabName != placement.subTabName
        item.tabName = placement.tabName
        item.subTabName = placement.subTabName
        return changed
    }

    fun primeWeaponCategory(name: String): String {
        val base = name.lowercase()
            .removeSuffix(" prime")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
        val firstWord = base.split(" ").firstOrNull().orEmpty()
        return when {
            base in meleeWeapons || firstWord in meleeWeapons -> "Prime Nahkampf"
            base in secondaryWeapons || firstWord in secondaryWeapons -> "Prime Sekundär"
            else -> "Primär Prime"
        }
    }

    private fun normalizeWarframeSubTab(subTabName: String): String {
        return when {
            subTabName.equals("Prime Warframes", ignoreCase = true) -> "Prime Warframe"
            subTabName.equals("Prime Warframe", ignoreCase = true) -> "Prime Warframe"
            subTabName.isBlank() -> "Warframe"
            else -> subTabName
        }
    }

    private fun placementForKnownSubTab(name: String, subTabName: String): CollectionPlacement? {
        val key = normalizedKey(subTabName)
        return when {
            key in tennoSubTabs -> CollectionPlacement("Tenno", normalizeWarframeSubTab(subTabName))
            key in weaponSubTabs -> CollectionPlacement("Waffen", normalizeWeaponSubTab(name, subTabName))
            key in companionSubTabs -> CollectionPlacement("Begleiter", normalizeCompanionSubTab(subTabName))
            key in sentinelSubTabs -> CollectionPlacement("Wächter", normalizeSentinelSubTab(subTabName))
            key in resourceSubTabs -> CollectionPlacement("Ressourcen", normalizeResourceSubTab(subTabName))
            key in modSubTabs -> CollectionPlacement("Mods", normalizeModSubTab(subTabName))
            else -> null
        }
    }

    private fun normalizeWeaponSubTab(name: String, subTabName: String): String {
        return when {
            subTabName.equals("Prime Waffen", ignoreCase = true) -> primeWeaponCategory(name)
            subTabName.equals("Primary Prime", ignoreCase = true) -> "Primär Prime"
            subTabName.equals("Prime Primary", ignoreCase = true) -> "Primär Prime"
            subTabName.equals("Prime Secondary", ignoreCase = true) -> "Prime Sekundär"
            subTabName.equals("Prime Melee", ignoreCase = true) -> "Prime Nahkampf"
            subTabName.isBlank() && name.endsWith(" Prime", ignoreCase = true) -> primeWeaponCategory(name)
            else -> subTabName
        }
    }

    private fun normalizeCompanionSubTab(subTabName: String): String {
        return when {
            subTabName.equals("Yulpaphyla", ignoreCase = true) -> "Vulpaphyla"
            else -> subTabName
        }
    }

    private fun normalizeSentinelSubTab(subTabName: String): String {
        return when {
            subTabName.equals("Wachter", ignoreCase = true) -> "Wächter"
            subTabName.equals("Prime Wachter", ignoreCase = true) -> "Prime Wächter"
            else -> subTabName
        }
    }

    private fun normalizeResourceSubTab(subTabName: String): String {
        return when (normalizedKey(subTabName)) {
            "offene welten" -> "Offene Welten"
            "syndikat" -> "Syndikat"
            "railjack" -> "Railjack"
            "selten" -> "Selten"
            "planet" -> "Planet"
            else -> subTabName.trim()
        }
    }

    private fun normalizeModSubTab(subTabName: String): String {
        return when (normalizedKey(subTabName).removeSuffix(" mods").trim()) {
            "mod warframe", "mods warframe", "warframe" -> "Warframe"
            "primar", "primary" -> "Primär"
            "sekundar", "secondary" -> "Sekundär"
            "nahkampf", "melee" -> "Nahkampf"
            "begleiter", "companion" -> "Begleiter"
            "archwing" -> "Archwing"
            "aura" -> "Aura"
            "exilus" -> "Exilus"
            "stance" -> "Stance"
            "primed" -> "Primed"
            "galvanized" -> "Galvanized"
            "riven" -> "Riven"
            else -> subTabName.trim()
        }
    }

    fun normalizedKey(value: String): String {
        return value.lowercase()
            .replace("ä", "a")
            .replace("ö", "o")
            .replace("ü", "u")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }
}
