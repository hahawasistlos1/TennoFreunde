package com.example.tennofreunde.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.AccentPurple,
    secondary = AppColors.AccentBlue,
    tertiary = Color(0xFFFFD54F),
    background = AppColors.BackgroundTop,
    surface = AppColors.BackgroundMiddle,
    surfaceVariant = AppColors.BackgroundBottom,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF006D7E),
    tertiary = Color(0xFF8A6500),
    background = Color(0xFFF8F7FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8E3F0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1B1B22),
    onSurface = Color(0xFF1B1B22),
    onSurfaceVariant = Color(0xFF4A4654)
)

@Composable
fun TennoFreundeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
