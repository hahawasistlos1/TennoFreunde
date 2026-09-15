package com.example.tennofreunde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tennofreunde.system.TennoSystem
import com.example.tennofreunde.screens.TennoScreen
import com.example.tennofreunde.ui.theme.TennoFreundeTheme
import com.example.tennofreunde.ui.AppLanguage

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TennoSystem.initialize(this)

        setContent {

            val sharedPreferences = getSharedPreferences(
                "settings",
                MODE_PRIVATE
            )

            var darkMode by remember {

                mutableStateOf(
                    sharedPreferences.getBoolean(
                        "dark_mode",
                        false
                    )
                )
            }
            var language by remember {
                mutableStateOf(AppLanguage.fromCode(sharedPreferences.getString("app_language", "de")))
            }
            val hubPreferences = getSharedPreferences("tenno_hub", MODE_PRIVATE)
            var largeText by remember {
                mutableStateOf(
                    sharedPreferences.getBoolean(
                        "large_text_mode",
                        hubPreferences.getBoolean("large_text_mode", false)
                    )
                )
            }
            var offlineMode by remember {
                mutableStateOf(
                    sharedPreferences.getBoolean(
                        "offline_mode",
                        hubPreferences.getBoolean("offline_mode", false)
                    )
                )
            }
            var compactMode by remember {
                mutableStateOf(sharedPreferences.getBoolean("compact_mode", hubPreferences.getBoolean("compact_mode", false)))
            }
            var colorStyle by remember { mutableStateOf(sharedPreferences.getString("color_style", "orokin") ?: "orokin") }

            TennoFreundeTheme(
                darkTheme = darkMode,
                largeText = largeText,
                compactMode = compactMode,
                colorStyle = colorStyle,
                dynamicColor = false
            ) {

                TennoScreen(

                    darkMode = darkMode,
                    language = language,
                    onLanguageChange = {
                        language = it
                        sharedPreferences.edit().putString("app_language", it.code).apply()
                    },

                    onDarkModeChange = {

                        darkMode = it

                        sharedPreferences
                            .edit()
                            .putBoolean("dark_mode", it)
                            .apply()
                    },
                    largeText = largeText,
                    onLargeTextChange = {
                        largeText = it
                        sharedPreferences.edit().putBoolean("large_text_mode", it).apply()
                        hubPreferences.edit().putBoolean("large_text_mode", it).apply()
                    },
                    offlineMode = offlineMode,
                    onOfflineModeChange = {
                        offlineMode = it
                        sharedPreferences.edit().putBoolean("offline_mode", it).apply()
                        hubPreferences.edit().putBoolean("offline_mode", it).apply()
                    },
                    compactMode = compactMode,
                    onCompactModeChange = {
                        compactMode = it
                        sharedPreferences.edit().putBoolean("compact_mode", it).apply()
                        hubPreferences.edit().putBoolean("compact_mode", it).apply()
                    },
                    colorStyle = colorStyle,
                    onColorStyleChange = {
                        colorStyle = it
                        sharedPreferences.edit().putString("color_style", it).apply()
                    }
                )
            }
        }
    }
}
