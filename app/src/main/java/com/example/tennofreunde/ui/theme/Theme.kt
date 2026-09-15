package com.example.tennofreunde.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

private fun darkScheme(style: String) = darkColorScheme(
    primary = when (style) { "corpus" -> Color(0xFF62D8FF); "stalker" -> Color(0xFFFF5D67); else -> AppColors.AccentPurple },
    secondary = when (style) { "corpus" -> Color(0xFF32B8E6); "stalker" -> Color(0xFFE53F4A); else -> AppColors.AccentBlue },
    tertiary = when (style) { "corpus" -> Color(0xFFE8F7FF); "stalker" -> Color(0xFFFFB0A8); else -> Color(0xFFFFD54F) },
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

val LocalCompactMode = staticCompositionLocalOf { false }
data class WarframePalette(val energy: Color, val metal: Color)
val LocalWarframePalette = staticCompositionLocalOf { WarframePalette(AppColors.EnergyCyan, AppColors.OrokinGold) }

@Composable
fun TennoFreundeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    largeText: Boolean = false,
    compactMode: Boolean = false,
    colorStyle: String = "orokin",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme(colorStyle)
        else -> LightColorScheme
    }

    val palette = when (colorStyle) {
        "corpus" -> WarframePalette(Color(0xFF62D8FF), Color(0xFFE8F7FF))
        "stalker" -> WarframePalette(Color(0xFFFF5D67), Color(0xFFFFB0A8))
        else -> WarframePalette(AppColors.EnergyCyan, AppColors.OrokinGold)
    }
    CompositionLocalProvider(LocalCompactMode provides compactMode, LocalWarframePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (largeText) Typography.scaled(1.16f) else Typography,
            content = content
        )
    }
}

private fun androidx.compose.material3.Typography.scaled(factor: Float) = copy(
    displayLarge = displayLarge.scaleBy(factor), displayMedium = displayMedium.scaleBy(factor), displaySmall = displaySmall.scaleBy(factor),
    headlineLarge = headlineLarge.scaleBy(factor), headlineMedium = headlineMedium.scaleBy(factor), headlineSmall = headlineSmall.scaleBy(factor),
    titleLarge = titleLarge.scaleBy(factor), titleMedium = titleMedium.scaleBy(factor), titleSmall = titleSmall.scaleBy(factor),
    bodyLarge = bodyLarge.scaleBy(factor), bodyMedium = bodyMedium.scaleBy(factor), bodySmall = bodySmall.scaleBy(factor),
    labelLarge = labelLarge.scaleBy(factor), labelMedium = labelMedium.scaleBy(factor), labelSmall = labelSmall.scaleBy(factor)
)

private fun TextStyle.scaleBy(factor: Float): TextStyle = copy(
    fontSize = fontSize.scaledOrSame(factor),
    lineHeight = lineHeight.scaledOrSame(factor)
)

private fun TextUnit.scaledOrSame(factor: Float): TextUnit =
    if (this == TextUnit.Unspecified) this else this * factor
