package com.example.tennofreunde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.example.tennofreunde.screens.TennoScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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

            MaterialTheme(

                colorScheme =

                    if (darkMode)
                        darkColorScheme()
                    else
                        lightColorScheme()

            ) {

                TennoScreen(

                    darkMode = darkMode,

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

