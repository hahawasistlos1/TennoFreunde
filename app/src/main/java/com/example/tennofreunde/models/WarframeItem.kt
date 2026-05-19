
package com.example.tennofreunde.models

data class WarframeItem(

    var name: String,

    var type: String = "warframe",

    var tabName: String,

    var subTabName: String,

    var infoFields: MutableList<InfoField>,

    var components: MutableList<ComponentItem>,

    var isNew: Boolean = false
)

