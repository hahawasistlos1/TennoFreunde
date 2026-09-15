package com.example.tennofreunde.ui

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    GERMAN("de", "Deutsch", "German"),
    ENGLISH("en", "English", "English"),
    FRENCH("fr", "Français", "French"),
    SPANISH("es", "Español", "Spanish"),
    ITALIAN("it", "Italiano", "Italian"),
    PORTUGUESE("pt", "Português", "Portuguese"),
    POLISH("pl", "Polski", "Polish");

    val displayName: String
        get() = nativeName

    fun displayNameFor(currentLanguage: AppLanguage): String {
        return if (currentLanguage == GERMAN) nativeName else englishName
    }

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: GERMAN
    }
}
