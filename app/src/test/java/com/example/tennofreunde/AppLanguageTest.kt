package com.example.tennofreunde

import com.example.tennofreunde.ui.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun englishUiShowsGermanAsGerman() {
        assertEquals("German", AppLanguage.GERMAN.displayNameFor(AppLanguage.ENGLISH))
    }

    @Test
    fun germanUiShowsGermanAsDeutsch() {
        assertEquals("Deutsch", AppLanguage.GERMAN.displayNameFor(AppLanguage.GERMAN))
    }

    @Test
    fun supportsMoreThanGermanAndEnglish() {
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromCode("fr"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.POLISH, AppLanguage.fromCode("pl"))
    }
}
