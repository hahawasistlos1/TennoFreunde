package com.example.tennofreunde

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun androidBackReturnsFromEveryMainSection() {
        listOf("Screenshot-Scanner", "Fertige Sachen", "Sammlung", "Einstellungen", "Tenno-Zentrale").forEach { label ->
            compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
            compose.onNodeWithText(label).performClick()
            compose.onNodeWithContentDescription("Zurück zur Hauptseite").assertIsDisplayed()
            pressBack()
            compose.onNodeWithText("DEIN ORIGIN-SYSTEM").assertIsDisplayed()
        }
    }

    @Test
    fun androidBackDismissesLiveDetailsBeforeLeavingOverview() {
        listOf("Weltenzyklus", "Void-Risse").forEach { label ->
            compose.onNodeWithText(label).performClick()
            compose.onNodeWithText("Zurück zur Übersicht").assertIsDisplayed()
            pressBack()
            compose.onNodeWithText("DEIN ORIGIN-SYSTEM").assertIsDisplayed()
        }
    }

    @Test
    fun androidBackClosesDrawerBeforeReturningFromScanner() {
        compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
        compose.onNodeWithText("Screenshot-Scanner").performClick()
        compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
        pressBack()
        compose.onNodeWithContentDescription("Zurück zur Hauptseite").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("DEIN ORIGIN-SYSTEM").assertIsDisplayed()
    }

    @Test
    fun screenshotScannerPageCanScrollToResultArea() {
        compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
        compose.onNodeWithText("Screenshot-Scanner").performClick()
        compose.onNodeWithText("ARSENAL-SCAN").assertIsDisplayed()

        compose.onRoot().performTouchInput { swipeUp() }

        compose.onNodeWithText("Wähle einen oder mehrere Warframe-Inventar-Screenshots aus.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun languageSelectionChangesLiveUiAndCanBeRestored() {
        compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
        compose.onNodeWithText("Einstellungen").performClick()
        compose.onNodeWithText("English").performClick()
        pressBack()
        compose.onNodeWithText("YOUR ORIGIN SYSTEM").assertIsDisplayed()

        compose.onNodeWithContentDescription("Seitenmenü öffnen").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Deutsch").performClick()
        pressBack()
        compose.onNodeWithText("DEIN ORIGIN-SYSTEM").assertIsDisplayed()
    }
}
