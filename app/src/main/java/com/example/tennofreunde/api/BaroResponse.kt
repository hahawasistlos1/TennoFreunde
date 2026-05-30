package com.example.tennofreunde.api

data class BaroResponse(

    val character: String,

    val location: String,

    val active: Boolean,

    val startString: String,

    val endString: String,

    val inventory: List<BaroItem>
)

data class BaroItem(

    val item: String,

    val ducats: Int,

    val credits: Int
)