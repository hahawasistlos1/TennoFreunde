package com.example.tennofreunde.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.ui.theme.AppBrushes
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import androidx.compose.material3.CardDefaults



@Composable
fun ProgressCard(
    checkedComponents: Int,
    totalComponents: Int
) {

    val progressPercent =
        if (totalComponents > 0)
            (checkedComponents * 100) / totalComponents
        else
            0

    Card(

        shape = AppShapes.Large,

        colors = CardDefaults.cardColors(
            containerColor = AppColors.Card
        ),


        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Gesamtfortschritt",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "$progressPercent%",
                        color = AppColors.TextPrimary ,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            AppColors.Accent.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                ) {

                    Text(
                        text = "$checkedComponents / $totalComponents",
                        color = AppColors.Accent
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            LinearProgressIndicator(

                progress = { progressPercent / 100f },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),

                color = AppColors.Accent,

                trackColor = AppColors.TextPrimary.copy(alpha = 0.12f),

            )
        }
    }
}