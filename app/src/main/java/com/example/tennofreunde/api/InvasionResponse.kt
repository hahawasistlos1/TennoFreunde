package com.example.tennofreunde.api

data class InvasionResponse(
    val node: String?,
    val desc: String?,
    val eta: String?,
    val completed: Boolean?,
    val completion: Double?,
    val attacker: InvasionFaction?,
    val defender: InvasionFaction?
)

data class InvasionFaction(
    val faction: String?,
    val factionKey: String?,
    val reward: InvasionReward?
)

data class InvasionReward(
    val itemString: String?,
    val credits: Int?
)
