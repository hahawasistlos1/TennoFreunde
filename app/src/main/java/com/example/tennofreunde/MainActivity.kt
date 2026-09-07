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

            TennoFreundeTheme(
                darkTheme = darkMode,
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
                    }
                )
            }
        }
    }
}
