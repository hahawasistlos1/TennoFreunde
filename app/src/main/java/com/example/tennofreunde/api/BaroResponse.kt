package com.example.tennofreunde.api

data class BaroResponse(
    val character: String = "Baro Ki'Teer",
    val location: String = "",
    val active: Boolean? = null,
    val activation: String? = null,
    val expiry: String? = null,
    val startString: String? = null,
    val endString: String? = null,
    val inventory: List<BaroItem> = emptyList()
)

data class BaroItem(

    val item: String,

    val ducats: Int,

    val credits: Int
)
