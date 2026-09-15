package com.example.tennofreunde.api

data class SortieResponse(
    val id: String?,
    val boss: String?,
    val faction: String?,
    val eta: String?,
    val expiry: String?,
    val variants: List<SortieVariant>?
)

data class SortieVariant(
    val node: String?,
    val missionType: String?,
    val modifier: String?,
    val modifierDescription: String?
)
