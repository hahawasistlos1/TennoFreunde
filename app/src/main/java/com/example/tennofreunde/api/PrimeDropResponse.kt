package com.example.tennofreunde.api

data class PrimeDropResponse(

    val part: String,

    val relic: String,

    val rotation: String? = "",

    val farmLocation: String? = ""
)