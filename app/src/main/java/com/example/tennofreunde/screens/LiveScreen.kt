package com.example.tennofreunde.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import java.net.URL
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedCard
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tennofreunde.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipRect
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.sp

data class FissureItem(

    val tier: String,

    val mission: String,

    val node: String,

    val enemy: String,

    val isHard: Boolean,

    val isStorm: Boolean,

    val timeLeft: String
)

@Composable
fun LiveScreen(
    onBack: () -> Unit
) {

    val warframeFont = FontFamily(

        Font(R.font.orbitron)
    )

    val context = LocalContext.current

    var fissuresText by remember {
        mutableStateOf("Lade...")
    }

    var fissureList by remember {
        mutableStateOf<List<FissureItem>>(emptyList())
    }

    var normalList by remember {
        mutableStateOf<List<FissureItem>>(emptyList())
    }

    var steelPathList by remember {
        mutableStateOf<List<FissureItem>>(emptyList())
    }

    var sortieText by remember {
        mutableStateOf("Lade Sortie...")
    }

    var arbitrationText by remember {
        mutableStateOf("Lade Arbitration...")
    }

    var archonText by remember {
        mutableStateOf("Lade Archon Hunt...")
    }

    var cetusText by remember {
        mutableStateOf("Lade Cetus...")
    }

    var fortunaText by remember {
        mutableStateOf("Lade Fortuna...")
    }

    var cambionText by remember {
        mutableStateOf("Lade Cambion...")
    }

    var baroText by remember {
        mutableStateOf("Lade Baro...")
    }

    var nightwaveText by remember {
        mutableStateOf("Lade Nightwave...")
    }

    var invasionText by remember {
        mutableStateOf("Lade Invasionen...")
    }

    var alertText by remember {
        mutableStateOf("Lade Alarmierungen...")
    }

    var stormText by remember {
        mutableStateOf("Lade Void Storms...")
    }

    var eventText by remember {
        mutableStateOf("Lade Events...")
    }

    var deepText by remember {
        mutableStateOf("Lade Deep Archimedea...")
    }

    var netraText by remember {
        mutableStateOf("Lade Netracells...")
    }

    var syndicateText by remember {
        mutableStateOf("Lade Syndikate...")
    }

    var selectedTab by remember {
        mutableStateOf("fissures")
    }
    var currentTime by remember {

        mutableLongStateOf(

            System.currentTimeMillis()
        )
    }

    LaunchedEffect(Unit) {

        while (true) {

            currentTime =
                System.currentTimeMillis()

            delay(1000)
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            withContext(Dispatchers.IO) {

                try {

                    val body =

                        URL("https://api.warframestat.us/pc")
                            .readText()

                    println(body)

                    val json =
                        JSONObject(body)


                    if (body.isNullOrEmpty()) {

                        fissuresText =
                            "Keine Daten erhalten"

                        return@withContext
                    }


                    val sortie =
                        json.getJSONObject("sortie")

                    val variants =
                        sortie.getJSONArray("variants")

                    val fissures =
                        json.getJSONArray("fissures")

                    val arbitration =
                        json.getJSONObject("arbitration")

                    val archon =
                        json.getJSONObject("archonHunt")
                    println(archon.toString())

                    val cetus =
                        json.getJSONObject("cetusCycle")

                    val fortuna =
                        json.getJSONObject("vallisCycle")

                    val cambion =
                        json.getJSONObject("cambionCycle")
                    println(cambion.toString())

                    val baro =
                        json.getJSONObject("voidTrader")

                    val nightwave =
                        json.getJSONObject("nightwave")

                    val invasions =
                        json.getJSONArray("invasions")

                    val alerts =
                        json.getJSONArray("alerts")

                    val storms =
                        json.optJSONArray("voidStorms")
                            ?: org.json.JSONArray()

                    val events =
                        json.getJSONArray("events")

                    val deep =
                        json.optJSONArray("archimedeas")

                    val netra =
                        json.optJSONObject("netracells")

                    val syndicates =
                        json.getJSONArray("syndicateMissions")

                    val syndicateList =
                        mutableListOf<String>()

                    for (i in 0 until syndicates.length()) {

                        val syndicate =
                            syndicates.getJSONObject(i)

                        val syndicateName =
                            syndicate.optString("syndicate")

                        val jobs =
                            syndicate.optJSONArray("jobs")

                        val jobList =
                            mutableListOf<String>()

                        if (jobs != null) {

                            for (j in 0 until jobs.length()) {

                                val job =
                                    jobs.getJSONObject(j)

                                val type =
                                    job.optString("type")

                                val enemy =
                                    job.optString("enemy")

                                jobList.add(

                                    "• $type ($enemy)"
                                )
                            }
                        }






                        syndicateList.add(

                            "👥 $syndicateName\n\n" +

                                    jobList.joinToString("\n")
                        )
                    }

                    syndicateText =

                        if (syndicateList.isEmpty()) {

                            "Keine Syndikatsmissionen"

                        } else {

                            syndicateList.joinToString("\n\n")
                        }

                    if (netra != null) {

                        val missions =
                            netra.optJSONArray("missions")

                        val netraList =
                            mutableListOf<String>()

                        if (missions != null) {

                            for (i in 0 until missions.length()) {

                                val mission =
                                    missions.getJSONObject(i)

                                val node =
                                    mission.optString("node")

                                val faction =
                                    mission.optString("faction")

                                val type =
                                    mission.optString("type")

                                netraList.add(

                                    "🧪 Mission ${i + 1}\n\n" +

                                            "Typ: $type\n" +

                                            "Ort: $node\n" +

                                            "Fraktion: $faction"
                                )
                            }
                        }

                        netraText =

                            if (netraList.isEmpty()) {

                                "Keine aktiven Netracells"

                            } else {

                                netraList.joinToString("\n\n")
                            }

                    } else {

                        netraText =
                            "Netracell Daten aktuell nicht verfügbar"
                    }

                    if (

                        deep != null &&

                        deep.length() > 0
                    ) {

                        val firstMission =
                            deep.getJSONObject(0)

                        val deviation =
                            firstMission.optString("deviation")

                        val risk =
                            firstMission.optInt("risk")

                        deepText =

                            "🏆 Deep Archimedea\n\n" +

                                    "Risk Level: $risk\n\n" +

                                    "Modifikator:\n$deviation"

                    } else {

                        deepText =
                            "Deep Archimedea Daten aktuell nicht verfügbar"
                    }

                    val eventList =
                        mutableListOf<String>()

                    for (i in 0 until events.length()) {

                        val event =
                            events.getJSONObject(i)

                        val description =
                            event.optString("description")

                        val faction =
                            event.optString("faction")

                        val node =
                            event.optString("node")

                        val health =
                            event.optInt("health")

                        eventList.add(

                            "🧬 Event\n\n" +

                                    "Beschreibung: $description\n\n" +

                                    "Fraktion: $faction\n" +

                                    "Ort: $node\n" +

                                    "Fortschritt: $health%"
                        )
                    }

                    eventText =

                        if (eventList.isEmpty()) {

                            "Keine aktiven Events"

                        } else {

                            eventList.joinToString("\n\n")
                        }

                    val stormList =
                        mutableListOf<String>()

                    for (i in 0 until storms.length()) {

                        val storm =
                            storms.getJSONObject(i)

                        val tier =
                            storm.optString("tier")

                        val node =
                            storm.optString("node")

                        val mission =
                            storm.optString("missionType")

                        val enemy =
                            storm.optString("enemy")

                        stormList.add(

                            "🚀 $tier\n\n" +

                                    "Mission: $mission\n" +

                                    "Ort: $node\n" +

                                    "Gegner: $enemy"
                        )
                    }

                    stormText =

                        if (storms.length() == 0) {

                            "Void Storm Daten aktuell nicht verfügbar"

                        } else if (stormList.isEmpty()) {

                            "Keine aktiven Void Storms"

                        } else {

                            stormList.joinToString("\n\n")
                        }

                    val alertList =
                        mutableListOf<String>()

                    for (i in 0 until alerts.length()) {

                        val alert =
                            alerts.getJSONObject(i)

                        val mission =
                            alert.optJSONObject("mission")

                        val node =
                            mission?.optString("node")
                                ?: "Unbekannt"

                        val type =
                            mission?.optString("type")
                                ?: "Unbekannt"

                        val faction =
                            mission?.optString("faction")
                                ?: "Unbekannt"

                        val reward =
                            mission
                                ?.optJSONObject("reward")
                                ?.optString("asString")
                                ?: "Keine Belohnung"

                        alertList.add(

                            "🚨 $type\n\n" +

                                    "Ort: $node\n" +

                                    "Fraktion: $faction\n\n" +

                                    "Belohnung: $reward"
                        )
                    }

                    alertText =

                        if (alertList.isEmpty()) {

                            "Keine aktiven Alarmierungen"

                        } else {

                            alertList.joinToString("\n\n")
                        }

                    val invasionList =
                        mutableListOf<String>()

                    for (i in 0 until invasions.length()) {

                        val invasion =
                            invasions.getJSONObject(i)

                        val completed =
                            invasion.optBoolean("completed")

                        if (!completed) {

                            val attacker =
                                invasion
                                    .optJSONObject("attackerReward")
                                    ?.optString("asString")
                                    ?: "Unbekannt"

                            val defender =
                                invasion
                                    .optJSONObject("defenderReward")
                                    ?.optString("asString")
                                    ?: "Unbekannt"

                            val node =
                                invasion.optString("node")

                            val completion =
                                invasion.optDouble("completion")

                            invasionList.add(

                                "⚔ $node\n\n" +

                                        "Angreifer: $attacker\n" +

                                        "Verteidiger: $defender\n\n" +

                                        "Fortschritt: ${completion.toInt()}%"
                            )
                        }
                    }




                    invasionText =

                        if (invasionList.isEmpty()) {

                            "Keine aktiven Invasionen"

                        } else {

                            invasionList.joinToString("\n\n")
                        }

                    val activeChallenges =
                        nightwave.getJSONArray("activeChallenges")

                    val challengeList =
                        mutableListOf<String>()

                    for (i in 0 until activeChallenges.length()) {

                        val challenge =
                            activeChallenges.getJSONObject(i)

                        val title =
                            challenge.optString("title")

                        val reputation =
                            challenge.optInt("reputation")

                        val elite =
                            challenge.optBoolean("isElite")

                        val desc =
                            challenge.optString("desc")

                        challengeList.add(

                            if (elite) {

                                "🔥 ELITE\n$title\n$desc\nRuf: $reputation"

                            } else {

                                "🌙 Weekly\n$title\n$desc\nRuf: $reputation"
                            }
                        )
                    }

                    nightwaveText =
                        challengeList.joinToString("\n\n")

                    val location =
                        baro.optString("location")

                    val baroActive =
                        baro.optBoolean("active")

                    val activation =
                        baro.optString("activation")

                    val expiry =
                        baro.optString("expiry")

                    val baroTime = try {

                        val targetTime =

                            if (baroActive) {

                                java.time.Instant.parse(expiry)

                            } else {

                                java.time.Instant.parse(activation)
                            }

                        val now =
                            java.time.Instant.now()

                        val secondsLeft =
                            targetTime.epochSecond - now.epochSecond

                        val hours =
                            secondsLeft / 3600

                        val minutes =
                            (secondsLeft % 3600) / 60

                        "${hours} h ${minutes} min"

                    } catch (e: Exception) {

                        "Unbekannt"
                    }

                    println(baro.toString())
                    baro.keys().forEach {

                        println("BARO KEY: $it")
                    }
                    baroText =

                        if (baroActive) {

                            "🟢 Aktiv\n\n" +

                                    "📍 Ort: $location\n\n" +

                                    "⏳ Geht in: $baroTime"

                        } else {

                            "🔴 Nicht da\n\n" +

                                    "📍 Nächster Ort: $location\n\n" +

                                    "⏳ Kommt in: $baroTime"
                        }

                    val active =
                        cambion.optString("active")

                    val cambionTime =
                        cambion.optString("timeLeft")

                    cambionText =

                        if (active.lowercase() == "fass") {

                            "🟠 Fass\n\nNoch: $cambionTime"

                        } else {

                            "🔵 Vome\n\nNoch: $cambionTime"
                        }

                    println(fortuna.toString())
                    fortuna.keys().forEach {

                        println("FORTUNA KEY: $it")
                    }

                    val isWarm =

                        fortuna.optBoolean("isWarm")

                    val fortunaExpiry =
                        fortuna.optString("expiry")

                    val fortunaTime = try {

                        val expiryTime =
                            java.time.Instant.parse(fortunaExpiry)

                        val now =
                            java.time.Instant.now()

                        val secondsLeft =
                            expiryTime.epochSecond - now.epochSecond

                        if (secondsLeft <= 0) {

                            "Wechselt gerade..."

                        } else {

                            val hours =
                                secondsLeft / 3600

                            val minutes =
                                (secondsLeft % 3600) / 60

                            "${hours} h ${minutes} min"
                        }

                    } catch (e: Exception) {

                        "Unbekannt"
                    }

                    fortunaText =

                        if (isWarm) {

                            "🔥 Warm\n\nNoch: $fortunaTime"

                        } else {

                            "❄️ Kalt\n\nNoch: $fortunaTime"
                        }

                    val isDay =
                        cetus.optBoolean("isDay")

                    val timeLeft =
                        cetus.optString("timeLeft")

                    cetusText =

                        if (isDay) {

                            "🌞 Tag\n\nNoch: $timeLeft"

                        } else {

                            "🌙 Nacht\n\nNoch: $timeLeft"
                        }

                    val boss =
                        archon.optString("boss")

                    val faction =
                        archon.optString("faction")

                    val missions =
                        archon.getJSONArray("missions")

                    val missionList =
                        mutableListOf<String>()

                    for (i in 0 until missions.length()) {

                        val mission =
                            missions.getJSONObject(i)

                        val type =
                            mission.optString("typeKey")

                        val node =
                            mission.optString("nodeKey")

                        missionList.add(

                            "${i + 1}. $type\nOrt: $node"
                        )
                    }

                    archonText =

                        "Boss: $boss\n" +

                                "Fraktion: $faction\n\n" +

                                missionList.joinToString("\n\n")

                    println(arbitration.toString())

                    val arbitrationType =
                        arbitration.optString("type")

                    val arbitrationNode =
                        arbitration.optString("node")

                    val arbitrationEnemy =
                        arbitration.optString("enemy")

                    if (

                        arbitrationType == "Unknown" ||

                        arbitrationNode == "SolNode000"
                    ) {

                        arbitrationText =
                            "Schiedsgericht Daten aktuell nicht verfügbar"

                    } else {

                        arbitrationText =

                            "$arbitrationType\n\n" +

                                    "Ort: $arbitrationNode\n" +

                                    "Gegner: $arbitrationEnemy"
                    }


                    val sortieList =
                        mutableListOf<String>()

                    for (i in 0 until variants.length()) {

                        val variant =
                            variants.getJSONObject(i)

                        val mission =
                            variant.optString("missionType")

                        val modifier =
                            variant.optString("modifier")

                        val node =
                            variant.optString("node")

                        sortieList.add(

                            "${i + 1}. $mission\n" +

                                    "Ort: $node\n" +

                                    "Modifier: $modifier"
                        )
                    }

                    sortieText =
                        sortieList.joinToString("\n\n")


                    val tempList =
                        mutableListOf<FissureItem>()

                    for (i in 0 until fissures.length()) {

                        val fissure =
                            fissures.getJSONObject(i)

                        val tierRaw =
                            fissure.optString("tier")

                        val tier = when (tierRaw) {

                            "Lith" -> "🟤 Lith"

                            "Meso" -> "🔵 Meso"

                            "Neo" -> "🟣 Neo"

                            "Axi" -> "🟡 Axi"

                            "Requiem" -> "🔴 Requiem"

                            else -> tierRaw
                        }

                        val missionRaw =
                            fissure.optString("missionType")

                        val mission = when (missionRaw) {

                            "Survival" -> "Überleben"

                            "Defense" -> "Verteidigung"

                            "Capture" -> "Gefangennahme"

                            "Rescue" -> "Rettung"

                            "Interception" -> "Abfangen"

                            "Spy" -> "Spionage"

                            "Sabotage" -> "Sabotage"

                            "Disruption" -> "Disruption"

                            "Extermination" -> "Auslöschung"

                            else -> missionRaw
                        }

                        val nodeRaw =
                            fissure.optString("node")

                        val node = nodeRaw

                            .replace("(Earth)", "(Erde)")
                            .replace("(Mars)", "(Mars)")
                            .replace("(Venus)", "(Venus)")
                            .replace("(Mercury)", "(Merkur)")
                            .replace("(Jupiter)", "(Jupiter)")
                            .replace("(Saturn)", "(Saturn)")
                            .replace("(Uranus)", "(Uranus)")
                            .replace("(Neptune)", "(Neptun)")
                            .replace("(Pluto)", "(Pluto)")
                            .replace("(Void)", "(Void)")

                        val enemy =
                            fissure.optString("enemy")

                        val isHard =
                            fissure.optBoolean("isHard")

                        val isStorm =
                            fissure.optBoolean("isStorm")

                        val expiry =
                            fissure.optString("expiry")

                        val expiryMillis = try {

                            java.time.Instant
                                .parse(expiry)
                                .toEpochMilli()

                        } catch (e: Exception) {

                            0L
                        }

                        val secondsLeft =

                            (expiryMillis - currentTime) / 1000

                        val hours =
                            secondsLeft / 3600

                        val minutes =
                            (secondsLeft % 3600) / 60

                        val seconds =
                            secondsLeft % 60

                        val timeLeft = when {

                            secondsLeft <= 0 ->
                                "Abgelaufen"

                            hours > 0 ->
                                "${hours} h ${minutes} min ${seconds} sek"

                            minutes > 0 ->
                                "${minutes} min ${seconds} sek"

                            else ->
                                "${seconds} sek"
                        }

                        tempList.add(

                            FissureItem(

                                tier = tier,

                                mission = mission,

                                node = node,

                                enemy = enemy,

                                isHard = isHard,

                                isStorm = isStorm,

                                timeLeft = timeLeft
                            )
                        )
                    }

                    normalList =
                        tempList.filter { !it.isHard }

                    steelPathList =
                        tempList.filter { it.isHard }


                } catch (e: Exception) {

                    e.printStackTrace()

                    val errorText =
                        e.toString()

                    fissuresText = errorText
                    sortieText = errorText
                    arbitrationText = errorText
                    archonText = errorText
                    cetusText = errorText
                    fortunaText = errorText
                    cambionText = errorText
                    baroText = errorText
                }
                delay(3000)
            }
        }
    }

    @Composable
    fun TennoCard(

        title: String,

        icon: String,

        color: Color,

        selected: Boolean,

        onClick: () -> Unit
    ) {

        Card(

            modifier = Modifier
                .width(

                    if (selected)
                        165.dp
                    else
                        150.dp
                )

                .height(

                    if (selected)
                        90.dp
                    else
                        80.dp
                )

                .animateContentSize()
                .shadow(

                    elevation = 20.dp,

                    shape = RoundedCornerShape(20.dp),

                    ambientColor = color,

                    spotColor = color
                ),

            colors = CardDefaults.cardColors(

                containerColor =

                    if (selected)
                        color
                    else
                        Color(0xFF182544)
            ),

            shape = RoundedCornerShape(20.dp),

            onClick = onClick
        ) {

            Column(

                modifier = Modifier
                    .fillMaxSize(),

                verticalArrangement =
                    Arrangement.Center,

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(

                    text = icon,

                    style =
                        MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text = title,

                    color = Color.White
                )
            }
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

                    Color.Black.copy(alpha = 0.45f)
                )
        )

        val infiniteTransition = rememberInfiniteTransition(

            label = ""
        )

        val starOffset by infiniteTransition.animateFloat(

            initialValue = 0f,

            targetValue = 40f,

            animationSpec = infiniteRepeatable(

                animation = tween(

                    durationMillis = 12000,

                    easing = LinearEasing
                ),

                repeatMode = RepeatMode.Reverse
            ),

            label = ""
        )

        Canvas(

            modifier = Modifier
                .fillMaxSize()
        ) {

            repeat(120) {

                drawCircle(

                    color = Color.White.copy(

                        alpha = 0.18f
                    ),

                    radius = Random.nextFloat() * 3f,

                    center = Offset(

                        x = Random.nextFloat() * size.width,

                        y =
                            (Random.nextFloat() * size.height)
                                    + starOffset
                    )
                )
            }
        }

        LazyColumn(

            modifier = Modifier
                .fillMaxSize(),

            contentPadding = PaddingValues(
                16.dp
            ),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)

        ) {

            item {

                val interactionSource = remember {

                    MutableInteractionSource()
                }

                val isPressed by interactionSource
                    .collectIsPressedAsState()

                val scale by animateFloatAsState(

                    targetValue =

                        if (isPressed)
                            0.96f
                        else
                            1f,

                    animationSpec = tween(120),

                    label = ""
                )

                OutlinedCard(

                    modifier = Modifier
                        .height(52.dp),

                    border = BorderStroke(

                        width = 1.dp,

                        color = Color(0xFF00E5FF)
                    ),

                    colors = CardDefaults.outlinedCardColors(

                        containerColor =
                            Color(0x44101B30)
                    ),

                    shape = RoundedCornerShape(18.dp),

                    onClick = {
                        onBack()
                    }
                ) {

                    Box(

                        modifier = Modifier
                            .padding(horizontal = 22.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(

                            text = "← Zurück",

                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Column(

                    modifier = Modifier
                        .fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    val titleGlow by animateColorAsState(

                        targetValue =

                            if (selectedTab == "fissures")
                                Color(0xFF66F2FF)
                            else
                                Color(0xFF00E5FF),

                        animationSpec = infiniteRepeatable(

                            animation = tween(1800),

                            repeatMode = RepeatMode.Reverse
                        ),

                        label = ""
                    )

                    Text(

                        text = "TENNO FREUNDE",

                        fontFamily = warframeFont,

                        modifier = Modifier
                            .shadow(

                                elevation = 22.dp,

                                ambientColor = Color(0xFF00E5FF),

                                spotColor = Color(0xFF00E5FF)
                            ),

                        style =
                            MaterialTheme.typography.headlineLarge,

                        color = titleGlow
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(

                        text = "Live System Online",

                        style =
                            MaterialTheme.typography.bodyLarge,

                        color = Color.LightGray
                    )

                    Spacer(
                        modifier =
                            Modifier.height(30.dp)
                    )
                }

                Row(

                    modifier = Modifier
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    val voidColor by animateColorAsState(

                        targetValue =

                            if (selectedTab == "fissures")
                                Color(0xFF00B8D4)
                            else
                                Color(0xFF182544),

                        label = ""
                    )

                    val glowSize by animateFloatAsState(

                        targetValue =

                            if (selectedTab == "fissures")
                                38f
                            else
                                18f,

                        animationSpec = infiniteRepeatable(

                            animation = tween(

                                durationMillis = 1400
                            ),

                            repeatMode = RepeatMode.Reverse
                        ),

                        label = ""
                    )

                    OutlinedCard(

                        modifier = Modifier
                            .width(
                                if (selectedTab == "fissures")
                                    165.dp
                                else
                                    150.dp
                            )

                            .height(
                                if (selectedTab == "fissures")
                                    90.dp
                                else
                                    80.dp
                            )

                            .animateContentSize()
                            .shadow(

                                elevation =

                                    if (selectedTab == "fissures")
                                        glowSize.dp
                                    else
                                        12.dp,

                                shape = RoundedCornerShape(20.dp),

                                ambientColor = Color(0xFF00E5FF),

                                spotColor = Color(0xFF00E5FF)
                            ),

                        border = BorderStroke(

                            width =

                                if (selectedTab == "fissures")
                                    2.dp
                                else
                                    0.dp,

                            color = Color(0xFF00E5FF)
                        ),

                        colors = CardDefaults.outlinedCardColors(

                            containerColor = voidColor
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            MediaPlayer
                                .create(context, R.raw.click)
                                .start()
                            selectedTab = "fissures"
                        }
                    ) {

                        Column(

                            modifier = Modifier
                                .fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Image(

                                painter = painterResource(

                                    id = R.drawable.void_icon
                                ),

                                contentDescription = "Void Icon",

                                modifier = Modifier
                                    .size(42.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text = "Void-Risse",

                                color = Color.White
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Card(

                        modifier = Modifier
                            .width(

                                if (selectedTab == "steel")
                                    165.dp
                                else
                                    150.dp
                            )

                            .height(

                                if (selectedTab == "steel")
                                    90.dp
                                else
                                    80.dp
                            )

                            .animateContentSize()
                            .shadow(

                                elevation = 20.dp,

                                shape = RoundedCornerShape(20.dp),

                                ),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "steel")
                                    Color(0xFFFF1744)
                                else
                                    Color(0xFF2A1A1A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "steel"
                        }
                    ) {

                        Column(

                            modifier = Modifier
                                .fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(

                                text = "🔥",

                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(

                                text = "Stählerner Pfad",

                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(

                    modifier = Modifier
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    TennoCard(

                        title = "Arbitration",

                        icon = "⚔",

                        color = Color(0xFF00FF99),

                        selected =
                            selectedTab == "arbitration",

                        onClick = {

                            selectedTab = "arbitration"
                        }
                    )

                    TennoCard(

                        title = "Archon Hunt",

                        icon = "👹",

                        color = Color(0xFFFF5252),

                        selected =
                            selectedTab == "archon",

                        onClick = {

                            selectedTab = "archon"
                        }
                    )

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "baro")
                                    Color(0xFFFFD54F)
                                else
                                    Color(0xFF2A241A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "baro"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🛒",
                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Baro",
                                color = Color.White
                            )
                        }
                    }

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "cetus")
                                    Color(0xFF40C4FF)
                                else
                                    Color(0xFF122B4A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "cetus"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🌍",

                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Weltenzyklus",

                                color = Color.White
                            )
                        }
                    }

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "nightwave")
                                    Color(0xFFE040FB)
                                else
                                    Color(0xFF2A1A2A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "nightwave"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🌙",
                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Nightwave",
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(

                    modifier = Modifier
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "events")
                                    Color(0xFFE040FB)
                                else
                                    Color(0xFF2A1A2A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "events"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🧬",
                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Events",
                                color = Color.White
                            )
                        }
                    }

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "storms")
                                    Color(0xFF40C4FF)
                                else
                                    Color(0xFF122B4A)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "storms"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🚀",
                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Void Storms",
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(

                    modifier = Modifier
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    Card(

                        modifier = Modifier
                            .width(170.dp)
                            .height(85.dp),

                        colors = CardDefaults.cardColors(

                            containerColor =

                                if (selectedTab == "netra")
                                    Color(0xFF69F0AE)
                                else
                                    Color(0xFF143A1E)
                        ),

                        shape = RoundedCornerShape(20.dp),

                        onClick = {
                            selectedTab = "netra"
                        }
                    ) {

                        Column(

                            modifier = Modifier.fillMaxSize(),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🧪",
                                style =
                                    MaterialTheme.typography.headlineMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text = "Netracells",
                                color = Color.White
                            )
                        }
                    }



                        Card(

                            modifier = Modifier
                                .width(170.dp)
                                .height(85.dp),

                            colors = CardDefaults.cardColors(

                                containerColor =

                                    if (selectedTab == "syndicates")
                                        Color(0xFF40C4FF)
                                    else
                                        Color(0xFF122B4A)
                            ),

                            shape = RoundedCornerShape(20.dp),

                            onClick = {
                                selectedTab = "syndicates"
                            }
                        ) {

                            Column(

                                modifier = Modifier.fillMaxSize(),

                                verticalArrangement =
                                    Arrangement.Center,

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text = "👥",
                                    style =
                                        MaterialTheme.typography.headlineMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    color = Color.White,
                                    text = "Syndikate"
                                )
                            }
                        }
                    }




                    AnimatedVisibility(

                        visible = selectedTab == "fissures",

                        enter = fadeIn(

                            animationSpec = tween(350)
                        ),

                        exit = fadeOut(

                            animationSpec = tween(250)
                        )
                    ) {

                        LazyVerticalGrid(

                            columns = GridCells.Adaptive(170.dp),

                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 520.dp),

                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            items(normalList) { fissure ->

                                OutlinedCard(

                                    modifier = Modifier
                                        .shadow(

                                            elevation = 14.dp,

                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        .border(

                                            width = 1.5.dp,

                                            color = Color(0xFF00E5FF),

                                            shape = RoundedCornerShape(8.dp)
                                        ),

                                    colors = CardDefaults.outlinedCardColors(

                                        containerColor = Color(0x66101B30)
                                    ),

                                    shape = RoundedCornerShape(8.dp)
                                ) {

                                    Column(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(

                                                brush = Brush.verticalGradient(

                                                    colors = listOf(

                                                        Color(0x441A2C4A),

                                                        Color(0x22101B30)
                                                    )
                                                )
                                            )
                                            .padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            )

                                            .drawBehind {

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(0f, 0f),

                                                    end = Offset(size.width * 0.55f, 0f),

                                                    strokeWidth = 2.dp.toPx()
                                                )

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(0f, 0f),

                                                    end = Offset(0f, 20.dp.toPx()),

                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }

                                            .drawBehind {

                                                val stroke = 3.dp.toPx()

                                                val line = 28.dp.toPx()

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(0f, line),

                                                    end = Offset(0f, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(0f, 0f),

                                                    end = Offset(line, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(size.width - line, 0f),

                                                    end = Offset(size.width, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFF00E5FF),

                                                    start = Offset(size.width, 0f),

                                                    end = Offset(size.width, line),

                                                    strokeWidth = stroke
                                                )

                                                val animatedX =

                                                    ((System.currentTimeMillis() % 2500L)
                                                            / 2500f) * size.width

                                                clipRect {

                                                    drawLine(

                                                        color = Color(0x66FFFFFF),

                                                        start = Offset(animatedX - 80f, 0f),

                                                        end = Offset(animatedX, size.height),

                                                        strokeWidth = 3.dp.toPx()
                                                    )
                                                }
                                            }

                                                Row(

                                                modifier =
                                                    Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Text(

                                            text = fissure.tier,

                                            color = Color(0xFF00E5FF),

                                            style =
                                                MaterialTheme.typography.titleMedium
                                        )

                                        Text(

                                            text = fissure.timeLeft,

                                            color = Color(0xFF00FF99),

                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.height(8.dp)
                                    )

                                    Text(

                                        text = fissure.mission,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(3.dp)
                                    )

                                    Row(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {

                                        Text(

                                            text = fissure.node,

                                            color = Color.LightGray,

                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )

                                        Text(

                                            text =
                                                if (fissure.isStorm)
                                                    "STORM"
                                                else
                                                    "VOID",

                                            color = Color(0xFF40C4FF),

                                            style =
                                                MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (selectedTab == "steel") {

                        LazyVerticalGrid(

                            columns = GridCells.Fixed(2),

                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 700.dp),

                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            items(steelPathList) { fissure ->

                                OutlinedCard(

                                    modifier = Modifier
                                        .shadow(

                                            elevation = 28.dp,

                                            shape = RoundedCornerShape(24.dp)
                                        )

                                        .border(

                                            width = 1.5.dp,

                                            color = Color(0xFFFF1744),

                                            shape = RoundedCornerShape(24.dp)
                                        ),

                                    colors = CardDefaults.outlinedCardColors(

                                        containerColor = Color(0x66101B30)
                                    ),

                                    shape = RoundedCornerShape(24.dp)
                                ) {

                                    Column(

                                        modifier = Modifier
                                            .fillMaxWidth()

                                            .background(

                                                brush = Brush.verticalGradient(

                                                    colors = listOf(

                                                        Color(0x443A1018),

                                                        Color(0x22101B30)
                                                    )
                                                )
                                            )

                                            .padding(18.dp)

                                            .drawBehind {

                                                val stroke = 3.dp.toPx()

                                                val line = 28.dp.toPx()

                                                drawLine(

                                                    color = Color(0xFFFF1744),

                                                    start = Offset(0f, line),

                                                    end = Offset(0f, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFFFF1744),

                                                    start = Offset(0f, 0f),

                                                    end = Offset(line, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFFFF1744),

                                                    start = Offset(size.width - line, 0f),

                                                    end = Offset(size.width, 0f),

                                                    strokeWidth = stroke
                                                )

                                                drawLine(

                                                    color = Color(0xFFFF1744),

                                                    start = Offset(size.width, 0f),

                                                    end = Offset(size.width, line),

                                                    strokeWidth = stroke
                                                )

                                                val animatedX =

                                                    ((System.currentTimeMillis() % 2500L)
                                                            / 2500f) * size.width

                                                clipRect {

                                                    drawLine(

                                                        color = Color(0x66FFFFFF),

                                                        start = Offset(animatedX - 80f, 0f),

                                                        end = Offset(animatedX, size.height),

                                                        strokeWidth = 3.dp.toPx()
                                                    )
                                                }
                                            }
                                    ) {


                                        Text(

                                            text = fissure.tier,

                                            color = Color(0xFFFF5252),

                                            style =
                                                MaterialTheme.typography.headlineSmall
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(8.dp)
                                        )

                                        Text(

                                            text = fissure.mission,

                                            color = Color.White
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(6.dp)
                                        )

                                        Text(

                                            text = fissure.node,

                                            color = Color.LightGray
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(12.dp)
                                        )

                                        Text(

                                            text =
                                                "⏳ ${fissure.timeLeft}",

                                            color = Color(0xFF00FF99)
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(8.dp)
                                        )

                                        Text(

                                            text =
                                                if (fissure.isStorm)
                                                    "⚡ Storm"
                                                else
                                                    "Steel Path",

                                            color = Color(0xFFFF5252)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)

                    )


                    if (selectedTab == "sortie") {


                        Text(

                            text = "🔥 Sortie",

                            color = Color.Yellow,

                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Card(

                            modifier = Modifier.fillMaxWidth(),

                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A1A)
                            ),

                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Text(

                                text = sortieText,

                                color = Color.White,

                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (selectedTab == "arbitration") {

                        OutlinedCard(

                            modifier = Modifier
                                .fillMaxWidth()

                                .shadow(

                                    elevation = 28.dp,

                                    shape = RoundedCornerShape(28.dp)
                                )

                                .border(

                                    width = 1.5.dp,

                                    color = Color(0xFF00FF99),

                                    shape = RoundedCornerShape(28.dp)
                                ),

                            colors = CardDefaults.outlinedCardColors(

                                containerColor = Color(0x66101B30)
                            ),

                            shape = RoundedCornerShape(28.dp)
                        ) {

                            Column(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(

                                        brush = Brush.verticalGradient(

                                            colors = listOf(

                                                Color(0x4413382B),

                                                Color(0x22101B30)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                                    .drawBehind {

                                        val stroke = 3.dp.toPx()

                                        val line = 28.dp.toPx()

                                        drawLine(

                                            color = Color(0xFF00FF99),

                                            start = Offset(0f, line),

                                            end = Offset(0f, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFF00FF99),

                                            start = Offset(0f, 0f),

                                            end = Offset(line, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFF00FF99),

                                            start = Offset(size.width - line, 0f),

                                            end = Offset(size.width, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFF00FF99),

                                            start = Offset(size.width, 0f),

                                            end = Offset(size.width, line),

                                            strokeWidth = stroke
                                        )
                                    }
                            ) {


                                Text(

                                    text = "⚔ Arbitration",

                                    color = Color(0xFF00FF99),

                                    style =
                                        MaterialTheme.typography.headlineMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                Text(

                                    text = arbitrationText,

                                    color = Color.White,

                                    style =
                                        MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    if (selectedTab == "archon") {

                        OutlinedCard(

                            modifier = Modifier
                                .fillMaxWidth()

                                .shadow(

                                    elevation = 28.dp,

                                    shape = RoundedCornerShape(28.dp)
                                )

                                .border(

                                    width = 1.5.dp,

                                    color = Color(0xFFFF5252),

                                    shape = RoundedCornerShape(28.dp)
                                ),

                            colors = CardDefaults.outlinedCardColors(

                                containerColor = Color(0x66101B30)
                            ),

                            shape = RoundedCornerShape(28.dp)
                        ) {

                            Column(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .background(

                                        brush = Brush.verticalGradient(

                                            colors = listOf(

                                                Color(0x443A1616),

                                                Color(0x22101B30)
                                            )
                                        )
                                    )

                                    .padding(20.dp)

                                    .drawBehind {

                                        val stroke = 3.dp.toPx()

                                        val line = 28.dp.toPx()

                                        drawLine(

                                            color = Color(0xFFFF5252),

                                            start = Offset(0f, line),

                                            end = Offset(0f, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFFFF5252),

                                            start = Offset(0f, 0f),

                                            end = Offset(line, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFFFF5252),

                                            start = Offset(size.width - line, 0f),

                                            end = Offset(size.width, 0f),

                                            strokeWidth = stroke
                                        )

                                        drawLine(

                                            color = Color(0xFFFF5252),

                                            start = Offset(size.width, 0f),

                                            end = Offset(size.width, line),

                                            strokeWidth = stroke
                                        )

                                        val animatedX =

                                            ((System.currentTimeMillis() % 2500L)
                                                    / 2500f) * size.width

                                        clipRect {

                                            drawLine(

                                                color = Color(0x66FFFFFF),

                                                start = Offset(animatedX - 80f, 0f),

                                                end = Offset(animatedX, size.height),

                                                strokeWidth = 3.dp.toPx()
                                            )
                                        }
                                    }
                            ) {

                                Text(

                                    text = "👹 Archon Hunt",

                                    color = Color(0xFFFF5252),

                                    style =
                                        MaterialTheme.typography.headlineMedium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                Text(

                                    text = archonText,

                                    color = Color.White,

                                    style =
                                        MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                val isDay =
                    cetusText.contains("Tag")

                val cetusTime =
                    cetusText.substringAfter("Noch: ")

                val isWarm =
                    fortunaText.contains("Warm")

                val fortunaTime =
                    fortunaText.substringAfter("Noch: ")

                if (selectedTab == "cetus") {

                    Row(

                        modifier = Modifier
                            .horizontalScroll(
                                rememberScrollState()
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        OutlinedCard(

                            modifier = Modifier
                                .width(180.dp)

                                .shadow(

                                    elevation = 18.dp,

                                    shape = RoundedCornerShape(18.dp)
                                )

                                .border(

                                    width = 1.dp,

                                    color = Color(0xFF40C4FF),

                                    shape = RoundedCornerShape(18.dp)
                                ),

                            colors =
                                CardDefaults.outlinedCardColors(

                                    containerColor =
                                        Color(0x44101830)
                                ),

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Column(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .padding(14.dp)
                            ) {

                                Text(

                                    text = "CETUS",

                                    color = Color(0xFF40C4FF),

                                    fontSize = 13.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(

                                    text =
                                        if (isDay)
                                            "☀ DAY"
                                        else
                                            "🌙 NIGHT",

                                    color = Color.White,

                                    fontSize = 20.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(

                                    text = cetusTime,

                                    color =
                                        Color.LightGray,

                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedCard(

                            modifier = Modifier
                                .width(180.dp)

                                .shadow(

                                    elevation = 18.dp,

                                    shape = RoundedCornerShape(18.dp)
                                )

                                .border(

                                    width = 1.dp,

                                    color = Color(0xFFFFD54F),

                                    shape = RoundedCornerShape(18.dp)
                                ),

                            colors =
                                CardDefaults.outlinedCardColors(

                                    containerColor =
                                        Color(0x44302010)
                                ),

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Column(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .padding(14.dp)
                            ) {

                                Text(

                                    text = "FORTUNA",

                                    color = Color(0xFFFFD54F),

                                    fontSize = 13.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(

                                    text =
                                        if (isWarm)
                                            "☀ WARM"
                                        else
                                            "❄ COLD",

                                    color = Color.White,

                                    fontSize = 20.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(

                                    text = fortunaTime,

                                    color =
                                        Color.LightGray,

                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                        if (selectedTab == "baro") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFFFFD54F),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x444A3A12),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFFFFD54F),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFFFD54F),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFFFD54F),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFFFD54F),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }

                                ) {

                                    Text(

                                        text = "🛒 Baro Ki'Teer",

                                        color = Color(0xFFFFD54F),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = baroText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }


                        if (selectedTab == "nightwave") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFFE040FB),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x4435124A),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }
                                ) {

                                    Text(

                                        text = "🌙 Nightwave",

                                        color = Color(0xFFE040FB),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = nightwaveText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        if (selectedTab == "invasions") {

                            Text(

                                text = "⚔ Invasionen",

                                color = Color.Red,

                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Card(

                                modifier = Modifier.fillMaxWidth(),

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A1A1A)
                                ),

                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Text(

                                    text = invasionText,

                                    color = Color.White,

                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        if (selectedTab == "alerts") {

                            Text(

                                text = "🚨 Alarmierungen",

                                color = Color.Yellow,

                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Card(

                                modifier = Modifier.fillMaxWidth(),

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A2A1A)
                                ),

                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Text(

                                    text = alertText,

                                    color = Color.White,

                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        if (selectedTab == "storms") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFF40C4FF),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x44102D4A),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }
                                ) {

                                    Text(

                                        text = "🚀 Void Storms",

                                        color = Color(0xFF40C4FF),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = stormText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        if (selectedTab == "events") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFFE040FB),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x443A124A),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFFE040FB),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }
                                ) {

                                    Text(

                                        text = "🧬 Events",

                                        color = Color(0xFFE040FB),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = eventText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }



                        if (selectedTab == "deep") {

                            Text(

                                text = "🏆 Deep Archimedea",

                                color = Color.Cyan,

                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Card(

                                modifier = Modifier.fillMaxWidth(),

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1A2A2A)
                                ),

                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Text(

                                    text = deepText,

                                    color = Color.White,

                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        if (selectedTab == "netra") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFF69F0AE),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x44143A1E),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFF69F0AE),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF69F0AE),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF69F0AE),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF69F0AE),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }
                                ) {

                                    Text(

                                        text = "🧪 Netracells",

                                        color = Color(0xFF69F0AE),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = netraText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }



                        if (selectedTab == "syndicates") {

                            OutlinedCard(

                                modifier = Modifier
                                    .fillMaxWidth()

                                    .shadow(

                                        elevation = 28.dp,

                                        shape = RoundedCornerShape(28.dp)
                                    )

                                    .border(

                                        width = 1.5.dp,

                                        color = Color(0xFF40C4FF),

                                        shape = RoundedCornerShape(28.dp)
                                    ),

                                colors = CardDefaults.outlinedCardColors(

                                    containerColor = Color(0x66101B30)
                                ),

                                shape = RoundedCornerShape(28.dp)
                            ) {

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()

                                        .background(

                                            brush = Brush.verticalGradient(

                                                colors = listOf(

                                                    Color(0x44122B4A),

                                                    Color(0x22101B30)
                                                )
                                            )
                                        )

                                        .padding(20.dp)

                                        .drawBehind {

                                            val stroke = 3.dp.toPx()

                                            val line = 28.dp.toPx()

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(0f, line),

                                                end = Offset(0f, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(0f, 0f),

                                                end = Offset(line, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(size.width - line, 0f),

                                                end = Offset(size.width, 0f),

                                                strokeWidth = stroke
                                            )

                                            drawLine(

                                                color = Color(0xFF40C4FF),

                                                start = Offset(size.width, 0f),

                                                end = Offset(size.width, line),

                                                strokeWidth = stroke
                                            )

                                            val animatedX =

                                                ((System.currentTimeMillis() % 2500L)
                                                        / 2500f) * size.width

                                            clipRect {

                                                drawLine(

                                                    color = Color(0x66FFFFFF),

                                                    start = Offset(animatedX - 80f, 0f),

                                                    end = Offset(animatedX, size.height),

                                                    strokeWidth = 3.dp.toPx()
                                                )
                                            }
                                        }
                                ) {

                                    Text(

                                        text = "👥 Syndikate",

                                        color = Color(0xFF40C4FF),

                                        style =
                                            MaterialTheme.typography.headlineMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )

                                    Text(

                                        text = syndicateText,

                                        color = Color.White,

                                        style =
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
















