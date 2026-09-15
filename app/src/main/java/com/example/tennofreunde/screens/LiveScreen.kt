package com.example.tennofreunde.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tennofreunde.R
import com.example.tennofreunde.api.*
import com.example.tennofreunde.data.GitHubApi
import com.example.tennofreunde.data.PrimeDropCache
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class LiveEntry(val title: String, val detail: String, val expiry: String? = null, val source: String? = null)
private data class LiveFeed(val entries: List<LiveEntry> = emptyList(), val loading: Boolean = true, val error: Boolean = false, val updated: Instant? = null, val diagnostic: String? = null)
private data class LivePlatform(val key: String, val label: String)
private enum class LiveSection { WORLD_CYCLES, FISSURES, ALERTS, EVENTS, BARO, INVASIONS, SORTIE, RELICS, DEALS, NEWS }
private val livePlatforms = listOf(
    LivePlatform("ps4", "PlayStation"),
    LivePlatform("pc", "PC"),
    LivePlatform("xb1", "Xbox"),
    LivePlatform("swi", "Switch"),
    LivePlatform("ios", "iOS"),
    LivePlatform("android", "Android")
)

@Composable
fun LiveScreen(language: AppLanguage, offlineMode: Boolean = false) {
    LiveDashboard(language, offlineMode)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveDashboard(language: AppLanguage, offlineMode: Boolean) {
    val german = language == AppLanguage.GERMAN
    val compact = LocalCompactMode.current
    val liveAccent = LocalWarframePalette.current.energy
    val context = LocalContext.current
    val feeds = remember { mutableStateMapOf<LiveSection, LiveFeed>() }
    var selected by rememberSaveable { mutableStateOf<LiveSection?>(null) }
    var platformKey by rememberSaveable { mutableStateOf(context.getSharedPreferences("live_settings", android.content.Context.MODE_PRIVATE).getString("platform", "ps4") ?: "ps4") }
    var relevantOnly by rememberSaveable { mutableStateOf(false) }
    var refreshIntervalMinutes by rememberSaveable { mutableIntStateOf(context.getSharedPreferences("live_settings", android.content.Context.MODE_PRIVATE).getInt("refresh_minutes", 2)) }
    var refresh by remember { mutableIntStateOf(0) }
    var now by remember { mutableStateOf(Instant.now()) }
    val selectedPlatform = livePlatforms.firstOrNull { it.key == platformKey } ?: livePlatforms.first()

    LaunchedEffect(Unit) { while (true) { now = Instant.now(); delay(1000) } }
    LaunchedEffect(refresh, language, platformKey, refreshIntervalMinutes, offlineMode) {
        feeds.clear()
        if (offlineMode) {
            LiveSection.entries.forEach { section ->
                val cached = loadLiveCache(context, platformKey, language.code, section).ifEmpty {
                    if (section == LiveSection.RELICS) {
                        runCatching {
                            PrimeDropCache.loadPrimeDrops(context).map { LiveEntry(it.part, "${it.relic}\n${it.farmLocation.orEmpty()}") }
                        }.getOrDefault(emptyList())
                    } else emptyList()
                }
                feeds[section] = LiveFeed(entries = cached, loading = false)
            }
            return@LaunchedEffect
        }
        while (true) {
            supervisorScope {
                LiveSection.entries.forEach { section ->
                    launch {
                        val previous = feeds[section] ?: LiveFeed()
                        feeds[section] = previous.copy(loading = true)
                        try {
                            val entries = loadSection(section, language, context, platformKey)
                            saveLiveCache(context, platformKey, language.code, section, entries)
                            feeds[section] = LiveFeed(entries, loading = false, updated = Instant.now())
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            val cached = loadLiveCache(context, platformKey, language.code, section).ifEmpty {
                                if (section == LiveSection.RELICS && previous.entries.isEmpty()) {
                                runCatching {
                                    PrimeDropCache.loadPrimeDrops(context).map { LiveEntry(it.part, "${it.relic}\n${it.farmLocation.orEmpty()}") }
                                }.getOrDefault(emptyList())
                                } else previous.entries
                            }
                            feeds[section] = previous.copy(entries = cached, loading = false, error = true, diagnostic = error.localizedMessage ?: error::class.java.simpleName)
                        }
                    }
                }
            }
            delay(refreshIntervalMinutes.coerceAtLeast(1) * 60000L)
        }
    }
    val visibleSections = remember(feeds.toMap(), relevantOnly) {
        if (!relevantOnly) LiveSection.entries.toList() else LiveSection.entries.filter { section ->
            section == LiveSection.BARO || feeds[section]?.entries.orEmpty().any { it.isRelevantForPlayer() }
        }.ifEmpty { LiveSection.entries.toList() }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(AppBrushes.MainBackground)) {
        val liveColumns = if (maxWidth >= 840.dp) 3 else 2
        val horizontalInset = if (compact) 12.dp else if (maxWidth >= 840.dp) 32.dp else 20.dp
        Image(painterResource(R.drawable.warframe_bg), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.22f)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = horizontalInset, vertical = if (compact) 12.dp else 20.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)) {
            item {
                Text(if (german) "DEIN ORIGIN-SYSTEM" else "YOUR ORIGIN SYSTEM", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Text("Tenno Freunde", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(if (german) "Missionen, Belohnungen und deine nächste Jagd." else "Missions, rewards and your next hunt.", color = AppColors.TextSecondary)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    livePlatforms.forEach { platform ->
                        AssistChip(
                            onClick = {
                                platformKey = platform.key
                                context.getSharedPreferences("live_settings", android.content.Context.MODE_PRIVATE).edit().putString("platform", platform.key).apply()
                                refresh++
                            },
                            label = { Text(platform.label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (platform.key == platformKey) liveAccent.copy(alpha = 0.2f) else AppColors.HudPanel,
                                labelColor = if (platform.key == platformKey) liveAccent else AppColors.TextSecondary
                            )
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${if (german) "Weltstatus" else "World state"} · ${selectedPlatform.label}", modifier = Modifier.padding(top = 16.dp), color = liveAccent)
                    TextButton(onClick = { refresh++ }, enabled = !offlineMode && feeds.isNotEmpty() && feeds.values.none { it.loading }) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (german) "Aktualisieren" else "Refresh")
                    }
                }
                if (offlineMode) {
                    Surface(color = AppColors.OrokinGold.copy(alpha = 0.14f), shape = AppShapes.Small) {
                        Text(
                            if (german) "OFFLINE · Gespeicherter Weltstatus" else "OFFLINE · Saved world state",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = AppColors.OrokinGold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { relevantOnly = !relevantOnly },
                        label = { Text(if (german) "Relevant für mich" else "Relevant to me") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (relevantOnly) AppColors.OrokinGold.copy(alpha = 0.2f) else AppColors.HudPanel,
                            labelColor = if (relevantOnly) AppColors.OrokinGold else AppColors.TextSecondary
                        )
                    )
                    listOf(1, 2, 5).forEach { minutes ->
                        AssistChip(
                            onClick = {
                                refreshIntervalMinutes = minutes
                                context.getSharedPreferences("live_settings", android.content.Context.MODE_PRIVATE).edit().putInt("refresh_minutes", minutes).apply()
                                refresh++
                            },
                            label = { Text(if (german) "${minutes} Min." else "${minutes} min") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (refreshIntervalMinutes == minutes) liveAccent.copy(alpha = 0.2f) else AppColors.HudPanel,
                                labelColor = if (refreshIntervalMinutes == minutes) liveAccent else AppColors.TextSecondary
                            )
                        )
                    }
                }
            }
            items(visibleSections.chunked(liveColumns)) { sections ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    sections.forEach { section ->
                        val feed = feeds[section] ?: LiveFeed()
                        val urgent = feed.entries.any { it.isUrgent(now) }
                        Surface(modifier = Modifier.weight(1f), shape = AppShapes.Medium, color = if (urgent) Color(0xFF343025) else AppColors.HudPanel, onClick = { selected = section }) {
                            Column(Modifier.padding(if (compact) 11.dp else 16.dp).heightIn(min = if (compact) 94.dp else 116.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)) {
                                Text(sectionTitle(section, german), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (feed.loading && feed.entries.isEmpty()) {
                                    LiveLoadingSkeleton()
                                } else {
                                    Text(when {
                                        feed.error -> if (feed.entries.isEmpty()) { if (german) "Nicht erreichbar" else "Unavailable" } else { if (german) "Gespeicherter Stand" else "Cached data" }
                                        feed.entries.isEmpty() -> if (german) "Aktuell keine Einträge" else "No active entries"
                                        section == LiveSection.WORLD_CYCLES -> feed.entries.first().let { if (activeUntil(it.expiry, now)) it.title else if (german) "Zyklen werden aktualisiert …" else "Updating cycles …" }
                                        section == LiveSection.BARO -> feed.entries.first().title
                                        else -> if (german) "${feed.entries.size} Einträge" else "${feed.entries.size} entries"
                                    }, color = if (urgent) AppColors.OrokinGold else liveAccent, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(if (german) "Details öffnen ›" else "Open details ›", color = AppColors.TextSecondary, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    repeat(liveColumns - sections.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            item {
                Text(
                    if (offlineMode) {
                        if (german) "Offline-Modus aktiv · Keine Netzabfragen · Letzter gespeicherter Stand"
                        else "Offline mode active · No network requests · Last saved state"
                    } else {
                        if (german) "Aktualisierung alle $refreshIntervalMinutes Minuten · Offline-Cache aktiv · Datenquelle: Warframe-Weltstatus"
                        else "Updates every $refreshIntervalMinutes minutes · Offline cache active · Source: Warframe world state"
                    },
                    color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    selected?.let { section ->
                LiveDetails(section, feeds[section] ?: LiveFeed(), now, german, onClose = { selected = null }, onRetry = { refresh++ })
    }
}

@Composable
private fun LiveLoadingSkeleton() {
    val liveAccent = LocalWarframePalette.current.energy
    val transition = rememberInfiniteTransition(label = "liveLoading")
    val alpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "liveLoadingAlpha"
    )
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.fillMaxWidth(0.82f).height(9.dp).background(liveAccent.copy(alpha = alpha), AppShapes.Small))
        Box(Modifier.fillMaxWidth(0.58f).height(7.dp).background(AppColors.OrokinGold.copy(alpha = alpha * 0.75f), AppShapes.Small))
        Box(Modifier.fillMaxWidth(0.7f).height(7.dp).background(Color.White.copy(alpha = alpha * 0.45f), AppShapes.Small))
    }
}

private suspend fun loadSection(section: LiveSection, language: AppLanguage, context: android.content.Context, platform: String): List<LiveEntry> {
    val api = WarframeApi.api
    val lang = language.code
    val de = language == AppLanguage.GERMAN
    return when (section) {
        LiveSection.WORLD_CYCLES -> listOf(
            worldCycleEntry(
                name = if (de) "Cetus / Eidolon" else "Cetus / Eidolon",
                cycle = api.getCetusCycle(platform, lang),
                de = de,
                dayText = if (de) "Tag auf den Ebenen" else "Day on the Plains",
                nightText = if (de) "Nacht auf den Ebenen" else "Night on the Plains",
                extra = if (de) "Eidolon-Jagd ist nachts verfügbar." else "Eidolon hunts are available at night."
            ),
            worldCycleEntry(
                name = if (de) "Fortuna / Orb Vallis" else "Fortuna / Orb Vallis",
                cycle = api.getVallisCycle(platform, lang),
                de = de,
                dayText = if (de) "Warmphase" else "Warm cycle",
                nightText = if (de) "Kaltphase" else "Cold cycle",
                extra = if (de) "Nützlich für Vallis-Farmen und Orb-Timing." else "Useful for Vallis farms and Orb timing."
            ),
            worldCycleEntry(
                name = if (de) "Erde" else "Earth",
                cycle = api.getEarthCycle(platform, lang),
                de = de,
                dayText = if (de) "Tag auf der Erde" else "Earth day",
                nightText = if (de) "Nacht auf der Erde" else "Earth night",
                extra = if (de) "Relevant für Pflanzen, Ressourcen und Erde-Farmen." else "Relevant for plants, resources, and Earth farms."
            ),
            worldCycleEntry(
                name = if (de) "Deimos / Cambion-Drift" else "Deimos / Cambion Drift",
                cycle = api.getCambionCycle(platform, lang),
                de = de,
                dayText = "Fass",
                nightText = "Vome",
                extra = if (de) "Hilft bei Deimos-Farmen und Isolation-Vault-Planung." else "Helps with Deimos farms and Isolation Vault planning."
            )
        )
        LiveSection.FISSURES -> api.getFissures(platform, lang).filter { activeUntil(it.expiry, Instant.now()) }.map {
            LiveEntry("${translate(it.tier, de) ?: "Void"} · ${translateNode(it.node, de) ?: if (de) "Unbekannter Ort" else "Unknown location"}",
                listOfNotNull(translate(it.missionType, de), translate(it.enemy, de), if (it.isHard == true) { if (de) "Stählerner Pfad" else "Steel Path" } else null, if (it.isStorm == true) { if (de) "Void-Sturm" else "Void Storm" } else null).joinToString(" · "), it.expiry)
        }
        LiveSection.ALERTS -> api.getAlerts(platform, lang).filter { activeUntil(it.expiry, Instant.now()) }.map {
            LiveEntry(translateNode(it.mission.node, de) ?: if (de) "Alarm" else "Alert", listOfNotNull(translate(it.mission.type, de), translate(it.mission.faction, de),
                it.mission.minEnemyLevel?.let { level -> "Level $level–${it.mission.maxEnemyLevel ?: level}" },
                it.mission.reward?.items?.joinToString(), it.mission.reward?.credits?.let { credits -> "$credits ${if (de) "Credits" else "credits"}" }).joinToString("\n"), it.expiry)
        }
        LiveSection.EVENTS -> api.getEvents(platform, lang).filter { it.active != false && activeUntil(it.expiry, Instant.now()) }.map {
            LiveEntry(it.description ?: if (de) "Ereignis" else "Event", listOfNotNull(it.health?.let { h -> "${if (de) "Fortschritt" else "Progress"}: ${h.toInt()} %" }, it.currentScore?.let { s -> "${if (de) "Punkte" else "Score"}: ${s.toInt()}" }).joinToString("\n"), it.expiry)
        }
        LiveSection.BARO -> api.getBaro(platform, lang).let { baro ->
            val now = Instant.now()
            val active = baroIsActive(baro, now)
            val targetTime = if (active) baro.expiry else baro.activation
            val timeText = targetTime?.let { remainingTime(it, now, de) }
                ?: translateStatus(if (active) baro.endString else baro.startString, de)
            listOf(LiveEntry(if (active) { if (de) "Baro ist da" else "Baro has arrived" } else { if (de) "Nächster Baro-Besuch" else "Next Baro visit" },
                listOfNotNull(translateNode(baro.location, de), timeText).joinToString("\n"), targetTime)) +
                baro.inventory.map { LiveEntry(it.item, "${it.ducats} ${if (de) "Dukaten" else "ducats"} · ${it.credits} Credits") }
        }
        LiveSection.INVASIONS -> api.getInvasions(platform, lang)
            .filter { it.completed != true }
            .sortedByDescending { listOfNotNull(it.attacker?.reward?.itemString, it.defender?.reward?.itemString).joinToString(" ").isNotBlank() }
            .map {
            LiveEntry(translateNode(it.node, de) ?: "Invasion", listOfNotNull(translate(it.desc, de),
                "${translate(it.attacker?.faction, de) ?: if (de) "Angreifer" else "Attacker"} ${if (de) "gegen" else "versus"} ${translate(it.defender?.faction, de) ?: if (de) "Verteidiger" else "Defender"}",
                it.attacker?.reward?.itemString?.let { reward -> "${if (de) "Angreifer" else "Attacker"}: $reward" },
                it.defender?.reward?.itemString?.let { reward -> "${if (de) "Verteidiger" else "Defender"}: $reward" },
                it.completion?.let { p -> "${if (de) "Fortschritt" else "Progress"}: ${p.toInt()} %" }, it.eta).joinToString("\n"))
        }
        LiveSection.SORTIE -> api.getSortie(platform, lang).let { sortie ->
            if (!activeUntil(sortie.expiry, Instant.now())) emptyList() else
                listOf(LiveEntry(sortie.boss ?: if (de) "Tägliche Sortie" else "Daily Sortie", translate(sortie.faction, de).orEmpty(), sortie.expiry)) +
                sortie.variants.orEmpty().mapIndexed { index, variant ->
                    LiveEntry("${index + 1}. ${translateNode(variant.node, de) ?: "Mission"}",
                        listOfNotNull(translate(variant.missionType, de), translate(variant.modifier, de), translateDescription(variant.modifierDescription, de)).joinToString("\n"))
                }
        }
        LiveSection.RELICS -> {
            val relics = GitHubApi.api.getRelics()
            PrimeDropCache.savePrimeDrops(context, relics.map { PrimeDropResponse(it.part, it.relic, it.rotation, it.farmLocation) })
            relics.map { LiveEntry(it.part, "${it.relic}\n${it.farmLocation}\n${if (de) "Rotation" else "Rotation"}: ${it.rotation}") }
        }
        LiveSection.DEALS -> api.getDailyDeals(platform, lang).filter { activeUntil(it.expiry, Instant.now()) }.map {
            LiveEntry(it.item ?: if (de) "Angebot" else "Deal", listOfNotNull(it.salePrice?.let { p -> "$p Platinum" }, it.originalPrice?.let { p -> "${if (de) "Regulär" else "Regular"}: $p Platinum" },
                it.discount?.let { d -> "$d % ${if (de) "Rabatt" else "off"}" }, it.total?.let { total -> "${if (de) "Verfügbar" else "Available"}: ${(total - (it.sold ?: 0)).coerceAtLeast(0)} / $total" }).joinToString("\n"), it.expiry)
        }
        LiveSection.NEWS -> api.getNews(platform, lang).sortedByDescending { it.date }.take(30).map {
            LiveEntry(
                it.translations?.get(lang) ?: it.message ?: if (de) "Neuigkeit" else "News",
                formatNewsDate(it.date),
                source = if (de) "Warframe News" else "Warframe news"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveDetails(section: LiveSection, feed: LiveFeed, now: Instant, german: Boolean, onClose: () -> Unit, onRetry: () -> Unit) {
    val liveAccent = LocalWarframePalette.current.energy
    var query by rememberSaveable(section) { mutableStateOf("") }
    var fissureTier by rememberSaveable(section) { mutableStateOf("Alle") }
    var rewardsOnly by rememberSaveable(section) { mutableStateOf(false) }
    var urgentOnly by rememberSaveable(section) { mutableStateOf(false) }
    val entries = remember(feed.entries, query, fissureTier, rewardsOnly, urgentOnly, now) {
        feed.entries.filter { entry ->
            val matchesText = entry.title.contains(query, true) || entry.detail.contains(query, true)
            val matchesTier = section != LiveSection.FISSURES || fissureTier == "Alle" || entry.title.contains(fissureTier, true)
            val matchesReward = !rewardsOnly || entry.hasRewardSignal()
            val matchesUrgency = !urgentOnly || entry.isUrgent(now)
            matchesText && matchesTier && matchesReward && matchesUrgency
        }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp), shape = AppShapes.Large, color = AppColors.HudPanel, contentColor = Color.White) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, if (german) "Zurück" else "Back", tint = liveAccent) }
                    Text(sectionTitle(section, german), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = AppColors.OrokinGold)
                }
                if (feed.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = liveAccent)
                if (feed.error) {
                    Text(if (feed.entries.isEmpty()) {
                        if (german) "Daten konnten nicht geladen werden. Bitte prüfe deine Internetverbindung." else "Data could not be loaded. Check your connection."
                    } else {
                        if (german) "Aktualisierung fehlgeschlagen. Letzter verfügbarer Stand." else "Refresh failed. Showing last available data."
                    }, color = Color(0xFFFFCC9A))
                    feed.diagnostic?.let {
                        Text(
                            if (german) "Diagnose: $it" else "Diagnostic: $it",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onRetry, enabled = !feed.loading) { Text(if (german) "Erneut versuchen" else "Try again") }
                }
                feed.updated?.let { Text("${if (german) "Stand" else "Updated"}: ${DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(it)}", color = liveAccent, style = MaterialTheme.typography.labelSmall) }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = {
                        Text(
                            if (section == LiveSection.RELICS) {
                                if (german) "Komponente oder Relikt suchen" else "Search component or relic"
                            } else {
                                if (german) "Diese Live-Daten durchsuchen" else "Search these live entries"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                )
                if (section == LiveSection.FISSURES) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Alle", "Lith", "Meso", "Neo", "Axi", "Requiem").forEach { tier ->
                            AssistChip(
                                onClick = { fissureTier = tier },
                                label = { Text(if (tier == "Alle" && !german) "All" else tier) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (fissureTier == tier) liveAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                    labelColor = if (fissureTier == tier) liveAccent else AppColors.TextSecondary
                                )
                            )
                        }
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (section == LiveSection.ALERTS || section == LiveSection.INVASIONS || section == LiveSection.BARO || section == LiveSection.DEALS) {
                        AssistChip(
                            onClick = { rewardsOnly = !rewardsOnly },
                            label = { Text(if (german) "Nur Belohnungen" else "Rewards only") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (rewardsOnly) AppColors.OrokinGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                labelColor = if (rewardsOnly) AppColors.OrokinGold else AppColors.TextSecondary
                            )
                        )
                    }
                    AssistChip(
                        onClick = { urgentOnly = !urgentOnly },
                        label = { Text(if (german) "Dringend" else "Urgent") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (urgentOnly) liveAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                            labelColor = if (urgentOnly) liveAccent else AppColors.TextSecondary
                        )
                    )
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (entries.isEmpty() && !feed.loading && !feed.error) item {
                        Text(
                            if (query.isBlank()) {
                                if (german) "Zurzeit keine aktiven Einträge." else "No active entries."
                            } else if (section == LiveSection.RELICS) {
                                if (german) "Keine passenden Relikte gefunden." else "No matching relics."
                            } else {
                                if (german) "Keine passenden Live-Einträge gefunden." else "No matching live entries."
                            },
                            color = AppColors.TextSecondary
                        )
                    }
                    items(entries) { entry ->
                        Surface(shape = AppShapes.Small, color = if (entry.isUrgent(now)) Color(0xFF3A3025) else Color(0xFF202A40), contentColor = Color.White) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(entry.title, fontWeight = FontWeight.SemiBold)
                                if (entry.detail.isNotBlank()) Text(entry.detail, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                entry.expiry?.let { Text(remainingTime(it, now, german), color = liveAccent, style = MaterialTheme.typography.labelLarge) }
                                entry.source?.let { Text("${if (german) "Quelle" else "Source"}: $it", color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text(if (german) "Zurück zur Übersicht" else "Back to overview") }
            }
        }
    }
}

private fun sectionTitle(section: LiveSection, german: Boolean): String = when (section) {
    LiveSection.WORLD_CYCLES -> if (german) "Weltenzyklus" else "World cycles"
    LiveSection.FISSURES -> if (german) "Void-Risse" else "Void Fissures"
    LiveSection.ALERTS -> if (german) "Alarme" else "Alerts"
    LiveSection.EVENTS -> if (german) "Ereignisse" else "Events"
    LiveSection.BARO -> "Baro Ki’Teer"
    LiveSection.INVASIONS -> if (german) "Invasionen" else "Invasions"
    LiveSection.SORTIE -> if (german) "Sortie" else "Sortie"
    LiveSection.RELICS -> if (german) "Relikte" else "Relics"
    LiveSection.DEALS -> if (german) "Tägliche Angebote" else "Daily Deals"
    LiveSection.NEWS -> if (german) "Neuigkeiten" else "News"
}

private fun translate(value: String?, german: Boolean): String? {
    if (!german || value == null) return value
    val exact = mapOf(
        "Extermination" to "Auslöschung", "Capture" to "Gefangennahme", "Defense" to "Verteidigung",
        "Mobile Defense" to "Mobile Verteidigung", "Survival" to "Überleben", "Excavation" to "Ausgrabung",
        "Spy" to "Spionage", "Sabotage" to "Sabotage", "Rescue" to "Rettung", "Interception" to "Abfangen",
        "Assassination" to "Attentat", "Infested" to "Befallene", "Grineer" to "Grineer", "Corpus" to "Corpus",
        "Lith" to "Lith", "Meso" to "Meso", "Neo" to "Neo", "Axi" to "Axi", "Requiem" to "Requiem"
    )
    exact[value]?.let { return it }
    return value
        .replace("Enemy Physical Enhancement", "Gegnerische physische Verstärkung")
        .replace("Enemy Elemental Enhancement", "Gegnerische Elementverstärkung")
        .replace("Weapon Restriction", "Waffenbeschränkung")
        .replace("Pistol Only", "Nur Pistole")
        .replace("Secondary Only", "Nur Sekundärwaffe")
        .replace("Melee Only", "Nur Nahkampf")
        .replace("Shotgun Only", "Nur Schrotflinte")
        .replace("Sniper Only", "Nur Scharfschützengewehr")
        .replace("Bow Only", "Nur Bogen")
        .replace("Assault Rifle Only", "Nur Sturmgewehr")
        .replace("Eximus Stronghold", "Eximus-Festung")
        .replace("Energy Reduction", "Energiereduzierung")
        .replace("Radiation Hazard", "Strahlungsgefahr")
        .replace("Augmented Enemy Armor", "Verstärkte gegnerische Rüstung")
        .replace("Impact", "Einschlag")
        .replace("Puncture", "Durchschlag")
        .replace("Slash", "Schnitt")
        .replace("Enemies can deal enhanced impact damage. Finishing damage is not resisted.", "Gegner verursachen erhöhten Einschlagschaden. Finisher-Schaden wird nicht widerstanden.")
        .replace("Eximus units have a much higher spawn rate in this mission. Some of their auras stack.", "Eximus-Einheiten erscheinen in dieser Mission deutlich häufiger. Einige ihrer Auren sind stapelbar.")
        .replace("Only secondary weapons may be used in this mission, any other weapon type is not allowed, and will be removed automatically if equipped.", "In dieser Mission sind nur Sekundärwaffen erlaubt. Andere ausgerüstete Waffen werden automatisch entfernt.")
}

private fun translateNode(value: String?, german: Boolean): String? {
    if (!german || value == null) return value
    return value
        .replace("(Earth)", "(Erde)")
        .replace("(Mercury)", "(Merkur)")
        .replace("(Neptune)", "(Neptun)")
        .replace("(Void)", "(Void)")
}

private fun translateStatus(value: String?, german: Boolean): String {
    if (value.isNullOrBlank()) return ""
    if (!german) return value
    return value
        .replace("Arrives in", "Ankunft in")
        .replace("Leaves in", "Abreise in")
        .replace("Active for", "Aktiv für")
}

internal fun baroIsActive(baro: BaroResponse, now: Instant = Instant.now()): Boolean {
    val activation = baro.activation?.let { runCatching { Instant.parse(it) }.getOrNull() }
    val expiry = baro.expiry?.let { runCatching { Instant.parse(it) }.getOrNull() }
    return if (activation != null && expiry != null) {
        !now.isBefore(activation) && now.isBefore(expiry)
    } else {
        baro.active == true
    }
}

private fun worldCycleEntry(
    name: String,
    cycle: CetusCycleResponse,
    de: Boolean,
    dayText: String,
    nightText: String,
    extra: String
): LiveEntry {
    val state = when {
        cycle.isDay == true || cycle.isWarm == true -> dayText
        cycle.isDay == false || cycle.isWarm == false -> nightText
        !cycle.state.isNullOrBlank() -> cycle.state
        else -> if (de) "Zyklus unbekannt" else "Unknown cycle"
    }
    val remaining = cycle.timeLeft?.let { if (de) "Restzeit: $it" else "Time left: $it" }
    return LiveEntry(
        title = "$name · $state",
        detail = listOfNotNull(remaining, extra).joinToString("\n"),
        expiry = cycle.expiry
    )
}

private fun translateDescription(value: String?, german: Boolean): String? {
    val translated = translate(value, german)
    if (!german || translated == null || translated != value) return translated
    return if (value.length > 45) "Diese Mission besitzt eine besondere Sortie-Bedingung." else value
}

private fun formatNewsDate(date: String?): String = date?.let {
    runCatching {
        val published = Instant.parse(it)
        if (published == Instant.EPOCH) "" else DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm").withZone(ZoneId.systemDefault()).format(published)
    }.getOrDefault("")
}.orEmpty()

private fun activeUntil(expiry: String?, now: Instant): Boolean =
    expiry?.let { runCatching { Instant.parse(it).isAfter(now) }.getOrDefault(true) } ?: true

private fun remainingTime(expiry: String, now: Instant, german: Boolean): String = runCatching {
    val seconds = Duration.between(now, Instant.parse(expiry)).seconds
    if (seconds <= 0) {
        if (german) "Abgelaufen · Aktualisierung folgt" else "Expired · Update pending"
    } else {
        "${seconds / 3600}h ${(seconds % 3600) / 60}m ${seconds % 60}s ${if (german) "verbleibend" else "remaining"}"
    }
}.getOrDefault(if (german) "Zeitangabe nicht verfügbar" else "Time unavailable")

private fun LiveEntry.isUrgent(now: Instant): Boolean {
    val expiryInstant = expiry?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
    val minutes = Duration.between(now, expiryInstant).toMinutes()
    return minutes in 0..59
}

private fun LiveEntry.hasRewardSignal(): Boolean {
    val combined = "$title\n$detail"
    return listOf("reward", "belohnung", "credits", "ducats", "dukaten", "platinum", "blueprint", "bp", "forma", "reactor", "catalyst")
        .any { combined.contains(it, ignoreCase = true) }
}

private fun LiveEntry.isRelevantForPlayer(): Boolean {
    val combined = "$title\n$detail"
    return listOf("prime", "relic", "relikt", "dukat", "ducat", "platinum", "forma", "reactor", "catalyst", "archon", "riven", "eidolon", "nightwave")
        .any { combined.contains(it, ignoreCase = true) }
}

private fun saveLiveCache(
    context: android.content.Context,
    platform: String,
    language: String,
    section: LiveSection,
    entries: List<LiveEntry>
) {
    val encoded = entries.take(80).joinToString("\u001E") { entry ->
        listOf(entry.title, entry.detail, entry.expiry.orEmpty(), entry.source.orEmpty())
            .joinToString("\u001F") { it.replace("\u001E", " ").replace("\u001F", " ") }
    }
    context.getSharedPreferences("live_cache", android.content.Context.MODE_PRIVATE)
        .edit()
        .putString("$platform:$language:${section.name}", encoded)
        .apply()
}

private fun loadLiveCache(
    context: android.content.Context,
    platform: String,
    language: String,
    section: LiveSection
): List<LiveEntry> {
    val encoded = context.getSharedPreferences("live_cache", android.content.Context.MODE_PRIVATE)
        .getString("$platform:$language:${section.name}", null)
        .orEmpty()
    if (encoded.isBlank()) return emptyList()
    return encoded.split("\u001E").mapNotNull { row ->
        val parts = row.split("\u001F", limit = 4)
        if (parts.size >= 2) {
            LiveEntry(
                title = parts[0],
                detail = parts[1],
                expiry = parts.getOrNull(2)?.ifBlank { null },
                source = parts.getOrNull(3)?.ifBlank { null }
            )
        } else {
            null
        }
    }
}

