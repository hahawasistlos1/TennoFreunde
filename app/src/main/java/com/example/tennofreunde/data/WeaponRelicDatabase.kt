package com.example.tennofreunde.data

object WeaponRelicDatabase {

    lateinit var weaponMap:
            Map<String, WeaponRelicEntry>

    fun load(
        entries: List<WeaponRelicEntry>
    ) {

        weaponMap =
            entries.associateBy {

                it.part.lowercase()
            }
    }
}

