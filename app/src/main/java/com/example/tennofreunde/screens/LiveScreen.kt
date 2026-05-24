package com.example.tennofreunde.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable



@Composable
fun LiveScreen() {

    var showMoreMenu by remember {

        mutableStateOf(false)
    }
    var showBaroPopup by remember {

        mutableStateOf(false)
    }

    Box(

        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(

            painter = painterResource(
                id = R.drawable.warframe_bg
            ),


            contentDescription = null,

            contentScale = ContentScale.Crop,

            modifier = Modifier
                .fillMaxSize()
        )

        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.55f)
                )
        )

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 8.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            if (showMoreMenu) {

                MoreMenuPopup(

                    onClose = {

                        showMoreMenu = false
                    },

                    onOpenBaro = {

                        showBaroPopup = true

                        showMoreMenu = false
                    }
                )
            }


            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Text(

                text = "TENNO FREUNDE",

                style =
                    MaterialTheme.typography.headlineLarge,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF00E5FF)
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Text(

                text = "LIVE SYSTEM ONLINE Beta 1.0",

                color = Color.LightGray,

                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Row(

                modifier = Modifier
                    .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(1.dp)
            ) {

                TopNavButton(
                    "Übersicht",
                    Icons.Default.Home,
                    onClick = {}
                )

                TopNavButton(
                    "Void",
                    Icons.Default.Settings,
                    onClick = {}
                )

                TopNavButton(
                    "Alerts",
                    Icons.Default.Warning,
                    onClick = {}
                )

                TopNavButton(
                    "Event",
                    Icons.Default.Star,
                    onClick = {}
                )

                TopNavButton(
                    "Mehr",
                    Icons.Default.Menu,
                    onClick = {

                        showMoreMenu = true
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(0.dp)
            )
            LiveOverviewPanel()
            Spacer(
                modifier = Modifier.height(0.dp)
            )

            DailyDealsPanel()
            Spacer(
                modifier = Modifier.height(0.dp)
            )

            NewsPanel()
            Spacer(
                modifier = Modifier.height(0.dp)
            )
            BottomNavigationBar()

        }

        if (showMoreMenu) {

            MoreMenuPopup(

                onClose = {

                    showMoreMenu = false
                },

                onOpenBaro = {

                    showBaroPopup = true

                    showMoreMenu = false
                }
            )
        }

        if (showBaroPopup) {

            BaroPopup(

                onClose = {

                    showBaroPopup = false
                }
            )
        }
    }
}


