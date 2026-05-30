package com.example.tennofreunde.api

data class EventResponse(

    val description: String?,

    val expiry: String?,

    val active: Boolean?,

    val health: Double?,

    val currentScore: Double?
)