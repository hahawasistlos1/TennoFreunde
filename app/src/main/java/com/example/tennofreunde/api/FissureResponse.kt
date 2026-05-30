package com.example.tennofreunde.api

data class FissureResponse(

    val tier: String?,

    val node: String?,

    val missionType: String?,

    val enemy: String?,

    val enemyLevel: String?,

    val activation: String?,

    val expiry: String?,

    val isStorm: Boolean?,

    val isHard: Boolean?
)