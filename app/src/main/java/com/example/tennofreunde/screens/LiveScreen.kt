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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import com.example.tennofreunde.api.BaroResponse
import com.example.tennofreunde.api.WarframeApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.tennofreunde.api.AlertResponse
import com.example.tennofreunde.api.FissureResponse
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import com.example.tennofreunde.api.EventResponse
import com.example.tennofreunde.data.PrimeDropCache
import com.example.tennofreunde.api.PrimeDropResponse
import androidx.compose.ui.platform.LocalContext
import com.example.tennofreunde.data.GitHubApi
import com.example.tennofreunde.data.RelicEntry

@Composable
fun LiveScreen() {
    val context = LocalContext.current

    var showMoreMenu by remember {

        mutableStateOf(false)
    }
    var showBaroPopup by remember {

        mutableStateOf(false)
    }

    var showAlertsPopup by remember {

        mutableStateOf(false)
    }

    var showVoidPopup by remember {

        mutableStateOf(false)
    }

    var showEventPopup by remember {

        mutableStateOf(false)
    }

    var baroData by remember {

        mutableStateOf<BaroResponse?>(null)
    }

    var alertsData by remember {

        mutableStateOf<List<AlertResponse>>(emptyList())
    }

    var fissuresData by remember {

        mutableStateOf<List<FissureResponse>>(emptyList())
    }

    var eventsData by remember {

        mutableStateOf<List<EventResponse>>(emptyList())
    }

    var primeDropsData by remember {

        mutableStateOf<List<RelicEntry>>(
            emptyList()
        )
    }

    LaunchedEffect(Unit) {

        while (true) {

            try {

                baroData =
                    WarframeApi.api.getBaro()

                alertsData =
                    WarframeApi.api.getAlerts()

                fissuresData =
                    WarframeApi.api.getFissures()

                eventsData =
                    WarframeApi.api.getEvents()

                primeDropsData =
                    GitHubApi.api.getRelics()

                PrimeDropCache.savePrimeDrops(

                    context,

                    primeDropsData.map {

                        PrimeDropResponse(

                            part = it.part,

                            relic = it.relic,

                            rotation = it.rotation,

                            farmLocation = it.farmLocation
                        )
                    }
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }
            primeDropsData =

                PrimeDropCache
                    .loadPrimeDrops(context)
                    .map {

                        RelicEntry(

                            part = it.part,

                            relic = it.relic,

                            rotation = it.rotation ?: "",

                            farmLocation = it.farmLocation ?: ""
                        )
                    }

            delay(120000)
        }
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

                    onClick = {

                        showVoidPopup = true
                    }
                )

                TopNavButton(

                    "Alerts",

                    Icons.Default.Warning,

                    onClick = {

                        showAlertsPopup = true
                    }
                )

                TopNavButton(

                    "Event",

                    Icons.Default.Star,

                    onClick = {

                        showEventPopup = true
                    }
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
            LiveOverviewPanel(

                alertsData = alertsData,

                fissuresData = fissuresData,

                eventsData = eventsData
            )

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

                baroData = baroData,

                onClose = {

                    showBaroPopup = false
                }
            )
        }
        if (showAlertsPopup) {

            AlertsPopup(

                alertsData = alertsData,

                onClose = {

                    showAlertsPopup = false
                }
            )
        }
        if (showVoidPopup) {

            VoidPopup(

                fissuresData = fissuresData,

                onClose = {

                    showVoidPopup = false
                }
            )
        }
        if (showEventPopup) {

            EventPopup(

                eventsData = eventsData,

                onClose = {

                    showEventPopup = false
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
fun LiveOverviewPanel(

    alertsData: List<AlertResponse>,

    fissuresData: List<FissureResponse>,

    eventsData: List<EventResponse>
) {

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

                subtitle = if (fissuresData.isNotEmpty()) {

                    "${fissuresData.first().tier ?: "?"} • ${fissuresData.first().missionType ?: "Mission"}"

                } else {

                    "Keine Fissures"
                }
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

                title = "EVENT",

                subtitle = if (

                    eventsData.isNotEmpty()

                ) {

                    "Aktiv"

                } else {

                    "Keine Events"
                }
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

                title = "ALERTS",

                subtitle = if (alertsData.isNotEmpty()) {

                    alertsData.first().mission.type ?: "Unbekannt"

                } else {

                    "Keine Alerts"
                }
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

    baroData: BaroResponse?,
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

                .height(500.dp)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(20.dp)
                )

                .verticalScroll(
                    rememberScrollState()
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
                modifier = Modifier.height(18.dp)
            )

            if (baroData == null) {

                Text(

                    text = "Lade Baro Daten...",

                    color = Color.White
                )

            } else if (baroData.inventory.isEmpty()) {

                Text(

                    text = "Baro Ki'Teer ist aktuell nicht da.",

                    color = Color.LightGray
                )

            } else {

                Text(

                    text = "Items geladen: ${baroData.inventory.size}",

                    color = Color.Green
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                baroData.inventory.forEach { item ->

                    BaroItemCard(

                        title = item.item,

                        ducats = "${item.ducats} Dukaten",

                        credits = "${item.credits} Credits"
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }
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
@Composable
fun AlertsPopup(

    alertsData: List<AlertResponse>,
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

                .height(500.dp)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(20.dp)
                )

                .verticalScroll(
                    rememberScrollState()
                )

                .padding(18.dp)
        ) {

            Text(

                text = "LIVE ALERTS",

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            alertsData.forEach { alert ->

                AlertCard(

                    missionType =
                        alert.mission.type ?: "Unbekannt",

                    node =
                        alert.mission.node ?: "Unbekannt",

                    faction =
                        alert.mission.faction ?: "Unbekannt",

                    minLevel =
                        alert.mission.minEnemyLevel ?: 0,

                    maxLevel =
                        alert.mission.maxEnemyLevel ?: 0,

                    credits =
                        alert.mission.reward?.credits ?: 0,

                    reward = if (

                        alert.mission.reward?.items
                            ?.isNotEmpty() == true

                    ) {

                        "Spezial Item"

                    } else {

                        "Keine Spezial Belohnung"
                    },

                    eta =
                        alert.expiry ?: ""
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

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
fun AlertCard(

    missionType: String,

    node: String,

    faction: String,

    minLevel: Int,

    maxLevel: Int,

    credits: Int,

    reward: String,

    eta: String
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

            text = missionType,

            color = Color.White,

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(

            text = node,

            color = Color(0xFF00E5FF)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Fraktion: $faction",

            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Level: $minLevel - $maxLevel",

            color = Color.LightGray
        )

        if (reward != "Keine Spezial Belohnung") {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(

                text = "Belohnung: $reward",

                color = Color(0xFFFFD54F)
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Credits: $credits",

            color = Color(0xFF00E5FF)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        var remainingTime by remember {

            mutableStateOf(
                formatRemainingTime(eta)
            )
        }

        LaunchedEffect(eta) {

            while (true) {

                remainingTime =
                    formatRemainingTime(eta)

                delay(1000)
            }
        }

        Text(

            text = "Noch aktiv: $remainingTime",

            color = Color.LightGray
        )
    }
}
@Composable
fun VoidPopup(

    fissuresData: List<FissureResponse>,
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

                .height(550.dp)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(20.dp)
                )

                .verticalScroll(
                    rememberScrollState()
                )

                .padding(18.dp)
        ) {

            Text(

                text = "VOID FISSURES",

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            fissuresData.forEach { fissure ->

                if (

                    fissure.isStorm != true &&

                    fissure.isHard != true &&

                    fissure.expiry != null &&

                    !isExpired(fissure.expiry)
                ) {

                    VoidCard(

                        tier = fissure.tier ?: "Unbekannt",

                        node = fissure.node ?: "Unbekannt",

                        missionType = fissure.missionType ?: "Mission",

                        enemy = fissure.enemy ?: "Unbekannt",

                        enemyLevel = fissure.enemyLevel ?: "?",

                        expiry = fissure.expiry ?: ""
                    )
                }
            }

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
fun formatRemainingTime(

    expiry: String
): String {

    return try {

        val endTime =
            Instant.parse(expiry)

        val now =
            Instant.now()

        val duration =
            Duration.between(now, endTime)

        val totalSeconds =
            duration.seconds

        if (totalSeconds <= 0) {

            "Abgelaufen"

        } else {

            val hours =
                totalSeconds / 3600

            val minutes =
                (totalSeconds % 3600) / 60

            val seconds =
                totalSeconds % 60

            if (hours > 0) {

                "${hours}h ${minutes}m ${seconds}s"

            } else {

                "${minutes}m ${seconds}s"
            }
        }

    } catch (e: Exception) {

        "Zeit Fehler"
    }
}

fun isExpired(

    expiry: String
): Boolean {

    return try {

        val endTime =
            Instant.parse(expiry)

        Instant.now().isAfter(endTime)

    } catch (e: Exception) {

        true
    }
}

@Composable
fun VoidCard(

    tier: String,

    node: String,

    missionType: String,

    enemy: String,

    enemyLevel: String,

    expiry: String
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

            text = "$tier RELIKT",

            color = Color(0xFF00E5FF),

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(

            text = "Ort: $node",

            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Mission: $missionType",

            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Gegner: $enemy",

            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Level: $enemyLevel",

            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        var remainingTime by remember {

            mutableStateOf(
                formatRemainingTime(expiry)
            )
        }

        LaunchedEffect(expiry) {

            while (true) {

                remainingTime =
                    formatRemainingTime(expiry)

                delay(1000)
            }
        }

        Text(

            text = "Noch aktiv: $remainingTime",

            color = Color(0xFFFFD54F)
        )
    }
}
@Composable
fun EventPopup(

    eventsData: List<EventResponse>,
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

                .height(550.dp)

                .background(

                    Color(0xFF101830),

                    RoundedCornerShape(20.dp)
                )

                .verticalScroll(
                    rememberScrollState()
                )

                .padding(18.dp)
        ) {

            Text(

                text = "LIVE EVENTS",

                color = Color(0xFF00E5FF),

                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            val activeEvents =

                eventsData.filter {

                    !it.description.isNullOrEmpty()
                }

            if (activeEvents.isEmpty()) {

                Text(

                    text = "Aktuell keine Events aktiv",

                    color = Color.LightGray
                )

            } else {

                activeEvents.forEach { event ->

                    EventCard(

                        description =
                            event.description ?: "Unbekannt",

                        health =
                            event.health ?: 0.0,

                        score =
                            event.currentScore ?: 0.0,

                        expiry =
                            event.expiry ?: ""
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

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
fun EventCard(

    description: String,

    health: Double,

    score: Double,

    expiry: String
) {

    var remainingTime by remember {

        mutableStateOf(
            formatRemainingTime(expiry)
        )
    }

    LaunchedEffect(expiry) {

        while (true) {

            remainingTime =
                formatRemainingTime(expiry)

            delay(1000)
        }
    }

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

            text = description,

            color = Color(0xFF00E5FF),

            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(

            text = "Event Leben: $health",

            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Fortschritt: $score",

            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "Noch aktiv: $remainingTime",

            color = Color(0xFFFFD54F)
        )
    }
}








