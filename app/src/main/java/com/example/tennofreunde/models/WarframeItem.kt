package com.example.tennofreunde.models

import androidx.compose.runtime.snapshots.SnapshotStateList

data class WarframeItem(

    var name: String,

    var type: String = "warframe",

    var tabName: String,

    var subTabName: String,

    var infoFields: MutableList<InfoField>,

    var components: SnapshotStateList<ComponentItem>,

    var isNew: Boolean = false,

    var imageName: String = "",

    var catalogSource: String = "shared"
)
