package com.example.tennofreunde.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {

    val BackgroundTop =
        Color(0xFF181825)

    val BackgroundMiddle =
        Color(0xFF232136)

    val BackgroundBottom =
        Color(0xFF2A273F)

    val Card =
        Color.White.copy(alpha = 0.06f)

    val CardBorder =
        Color.White.copy(alpha = 0.08f)

    val AccentPurple =
        Color(0xFFC8A8FF)

    val AccentBlue =
        Color(0xFF8BE9FD)

    val TextPrimary =
        Color.White

    val TextSecondary =
        Color(0xFFD0D0D0)

    val Accent = Color(0xFFC8A8FF)
}

object AppShapes {

    val Large =
        RoundedCornerShape(24.dp)

    val Medium =
        RoundedCornerShape(18.dp)

    val Small =
        RoundedCornerShape(14.dp)
}

object AppBrushes {

    val MainBackground =
        Brush.verticalGradient(

            colors = listOf(

                AppColors.BackgroundTop,
                AppColors.BackgroundMiddle,
                AppColors.BackgroundBottom
            )
        )
}