
package com.example.tennofreunde.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class WeaponItem(

    var name: String,

    var category: String,

    var components:
    SnapshotStateList<ComponentItem> =
        mutableStateListOf(),

    var infoFields:
    SnapshotStateList<InfoField> =
        mutableStateListOf(),

    var isNew: Boolean = false
)