@Composable
fun TopNavButton(

    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Box(

        modifier = Modifier
            .clickable {

                onClick()
            }
            .height(24.dp)

            .background(

                Color(0xCC101830),

                RoundedCornerShape(10.dp)
            )

            .padding(
                horizontal = 5.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Row(

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                imageVector = icon,

                contentDescription = null,

                tint = Color.White,

                modifier = Modifier
                    .height(14.dp)
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Text(

                style = MaterialTheme.typography.bodySmall,

                text = title,

                color = Color.White
            )
        }
    }
}

@Composable
fun LiveOverviewPanel() {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .background(

                Color(0xCC101830),

                RoundedCornerShape(16.dp)
            )

            .padding(14.dp)
    ) {

        Text(

            text = "LIVE ÜBERSICHT",

            color = Color(0xFF00E5FF),

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        Row(

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OverviewMiniCard(

                title = "VOID",

                subtitle = "Neo Mission"
            )

            OverviewMiniCard(

                title = "BARO",

                subtitle = "Aktiv"
            )
        }

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        Row(

            horizontalArrangement =
                Arrangement.spacedBy(0.dp)
        ) {

            OverviewMiniCard(

                title = "STEEL",

                subtitle = "Survival"
            )

            OverviewMiniCard(

                title = "ARCHON",

                subtitle = "Jagd"
            )
        }

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        Row(

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OverviewMiniCard(

                title = "NIGHTWAVE",

                subtitle = "Elite"
            )

            OverviewMiniCard(

                title = "ALERTS",

                subtitle = "3 Aktiv"
            )
        }
    }
}
@Composable
fun OverviewMiniCard(

    title: String,

    subtitle: String
) {

    Box(

        modifier = Modifier

            .width(160.dp)

            .height(82.dp)

            .background(

                Color(0xCC101830),

                RoundedCornerShape(12.dp)
            )

            .padding(10.dp)
    ) {

        Column {

            Text(

                text = title,

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Text(

                text = subtitle,

                color = Color.White
            )
        }
    }
}
@Composable
fun DailyDealsPanel() {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .background(

                Color(0xCC101830),

                RoundedCornerShape(16.dp)
            )

            .padding(14.dp)
    ) {

        Text(

            text = "TÄGLICHE DEALS",

            color = Color(0xFF00E5FF),

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        Row(

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            DealMiniCard(

                title = "FORMA",

                price = "35P"
            )

            DealMiniCard(

                title = "RIVEN",

                price = "120P"
            )

            DealMiniCard(

                title = "BOOSTER",

                price = "60P"
            )
        }
    }
}
@Composable
fun DealMiniCard(

    title: String,

    price: String
) {

    Box(

        modifier = Modifier

            .width(100.dp)

            .height(70.dp)

            .background(

                Color(0xCC101830),

                RoundedCornerShape(12.dp)
            )

            .padding(10.dp)
    ) {

        Column {

            Text(

                text = title,

                color = Color.White,

                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Text(

                text = price,

                color = Color(0xFF00E5FF)
            )
        }
    }
}
@Composable
fun NewsPanel() {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .background(

                Color(0xCC101830),

                RoundedCornerShape(16.dp)
            )

            .padding(14.dp)
    ) {

        Text(

            text = "NEUIGKEITEN",

            color = Color(0xFF00E5FF),

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        NewsCard(

            title = "Neues Event verfügbar",

            subtitle = "Schatten der Orokin"
        )

        Spacer(
            modifier = Modifier.height(0.dp)
        )

        NewsCard(

            title = "Baro Inventar aktualisiert",

            subtitle = "Neue Primed Mods"
        )
    }
}
@Composable
fun NewsCard(

    title: String,

    subtitle: String
) {

    Box(

        modifier = Modifier

            .fillMaxWidth()

            .background(

                Color(0xCC101830),

                RoundedCornerShape(12.dp)
            )

            .padding(12.dp)
    ) {

        Column {

            Text(

                text = title,

                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(0.dp)
            )

            Text(

                text = subtitle,

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun BottomNavigationBar() {

    Row(

        modifier = Modifier
            .fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceEvenly
    ) {

        BottomNavItem("LIVE")

        BottomNavItem("CLAN")

        BottomNavItem("COMMUNITY")

        BottomNavItem("PROFILE")
    }
}
@Composable
fun BottomNavItem(

    title: String
) {

    Box(

        modifier = Modifier

            .background(

                Color(0xCC101830),

                RoundedCornerShape(10.dp)
            )

            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(

            text = title,

            color = Color.White,

            style =
                MaterialTheme.typography.bodySmall
        )
    }
}
@Composable
fun MoreMenuPopup(

    onClose: () -> Unit,
    onOpenBaro: () -> Unit
) {

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.7f)
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            modifier = Modifier

                .fillMaxWidth(0.8f)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(18.dp)
                )

                .padding(20.dp)
        ) {

            Text(

                text = "MEHR",

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.titleLarge
            )

            PopupMenuButton(

                title = "Händler",

                onClick = {

                    onOpenBaro()
                }
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            PopupMenuButton(

                title = "Invasionen",

                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            PopupMenuButton(

                title = "Sorties",

                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            PopupMenuButton(

                title = "Relics",

                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            PopupMenuButton(

                title = "Cetus",

                onClick = {}
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            TopNavButton(

                title = "Schließen",

                icon = Icons.Default.Menu,

                onClick = {

                    onClose()
                }
            )
        }
    }
}
@Composable
fun PopupMenuButton(

    title: String,
    onClick: () -> Unit
) {

    Box(

        modifier = Modifier

            .clickable {

                onClick()
            }

            .fillMaxWidth()

            .background(

                Color(0xCC1A2745),

                RoundedCornerShape(12.dp)
            )

            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            )
    ) {

        Text(

            text = title,

            color = Color.White,

            style =
                MaterialTheme.typography.bodyLarge
        )
    }
}
@Composable
fun BaroPopup(

    onClose: () -> Unit
) {

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.75f)
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            modifier = Modifier

                .fillMaxWidth(0.9f)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(20.dp)
                )

                .padding(18.dp)
        ) {

            Text(

                text = "BARO KI'TEER",

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(

                text = "Verschwindet in 2 Tagen",

                color = Color.LightGray
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            BaroItemCard(

                title = "Primed Flow",

                ducats = "350 Dukaten",

                credits = "150.000 Credits"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            BaroItemCard(

                title = "Sands of Inaros",

                ducats = "100 Dukaten",

                credits = "25.000 Credits"
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            TopNavButton(

                title = "Schließen",

                icon = Icons.Default.Menu,

                onClick = {

                    onClose()
                }
            )
        }
    }
}
@Composable
fun BaroItemCard(

    title: String,

    ducats: String,

    credits: String
) {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .background(

                Color(0xCC1A2745),

                RoundedCornerShape(14.dp)
            )

            .padding(14.dp)
    ) {

        Text(

            text = title,

            color = Color.White,

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(

            text = ducats,

            color = Color(0xFFFFD54F)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = credits,

            color = Color.LightGray
        )
    }
}