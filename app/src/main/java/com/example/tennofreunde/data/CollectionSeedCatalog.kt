package com.example.tennofreunde.data

import androidx.compose.runtime.mutableStateListOf
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.WarframeItem

object CollectionSeedCatalog {
    fun items(): List<WarframeItem> = resources() + mods()

    private fun resources() = listOf(
        resource("Orokin Cell", "Selten", "Saturn, Ceres, Deimos; Bosse und Survival/Defense sind gute Farmorte"),
        resource("Argon Crystal", "Selten", "Void-Missionen; zerfällt nach einiger Zeit"),
        resource("Neural Sensors", "Selten", "Jupiter und Kuva Fortress"),
        resource("Neurodes", "Selten", "Erde, Deimos, Lua und Eris"),
        resource("Control Module", "Selten", "Void, Europa und Neptun"),
        resource("Gallium", "Selten", "Mars und Uranus"),
        resource("Morphics", "Selten", "Merkur, Mars, Pluto und Europa"),
        resource("Tellurium", "Selten", "Archwing, Uranus Sealab und Railjack"),
        resource("Ferrite", "Planet", "Merkur, Erde, Neptun und Void"),
        resource("Rubedo", "Planet", "Erde, Europa, Phobos, Pluto und Sedna"),
        resource("Alloy Plate", "Planet", "Venus, Jupiter, Ceres, Sedna und Pluto"),
        resource("Polymer Bundle", "Planet", "Merkur, Venus und Uranus"),
        resource("Plastids", "Planet", "Saturn, Uranus, Phobos, Pluto und Eris"),
        resource("Nano Spores", "Planet", "Saturn, Deimos, Neptune und Eris"),
        resource("Salvage", "Planet", "Mars, Jupiter und Sedna"),
        resource("Cryotic", "Planet", "Excavation-Missionen"),
        resource("Oxium", "Planet", "Oxium Ospreys, Corpus-Missionen"),
        resource("Kuva", "Syndikat", "Kuva Siphon/Flood, Sorties, Steel Path, Nightwave"),
        resource("Endo", "Syndikat", "Ayatan-Schätze, Arbitrations, Rathuum, Mission Rewards"),
        resource("Aya", "Syndikat", "Bounties, Void-Missionen, Relikt-Packs"),
        resource("Entrati Lanthorn", "Offene Welten", "Zariman und Sanctum Anatomica"),
        resource("Voidgel Orb", "Offene Welten", "Zariman"),
        resource("Toroid", "Offene Welten", "Orb Vallis: Enrichment Labs, Spaceport, Temple of Profit"),
        resource("Breath Of The Eidolon", "Offene Welten", "Cetus Bounties"),
        resource("Cetirine", "Offene Welten", "Cambion Drift"),
        resource("Fish Oil", "Offene Welten", "Fische auf den Ebenen von Eidolon zerlegen"),
        resource("Grokdrul", "Offene Welten", "Grineer-Lager auf den Ebenen von Eidolon"),
        resource("Iradite", "Offene Welten", "Ebenen von Eidolon, besonders abseits der Tore"),
        resource("Thermal Sludge", "Offene Welten", "Orb Vallis Container und Lager"),
        resource("Carbides", "Railjack", "Railjack Grineer-Missionen"),
        resource("Cubic Diodes", "Railjack", "Railjack Corpus-Missionen"),
        resource("Pustrels", "Railjack", "Railjack Asteroiden und Wracks")
    )

