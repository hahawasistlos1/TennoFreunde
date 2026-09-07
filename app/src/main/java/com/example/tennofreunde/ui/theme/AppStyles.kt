package com.example.tennofreunde.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
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

    val Card = Color(0xE61A2236)
    val HudPanel = Color(0xEE111827)

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
    val OrokinGold = Color(0xFFD8BC72)
    val EnergyCyan = Color(0xFF71E6F2)
}

object AppShapes {

    val Large =
        CutCornerShape(topStart = 4.dp, topEnd = 22.dp, bottomEnd = 4.dp, bottomStart = 22.dp)

    val Medium =
        CutCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomEnd = 2.dp, bottomStart = 16.dp)

    val Small =
        CutCornerShape(topStart = 2.dp, topEnd = 10.dp, bottomEnd = 2.dp, bottomStart = 10.dp)
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
