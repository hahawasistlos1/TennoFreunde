package com.example.tennofreunde.utils

fun translateComponent(
    component: String
): String {

    return when (
        component.lowercase()
    ) {


        "barrel" -> "Lauf"

        "receiver" -> "Gehäuse"

        "stock" -> "Schaft"

        "blade" -> "Klinge"

        "handle" -> "Griff"

        "disc" -> "Scheibe"

        "grip" -> "Halterung"

        "link" -> "Verbindung"

        "string" -> "Sehne"

        else -> component
    }
}