    private fun mods() = listOf(
        mod("Vitality", "Warframe", "Basis-Mod; häufige Gegner/Container, frühe Missionen"),
        mod("Redirection", "Warframe", "Basis-Mod; häufige Gegner/Container, frühe Missionen"),
        mod("Steel Fiber", "Warframe", "Basis-Mod; häufige Gegner/Container"),
        mod("Intensify", "Warframe", "Orokin Vaults/Rotationen und normale Drops"),
        mod("Continuity", "Warframe", "Normale Drops und Belohnungen"),
        mod("Streamline", "Warframe", "Normale Drops und Belohnungen"),
        mod("Stretch", "Warframe", "Normale Drops und Belohnungen"),
        mod("Flow", "Warframe", "Normale Drops und Belohnungen"),
        mod("Adaptation", "Warframe", "Arbitrations"),
        mod("Rolling Guard", "Warframe", "Arbitrations: Arbitration Honors"),
        mod("Serration", "Primär", "Basis-Gewehr-Mod; häufige Gegner/Container"),
        mod("Split Chamber", "Primär", "Boss-/Mission-Drops und Händlerrotationen"),
        mod("Point Strike", "Primär", "Normale Drops"),
        mod("Vital Sense", "Primär", "Normale Drops"),
        mod("Hell's Chamber", "Primär", "Shotgun-Drops und Belohnungen"),
        mod("Hornet Strike", "Sekundär", "Basis-Pistolen-Mod; häufige Gegner/Container"),
        mod("Barrel Diffusion", "Sekundär", "Normale Drops und Belohnungen"),
        mod("Lethal Torrent", "Sekundär", "Nightmare-Missionen"),
        mod("Target Cracker", "Sekundär", "Normale Drops"),
        mod("Pistol Gambit", "Sekundär", "Normale Drops"),
        mod("Pressure Point", "Nahkampf", "Basis-Nahkampf-Mod; häufige Gegner/Container"),
        mod("Condition Overload", "Nahkampf", "Deimos/Cambion Drift Gegner und Necralisk-Bounties"),
        mod("Blood Rush", "Nahkampf", "Lua Spy und Acolyte/Rotationen"),
        mod("Weeping Wounds", "Nahkampf", "Acolyte/Steel Path"),
        mod("Organ Shatter", "Nahkampf", "Normale Drops"),
        mod("Berserker Fury", "Nahkampf", "Normale Drops"),
        mod("Animal Instinct", "Begleiter", "Nightmare-Missionen"),
        mod("Fetch", "Begleiter", "Kubrow/Kavat Drops"),
        mod("Vacuum", "Begleiter", "Sentinel-Mod; häufige Drops"),
        mod("Medi-Ray", "Begleiter", "Cephalon Simaris"),
        mod("Energy Siphon", "Aura", "Nightwave Cred Offerings"),
        mod("Corrosive Projection", "Aura", "Nightwave Cred Offerings"),
        mod("Steel Charge", "Aura", "Nightwave Cred Offerings"),
        mod("Power Drift", "Exilus", "Lua Challenge Rooms"),
        mod("Cunning Drift", "Exilus", "Lua Challenge Rooms"),
        mod("Speed Drift", "Exilus", "Lua Challenge Rooms"),
        mod("Crimson Dervish", "Stance", "Grineer Drops"),
        mod("Blind Justice", "Stance", "Stalker/Shadow Stalker"),
        mod("Primed Continuity", "Primed", "Baro Ki'Teer"),
        mod("Primed Flow", "Primed", "Baro Ki'Teer"),
        mod("Primed Pressure Point", "Primed", "Baro Ki'Teer"),
        mod("Galvanized Chamber", "Galvanized", "Arbitrations: Arbitration Honors"),
        mod("Galvanized Diffusion", "Galvanized", "Arbitrations: Arbitration Honors"),
        mod("Galvanized Hell", "Galvanized", "Arbitrations: Arbitration Honors"),
        mod("Rifle Riven Mod", "Riven", "Sorties, Archon Hunts, Teshin/Steel Path Honors"),
        mod("Pistol Riven Mod", "Riven", "Sorties, Archon Hunts, Teshin/Steel Path Honors"),
        mod("Melee Riven Mod", "Riven", "Sorties, Archon Hunts, Teshin/Steel Path Honors")
    )

    private fun resource(name: String, subTab: String, location: String) = collectionItem(
        name = name,
        type = "resource",
        tab = "Ressourcen",
        subTab = subTab,
        location = location
    )

    private fun mod(name: String, subTab: String, location: String) = collectionItem(
        name = name,
        type = "mod",
        tab = "Mods",
        subTab = subTab,
        location = location
    )

    private fun collectionItem(
        name: String,
        type: String,
        tab: String,
        subTab: String,
        location: String
    ) = WarframeItem(
        name = name,
        type = type,
        tabName = tab,
        subTabName = subTab,
        infoFields = mutableListOf(InfoField("Fundort", location)),
        components = mutableStateListOf(ComponentItem("Vorhanden", farmLocation = location)),
        isNew = false
    )
}
