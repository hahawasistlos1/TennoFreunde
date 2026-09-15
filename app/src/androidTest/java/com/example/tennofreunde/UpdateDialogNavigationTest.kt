package com.example.tennofreunde

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import android.view.KeyEvent
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tennofreunde.screens.UpdateDialog
import org.junit.Rule
import org.junit.Test

class UpdateDialogNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun updateDialogCanBeDismissedWithAndroidBack() {
        val visible = mutableStateOf(true)
        compose.setContent {
            MaterialTheme {
                UpdateDialog(
                    showDialog = visible.value,
                    updateTitle = "Testversion",
                    updateMessage = "Test",
                    updateUrl = "https://example.com",
                    german = true,
                    lastBackupAt = System.currentTimeMillis(),
                    onExportBackup = {},
                    onDismiss = { visible.value = false }
                )
            }
        }
        compose.onNodeWithText("Update Testversion").assertIsDisplayed()
        // Send the system key to the focused dialog, not Espresso's empty host window.
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.onNodeWithText("Update Testversion").assertDoesNotExist()
    }
}
