package com.example.tennofreunde.models

data class ComponentItem(

    val name: String,

    var checked: Boolean = false,

    val farmLocation: String = "",

    val relic: String = "",

    val rotation: String = "",

    val activeMission: String = ""
)