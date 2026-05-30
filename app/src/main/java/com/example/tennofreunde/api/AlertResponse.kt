package com.example.tennofreunde.api

data class AlertResponse(

    val mission: AlertMission,

    val eta: String?,

    val expiry: String?
)

data class AlertMission(

    val node: String?,

    val type: String?,

    val faction: String?,

    val minEnemyLevel: Int?,

    val maxEnemyLevel: Int?,

    val reward: AlertReward?
)
data class AlertReward(

    val credits: Int?,

    val items: List<String>?
)