package com.example.tennofreunde.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ComponentItem(

    var name: String,

    checked: Boolean = false
) {

    var checked by mutableStateOf(checked)
}