package com.example.tennofreunde.models

data class WarframeItem(

    var name: String,

    var tabName: String,

    var infoFields: MutableList<InfoField> = mutableListOf(),

    var components: MutableList<ComponentItem> = mutableListOf(),

    var isNew: Boolean = false
)

