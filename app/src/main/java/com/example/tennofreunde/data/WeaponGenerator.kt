package com.example.tennofreunde.data

import androidx.compose.runtime.toMutableStateList
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WeaponItem

object WeaponGenerator {

    fun generateWeapons(

        entries: List<WeaponRelicEntry>

    ): MutableList<WeaponItem> {

        val groupedWeapons =
            mutableMapOf<String, MutableList<String>>()

        val blockedNames = setOf(

            "ash",
            "atlas",
            "banshee",
            "baruuk",
            "caliban",
            "chroma",
            "ember",
            "equinox",
            "frost",
            "gara",
            "garuda",
            "gauss",
            "grendel",
            "gyre",
            "harrow",
            "hildryn",
            "hydroid",
            "inaros",
            "ivara",
            "khora",
            "lavos",
            "limbo",
            "loki",
            "mag",
            "mesa",
            "mirage",
            "nekros",
            "nezha",
            "nidus",
            "nova",
            "nyx",
            "oberon",
            "octavia",
            "protea",
            "revenant",
            "rhino",
            "saryn",
            "sevagoth",
            "titania",
            "trinity",
            "valkyr",
            "vauban",
            "volt",
            "voruna",
            "wisp",
            "wukong",
            "xaku",
            "yareli",
            "zephyr",

            "carrier",
            "dethcube",
            "helios",
            "nautilus",
            "shade",
            "wyrm",
            "odonata",
            "kavasa"
        )

        val ignoredComponents = setOf(

            "chassis",
            "systems",
            "neuroptics",

            "carapace",
            "cerebrum",
            "harness"
        )

        val componentKeywords = setOf(

            "bp",
            "barrel",
            "receiver",
            "stock",
            "blade",
            "handle",
            "disc",
            "grip",
            "link",
            "string",

            "chassis",
            "systems",
            "neuroptics",

            "carapace",
            "cerebrum",
            "harness"
        )



        for (entry in entries) {

            val words =
                entry.part.split(" ")

            if (words.size < 3)
                continue

            val componentIndex =
                words.indexOfLast {
                    componentKeywords.contains(it)
                }

            if (componentIndex == -1)
                continue

            val componentName =
                words[componentIndex]

            if (
                ignoredComponents.contains(
                    componentName
                )
            ) {
                continue
            }

            val weaponName =
                words.subList(
                    1,
                    componentIndex
                ).joinToString(" ")
            val invalidWords = setOf(
                "chassis",
                "systems",
                "neuroptics",
                "carapace",
                "cerebrum",
                "harness"
            )

            if (
                invalidWords.any {
                    weaponName.contains(it)
                }
            ) {
                continue
            }
            if (
                blockedNames.contains(
                    weaponName.lowercase()
                )
            ) {
                continue
            }
       weaponName.contains("trinity") ||
            groupedWeapons
                .getOrPut(
                    weaponName
                ) {
                    mutableListOf()
                }
                .add(componentName)
        }

        val weapons =
            mutableListOf<WeaponItem>()

        groupedWeapons.forEach {

                (weaponName, components) ->
            val secondaryWeapons = setOf(

                "afuris",
                "akarius",
                "akbolto",
                "akjagara",
                "aksomati",
                "akstiletto",
                "ballistica",
                "bronco",
                "epitaph",
                "euphona",
                "hystrix",
                "knell",
                "kompressa",
                "lex",
                "magnus",
                "pandero",
                "pyrana",
                "sicarus",
                "vasto",
                "velox",
                "zakti",
                "zylok",
                "akbronco",
                "aklex",
                "akmagnus",
                "akvasto",
                "hikou"
            )

            val meleeWeapons = setOf(

                "aegis",
                "ankyros",
                "crane",
                "dakra",
                "destreza",
                "fang",
                "galariak",
                "galatine",
                "glaive",
                "gram",
                "guandao",
                "gunsen",
                "kamas",
                "karyst",
                "keres",
                "kestrel",
                "kronen",
                "masseter",
                "nikana",
                "okina",
                "orthos",
                "pangolin",
                "quassus",
                "reaper",
                "redeemer",
                "sarofang",
                "scindo",
                "skyla",
                "spira",
                "tatsu",
                "tekko",
                "venato",
                "zoren",
                "bo",
                "fragor",
                "kogake",
                "ninkondi",
                "tipedo",
                "venka",
                "volnus"
            )

            val category = when {

                meleeWeapons.contains(
                    weaponName.lowercase()
                ) -> "Prime Nahkampf"

                secondaryWeapons.contains(
                    weaponName.lowercase()
                ) -> "Prime Sekundär"

                else -> "Primär Prime"
            }

            weapons.add(

                WeaponItem(

                    name =
                        "$weaponName Prime",

                    category = category,

                    components =
                        components
                            .distinct()
                            .map {

                                ComponentItem(
                                    name = it
                                )
                            }
                            .toMutableStateList()
                )
            )
        }


        weapons
            .sortedBy { it.name }
            .forEach {


            }

        return weapons
    }
}