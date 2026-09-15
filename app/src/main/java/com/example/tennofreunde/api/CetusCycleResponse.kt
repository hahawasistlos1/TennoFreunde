package com.example.tennofreunde.api

data class CetusCycleResponse(
    val id: String?,
    val expiry: String?,
    val activation: String?,
    val isDay: Boolean?,
    val isWarm: Boolean?,
    val state: String?,
    val timeLeft: String?
)
