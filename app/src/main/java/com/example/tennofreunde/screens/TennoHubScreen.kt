package com.example.tennofreunde.screens

import android.content.Context
import android.content.SharedPreferences
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.data.FoundryTimer
import com.example.tennofreunde.data.FoundryTimerStore
import com.example.tennofreunde.data.foundryRemainingLabel
import com.example.tennofreunde.data.RecurringTask
import com.example.tennofreunde.data.TaskCadence
import com.example.tennofreunde.data.activeTaskIds
import com.example.tennofreunde.data.defaultRecurringTasks
import com.example.tennofreunde.data.encodeTaskCompletion
import com.example.tennofreunde.data.nextProfileCopyName
import com.example.tennofreunde.data.RelicInventoryStore
import com.example.tennofreunde.data.parseRelicInventory
import com.example.tennofreunde.data.updateRelicCount
import com.example.tennofreunde.data.PrimeTradeInventoryStore
import com.example.tennofreunde.data.TradeRecommendation
import com.example.tennofreunde.data.addPrimeTradeEntry
import com.example.tennofreunde.data.tradeRecommendation
import com.example.tennofreunde.data.updatePrimeTradeQuantity
import com.example.tennofreunde.data.BuildArchiveStore
import com.example.tennofreunde.data.SavedBuild
import com.example.tennofreunde.data.buildCompleteness
import com.example.tennofreunde.data.buildShareText
import com.example.tennofreunde.data.duplicateBuild
import com.example.tennofreunde.data.upsertBuild
import com.example.tennofreunde.data.StandingGoal
import com.example.tennofreunde.data.StandingGoalStore
import com.example.tennofreunde.data.applyStandingDay
import com.example.tennofreunde.data.standingDaysRemaining
import com.example.tennofreunde.data.upsertStandingGoal
import com.example.tennofreunde.data.ProgressionGoal
import com.example.tennofreunde.data.ProgressionGoalStore
import com.example.tennofreunde.data.adjustProgressionGoal
import com.example.tennofreunde.data.progressionCategoryProgress
import com.example.tennofreunde.data.upsertProgressionGoal
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.system.TennoSystem
import com.example.tennofreunde.system.TennoWidgetProvider
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TennoHubScreen(
    items: List<WarframeItem>,
    fissuresData: List<FissureResponse>,
    language: AppLanguage,
    activeProfile: String,
    onProfileChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE) }
    val dataPrefs = remember { context.getSharedPreferences("tenno_data", Context.MODE_PRIVATE) }
    val german = language == AppLanguage.GERMAN

    var query by remember { mutableStateOf("") }
    var profiles by remember {
        mutableStateOf(prefs.getStringSet("profiles", setOf("Tenno"))?.toList().orEmpty())
    }
    var newProfile by remember { mutableStateOf("") }
    var favorites by remember {
        mutableStateOf(prefs.getStringSet("favorites", emptySet()) ?: emptySet())
    }
    var priorityMap by remember {
        mutableStateOf(loadPriorityMap(prefs))
    }
    var showOnboarding by remember { mutableStateOf(prefs.getBoolean("show_hub_onboarding", true)) }
    var selectedDetail by remember { mutableStateOf<WarframeItem?>(null) }
    var profilePendingDelete by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf("planner") }
    val tasks = remember { defaultRecurringTasks() }
    var checkedTasks by remember(activeProfile) {
        mutableStateOf(loadRecurringTaskIds(prefs, activeProfile, tasks))
    }
    var baroAlert by remember { mutableStateOf(prefs.getBoolean("alert_baro", true)) }
    var eidolonAlert by remember { mutableStateOf(prefs.getBoolean("alert_eidolon", false)) }
    var fissureAlert by remember { mutableStateOf(prefs.getBoolean("alert_fissure", false)) }
    var widgetSummary by remember { mutableStateOf(prefs.getBoolean("widget_summary", true)) }
    var relicInventory by remember { mutableStateOf(prefs.getString("relic_inventory", "") ?: "") }
    var relicEntries by remember { mutableStateOf(RelicInventoryStore.load(context)) }
    var newRelicName by remember { mutableStateOf("") }
    var ducatParts by remember { mutableStateOf(prefs.getString("ducat_parts", "") ?: "") }
    var platinumNotes by remember { mutableStateOf(prefs.getString("platinum_notes", "") ?: "") }
    var primeTrades by remember { mutableStateOf(PrimeTradeInventoryStore.load(context)) }
    var newTradeName by remember { mutableStateOf("") }
    var newTradeQuantity by remember { mutableStateOf("1") }
    var newTradeDucats by remember { mutableStateOf("45") }
    var newTradePlatinum by remember { mutableStateOf("0") }
    var tradingNotes by remember { mutableStateOf(prefs.getString("trading_notes", "") ?: "") }
    var loadoutNotes by remember { mutableStateOf(prefs.getString("loadout_notes", "") ?: "") }
    var modNotes by remember { mutableStateOf(prefs.getString("mod_notes", "") ?: "") }
    var syndicateNotes by remember { mutableStateOf(prefs.getString("syndicate_notes", "") ?: "") }
    var standingNotes by remember { mutableStateOf(prefs.getString("standing_notes", "") ?: "") }
    var standingGoals by remember { mutableStateOf(StandingGoalStore.load(context)) }
    var newStandingName by remember { mutableStateOf("") }
    var newStandingCurrent by remember { mutableStateOf("0") }
    var newStandingTarget by remember { mutableStateOf("") }
    var newStandingDaily by remember { mutableStateOf("") }
    var foundryNotes by remember { mutableStateOf(prefs.getString("foundry_notes", "") ?: "") }
    var foundryTimers by remember { mutableStateOf(FoundryTimerStore.load(context)) }
    var newFoundryItem by remember { mutableStateOf("") }
    var foundryDurationHours by remember { mutableLongStateOf(24L) }
    var foundryClock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var tagNotes by remember { mutableStateOf(prefs.getString("tag_notes", "") ?: "") }
    var refinementNotes by remember { mutableStateOf(prefs.getString("refinement_notes", "") ?: "") }
    var bestMissionNotes by remember { mutableStateOf(prefs.getString("best_mission_notes", "") ?: "") }
    var primeVaultNotes by remember { mutableStateOf(prefs.getString("prime_vault_notes", "") ?: "") }
    var primeWishlistNotes by remember { mutableStateOf(prefs.getString("prime_wishlist_notes", "") ?: "") }
    var resourceNotes by remember { mutableStateOf(prefs.getString("resource_notes", "") ?: "") }
    var bossdropNotes by remember { mutableStateOf(prefs.getString("bossdrop_notes", "") ?: "") }
    var nightwaveNotes by remember { mutableStateOf(prefs.getString("nightwave_notes", "") ?: "") }
    var archonNotes by remember { mutableStateOf(prefs.getString("archon_notes", "") ?: "") }
    var kuvaTenetNotes by remember { mutableStateOf(prefs.getString("kuva_tenet_notes", "") ?: "") }
    var incarnonNotes by remember { mutableStateOf(prefs.getString("incarnon_notes", "") ?: "") }
    var steelPathNotes by remember { mutableStateOf(prefs.getString("steel_path_notes", "") ?: "") }
    var arbitrationNotes by remember { mutableStateOf(prefs.getString("arbitration_notes", "") ?: "") }
    var duviriNotes by remember { mutableStateOf(prefs.getString("duviri_notes", "") ?: "") }
    var helminthNotes by remember { mutableStateOf(prefs.getString("helminth_notes", "") ?: "") }
    var railjackNotes by remember { mutableStateOf(prefs.getString("railjack_notes", "") ?: "") }
    var necramechNotes by remember { mutableStateOf(prefs.getString("necramech_notes", "") ?: "") }
    var companionNotes by remember { mutableStateOf(prefs.getString("companion_notes", "") ?: "") }
    var focusNotes by remember { mutableStateOf(prefs.getString("focus_notes", "") ?: "") }
    var marketPriceNotes by remember { mutableStateOf(prefs.getString("market_price_notes", "") ?: "") }
    var tradingWishlistNotes by remember { mutableStateOf(prefs.getString("trading_wishlist_notes", "") ?: "") }
    var buyerSellerNotes by remember { mutableStateOf(prefs.getString("buyer_seller_notes", "") ?: "") }
    var priceHistoryNotes by remember { mutableStateOf(prefs.getString("price_history_notes", "") ?: "") }
    var progressionNotes by remember { mutableStateOf(prefs.getString("progression_notes", "") ?: "") }
    var progressionGoals by remember(activeProfile) { mutableStateOf(ProgressionGoalStore.load(context, activeProfile)) }
    var newProgressionTitle by remember { mutableStateOf("") }
    var newProgressionCategory by remember { mutableStateOf("quests") }
    var newProgressionCurrent by remember { mutableStateOf("0") }
    var newProgressionTarget by remember { mutableStateOf("") }
    var levelFormaNotes by remember { mutableStateOf(prefs.getString("level_forma_notes", "") ?: "") }
    var adapterNotes by remember { mutableStateOf(prefs.getString("adapter_notes", "") ?: "") }
    var questNotes by remember { mutableStateOf(prefs.getString("quest_notes", "") ?: "") }
    var intrinsicsNotes by remember { mutableStateOf(prefs.getString("intrinsics_notes", "") ?: "") }
    var buildTemplateNotes by remember { mutableStateOf(prefs.getString("build_template_notes", "") ?: "") }
    var arcaneNotes by remember { mutableStateOf(prefs.getString("arcane_notes", "") ?: "") }
    var buildExchangeNotes by remember { mutableStateOf(prefs.getString("build_exchange_notes", "") ?: "") }
    var savedBuilds by remember { mutableStateOf(BuildArchiveStore.load(context)) }
    var editingBuildId by remember { mutableStateOf<String?>(null) }
    var buildName by remember { mutableStateOf("") }
    var buildTarget by remember { mutableStateOf("") }
    var buildPurpose by remember { mutableStateOf("steel_path") }
    var buildMods by remember { mutableStateOf("") }
    var buildArcanes by remember { mutableStateOf("") }
    var buildHelminth by remember { mutableStateOf("") }
    var buildFocus by remember { mutableStateOf("") }
    var qualityNotes by remember { mutableStateOf(prefs.getString("quality_notes", "") ?: "") }
    var rememberLastPage by remember { mutableStateOf(prefs.getBoolean("remember_last_page", true)) }
    var backupReminder by remember { mutableStateOf(prefs.getBoolean("backup_reminder", true)) }
    var pinFavorites by remember { mutableStateOf(prefs.getBoolean("pin_favorites", true)) }
    var roadmapDone by remember { mutableStateOf(prefs.getStringSet("roadmap_done_41_140", emptySet()) ?: emptySet()) }
    var roadmapNotes by remember { mutableStateOf(prefs.getString("roadmap_notes_41_140", "") ?: "") }

    LaunchedEffect(activeProfile) {
        while (true) {
            foundryClock = System.currentTimeMillis()
            val active = loadRecurringTaskIds(prefs, activeProfile, tasks)
            if (active != checkedTasks) checkedTasks = active
            delay(30_000L)
        }
    }

    fun clearBuildEditor() {
        editingBuildId = null
        buildName = ""
        buildTarget = ""
        buildPurpose = "steel_path"
        buildMods = ""
        buildArcanes = ""
        buildHelminth = ""
        buildFocus = ""
    }

    fun editBuild(build: SavedBuild) {
        editingBuildId = build.id
        buildName = build.name
        buildTarget = build.target
        buildPurpose = build.purpose
        buildMods = build.mods
        buildArcanes = build.arcanes
        buildHelminth = build.helminth
        buildFocus = build.focusSchool
    }

    val totalComponents = items.sumOf { it.components.size }.coerceAtLeast(1)
    val ownedComponents = items.sumOf { item -> item.components.count { it.checked } }
    val progress by animateFloatAsState(
        targetValue = ownedComponents.toFloat() / totalComponents,
        label = "arsenalProgress"
    )
    val unfinishedItems = remember(items, ownedComponents) {
        items.filter { item -> item.components.any { !it.checked } }
    }
    val almostDoneItems = remember(items, ownedComponents) {
        items
            .filter { it.components.size > 1 }
            .map { item -> item to item.components.count { it.checked } }
            .filter { (item, checked) -> checked > 0 && checked < item.components.size }
            .sortedWith(
                compareByDescending<Pair<WarframeItem, Int>> { (item, checked) ->
                    checked.toFloat() / item.components.size
                }.thenBy { it.first.name }
            )
            .take(6)
    }
    val allRecommendations = remember(items, fissuresData, favorites, priorityMap, ownedComponents, german) {
        buildFarmRecommendations(items, fissuresData, favorites, priorityMap, german)
    }
    val recommendations = allRecommendations.take(12)
    val activeMatches = recommendations.count { it.activeFissure != null }
    val urgentItems = priorityMap.count { (_, priority) -> priority == WatchPriority.Now.key }
    val farmDay = LocalDate.now().toString()
    val dailyPlanPreferenceKey = "daily_farm_plan_${activeProfile}_$farmDay"
    val dailyDonePreferenceKey = "daily_farm_done_${activeProfile}_$farmDay"
    var dailyFarmKeys by remember(activeProfile, farmDay) {
        mutableStateOf(prefs.getStringSet(dailyPlanPreferenceKey, emptySet()).orEmpty())
    }
    var sessionDone by remember(activeProfile, farmDay) {
        mutableStateOf(prefs.getStringSet(dailyDonePreferenceKey, emptySet()).orEmpty())
    }
    LaunchedEffect(activeProfile, farmDay, allRecommendations) {
        if (!prefs.contains(dailyPlanPreferenceKey) && allRecommendations.isNotEmpty()) {
            dailyFarmKeys = allRecommendations.take(5).mapTo(mutableSetOf(), ::sessionKey)
            prefs.edit().putStringSet(dailyPlanPreferenceKey, dailyFarmKeys).apply()
        } else if (allRecommendations.isNotEmpty()) {
            val availableKeys = allRecommendations.mapTo(mutableSetOf(), ::sessionKey)
            val cleanedPlan = dailyFarmKeys intersect availableKeys
            if (cleanedPlan != dailyFarmKeys) {
                dailyFarmKeys = cleanedPlan
                sessionDone = sessionDone intersect cleanedPlan
                prefs.edit()
                    .putStringSet(dailyPlanPreferenceKey, dailyFarmKeys)
                    .putStringSet(dailyDonePreferenceKey, sessionDone)
                    .apply()
            }
        }
    }

    val searchResults = remember(query, items, ownedComponents) {
        if (query.isBlank()) {
            emptyList()
        } else {
            items.filter { item ->
                item.name.contains(query, true) ||
                    item.components.any { component -> component.name.contains(query, true) }
            }.take(30)
        }
    }
    val localSearchResults = remember(
        query,
        german,
        relicInventory,
        ducatParts,
        platinumNotes,
        tradingNotes,
        loadoutNotes,
        modNotes,
        syndicateNotes,
        standingNotes,
        foundryNotes,
        tagNotes,
        refinementNotes,
        bestMissionNotes,
        primeVaultNotes,
        primeWishlistNotes,
        resourceNotes,
        bossdropNotes,
        nightwaveNotes,
        archonNotes,
        kuvaTenetNotes,
        incarnonNotes,
        steelPathNotes,
        arbitrationNotes,
        duviriNotes,
        helminthNotes,
        railjackNotes,
        necramechNotes,
        companionNotes,
        focusNotes,
        marketPriceNotes,
        tradingWishlistNotes,
        buyerSellerNotes,
        priceHistoryNotes,
        progressionNotes,
        levelFormaNotes,
        adapterNotes,
        questNotes,
        intrinsicsNotes,
        buildTemplateNotes,
        arcaneNotes,
        buildExchangeNotes,
        qualityNotes,
        roadmapNotes
    ) {
        if (query.isBlank()) {
            emptyList()
        } else {
            listOf(
                (if (german) "Relikt-Inventar" else "Relic inventory") to relicInventory,
                (if (german) "Ducaten" else "Ducats") to ducatParts,
                (if (german) "Platinum" else "Platinum") to platinumNotes,
                (if (german) "Trading" else "Trading") to tradingNotes,
                (if (german) "Builds" else "Builds") to loadoutNotes,
                (if (german) "Mods" else "Mods") to modNotes,
                (if (german) "Syndikate" else "Syndicates") to syndicateNotes,
                (if (german) "Standing" else "Standing") to standingNotes,
                (if (german) "Foundry" else "Foundry") to foundryNotes,
                (if (german) "Tags" else "Tags") to tagNotes,
                (if (german) "Raffinierung" else "Refinement") to refinementNotes,
                (if (german) "Beste Mission" else "Best mission") to bestMissionNotes,
                (if (german) "Prime Vault" else "Prime Vault") to primeVaultNotes,
                (if (german) "Prime-Wunschliste" else "Prime wishlist") to primeWishlistNotes,
                (if (german) "Ressourcen" else "Resources") to resourceNotes,
                (if (german) "Bossdrops" else "Boss drops") to bossdropNotes,
                (if (german) "Nightwave" else "Nightwave") to nightwaveNotes,
                (if (german) "Archon-Shards" else "Archon shards") to archonNotes,
                (if (german) "Kuva/Tenet" else "Kuva/Tenet") to kuvaTenetNotes,
                (if (german) "Incarnon" else "Incarnon") to incarnonNotes,
                (if (german) "Steel Path" else "Steel Path") to steelPathNotes,
                (if (german) "Arbitrations" else "Arbitrations") to arbitrationNotes,
                (if (german) "Duviri" else "Duviri") to duviriNotes,
                (if (german) "Helminth" else "Helminth") to helminthNotes,
                (if (german) "Railjack" else "Railjack") to railjackNotes,
                (if (german) "Necramech" else "Necramech") to necramechNotes,
                (if (german) "Companions" else "Companions") to companionNotes,
                (if (german) "Focus" else "Focus") to focusNotes,
                (if (german) "Marktpreise" else "Market prices") to marketPriceNotes,
                (if (german) "Trading-Wunschliste" else "Trading wishlist") to tradingWishlistNotes,
                (if (german) "Käufer und Verkäufer" else "Buyers and sellers") to buyerSellerNotes,
                (if (german) "Preisverlauf" else "Price history") to priceHistoryNotes,
                (if (german) "Progression" else "Progression") to progressionNotes,
                (if (german) "Level und Forma" else "Level and forma") to levelFormaNotes,
                (if (german) "Adapter" else "Adapters") to adapterNotes,
                (if (german) "Quests und Sternenkarte" else "Quests and star chart") to questNotes,
                (if (german) "Intrinsics" else "Intrinsics") to intrinsicsNotes,
                (if (german) "Build-Vorlagen" else "Build templates") to buildTemplateNotes,
                (if (german) "Arkanes" else "Arcanes") to arcaneNotes,
                (if (german) "Build-Import/Export" else "Build import/export") to buildExchangeNotes,
                (if (german) "Bedienung" else "Usability") to qualityNotes,
                (if (german) "Roadmap 41-140" else "Roadmap 41-140") to roadmapNotes
            ).filter { (_, value) -> value.contains(query, ignoreCase = true) }
        }
    }
    val backupSummary = remember(items, favorites, priorityMap, activeProfile, ownedComponents) {
        BackupSummary(
            profile = activeProfile,
            itemCount = items.size,
            componentCount = totalComponents,
            ownedComponentCount = ownedComponents,
            favoriteCount = favorites.size,
            priorityCount = priorityMap.size
        )
    }

    selectedDetail?.let { item ->
        ItemDetailDialog(
            item = item,
            fissuresData = fissuresData,
            german = german,
            onDismiss = { selectedDetail = null }
        )
    }

    profilePendingDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profilePendingDelete = null },
            title = { Text(if (german) "Profil löschen?" else "Delete profile?") },
            text = {
                Text(
                    if (german) "Das Profil „$profile“ und sein lokaler Fortschritt werden gelöscht. Andere Profile bleiben erhalten."
                    else "Profile “$profile” and its local progress will be deleted. Other profiles remain available."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val remaining = profiles.filterNot { it == profile }.ifEmpty { listOf("Tenno") }
                        profiles = remaining
                        prefs.edit()
                            .putStringSet("profiles", remaining.toSet())
                            .remove("tasks_v2_$profile")
                            .remove("tasks_$profile")
                            .remove("session_done_$profile")
                            .remove("progression_goals_v1_$profile")
                            .also { editor ->
                                prefs.all.keys
                                    .filter { it.startsWith("daily_farm_plan_${profile}_") || it.startsWith("daily_farm_done_${profile}_") }
                                    .forEach(editor::remove)
                            }
                            .apply()
                        dataPrefs.edit().remove("local_progress_$profile").apply()
                        if (activeProfile == profile) {
                            onProfileChanged(remaining.first())
                            checkedTasks = loadRecurringTaskIds(prefs, remaining.first(), tasks)
                        }
                        profilePendingDelete = null
                    }
                ) { Text(if (german) "Profil löschen" else "Delete profile") }
            },
            dismissButton = {
                TextButton(onClick = { profilePendingDelete = null }) {
                    Text(if (german) "Abbrechen" else "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HudCard {
            Text(
                text = if (german) "TENNO-ZENTRALE" else "TENNO HUB",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (german) "Dein Farm-Assistent" else "Your farming assistant",
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "$activeProfile · $ownedComponents / $totalComponents ${if (german) "Komponenten" else "components"}",
                color = AppColors.TextSecondary
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = AppColors.EnergyCyan,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = "${(progress * 100).toInt()} %",
                color = AppColors.EnergyCyan,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (showOnboarding) {
            HudCard {
                Text(
                    text = if (german) "ERSTER START" else "FIRST START",
                    color = AppColors.OrokinGold,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (german) {
                        "Wähle oben dein Profil, markiere wichtige Items als Favorit und nutze den Scanner für schnelle Fortschritte."
                    } else {
                        "Choose your profile, mark important items as favorites, and use the scanner for fast progress updates."
                    },
                    color = AppColors.TextSecondary
                )
                AssistChip(
                    onClick = {
                        showOnboarding = false
                        prefs.edit().putBoolean("show_hub_onboarding", false).apply()
                    },
                    label = { Text(if (german) "Verstanden" else "Got it") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = AppColors.EnergyCyan.copy(alpha = 0.18f),
                        labelColor = AppColors.EnergyCyan
                    )
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Route,
                title = if (german) "Farm-Plan" else "Farm plan",
                value = recommendations.size.toString(),
                subtitle = if (german) "nächste Teile" else "next parts"
            )
            MetricCard(
                icon = Icons.Default.Whatshot,
                title = if (german) "Jetzt aktiv" else "Active now",
                value = activeMatches.toString(),
                subtitle = if (german) "passende Risse" else "matching fissures"
            )
            MetricCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = if (german) "Jetzt" else "Now",
                value = urgentItems.toString(),
                subtitle = if (german) "Top-Priorität" else "top priority"
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(if (german) "Globale Suche" else "Global search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.EnergyCyan,
                unfocusedBorderColor = AppColors.CardBorder,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.EnergyCyan
            )
        )

        if (query.isNotBlank()) {
            HudCard {
                Text(
                    text = "${searchResults.size} ${if (german) "Treffer" else "results"}",
                    color = AppColors.OrokinGold
                )
                searchResults.forEach { item ->
                    FavoriteItemRow(item, item.name in favorites) {
                        val nextFavorites: Set<String>
                        val nextPriorities: Map<String, String>
                        if (item.name in favorites) {
                            nextFavorites = favorites - item.name
                            nextPriorities = priorityMap - item.name
                        } else {
                            nextFavorites = favorites + item.name
                            nextPriorities = priorityMap + (item.name to WatchPriority.Now.key)
                        }
                        favorites = nextFavorites
                        priorityMap = nextPriorities
                        prefs.edit().putStringSet("favorites", nextFavorites).apply()
                        savePriorityMap(prefs, nextPriorities)
                    }
                }
                localSearchResults.forEach { (title, value) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), AppShapes.Small)
                            .padding(10.dp)
                    ) {
                        Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(value.lineSequence().firstOrNull { it.contains(query, ignoreCase = true) } ?: value.take(90), color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (searchResults.isEmpty()) {
                    Text(
                        text = if (localSearchResults.isEmpty()) {
                            if (german) "Nichts gefunden" else "Nothing found"
                        } else {
                            if (german) "Keine Items gefunden, aber lokale Notizen passen." else "No items found, but local notes match."
                        },
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        HubSection(
            title = if (german) "Intelligenter Farm-Plan" else "Smart farm plan",
            icon = Icons.Default.Route,
            expanded = expanded == "planner",
            onClick = { expanded = if (expanded == "planner") "" else "planner" }
        ) {
            Text(
                text = if (favorites.isEmpty()) {
                    if (german) "Zeigt die sinnvollsten fehlenden Teile aus deinem ganzen Arsenal. Favoriten grenzen den Plan ein."
                    else "Shows the best missing parts across your arsenal. Favorites narrow the plan."
                } else if (german) {
                    "Sortiert nach deiner Priorität, aktiven Void-Rissen und fast fertigen Teilen."
                } else {
                    "Sorts by your priority, active Void Fissures, and almost finished gear."
                },
                color = AppColors.TextSecondary
            )

            if (recommendations.isEmpty()) {
                Text(
                    text = if (german) "Alles erledigt oder keine Komponenten geladen." else "Everything done or no components loaded.",
                    color = AppColors.TextSecondary
                )
            } else {
                recommendations.forEach { recommendation ->
                    FarmRecommendationRow(
                        recommendation = recommendation,
                        german = german,
                        completed = sessionKey(recommendation) in sessionDone,
                        inDailyPlan = sessionKey(recommendation) in dailyFarmKeys,
                        onCompletedChanged = { checked ->
                            val key = sessionKey(recommendation)
                            val nextDone = if (checked) sessionDone + key else sessionDone - key
                            sessionDone = nextDone
                            prefs.edit().putStringSet(dailyDonePreferenceKey, nextDone).apply()
                        },
                        onDailyPlanChanged = { included ->
                            val key = sessionKey(recommendation)
                            dailyFarmKeys = if (included) dailyFarmKeys + key else dailyFarmKeys - key
                            if (!included) sessionDone = sessionDone - key
                            prefs.edit()
                                .putStringSet(dailyPlanPreferenceKey, dailyFarmKeys)
                                .putStringSet(dailyDonePreferenceKey, sessionDone)
                                .apply()
                        },
                        onOpenDetails = { selectedDetail = recommendation.item }
                    )
                }
            }
        }

        HubSection(
            title = if (german) "Heute farmen" else "Farm today",
            icon = Icons.Default.Checklist,
            expanded = expanded == "session",
            onClick = { expanded = if (expanded == "session") "" else "session" }
        ) {
            val sessionItems = allRecommendations.filter { sessionKey(it) in dailyFarmKeys }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${sessionDone.count { it in dailyFarmKeys }}/${dailyFarmKeys.size} ${if (german) "erledigt" else "done"}",
                    color = AppColors.EnergyCyan,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = {
                    dailyFarmKeys = allRecommendations.take(5).mapTo(mutableSetOf(), ::sessionKey)
                    sessionDone = emptySet()
                    prefs.edit()
                        .putStringSet(dailyPlanPreferenceKey, dailyFarmKeys)
                        .putStringSet(dailyDonePreferenceKey, emptySet())
                        .apply()
                }) {
                    Text(if (german) "Plan neu erstellen" else "Rebuild plan")
                }
            }
            if (sessionItems.isEmpty()) {
                Text(
                    text = if (german) "Keine offenen Ziele für diese Session." else "No open goals for this session.",
                    color = AppColors.TextSecondary
                )
            } else {
                Text(
                    text = if (german) "Kompakter Plan für die nächste Spielrunde." else "Compact plan for your next run.",
                    color = AppColors.TextSecondary
                )
                sessionItems.forEach { recommendation ->
                    val key = sessionKey(recommendation)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextDone = if (key in sessionDone) sessionDone - key else sessionDone + key
                                sessionDone = nextDone
                                prefs.edit().putStringSet(dailyDonePreferenceKey, nextDone).apply()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = key in sessionDone,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.EnergyCyan)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(recommendation.item.name, color = AppColors.TextPrimary)
                            Text(recommendation.component.name, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            dailyFarmKeys = dailyFarmKeys - key
                            sessionDone = sessionDone - key
                            prefs.edit()
                                .putStringSet(dailyPlanPreferenceKey, dailyFarmKeys)
                                .putStringSet(dailyDonePreferenceKey, sessionDone)
                                .apply()
                        }) {
                            Icon(Icons.Default.DeleteOutline, if (german) "Aus Tagesplan entfernen" else "Remove from daily plan", tint = AppColors.TextSecondary)
                        }
                    }
                }
            }
        }

        HubSection(
            title = if (german) "Relikt- & Prime-Farm" else "Relic & prime farm",
            icon = Icons.Default.Whatshot,
            expanded = expanded == "primeFarm",
            onClick = { expanded = if (expanded == "primeFarm") "" else "primeFarm" }
        ) {
            BackupSummaryRow(
                if (german) "Fast vollständige Sets" else "Almost complete sets",
                almostDoneItems.size.toString()
            )
            Text(
                text = if (german) "Raffinierung: Bronze/common meist intakt, Silber/uncommon gezielt verbessert, Gold/rare eher radiant."
                else "Refinement: bronze/common usually intact, silver/uncommon targeted refinement, gold/rare preferably radiant.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            HubTextField(
                value = refinementNotes,
                onValueChange = {
                    refinementNotes = it
                    prefs.edit().putString("refinement_notes", it).apply()
                },
                label = if (german) "Relikt-Raffinierungsplan" else "Relic refinement plan"
            )
            HubTextField(
                value = bestMissionNotes,
                onValueChange = {
                    bestMissionNotes = it
                    prefs.edit().putString("best_mission_notes", it).apply()
                },
                label = if (german) "Beste Mission jetzt" else "Best mission now"
            )
            HubTextField(
                value = primeVaultNotes,
                onValueChange = {
                    primeVaultNotes = it
                    prefs.edit().putString("prime_vault_notes", it).apply()
                },
                label = if (german) "Prime-Vault-Tracker" else "Prime Vault tracker"
            )
            HubTextField(
                value = primeWishlistNotes,
                onValueChange = {
                    primeWishlistNotes = it
                    prefs.edit().putString("prime_wishlist_notes", it).apply()
                },
                label = if (german) "Wunschliste kommende Prime-Teile" else "Upcoming Prime wishlist"
            )
        }

        HubSection(
            title = if (german) "Farm-Aufgaben" else "Farm tasks",
            icon = Icons.Default.Route,
            expanded = expanded == "farmTasks",
            onClick = { expanded = if (expanded == "farmTasks") "" else "farmTasks" }
        ) {
            HubTextField(
                value = resourceNotes,
                onValueChange = {
                    resourceNotes = it
                    prefs.edit().putString("resource_notes", it).apply()
                },
                label = if (german) "Ressourcen-Farmplaner" else "Resource farm planner"
            )
            HubTextField(
                value = bossdropNotes,
                onValueChange = {
                    bossdropNotes = it
                    prefs.edit().putString("bossdrop_notes", it).apply()
                },
                label = if (german) "Bossdrop-Tracker" else "Boss drop tracker"
            )
            HubTextField(
                value = nightwaveNotes,
                onValueChange = {
                    nightwaveNotes = it
                    prefs.edit().putString("nightwave_notes", it).apply()
                },
                label = if (german) "Nightwave-Aufgaben" else "Nightwave tasks"
            )
            HubTextField(
                value = archonNotes,
                onValueChange = {
                    archonNotes = it
                    prefs.edit().putString("archon_notes", it).apply()
                },
                label = if (german) "Archon-Shard-Tracker" else "Archon shard tracker"
            )
        }

        HubSection(
            title = if (german) "Rotationen & Endgame" else "Rotations & endgame",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            expanded = expanded == "endgameRotations",
            onClick = { expanded = if (expanded == "endgameRotations") "" else "endgameRotations" }
        ) {
            HubTextField(
                value = kuvaTenetNotes,
                onValueChange = {
                    kuvaTenetNotes = it
                    prefs.edit().putString("kuva_tenet_notes", it).apply()
                },
                label = if (german) "Kuva-/Tenet-Waffen" else "Kuva/Tenet weapons"
            )
            HubTextField(
                value = incarnonNotes,
                onValueChange = {
                    incarnonNotes = it
                    prefs.edit().putString("incarnon_notes", it).apply()
                },
                label = if (german) "Incarnon-Rotation" else "Incarnon rotation"
            )
            HubTextField(
                value = steelPathNotes,
                onValueChange = {
                    steelPathNotes = it
                    prefs.edit().putString("steel_path_notes", it).apply()
                },
                label = if (german) "Steel-Path-Daily" else "Steel Path daily"
            )
            HubTextField(
                value = arbitrationNotes,
                onValueChange = {
                    arbitrationNotes = it
                    prefs.edit().putString("arbitration_notes", it).apply()
                },
                label = if (german) "Arbitrations" else "Arbitrations"
            )
            HubTextField(
                value = duviriNotes,
                onValueChange = {
                    duviriNotes = it
                    prefs.edit().putString("duviri_notes", it).apply()
                },
                label = if (german) "Duviri-Spirale / Circuit" else "Duviri spiral / Circuit"
            )
        }

        HubSection(
            title = if (german) "Systeme & Begleiter" else "Systems & companions",
            icon = Icons.Default.Groups,
            expanded = expanded == "systemsCompanions",
            onClick = { expanded = if (expanded == "systemsCompanions") "" else "systemsCompanions" }
        ) {
            HubTextField(
                value = helminthNotes,
                onValueChange = {
                    helminthNotes = it
                    prefs.edit().putString("helminth_notes", it).apply()
                },
                label = if (german) "Helminth-Futterplaner" else "Helminth feeding planner"
            )
            HubTextField(
                value = railjackNotes,
                onValueChange = {
                    railjackNotes = it
                    prefs.edit().putString("railjack_notes", it).apply()
                },
                label = if (german) "Railjack-Komponenten" else "Railjack components"
            )
            HubTextField(
                value = necramechNotes,
                onValueChange = {
                    necramechNotes = it
                    prefs.edit().putString("necramech_notes", it).apply()
                },
                label = if (german) "Necramech-Teile" else "Necramech parts"
            )
            HubTextField(
                value = companionNotes,
                onValueChange = {
                    companionNotes = it
                    prefs.edit().putString("companion_notes", it).apply()
                },
                label = if (german) "Companion-/Beast-Tracker" else "Companion / beast tracker"
            )
            HubTextField(
                value = focusNotes,
                onValueChange = {
                    focusNotes = it
                    prefs.edit().putString("focus_notes", it).apply()
                },
                label = if (german) "Focus-Schulen-Fortschritt" else "Focus school progress"
            )
        }

        HubSection(
            title = if (german) "Fast fertig" else "Almost finished",
            icon = Icons.Default.LocalFireDepartment,
            expanded = expanded == "almost",
            onClick = { expanded = if (expanded == "almost") "" else "almost" }
        ) {
            if (almostDoneItems.isEmpty()) {
                Text(
                    text = if (german) "Noch keine angefangenen Gegenstände gefunden." else "No started items found yet.",
                    color = AppColors.TextSecondary
                )
            } else {
                almostDoneItems.forEach { (item, checked) ->
                    val missing = item.components.size - checked
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = item.name,
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (german) {
                                "Noch $missing Teil(e) fehlen · $checked/${item.components.size}"
                            } else {
                                "$missing part(s) missing · $checked/${item.components.size}"
                            },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        HubSection(
            title = if (german) "Favoriten & Beobachtung" else "Favorites & watchlist",
            icon = Icons.Default.Star,
            expanded = expanded == "favorites",
            onClick = { expanded = if (expanded == "favorites") "" else "favorites" }
        ) {
            if (favorites.isEmpty()) {
                Text(
                    text = if (german) "Noch keine Favoriten. Nutze die globale Suche." else "No favorites yet. Use global search.",
                    color = AppColors.TextSecondary
                )
            }
            favorites.sorted().forEach { name ->
                val item = items.firstOrNull { it.name == name }
                val priority = priorityFor(name, priorityMap)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FavoriteItemRow(item, true) {
                        val nextFavorites = favorites - name
                        val nextPriorities = priorityMap - name
                        favorites = nextFavorites
                        priorityMap = nextPriorities
                        prefs.edit().putStringSet("favorites", nextFavorites).apply()
                        savePriorityMap(prefs, nextPriorities)
                    }
                    if (item != null) {
                        PriorityControls(
                            selected = priority,
                            german = german,
                            onPrioritySelected = { selected ->
                                val nextPriorities = priorityMap + (name to selected.key)
                                priorityMap = nextPriorities
                                savePriorityMap(prefs, nextPriorities)
                            }
                        )
                    }
                }
            }
        }

        HubSection(
            title = if (german) "Tages- & Wochenaufgaben" else "Daily & weekly tasks",
            icon = Icons.Default.Checklist,
            expanded = expanded == "tasks",
            onClick = { expanded = if (expanded == "tasks") "" else "tasks" }
        ) {
            tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checkedTasks = if (task.id in checkedTasks) checkedTasks - task.id else checkedTasks + task.id
                            saveRecurringTaskIds(prefs, activeProfile, tasks, checkedTasks)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.id in checkedTasks,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = AppColors.EnergyCyan)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(task.name(german), color = AppColors.TextPrimary)
                        Text(
                            if (task.cadence == TaskCadence.DAILY) {
                                if (german) "Wird täglich zurückgesetzt" else "Resets daily"
                            } else {
                                if (german) "Wird montags zurückgesetzt" else "Resets on Monday"
                            },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (checkedTasks.isNotEmpty()) {
                TextButton(
                    onClick = {
                        checkedTasks = emptySet()
                        saveRecurringTaskIds(prefs, activeProfile, tasks, checkedTasks)
                    }
                ) {
                    Text(if (german) "Häkchen für dieses Profil zurücksetzen" else "Reset checks for this profile")
                }
            }
        }

        HubSection(
            title = if (german) "Profile" else "Profiles",
            icon = Icons.Default.Groups,
            expanded = expanded == "profiles",
            onClick = { expanded = if (expanded == "profiles") "" else "profiles" }
        ) {
            profiles.forEach { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onProfileChanged(profile)
                            checkedTasks = loadRecurringTaskIds(prefs, profile, tasks)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = profile == activeProfile,
                        onClick = null
                    )
                    Text(profile, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val copyName = nextProfileCopyName(profile, profiles, german)
                            val sourceProgress = dataPrefs.getString("local_progress_$profile", null)
                            if (sourceProgress != null) {
                                dataPrefs.edit().putString("local_progress_$copyName", sourceProgress).apply()
                            }
                            val sessionState = prefs.getStringSet("session_done_$profile", null)
                            val progressionState = prefs.getString("progression_goals_v1_$profile", null)
                            prefs.edit().apply {
                                if (sessionState != null) putStringSet("session_done_$copyName", sessionState)
                                if (progressionState != null) putString("progression_goals_v1_$copyName", progressionState)
                                prefs.all.forEach { (key, value) ->
                                    val planPrefix = "daily_farm_plan_${profile}_"
                                    val donePrefix = "daily_farm_done_${profile}_"
                                    when {
                                        key.startsWith(planPrefix) && value is Set<*> ->
                                            putStringSet("daily_farm_plan_${copyName}_${key.removePrefix(planPrefix)}", value.filterIsInstance<String>().toSet())
                                        key.startsWith(donePrefix) && value is Set<*> ->
                                            putStringSet("daily_farm_done_${copyName}_${key.removePrefix(donePrefix)}", value.filterIsInstance<String>().toSet())
                                    }
                                }
                            }.apply()
                            saveRecurringTaskIds(
                                prefs,
                                copyName,
                                tasks,
                                loadRecurringTaskIds(prefs, profile, tasks)
                            )
                            profiles = (profiles + copyName).distinct()
                            prefs.edit().putStringSet("profiles", profiles.toSet()).apply()
                            onProfileChanged(copyName)
                            checkedTasks = loadRecurringTaskIds(prefs, copyName, tasks)
                        }
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            if (german) "Profil mit Fortschritt kopieren" else "Copy profile with progress",
                            tint = AppColors.EnergyCyan
                        )
                    }
                    IconButton(
                        onClick = { profilePendingDelete = profile },
                        enabled = profiles.size > 1
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            if (german) "Profil löschen" else "Delete profile",
                            tint = if (profiles.size > 1) AppColors.TextSecondary else AppColors.TextSecondary.copy(alpha = 0.35f)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newProfile,
                    onValueChange = { newProfile = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (german) "Neues Profil" else "New profile") }
                )
                IconButton(
                    onClick = {
                        val name = newProfile.trim()
                        if (name.isNotEmpty() && name !in profiles) {
                            profiles = (profiles + name).distinct()
                            prefs.edit().putStringSet("profiles", profiles.toSet()).apply()
                            onProfileChanged(name)
                            newProfile = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = AppColors.EnergyCyan
                    )
                }
            }
        }

        HubSection(
            title = if (german) "Arsenal-Planer" else "Arsenal planner",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            expanded = expanded == "arsenalTools",
            onClick = { expanded = if (expanded == "arsenalTools") "" else "arsenalTools" }
        ) {
            val masteryItems = items.filter { masteryXpForItem(it) > 0 }
            val completedMasteryItems = masteryItems.filter { item -> item.components.isNotEmpty() && item.components.all { it.checked } }
            val masteryEstimate = completedMasteryItems.sumOf(::masteryXpForItem)
            BackupSummaryRow(if (german) "Mastery-Kandidaten" else "Mastery candidates", "${masteryItems.size - completedMasteryItems.size}")
            BackupSummaryRow(if (german) "Geschätzte MR-XP erledigt" else "Estimated MR XP done", masteryEstimate.toString())
            BackupSummaryRow(if (german) "Offene Sets fürs Trading" else "Open sets for trading", unfinishedItems.size.toString())
            Text(
                text = if (german) {
                    "Die Werte sind bewusst als Planhilfe gerechnet. Exakte Mastery hängt im Spiel von Item-Typ und Levelstand ab."
                } else {
                    "These values are planning estimates. Exact mastery depends on item type and in-game level state."
                },
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Sammlungsanalyse" else "Collection analysis",
            icon = Icons.Default.Search,
            expanded = expanded == "collectionAnalysis",
            onClick = { expanded = if (expanded == "collectionAnalysis") "" else "collectionAnalysis" }
        ) {
            val duplicateCount = items.groupBy { it.name.lowercase() }.count { (_, grouped) -> grouped.size > 1 }
            BackupSummaryRow(if (german) "Profile gespeichert" else "Saved profiles", profiles.size.toString())
            BackupSummaryRow(if (german) "Aktives Profil" else "Active profile", activeProfile)
            BackupSummaryRow(if (german) "Duplikate erkannt" else "Duplicates detected", duplicateCount.toString())
            HubTextField(
                value = tagNotes,
                onValueChange = {
                    tagNotes = it
                    prefs.edit().putString("tag_notes", it).apply()
                },
                label = if (german) "Tags für Items / Kategorien" else "Tags for items / categories"
            )
            Text(
                text = if (german) "Bei ausgeschalteter A–Z-Sortierung kannst du Sammlungskarten lange drücken und per Ziehen dauerhaft neu anordnen."
                else "With A–Z sorting disabled, long-press collection cards and drag them into a saved custom order.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Ducaten & Platinum" else "Ducats & Platinum",
            icon = Icons.Default.Whatshot,
            expanded = expanded == "marketTools",
            onClick = { expanded = if (expanded == "marketTools") "" else "marketTools" }
        ) {
            val ducatEstimate = estimateDucats(ducatParts)
            val structuredDucats = primeTrades.sumOf { it.sellableDucats }
            val structuredPlatinum = primeTrades.sumOf { it.sellablePlatinum }
            BackupSummaryRow(
                if (german) "Verkaufbare Ducaten" else "Sellable ducats",
                structuredDucats.toString()
            )
            BackupSummaryRow(
                if (german) "Möglicher Marktwert" else "Potential market value",
                "$structuredPlatinum Platinum"
            )
            OutlinedTextField(
                value = newTradeName,
                onValueChange = { newTradeName = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Prime-Teil oder Set" else "Prime part or set") },
                placeholder = { Text(if (german) "z. B. Wisp Prime Chassis" else "e.g. Wisp Prime Chassis") }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTradeQuantity,
                    onValueChange = { newTradeQuantity = it.filter(Char::isDigit).take(3) },
                    modifier = Modifier.width(92.dp),
                    singleLine = true,
                    label = { Text(if (german) "Menge" else "Qty") }
                )
                OutlinedTextField(
                    value = newTradeDucats,
                    onValueChange = { newTradeDucats = it.filter(Char::isDigit).take(4) },
                    modifier = Modifier.width(105.dp),
                    singleLine = true,
                    label = { Text("Ducaten") }
                )
                OutlinedTextField(
                    value = newTradePlatinum,
                    onValueChange = { newTradePlatinum = it.filter(Char::isDigit).take(5) },
                    modifier = Modifier.width(110.dp),
                    singleLine = true,
                    label = { Text("Platinum") }
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(15, 25, 45, 65, 100).forEach { value ->
                    AssistChip(
                        onClick = { newTradeDucats = value.toString() },
                        label = { Text(value.toString()) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (newTradeDucats == value.toString()) AppColors.EnergyCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                            labelColor = if (newTradeDucats == value.toString()) AppColors.EnergyCyan else AppColors.TextSecondary
                        )
                    )
                }
            }
            Button(
                onClick = {
                    primeTrades = addPrimeTradeEntry(
                        primeTrades,
                        newTradeName,
                        newTradeQuantity.toIntOrNull() ?: 1,
                        newTradeDucats.toIntOrNull() ?: 0,
                        newTradePlatinum.toIntOrNull() ?: 0
                    )
                    PrimeTradeInventoryStore.save(context, primeTrades)
                    newTradeName = ""
                    newTradeQuantity = "1"
                },
                enabled = newTradeName.isNotBlank(),
                shape = AppShapes.Small
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(if (german) "Prime-Teil eintragen" else "Add Prime part")
            }
            primeTrades.forEach { entry ->
                val recommendation = tradeRecommendation(entry)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Small,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.name, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${entry.quantity} × ${entry.ducatsEach} Ducaten · ${entry.platinumEach} Platinum",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                primeTrades = updatePrimeTradeQuantity(primeTrades, entry.id, -1)
                                PrimeTradeInventoryStore.save(context, primeTrades)
                            }) { Icon(Icons.Default.Remove, null, tint = AppColors.TextSecondary) }
                            Text(entry.quantity.toString(), color = AppColors.OrokinGold)
                            IconButton(onClick = {
                                primeTrades = updatePrimeTradeQuantity(primeTrades, entry.id, 1)
                                PrimeTradeInventoryStore.save(context, primeTrades)
                            }) { Icon(Icons.Default.Add, null, tint = AppColors.EnergyCyan) }
                            IconButton(onClick = {
                                primeTrades = primeTrades.filterNot { it.id == entry.id }
                                PrimeTradeInventoryStore.save(context, primeTrades)
                            }) { Icon(Icons.Default.DeleteOutline, null, tint = AppColors.TextSecondary) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = entry.keepOne,
                                onCheckedChange = { keep ->
                                    primeTrades = primeTrades.map { if (it.id == entry.id) it.copy(keepOne = keep) else it }
                                    PrimeTradeInventoryStore.save(context, primeTrades)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.EnergyCyan)
                            )
                            Text(
                                if (german) "Ein Exemplar behalten" else "Keep one copy",
                                color = AppColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                when (recommendation) {
                                    TradeRecommendation.KEEP -> if (german) "BEHALTEN" else "KEEP"
                                    TradeRecommendation.BARO -> "BARO"
                                    TradeRecommendation.MARKET -> if (german) "MARKT" else "MARKET"
                                },
                                color = when (recommendation) {
                                    TradeRecommendation.KEEP -> AppColors.TextSecondary
                                    TradeRecommendation.BARO -> AppColors.OrokinGold
                                    TradeRecommendation.MARKET -> AppColors.EnergyCyan
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = AppColors.CardBorder)
            BackupSummaryRow(if (german) "Alte Text-Schätzung" else "Old text estimate", ducatEstimate.toString())
            HubTextField(
                value = ducatParts,
                onValueChange = {
                    ducatParts = it
                    prefs.edit().putString("ducat_parts", it).apply()
                },
                label = if (german) "Alte Ducaten-Notizen" else "Old ducat notes"
            )
            HubTextField(
                value = platinumNotes,
                onValueChange = {
                    platinumNotes = it
                    prefs.edit().putString("platinum_notes", it).apply()
                },
                label = if (german) "Platinum-Preise / Markt-Notizen" else "Platinum prices / market notes"
            )
            Text(
                text = if (german) "Ducaten-Schätzung: seltene Begriffe wie rare/gold zählen höher, uncommon/silver mittel, Rest konservativ."
                else "Ducat estimate: rare/gold terms count higher, uncommon/silver medium, everything else conservative.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Trading & Relikt-Inventar" else "Trading & relic inventory",
            icon = Icons.Default.Star,
            expanded = expanded == "tradingRelics",
            onClick = { expanded = if (expanded == "tradingRelics") "" else "tradingRelics" }
        ) {
            Text(
                if (german) "Relikt-Mengen" else "Relic quantities",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newRelicName,
                    onValueChange = { newRelicName = it.take(30) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (german) "Relikt" else "Relic") },
                    placeholder = { Text("Axi A1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.EnergyCyan,
                        focusedLabelColor = AppColors.EnergyCyan
                    )
                )
                IconButton(
                    onClick = {
                        relicEntries = updateRelicCount(relicEntries, newRelicName, 1)
                        RelicInventoryStore.save(context, relicEntries)
                        newRelicName = ""
                    },
                    enabled = newRelicName.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, if (german) "Relikt hinzufügen" else "Add relic", tint = AppColors.EnergyCyan)
                }
            }
            if (relicEntries.isNotEmpty()) {
                BackupSummaryRow(
                    if (german) "Relikte insgesamt" else "Total relics",
                    relicEntries.sumOf { it.count }.toString()
                )
                relicEntries.forEach { relic ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(relic.name, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            relicEntries = updateRelicCount(relicEntries, relic.name, -1)
                            RelicInventoryStore.save(context, relicEntries)
                        }) {
                            Icon(Icons.Default.Remove, if (german) "Ein Relikt entfernen" else "Remove one relic", tint = AppColors.TextSecondary)
                        }
                        Text(relic.count.toString(), color = AppColors.OrokinGold, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            relicEntries = updateRelicCount(relicEntries, relic.name, 1)
                            RelicInventoryStore.save(context, relicEntries)
                        }) {
                            Icon(Icons.Default.Add, if (german) "Ein Relikt hinzufügen" else "Add one relic", tint = AppColors.EnergyCyan)
                        }
                        IconButton(onClick = {
                            relicEntries = relicEntries.filterNot { it.id == relic.id }
                            RelicInventoryStore.save(context, relicEntries)
                        }) {
                            Icon(Icons.Default.DeleteOutline, if (german) "Relikt löschen" else "Delete relic", tint = AppColors.TextSecondary)
                        }
                    }
                }
            }
            HubTextField(
                value = relicInventory,
                onValueChange = {
                    relicInventory = it
                    prefs.edit().putString("relic_inventory", it).apply()
                },
                label = if (german) "Alte Reliktzeilen / Notizen" else "Old relic lines / notes"
            )
            val importableRelics = parseRelicInventory(relicInventory)
            if (importableRelics.isNotEmpty()) {
                TextButton(
                    onClick = {
                        importableRelics.forEach { imported ->
                            relicEntries = updateRelicCount(relicEntries, imported.name, imported.count)
                        }
                        RelicInventoryStore.save(context, relicEntries)
                        relicInventory = ""
                        prefs.edit().putString("relic_inventory", "").apply()
                    }
                ) {
                    Text(
                        if (german) "${importableRelics.size} Reliktzeilen in Mengen-Tracker übernehmen"
                        else "Import ${importableRelics.size} relic lines into quantity tracker"
                    )
                }
            }
            HubTextField(
                value = tradingNotes,
                onValueChange = {
                    tradingNotes = it
                    prefs.edit().putString("trading_notes", it).apply()
                },
                label = if (german) "Trading-Checkliste" else "Trading checklist"
            )
            BackupSummaryRow(if (german) "Bekannte Relikttypen" else "Known relic types", relicEntries.size.toString())
            Text(
                text = if (german) "Drop-Chance-Hilfe: Bronze/common meist intakt, Silber/uncommon gezielt verbessert, Gold/rare eher radiant."
                else "Drop chance help: bronze/common usually intact, silver/uncommon targeted refinement, gold/rare preferably radiant.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Markt-Manager" else "Market manager",
            icon = Icons.Default.Whatshot,
            expanded = expanded == "marketManager",
            onClick = { expanded = if (expanded == "marketManager") "" else "marketManager" }
        ) {
            val ducatEstimate = primeTrades.sumOf { it.sellableDucats }.takeIf { primeTrades.isNotEmpty() }
                ?: estimateDucats(ducatParts)
            val platinumEstimate = primeTrades.sumOf { it.sellablePlatinum }.takeIf { primeTrades.isNotEmpty() }
                ?: estimatePlatinum(platinumNotes + "\n" + marketPriceNotes + "\n" + priceHistoryNotes)
            val completedSets = items.count { item -> item.components.isNotEmpty() && item.components.all { it.checked } }
            val duplicateCount = items.groupBy { it.name.lowercase() }.count { (_, grouped) -> grouped.size > 1 }
            BackupSummaryRow(if (german) "Verkaufbare Sets" else "Sellable sets", completedSets.toString())
            BackupSummaryRow(if (german) "Inventarwert grob" else "Rough inventory value", "$platinumEstimate Platinum")
            BackupSummaryRow(
                if (german) "Ducaten pro Platinum" else "Ducats per Platinum",
                if (platinumEstimate > 0) "%.1f".format(ducatEstimate.toFloat() / platinumEstimate) else "-"
            )
            BackupSummaryRow(if (german) "Trading-Duplikate" else "Trading duplicates", duplicateCount.toString())
            HubTextField(
                value = marketPriceNotes,
                onValueChange = {
                    marketPriceNotes = it
                    prefs.edit().putString("market_price_notes", it).apply()
                },
                label = if (german) "Prime-Set-Preise / Mindestpreise / Warframe.market-Links" else "Prime set prices / minimum prices / Warframe.market links"
            )
            HubTextField(
                value = tradingWishlistNotes,
                onValueChange = {
                    tradingWishlistNotes = it
                    prefs.edit().putString("trading_wishlist_notes", it).apply()
                },
                label = if (german) "Trading-Wunschliste" else "Trading wishlist"
            )
            HubTextField(
                value = buyerSellerNotes,
                onValueChange = {
                    buyerSellerNotes = it
                    prefs.edit().putString("buyer_seller_notes", it).apply()
                },
                label = if (german) "Käufer-/Verkäufer-Notizen" else "Buyer/seller notes"
            )
            HubTextField(
                value = priceHistoryNotes,
                onValueChange = {
                    priceHistoryNotes = it
                    prefs.edit().putString("price_history_notes", it).apply()
                },
                label = if (german) "Preisverlauf / behalten oder verkaufen" else "Price history / keep or sell"
            )
            Text(
                text = if (german) "Tipp: Schreibe Preise als Zahl mit p oder Platinum. Die App bildet daraus eine grobe Inventarwert-Schätzung."
                else "Tip: Write prices as a number with p or Platinum. The app turns that into a rough inventory value estimate.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Progression-Tracker" else "Progression tracker",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            expanded = expanded == "progressionTools",
            onClick = { expanded = if (expanded == "progressionTools") "" else "progressionTools" }
        ) {
            val masteryItems = items.filter { masteryXpForItem(it) > 0 }
            val completedMasteryItems = masteryItems.filter { item -> item.components.isNotEmpty() && item.components.all { it.checked } }
            val masteryEstimate = completedMasteryItems.sumOf(::masteryXpForItem)
            val frameXp = completedMasteryItems.filter { it.type.equals("warframe", true) }.sumOf(::masteryXpForItem)
            val weaponXp = completedMasteryItems.filter { it.type.equals("weapon", true) }.sumOf(::masteryXpForItem)
            val companionXp = completedMasteryItems.filter { it.type.equals("companion", true) }.sumOf(::masteryXpForItem)
            BackupSummaryRow(if (german) "Geschätzte MR-XP erledigt" else "Estimated MR XP done", masteryEstimate.toString())
            BackupSummaryRow(if (german) "Warframes" else "Warframes", frameXp.toString())
            BackupSummaryRow(if (german) "Waffen" else "Weapons", weaponXp.toString())
            BackupSummaryRow(if (german) "Begleiter" else "Companions", companionXp.toString())
            BackupSummaryRow(if (german) "Offene Mastery-Kandidaten" else "Open mastery candidates", (masteryItems.size - completedMasteryItems.size).coerceAtLeast(0).toString())
            BackupSummaryRow(if (german) "Aktive Wochenziele" else "Active weekly goals", (tasks.size - checkedTasks.size).coerceAtLeast(0).toString())
            HorizontalDivider(color = AppColors.CardBorder)
            Text(
                if (german) "Fortschrittsziele für $activeProfile" else "Progress goals for $activeProfile",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            progressionCategories().forEach { category ->
                val categoryGoals = progressionGoals.filter { it.category == category }
                if (categoryGoals.isNotEmpty()) {
                    val categoryProgress = progressionCategoryProgress(progressionGoals, category)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(progressionCategoryLabel(category, german), color = AppColors.TextPrimary)
                        Text("${(categoryProgress * 100).toInt()} %", color = AppColors.EnergyCyan)
                    }
                    LinearProgressIndicator(
                        progress = { categoryProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = AppColors.EnergyCyan,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }
            OutlinedTextField(
                value = newProgressionTitle,
                onValueChange = { newProgressionTitle = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Neues Ziel" else "New goal") },
                placeholder = { Text(if (german) "z. B. Erde vollständig" else "e.g. Complete Earth") }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                progressionCategories().forEach { category ->
                    AssistChip(
                        onClick = { newProgressionCategory = category },
                        label = { Text(progressionCategoryLabel(category, german)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (newProgressionCategory == category) AppColors.EnergyCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                            labelColor = if (newProgressionCategory == category) AppColors.EnergyCyan else AppColors.TextSecondary
                        )
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newProgressionCurrent,
                    onValueChange = { newProgressionCurrent = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    label = { Text(if (german) "Erledigt" else "Done") }
                )
                OutlinedTextField(
                    value = newProgressionTarget,
                    onValueChange = { newProgressionTarget = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    label = { Text(if (german) "Gesamt" else "Total") }
                )
            }
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    progressionGoals = upsertProgressionGoal(
                        progressionGoals,
                        ProgressionGoal(
                            id = "progress_$now",
                            title = newProgressionTitle.trim(),
                            category = newProgressionCategory,
                            current = newProgressionCurrent.toIntOrNull() ?: 0,
                            target = newProgressionTarget.toIntOrNull() ?: 0
                        )
                    )
                    ProgressionGoalStore.save(context, activeProfile, progressionGoals)
                    newProgressionTitle = ""
                    newProgressionCurrent = "0"
                    newProgressionTarget = ""
                },
                enabled = newProgressionTitle.isNotBlank() && (newProgressionTarget.toIntOrNull() ?: 0) > 0,
                shape = AppShapes.Small
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(if (german) "Fortschrittsziel speichern" else "Save progress goal")
            }
            progressionGoals.forEach { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Small,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(goal.title, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${progressionCategoryLabel(goal.category, german)} · ${goal.current}/${goal.target}",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                progressionGoals = adjustProgressionGoal(progressionGoals, goal.id, -1)
                                ProgressionGoalStore.save(context, activeProfile, progressionGoals)
                            }) { Icon(Icons.Default.Remove, null, tint = AppColors.TextSecondary) }
                            IconButton(onClick = {
                                progressionGoals = adjustProgressionGoal(progressionGoals, goal.id, 1)
                                ProgressionGoalStore.save(context, activeProfile, progressionGoals)
                            }) { Icon(Icons.Default.Add, null, tint = AppColors.EnergyCyan) }
                            IconButton(onClick = {
                                progressionGoals = progressionGoals.filterNot { it.id == goal.id }
                                ProgressionGoalStore.save(context, activeProfile, progressionGoals)
                            }) { Icon(Icons.Default.DeleteOutline, null, tint = AppColors.TextSecondary) }
                        }
                        LinearProgressIndicator(
                            progress = { goal.progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = AppColors.EnergyCyan,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                }
            }
            HorizontalDivider(color = AppColors.CardBorder)
            HubTextField(
                value = progressionNotes,
                onValueChange = {
                    progressionNotes = it
                    prefs.edit().putString("progression_notes", it).apply()
                },
                label = if (german) "Zusätzliche Progressions-Notizen" else "Additional progression notes"
            )
            HubTextField(
                value = levelFormaNotes,
                onValueChange = {
                    levelFormaNotes = it
                    prefs.edit().putString("level_forma_notes", it).apply()
                },
                label = if (german) "Level, Forma, Catalyst/Reactor und Exilus" else "Level, forma, catalyst/reactor and Exilus"
            )
            HubTextField(
                value = questNotes,
                onValueChange = {
                    questNotes = it
                    prefs.edit().putString("quest_notes", it).apply()
                },
                label = if (german) "Quest, Junction, Sternenkarte und Steel Path" else "Quest, junction, star chart and Steel Path"
            )
            HubTextField(
                value = intrinsicsNotes,
                onValueChange = {
                    intrinsicsNotes = it
                    prefs.edit().putString("intrinsics_notes", it).apply()
                },
                label = if (german) "Intrinsics, Fokus und Syndikat-Ränge" else "Intrinsics, focus and syndicate ranks"
            )
            HubTextField(
                value = adapterNotes,
                onValueChange = {
                    adapterNotes = it
                    prefs.edit().putString("adapter_notes", it).apply()
                },
                label = if (german) "Archon-Shards, Adapter und besondere Upgrades" else "Archon shards, adapters and special upgrades"
            )
        }

        HubSection(
            title = if (german) "Builds, Mods & Wiki" else "Builds, mods & wiki",
            icon = Icons.Default.Route,
            expanded = expanded == "buildTools",
            onClick = { expanded = if (expanded == "buildTools") "" else "buildTools" }
        ) {
            HubTextField(
                value = loadoutNotes,
                onValueChange = {
                    loadoutNotes = it
                    prefs.edit().putString("loadout_notes", it).apply()
                },
                label = if (german) "Builds / Loadouts" else "Builds / loadouts"
            )
            HubTextField(
                value = modNotes,
                onValueChange = {
                    modNotes = it
                    prefs.edit().putString("mod_notes", it).apply()
                },
                label = if (german) "Mod-Sammlung" else "Mod collection"
            )
            Text(
                text = if (german) "Offline-Wiki: Relikte, Sortie, Archon, Nightwave, Baro, Standing, Steel Path."
                else "Offline wiki: relics, sortie, archon, Nightwave, Baro, standing, Steel Path.",
                color = AppColors.TextSecondary
            )
        }

        HubSection(
            title = if (german) "Build-Archiv" else "Build archive",
            icon = Icons.Default.Route,
            expanded = expanded == "buildArchive",
            onClick = { expanded = if (expanded == "buildArchive") "" else "buildArchive" }
        ) {
            BackupSummaryRow(if (german) "Gespeicherte Builds" else "Saved builds", savedBuilds.size.toString())
            BackupSummaryRow(if (german) "Favoriten" else "Favorites", savedBuilds.count { it.favorite }.toString())
            BackupSummaryRow(if (german) "Noch unvollständig" else "Still incomplete", savedBuilds.count { buildCompleteness(it) < 100 }.toString())
            Text(
                if (editingBuildId == null) {
                    if (german) "Neuen Build anlegen" else "Create new build"
                } else {
                    if (german) "Build bearbeiten" else "Edit build"
                },
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = buildName,
                onValueChange = { buildName = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Build-Name" else "Build name") },
                placeholder = { Text(if (german) "z. B. Wisp Stahlpfad" else "e.g. Wisp Steel Path") }
            )
            OutlinedTextField(
                value = buildTarget,
                onValueChange = { buildTarget = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Warframe oder Waffe" else "Warframe or weapon") },
                placeholder = { Text("Wisp Prime") }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                buildPurposes().forEach { purpose ->
                    AssistChip(
                        onClick = { buildPurpose = purpose },
                        label = { Text(buildPurposeLabel(purpose, german)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (buildPurpose == purpose) AppColors.EnergyCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                            labelColor = if (buildPurpose == purpose) AppColors.EnergyCyan else AppColors.TextSecondary
                        )
                    )
                }
            }
            HubTextField(
                value = buildMods,
                onValueChange = { buildMods = it.take(2_000) },
                label = if (german) "Mods – einer pro Zeile" else "Mods – one per line"
            )
            OutlinedTextField(
                value = buildArcanes,
                onValueChange = { buildArcanes = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Arkanes" else "Arcanes") }
            )
            OutlinedTextField(
                value = buildHelminth,
                onValueChange = { buildHelminth = it.take(120) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Helminth") }
            )
            OutlinedTextField(
                value = buildFocus,
                onValueChange = { buildFocus = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Fokus-Schule" else "Focus school") }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val existing = editingBuildId?.let { id -> savedBuilds.firstOrNull { it.id == id } }
                        val build = SavedBuild(
                            id = editingBuildId ?: "build_$now",
                            name = buildName.trim(),
                            target = buildTarget.trim(),
                            purpose = buildPurpose,
                            mods = buildMods.trim(),
                            arcanes = buildArcanes.trim(),
                            helminth = buildHelminth.trim(),
                            focusSchool = buildFocus.trim(),
                            favorite = existing?.favorite ?: false,
                            updatedAt = now
                        )
                        savedBuilds = upsertBuild(savedBuilds, build)
                        BuildArchiveStore.save(context, savedBuilds)
                        clearBuildEditor()
                    },
                    enabled = buildName.isNotBlank() && buildTarget.isNotBlank(),
                    shape = AppShapes.Small
                ) {
                    Text(if (editingBuildId == null) {
                        if (german) "Build speichern" else "Save build"
                    } else if (german) "Änderungen speichern" else "Save changes")
                }
                if (editingBuildId != null) {
                    TextButton(onClick = { clearBuildEditor() }) {
                        Text(if (german) "Bearbeiten abbrechen" else "Cancel editing")
                    }
                }
            }
            savedBuilds.forEach { saved ->
                val completeness = buildCompleteness(saved)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Small,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(saved.name, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    "${saved.target} · ${buildPurposeLabel(saved.purpose, german)}",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                savedBuilds = savedBuilds.map { if (it.id == saved.id) it.copy(favorite = !it.favorite) else it }
                                BuildArchiveStore.save(context, savedBuilds)
                            }) {
                                Icon(
                                    if (saved.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    if (german) "Favorit" else "Favorite",
                                    tint = if (saved.favorite) AppColors.OrokinGold else AppColors.TextSecondary
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { completeness / 100f },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = if (completeness == 100) AppColors.EnergyCyan else AppColors.OrokinGold,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        Text(
                            if (german) "$completeness % vollständig" else "$completeness% complete",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { editBuild(saved) }) {
                                Text(if (german) "Bearbeiten" else "Edit")
                            }
                            TextButton(onClick = {
                                savedBuilds = duplicateBuild(savedBuilds, saved, System.currentTimeMillis(), german)
                                BuildArchiveStore.save(context, savedBuilds)
                            }) {
                                Text(if (german) "Duplizieren" else "Duplicate")
                            }
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(ClipboardManager::class.java)
                                val share = buildShareText(
                                    saved.copy(purpose = buildPurposeLabel(saved.purpose, german)),
                                    german
                                )
                                clipboard?.setPrimaryClip(ClipData.newPlainText(saved.name, share))
                                Toast.makeText(
                                    context,
                                    if (german) "Build in Zwischenablage kopiert." else "Build copied to clipboard.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                                Text(if (german) "Kopieren" else "Copy")
                            }
                            TextButton(onClick = {
                                savedBuilds = savedBuilds.filterNot { it.id == saved.id }
                                BuildArchiveStore.save(context, savedBuilds)
                                if (editingBuildId == saved.id) clearBuildEditor()
                            }) {
                                Text(if (german) "Löschen" else "Delete", color = Color(0xFFFF9B8F))
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = AppColors.CardBorder)
            Text(
                if (german) "Vorhandene freie Build-Notizen" else "Existing free-form build notes",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            HubTextField(
                value = buildTemplateNotes,
                onValueChange = {
                    buildTemplateNotes = it
                    prefs.edit().putString("build_template_notes", it).apply()
                },
                label = if (german) "Alte Loadout-Notizen" else "Old loadout notes"
            )
            HubTextField(
                value = arcaneNotes,
                onValueChange = {
                    arcaneNotes = it
                    prefs.edit().putString("arcane_notes", it).apply()
                },
                label = if (german) "Alte Arkane-/Helminth-Notizen" else "Old arcane/Helminth notes"
            )
            HubTextField(
                value = buildExchangeNotes,
                onValueChange = {
                    buildExchangeNotes = it
                    prefs.edit().putString("build_exchange_notes", it).apply()
                },
                label = if (german) "Alte Build-Austausch-Notizen" else "Old build exchange notes"
            )
        }

        HubSection(
            title = if (german) "Syndikate, Standing & Foundry" else "Syndicates, standing & foundry",
            icon = Icons.Default.Groups,
            expanded = expanded == "standingFoundry",
            onClick = { expanded = if (expanded == "standingFoundry") "" else "standingFoundry" }
        ) {
            Text(
                if (german) "Standing-Ziele" else "Standing goals",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = newStandingName,
                onValueChange = { newStandingName = it.take(60) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Syndikat oder Ziel" else "Syndicate or goal") },
                placeholder = { Text(if (german) "z. B. Entrati Rang 5" else "e.g. Entrati Rank 5") }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newStandingCurrent,
                    onValueChange = { newStandingCurrent = it.filter(Char::isDigit).take(7) },
                    modifier = Modifier.width(105.dp),
                    singleLine = true,
                    label = { Text(if (german) "Aktuell" else "Current") }
                )
                OutlinedTextField(
                    value = newStandingTarget,
                    onValueChange = { newStandingTarget = it.filter(Char::isDigit).take(7) },
                    modifier = Modifier.width(105.dp),
                    singleLine = true,
                    label = { Text(if (german) "Ziel" else "Target") }
                )
                OutlinedTextField(
                    value = newStandingDaily,
                    onValueChange = { newStandingDaily = it.filter(Char::isDigit).take(7) },
                    modifier = Modifier.width(115.dp),
                    singleLine = true,
                    label = { Text(if (german) "Pro Tag" else "Per day") }
                )
            }
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    standingGoals = upsertStandingGoal(
                        standingGoals,
                        StandingGoal(
                            id = "standing_$now",
                            name = newStandingName.trim(),
                            current = newStandingCurrent.toIntOrNull() ?: 0,
                            target = newStandingTarget.toIntOrNull() ?: 0,
                            dailyGain = newStandingDaily.toIntOrNull() ?: 0
                        )
                    )
                    StandingGoalStore.save(context, standingGoals)
                    newStandingName = ""
                    newStandingCurrent = "0"
                    newStandingTarget = ""
                    newStandingDaily = ""
                },
                enabled = newStandingName.isNotBlank() && (newStandingTarget.toIntOrNull() ?: 0) > 0,
                shape = AppShapes.Small
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(if (german) "Standing-Ziel speichern" else "Save standing goal")
            }
            standingGoals.forEach { goal ->
                val days = standingDaysRemaining(goal)
                val progressValue = goal.current.toFloat() / goal.target.coerceAtLeast(1)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Small,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(goal.name, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    "${goal.current} / ${goal.target} · ${goal.dailyGain} ${if (german) "pro Tag" else "per day"}",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                when (days) {
                                    0 -> if (german) "FERTIG" else "DONE"
                                    null -> if (german) "Kein Tageswert" else "No daily value"
                                    1 -> if (german) "1 Tag" else "1 day"
                                    else -> if (german) "$days Tage" else "$days days"
                                },
                                color = if (days == 0) AppColors.EnergyCyan else AppColors.OrokinGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressValue.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = AppColors.EnergyCyan,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = {
                                    standingGoals = applyStandingDay(standingGoals, goal.id)
                                    StandingGoalStore.save(context, standingGoals)
                                },
                                enabled = goal.current < goal.target && goal.dailyGain > 0
                            ) {
                                Text(if (german) "+ Spieltag" else "+ Day")
                            }
                            TextButton(onClick = {
                                standingGoals = standingGoals.filterNot { it.id == goal.id }
                                StandingGoalStore.save(context, standingGoals)
                            }) {
                                Text(if (german) "Löschen" else "Delete", color = Color(0xFFFF9B8F))
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = AppColors.CardBorder)
            HubTextField(
                value = syndicateNotes,
                onValueChange = {
                    syndicateNotes = it
                    prefs.edit().putString("syndicate_notes", it).apply()
                },
                label = if (german) "Zusätzliche Syndikat-Notizen" else "Additional syndicate notes"
            )
            HubTextField(
                value = standingNotes,
                onValueChange = {
                    standingNotes = it
                    prefs.edit().putString("standing_notes", it).apply()
                },
                label = if (german) "Zusätzliche Standing-Notizen" else "Additional standing notes"
            )
            HubTextField(
                value = foundryNotes,
                onValueChange = {
                    foundryNotes = it
                    prefs.edit().putString("foundry_notes", it).apply()
                },
                label = if (german) "Zusätzliche Foundry-Notizen" else "Additional Foundry notes"
            )
            HorizontalDivider(color = AppColors.CardBorder)
            Text(
                if (german) "Bauzeit-Tracker" else "Crafting timer tracker",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = newFoundryItem,
                onValueChange = { newFoundryItem = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (german) "Was wird gebaut?" else "What is crafting?") },
                placeholder = { Text(if (german) "z. B. Wisp Prime" else "e.g. Wisp Prime") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.EnergyCyan,
                    focusedLabelColor = AppColors.EnergyCyan
                )
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1L, 12L, 24L, 72L).forEach { hours ->
                    AssistChip(
                        onClick = { foundryDurationHours = hours },
                        label = { Text(if (hours == 72L) "3 ${if (german) "Tage" else "days"}" else "$hours h") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (foundryDurationHours == hours) AppColors.EnergyCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                            labelColor = if (foundryDurationHours == hours) AppColors.EnergyCyan else AppColors.TextSecondary
                        )
                    )
                }
            }
            Button(
                onClick = {
                    val name = newFoundryItem.trim()
                    if (name.isNotBlank()) {
                        val now = System.currentTimeMillis()
                        val timer = FoundryTimer(
                            id = "${now}_${name.hashCode().toUInt()}",
                            name = name,
                            createdAt = now,
                            finishesAt = now + foundryDurationHours * 60L * 60L * 1000L
                        )
                        foundryTimers = (foundryTimers + timer).sortedBy { it.finishesAt }
                        FoundryTimerStore.save(context, foundryTimers)
                        FoundryTimerStore.schedule(context, timer)
                        TennoWidgetProvider.updateAll(context)
                        newFoundryItem = ""
                    }
                },
                enabled = newFoundryItem.isNotBlank(),
                shape = AppShapes.Small
            ) {
                Icon(Icons.Default.AddAlarm, null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(if (german) "Timer starten" else "Start timer")
            }
            if (foundryTimers.isEmpty()) {
                Text(
                    if (german) "Noch keine laufenden Foundry-Bauten." else "No active Foundry crafts yet.",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                foundryTimers.forEach { timer ->
                    val ready = timer.finishesAt <= foundryClock
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.Small,
                        colors = CardDefaults.cardColors(
                            containerColor = if (ready) AppColors.EnergyCyan.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(timer.name, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    foundryRemainingLabel(timer.finishesAt - foundryClock, german),
                                    color = if (ready) AppColors.EnergyCyan else AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(
                                onClick = {
                                    FoundryTimerStore.cancel(context, timer.id)
                                    foundryTimers = foundryTimers.filterNot { it.id == timer.id }
                                    FoundryTimerStore.save(context, foundryTimers)
                                    TennoWidgetProvider.updateAll(context)
                                }
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    if (german) "Timer entfernen" else "Remove timer",
                                    tint = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        HubSection(
            title = if (german) "Benachrichtigungen" else "Notifications",
            icon = Icons.Default.NotificationsActive,
            expanded = expanded == "alerts",
            onClick = { expanded = if (expanded == "alerts") "" else "alerts" }
        ) {
            SettingSwitch(if (german) "Baro Ki'Teer beobachten" else "Watch Baro Ki'Teer", baroAlert) {
                baroAlert = it
                prefs.edit().putBoolean("alert_baro", it).apply()
                TennoSystem.scheduleReminderWork(context)
            }
            SettingSwitch(if (german) "Vor Eidolon-Nacht erinnern" else "Alert before Eidolon night", eidolonAlert) {
                eidolonAlert = it
                prefs.edit().putBoolean("alert_eidolon", it).apply()
                TennoSystem.scheduleReminderWork(context)
            }
            SettingSwitch(if (german) "Void-Risse für Favoriten" else "Fissures for favorites", fissureAlert) {
                fissureAlert = it
                prefs.edit().putBoolean("alert_fissure", it).apply()
                TennoSystem.scheduleReminderWork(context)
            }
            SettingSwitch(if (german) "Widget-Kurzübersicht aktivieren" else "Enable widget summary", widgetSummary) {
                widgetSummary = it
                prefs.edit().putBoolean("widget_summary", it).apply()
                TennoWidgetProvider.updateAll(context)
            }
            Text(
                text = if (german) "Die Beobachtungsliste wird in der App gespeichert. Android-Systembenachrichtigungen laufen über einen eigenen Kanal; das Homescreen-Widget zeigt Profil und priorisierte Farmziele."
                else "The watchlist is stored in the app. Android system notifications use a dedicated channel; the home screen widget shows profile and prioritized farm targets.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Sicherung & Gerätewechsel" else "Backup & device transfer",
            icon = Icons.Default.Backup,
            expanded = expanded == "backup",
            onClick = { expanded = if (expanded == "backup") "" else "backup" }
        ) {
            Text(
                text = if (german) {
                    "Import und Export findest du im Seitenmenü. Diese Übersicht zeigt dir, was aktuell in deiner Sicherung wichtig ist."
                } else {
                    "Import and export are in the side menu. This overview shows what currently matters for your backup."
                },
                color = AppColors.TextSecondary
            )
            BackupSummaryRow(if (german) "Profil" else "Profile", backupSummary.profile)
            BackupSummaryRow(if (german) "Einträge" else "Items", backupSummary.itemCount.toString())
            BackupSummaryRow(if (german) "Komponenten" else "Components", "${backupSummary.ownedComponentCount}/${backupSummary.componentCount}")
            BackupSummaryRow(if (german) "Favoriten" else "Favorites", backupSummary.favoriteCount.toString())
            BackupSummaryRow(if (german) "Prioritäten" else "Priorities", backupSummary.priorityCount.toString())
            BackupSummaryRow(
                if (german) "Lokale Werkzeugnotizen" else "Local tool notes",
                localNoteCount(
                    listOf(
                        relicInventory,
                        ducatParts,
                        platinumNotes,
                        tradingNotes,
                        loadoutNotes,
                        modNotes,
                        syndicateNotes,
                        standingNotes,
                        foundryNotes,
                        tagNotes,
                        refinementNotes,
                        bestMissionNotes,
                        primeVaultNotes,
                        primeWishlistNotes,
                        resourceNotes,
                        bossdropNotes,
                        nightwaveNotes,
                        archonNotes,
                        kuvaTenetNotes,
                        incarnonNotes,
                        steelPathNotes,
                        arbitrationNotes,
                        duviriNotes,
                        helminthNotes,
                        railjackNotes,
                        necramechNotes,
                        companionNotes,
                        focusNotes,
                        marketPriceNotes,
                        tradingWishlistNotes,
                        buyerSellerNotes,
                        priceHistoryNotes,
                        progressionNotes,
                        levelFormaNotes,
                        adapterNotes,
                        questNotes,
                        intrinsicsNotes,
                        buildTemplateNotes,
                        arcaneNotes,
                        buildExchangeNotes,
                        qualityNotes
                    )
                ).toString()
            )
        }

        HubSection(
            title = if (german) "Bedienung & Qualität" else "Usability & quality",
            icon = Icons.Default.Checklist,
            expanded = expanded == "qualityTools",
            onClick = { expanded = if (expanded == "qualityTools") "" else "qualityTools" }
        ) {
            Text(
                if (german) "Kompakter Modus, größere Schrift, Farbschema und Offline-Modus lassen sich direkt unter Einstellungen ändern."
                else "Compact mode, larger text, color scheme, and offline mode can be changed directly in Settings.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                if (german) "Der wirksame Offline-Modus befindet sich unter Einstellungen."
                else "The active offline mode is available in Settings.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            SettingSwitch(if (german) "Letzte Seite merken" else "Remember last page", rememberLastPage) {
                rememberLastPage = it
                prefs.edit().putBoolean("remember_last_page", it).apply()
            }
            SettingSwitch(if (german) "Backup-Erinnerung aktiv" else "Backup reminder active", backupReminder) {
                backupReminder = it
                prefs.edit().putBoolean("backup_reminder", it).apply()
                TennoSystem.scheduleReminderWork(context)
            }
            SettingSwitch(if (german) "Favoriten in der Sammlung oben anpinnen" else "Pin favorites at top of collection", pinFavorites) {
                pinFavorites = it
                prefs.edit().putBoolean("pin_favorites", it).apply()
            }
            HubTextField(
                value = qualityNotes,
                onValueChange = {
                    qualityNotes = it
                    prefs.edit().putString("quality_notes", it).apply()
                },
                label = if (german) "UI-/Tablet-/Leere-Zustände-/Farbschema-Notizen" else "UI/tablet/empty-state/color-scheme notes"
            )
            Text(
                text = if (german) "Die fertigen Optionen wirken direkt in der App und werden lokal gespeichert."
                else "Completed options take effect in the app and are stored locally.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HubSection(
            title = if (german) "Roadmap 41-140" else "Roadmap 41-140",
            icon = Icons.Default.Checklist,
            expanded = expanded == "roadmapRest",
            onClick = { expanded = if (expanded == "roadmapRest") "" else "roadmapRest" }
        ) {
            val roadmapFeatures = remainingRoadmapFeatures()
            BackupSummaryRow(
                if (german) "In App verwaltet" else "Managed in app",
                "${roadmapDone.size}/${roadmapFeatures.size}"
            )
            LinearProgressIndicator(
                progress = { roadmapDone.size.toFloat() / roadmapFeatures.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = AppColors.EnergyCyan,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            HubTextField(
                value = roadmapNotes,
                onValueChange = {
                    roadmapNotes = it
                    prefs.edit().putString("roadmap_notes_41_140", it).apply()
                },
                label = if (german) "Notizen für den restlichen Ausbau" else "Notes for the remaining upgrade"
            )
            roadmapFeatures.groupBy { it.area(german) }.forEach { (area, features) ->
                Text(area, color = AppColors.OrokinGold, fontWeight = FontWeight.SemiBold)
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val key = feature.id.toString()
                                val next = if (key in roadmapDone) roadmapDone - key else roadmapDone + key
                                roadmapDone = next
                                prefs.edit().putStringSet("roadmap_done_41_140", next).apply()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = feature.id.toString() in roadmapDone,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.EnergyCyan)
                        )
                        Text(
                            text = "${feature.id}. ${feature.title(german)}",
                            modifier = Modifier.weight(1f),
                            color = AppColors.TextPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        HubSection(
            title = if (german) "Release-Status" else "Release status",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            expanded = expanded == "release",
            onClick = { expanded = if (expanded == "release") "" else "release" }
        ) {
            listOf(
                if (german) "Navigation und Zurück-Verhalten geprüft" else "Navigation and back behavior verified",
                if (german) "Live-Daten mit echten Warframe-Endpunkten verbunden" else "Live data connected to real Warframe endpoints",
                if (german) "Scanner, Farm-Plan, Prioritäten und Profile lokal integriert" else "Scanner, farm plan, priorities, and profiles integrated locally",
                if (german) "Android-Benachrichtigungen, Widget und Crash-Log technisch integriert" else "Android notifications, widget, and crash log integrated technically",
                if (german) "Nächster großer Release-Schritt: signierter GitHub-Release-Build" else "Next major release step: signed GitHub release build"
            ).forEach { line ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Checklist, contentDescription = null, tint = AppColors.EnergyCyan)
                    Text(line, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .height(112.dp)
            .fillMaxWidth(0.31f),
        shape = AppShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.EnergyCyan)
            Text(value, color = AppColors.TextPrimary, style = MaterialTheme.typography.titleLarge)
            Text(title, color = AppColors.OrokinGold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FarmRecommendationRow(
    recommendation: FarmRecommendation,
    german: Boolean,
    completed: Boolean,
    inDailyPlan: Boolean,
    onCompletedChanged: (Boolean) -> Unit,
    onDailyPlanChanged: (Boolean) -> Unit,
    onOpenDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Medium,
        colors = CardDefaults.cardColors(
            containerColor = if (recommendation.activeFissure != null) {
                AppColors.EnergyCyan.copy(alpha = 0.13f)
            } else {
                Color.White.copy(alpha = 0.05f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = recommendation.item.name,
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recommendation.component.name,
                color = AppColors.OrokinGold
            )
            Text(
                text = recommendation.priority.label(german),
                color = AppColors.EnergyCyan,
                style = MaterialTheme.typography.labelMedium
            )
            val relicText = recommendation.component.relic.ifBlank {
                if (german) "Relikt unbekannt" else "Unknown relic"
            }
            Text(
                text = relicText,
                color = AppColors.EnergyCyan,
                style = MaterialTheme.typography.bodySmall
            )
            recommendation.activeFissure?.let { fissure ->
                Text(
                    text = if (german) {
                        "Jetzt spielbar: ${fissure.node ?: "Unbekannt"} · ${fissure.missionType ?: "Mission"}"
                    } else {
                        "Playable now: ${fissure.node ?: "Unknown"} · ${fissure.missionType ?: "Mission"}"
                    },
                    color = Color(0xFFFFD54F),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (recommendation.reason.isNotBlank()) {
                Text(
                    text = recommendation.reason,
                    modifier = Modifier
                        .background(
                            color = AppColors.OrokinGold.copy(alpha = 0.16f),
                            shape = AppShapes.Small
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    color = AppColors.OrokinGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { onCompletedChanged(!completed) },
                    label = { Text(if (completed) if (german) "Session erledigt" else "Done this session" else if (german) "Für Session abhaken" else "Mark for session") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (completed) AppColors.EnergyCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                        labelColor = if (completed) AppColors.EnergyCyan else AppColors.TextSecondary
                    )
                )
                AssistChip(
                    onClick = { onDailyPlanChanged(!inDailyPlan) },
                    label = {
                        Text(
                            if (inDailyPlan) {
                                if (german) "Im Tagesplan" else "In daily plan"
                            } else {
                                if (german) "Heute einplanen" else "Add for today"
                            }
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (inDailyPlan) AppColors.OrokinGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                        labelColor = if (inDailyPlan) AppColors.OrokinGold else AppColors.TextSecondary
                    )
                )
                AssistChip(
                    onClick = onOpenDetails,
                    label = { Text(if (german) "Details" else "Details") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = AppColors.OrokinGold.copy(alpha = 0.14f),
                        labelColor = AppColors.OrokinGold
                    )
                )
            }
        }
    }
}

@Composable
private fun ItemDetailDialog(
    item: WarframeItem,
    fissuresData: List<FissureResponse>,
    german: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tenno_item_notes", Context.MODE_PRIVATE) }
    var itemNote by remember(item.name) { mutableStateOf(prefs.getString("note_${item.name}", "") ?: "") }
    var farmNote by remember(item.name) { mutableStateOf(prefs.getString("farm_${item.name}", "") ?: "") }
    val missingComponents = item.components.filterNot { it.checked }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (german) "Schließen" else "Close")
            }
        },
        containerColor = AppColors.HudPanel,
        titleContentColor = AppColors.TextPrimary,
        textContentColor = AppColors.TextSecondary,
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (missingComponents.isEmpty()) {
                        if (german) "Alle Komponenten sind erledigt." else "All components are complete."
                    } else {
                        if (german) "${missingComponents.size} offene Komponente(n)" else "${missingComponents.size} open component(s)"
                    },
                    color = AppColors.EnergyCyan
                )
                missingComponents.forEach { component ->
                    val tier = relicTier(component.relic)
                    val fissure = tier?.let { targetTier ->
                        fissuresData.firstOrNull { fissure ->
                            fissure.isStorm != true &&
                                fissure.isHard != true &&
                                !fissure.expiry.isNullOrBlank() &&
                                !isExpired(fissure.expiry) &&
                                fissure.tier.equals(targetTier, ignoreCase = true)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), AppShapes.Small)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(component.name, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = component.relic.ifBlank { if (german) "Relikt unbekannt" else "Unknown relic" },
                            color = AppColors.OrokinGold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = component.farmLocation.ifBlank { if (german) "Farm-Ort noch nicht gepflegt" else "Farm location not set yet" },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        fissure?.let {
                            Text(
                                text = if (german) "Aktiv: ${it.node ?: "Unbekannt"} · ${it.missionType ?: "Mission"}" else "Active: ${it.node ?: "Unknown"} · ${it.missionType ?: "Mission"}",
                                color = Color(0xFFFFD54F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                HubTextField(
                    value = itemNote,
                    onValueChange = {
                        itemNote = it
                        prefs.edit().putString("note_${item.name}", it).apply()
                    },
                    label = if (german) "Eigene Item-Notiz" else "Custom item note"
                )
                HubTextField(
                    value = farmNote,
                    onValueChange = {
                        farmNote = it
                        prefs.edit().putString("farm_${item.name}", it).apply()
                    },
                    label = if (german) "Eigener Farm-Ort / Tipp" else "Custom farm location / tip"
                )
            }
        }
    )
}

@Composable
private fun HubSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    HudCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.EnergyCyan)
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = AppColors.TextSecondary
            )
        }
        if (expanded) {
            HorizontalDivider(color = AppColors.CardBorder)
            content()
        }
    }
}

@Composable
private fun HudCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun FavoriteItemRow(
    item: WarframeItem?,
    favorite: Boolean,
    onToggle: () -> Unit
) {
    if (item == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, color = AppColors.TextPrimary)
            Text(
                text = "${item.components.count { it.checked }}/${item.components.size}",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(
            imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = null,
            tint = if (favorite) AppColors.OrokinGold else AppColors.TextSecondary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PriorityControls(
    selected: WatchPriority,
    german: Boolean,
    onPrioritySelected: (WatchPriority) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        WatchPriority.visibleChoices.forEach { priority ->
            AssistChip(
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.label(german), maxLines = 1) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (priority == selected) {
                        AppColors.EnergyCyan.copy(alpha = 0.18f)
                    } else {
                        Color.White.copy(alpha = 0.04f)
                    },
                    labelColor = if (priority == selected) AppColors.EnergyCyan else AppColors.TextSecondary
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = if (priority == selected) AppColors.EnergyCyan else AppColors.CardBorder
                )
            )
        }
    }
}

private fun loadRecurringTaskIds(
    prefs: SharedPreferences,
    profile: String,
    tasks: List<RecurringTask>,
    date: LocalDate = LocalDate.now()
): Set<String> {
    val key = "tasks_v2_$profile"
    if (prefs.contains(key)) {
        return activeTaskIds(prefs.getStringSet(key, emptySet()).orEmpty(), tasks, date)
    }

    val legacy = prefs.getStringSet("tasks_$profile", emptySet()).orEmpty()
    val migrated = tasks.filter { task ->
        legacy.any { old ->
            old.equals(task.germanName, ignoreCase = true) ||
                old.equals(task.englishName, ignoreCase = true) ||
                old.equals(task.id, ignoreCase = true)
        }
    }.mapTo(mutableSetOf()) { it.id }
    saveRecurringTaskIds(prefs, profile, tasks, migrated, date)
    return migrated
}

private fun saveRecurringTaskIds(
    prefs: SharedPreferences,
    profile: String,
    tasks: List<RecurringTask>,
    checkedIds: Set<String>,
    date: LocalDate = LocalDate.now()
) {
    val stored = tasks
        .filter { it.id in checkedIds }
        .mapTo(mutableSetOf()) { encodeTaskCompletion(it, date) }
    prefs.edit().putStringSet("tasks_v2_$profile", stored).apply()
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = AppColors.TextPrimary)
        Switch(checked, onCheckedChange = onChange)
    }
}

@Composable
private fun HubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.EnergyCyan,
            unfocusedBorderColor = AppColors.CardBorder,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.EnergyCyan
        )
    )
}

@Composable
private fun BackupSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = AppColors.TextSecondary)
        Text(value, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

private data class BackupSummary(
    val profile: String,
    val itemCount: Int,
    val componentCount: Int,
    val ownedComponentCount: Int,
    val favoriteCount: Int,
    val priorityCount: Int
)

private data class RoadmapFeature(
    val id: Int,
    val areaDe: String,
    val areaEn: String,
    val titleDe: String,
    val titleEn: String
) {
    fun area(german: Boolean): String = if (german) areaDe else areaEn
    fun title(german: Boolean): String = if (german) titleDe else titleEn
}

private fun remainingRoadmapFeatures(): List<RoadmapFeature> {
    fun f(id: Int, areaDe: String, areaEn: String, titleDe: String, titleEn: String) =
        RoadmapFeature(id, areaDe, areaEn, titleDe, titleEn)

    return listOf(
        f(41, "Scanner", "Scanner", "Scanner-Modus für Foundry", "Scanner mode for Foundry"),
        f(42, "Scanner", "Scanner", "Scanner-Modus für Mods", "Scanner mode for mods"),
        f(43, "Scanner", "Scanner", "Scanner-Modus für Mission-Endscreen", "Scanner mode for mission end screen"),
        f(44, "Scanner", "Scanner", "Mehrere Screenshots nacheinander scannen", "Scan multiple screenshots in a row"),
        f(45, "Scanner", "Scanner", "Bilder-Vorschau im Scanner", "Image preview in scanner"),
        f(46, "Scanner", "Scanner", "Vorher/Nachher-Vergleich", "Before/after comparison"),
        f(47, "Scanner", "Scanner", "Undo letzter Scan", "Undo last scan"),
        f(48, "Scanner", "Scanner", "Export des Scanner-Verlaufs", "Export scanner history"),
        f(49, "Scanner", "Scanner", "OCR-Regeln pro Sprache", "OCR rules per language"),
        f(50, "Scanner", "Scanner", "Lernfunktion für falsch erkannte Begriffe", "Learning for misread terms"),
        f(51, "Live-Daten", "Live data", "Weltenzyklus-Karte statt nur Liste", "World cycle cards instead of list only"),
        f(52, "Live-Daten", "Live data", "Countdown-Kacheln für Cetus, Vallis, Erde, Deimos", "Countdown tiles for Cetus, Vallis, Earth, Deimos"),
        f(53, "Live-Daten", "Live data", "Push-Hinweis kurz vor Eidolon-Nacht", "Push hint before Eidolon night"),
        f(54, "Live-Daten", "Live data", "Push-Hinweis kurz vor Baro", "Push hint before Baro"),
        f(55, "Live-Daten", "Live data", "Alarm-Belohnungen filtern", "Filter alert rewards"),
        f(56, "Live-Daten", "Live data", "Invasionen nach Belohnung sortieren", "Sort invasions by reward"),
        f(57, "Live-Daten", "Live data", "Sortie-Modifikatoren besser erklären", "Explain sortie modifiers better"),
        f(58, "Live-Daten", "Live data", "News mit Link, Datum und Quelle", "News with link, date, and source"),
        f(59, "Live-Daten", "Live data", "Relevant für mich-Live-Ansicht", "Relevant for me live view"),
        f(60, "Live-Daten", "Live data", "Offline-Cache für letzte Live-Daten", "Offline cache for latest live data"),
        f(61, "Live-Daten", "Live data", "Plattformauswahl", "Platform selection"),
        f(62, "Live-Daten", "Live data", "Manuelles Aktualisierungsintervall", "Manual refresh interval"),
        f(63, "Live-Daten", "Live data", "Live-Fehlerdiagnose", "Live error diagnostics"),
        f(64, "Live-Daten", "Live data", "Farbcodes für Dringlichkeit", "Urgency color coding"),
        f(65, "Live-Daten", "Live data", "Live-Daten als Startseite wählbar", "Live data as selectable start page"),
        f(66, "Trading & Markt", "Trading & market", "Voller Ducaten-Rechner mit Mengen", "Full ducat calculator with quantities"),
        f(67, "Trading & Markt", "Trading & market", "Ducaten pro Platinum-Verhältnis", "Ducat per Platinum ratio"),
        f(68, "Trading & Markt", "Trading & market", "Prime-Set-Verkaufspreise notieren", "Record Prime set prices"),
        f(69, "Trading & Markt", "Trading & market", "Trading-Wunschliste", "Trading wishlist"),
        f(70, "Trading & Markt", "Trading & market", "Käufer-/Verkäufer-Notizen", "Buyer/seller notes"),
        f(71, "Trading & Markt", "Trading & market", "Set vollständig verkaufbar-Anzeige", "Set ready to sell indicator"),
        f(72, "Trading & Markt", "Trading & market", "Preisverlauf manuell speichern", "Manual price history"),
        f(73, "Trading & Markt", "Trading & market", "Warframe.market-Link pro Item", "Warframe.market link per item"),
        f(74, "Trading & Markt", "Trading & market", "Mindestpreis-Warnung", "Minimum price warning"),
        f(75, "Trading & Markt", "Trading & market", "Baro-Verkaufsassistent", "Baro selling assistant"),
        f(76, "Trading & Markt", "Trading & market", "Behalten oder verkaufen-Empfehlung", "Keep or sell recommendation"),
        f(77, "Trading & Markt", "Trading & market", "Inventarwert-Schätzung", "Inventory value estimate"),
        f(78, "Trading & Markt", "Trading & market", "Raritätsfilter", "Rarity filter"),
        f(79, "Trading & Markt", "Trading & market", "Duplikate fürs Trading markieren", "Mark duplicates for trading"),
        f(80, "Trading & Markt", "Trading & market", "Exportbare Trading-Liste", "Exportable trading list"),
        f(81, "Progression", "Progression", "Mastery-Rank-Rechner", "Mastery rank calculator"),
        f(82, "Progression", "Progression", "MR-XP pro Kategorie", "MR XP per category"),
        f(83, "Progression", "Progression", "Level-Tracker für Waffen/Frames", "Level tracker for weapons/frames"),
        f(84, "Progression", "Progression", "Forma-Zähler pro Item", "Forma counter per item"),
        f(85, "Progression", "Progression", "Catalyst/Reactor-Tracker", "Catalyst/Reactor tracker"),
        f(86, "Progression", "Progression", "Exilus-Adapter-Tracker", "Exilus adapter tracker"),
        f(87, "Progression", "Progression", "Archon-Shard-Planer", "Archon shard planner"),
        f(88, "Progression", "Progression", "Fokus-Punkte-Tracker", "Focus points tracker"),
        f(89, "Progression", "Progression", "Quest-Fortschritt", "Quest progress"),
        f(90, "Progression", "Progression", "Junction-Checkliste", "Junction checklist"),
        f(91, "Progression", "Progression", "Star-Chart-Fortschritt", "Star chart progress"),
        f(92, "Progression", "Progression", "Steel-Path-Fortschritt", "Steel Path progress"),
        f(93, "Progression", "Progression", "Intrinsics-Tracker", "Intrinsics tracker"),
        f(94, "Progression", "Progression", "Syndikat-Rangplaner", "Syndicate rank planner"),
        f(95, "Progression", "Progression", "Standing-Tageslimit-Rechner", "Standing daily cap calculator"),
        f(96, "Builds & Loadouts", "Builds & loadouts", "Loadout-Vorlagen", "Loadout templates"),
        f(97, "Builds & Loadouts", "Builds & loadouts", "Builds pro Warframe", "Builds per Warframe"),
        f(98, "Builds & Loadouts", "Builds & loadouts", "Builds pro Waffe", "Builds per weapon"),
        f(99, "Builds & Loadouts", "Builds & loadouts", "Mod-Ränge speichern", "Save mod ranks"),
        f(100, "Builds & Loadouts", "Builds & loadouts", "Arkanes speichern", "Save arcanes"),
        f(101, "Builds & Loadouts", "Builds & loadouts", "Helminth-Fähigkeit speichern", "Save Helminth ability"),
        f(102, "Builds & Loadouts", "Builds & loadouts", "Fokus-Schule pro Build", "Focus school per build"),
        f(103, "Builds & Loadouts", "Builds & loadouts", "Einsatzzweck pro Build", "Build purpose"),
        f(104, "Builds & Loadouts", "Builds & loadouts", "Lieblingsbuild markieren", "Mark favorite build"),
        f(105, "Builds & Loadouts", "Builds & loadouts", "Build-Notizen", "Build notes"),
        f(106, "Builds & Loadouts", "Builds & loadouts", "Build kopieren", "Copy build"),
        f(107, "Builds & Loadouts", "Builds & loadouts", "Build exportieren", "Export build"),
        f(108, "Builds & Loadouts", "Builds & loadouts", "Build importieren", "Import build"),
        f(109, "Builds & Loadouts", "Builds & loadouts", "Screenshot zu Build speichern", "Save screenshot to build"),
        f(110, "Builds & Loadouts", "Builds & loadouts", "Unvollständiger Build-Warnung", "Incomplete build warning"),
        f(111, "Qualität & Bedienung", "Quality & UX", "Bessere Tablet-Ansicht", "Better tablet layout"),
        f(112, "Qualität & Bedienung", "Quality & UX", "Kompakter Modus", "Compact mode"),
        f(113, "Qualität & Bedienung", "Quality & UX", "Größere Schrift Option", "Large text option"),
        f(114, "Qualität & Bedienung", "Quality & UX", "Farbschema-Auswahl", "Color scheme selection"),
        f(115, "Qualität & Bedienung", "Quality & UX", "App-Suche über alles", "Search across app"),
        f(116, "Qualität & Bedienung", "Quality & UX", "Schnellaktionen im Menü", "Quick actions in menu"),
        f(117, "Qualität & Bedienung", "Quality & UX", "Favoriten oben anpinnen", "Pin favorites to top"),
        f(118, "Qualität & Bedienung", "Quality & UX", "Letzte Seite merken", "Remember last page"),
        f(119, "Qualität & Bedienung", "Quality & UX", "Bessere leere Zustände", "Better empty states"),
        f(120, "Qualität & Bedienung", "Quality & UX", "Lade-Skeletons", "Loading skeletons"),
        f(121, "Qualität & Bedienung", "Quality & UX", "Fehlerseite mit Lösungsvorschlägen", "Error page with suggestions"),
        f(122, "Qualität & Bedienung", "Quality & UX", "Offline-Modus-Schalter", "Offline mode switch"),
        f(123, "Qualität & Bedienung", "Quality & UX", "Lokale Datenprüfung", "Local data validation"),
        f(124, "Qualität & Bedienung", "Quality & UX", "Backup-Erinnerung", "Backup reminder"),
        f(125, "Qualität & Bedienung", "Quality & UX", "Änderungsverlauf anzeigen", "Show change history"),
        f(126, "Technik & Release", "Tech & release", "Android-Benachrichtigungen mit WorkManager", "Android notifications with WorkManager"),
        f(127, "Technik & Release", "Tech & release", "NotificationChannel pro Typ", "NotificationChannel per type"),
        f(128, "Technik & Release", "Tech & release", "Homescreen-Widget", "Home screen widget"),
        f(129, "Technik & Release", "Tech & release", "Splashscreen professioneller machen", "Improve splash screen"),
        f(130, "Technik & Release", "Tech & release", "Eigenes App-Icon", "Custom app icon"),
        f(131, "Technik & Release", "Tech & release", "Version/Buildnummer in Einstellungen", "Version/build number in settings"),
        f(132, "Technik & Release", "Tech & release", "Datenschutz-Seite", "Privacy page"),
        f(133, "Technik & Release", "Tech & release", "Crash-Schutz mit Fehlerprotokoll", "Crash guard with error log"),
        f(134, "Technik & Release", "Tech & release", "Lokale Datenmigrationen", "Local data migrations"),
        f(135, "Technik & Release", "Tech & release", "Automatische Backup-Dateinamen mit Datum", "Automatic dated backup filenames"),
        f(136, "Technik & Release", "Tech & release", "Import-Vorschau vor Überschreiben", "Import preview before overwrite"),
        f(137, "Technik & Release", "Tech & release", "Testdaten-Modus", "Test data mode"),
        f(138, "Technik & Release", "Tech & release", "Mehr UI-Tests", "More UI tests"),
        f(139, "Technik & Release", "Tech & release", "Release-Checkliste in der App", "Release checklist in app"),
        f(140, "Technik & Release", "Tech & release", "Signierter Release-Build", "Signed release build")
    )
}

private data class FarmRecommendation(
    val item: WarframeItem,
    val component: ComponentItem,
    val activeFissure: FissureResponse?,
    val priority: WatchPriority,
    val reason: String,
    val score: Int
)

private fun sessionKey(recommendation: FarmRecommendation): String {
    return "${recommendation.item.name}|${recommendation.component.name}"
}

private data class WatchPriority(
    val key: String,
    val germanLabel: String,
    val englishLabel: String,
    val score: Int
) {
    fun label(german: Boolean): String = if (german) germanLabel else englishLabel

    companion object {
        val Now = WatchPriority("now", "Jetzt farmen", "Farm now", 70)
        val Later = WatchPriority("later", "Später", "Later", 25)
        val Ignore = WatchPriority("ignore", "Ignorieren", "Ignore", -500)
        val visibleChoices = listOf(Now, Later, Ignore)
        private val all = visibleChoices.associateBy { it.key }

        fun fromKey(key: String?): WatchPriority = all[key] ?: Later
    }
}

private fun buildFarmRecommendations(
    items: List<WarframeItem>,
    fissuresData: List<FissureResponse>,
    favorites: Set<String>,
    priorityMap: Map<String, String>,
    german: Boolean
): List<FarmRecommendation> {
    val activeFissures = fissuresData.filter { fissure ->
        fissure.isStorm != true &&
            fissure.isHard != true &&
            !fissure.expiry.isNullOrBlank() &&
            !isExpired(fissure.expiry)
    }

    return items
        .filter { item ->
            val priority = priorityFor(item.name, priorityMap)
            priority != WatchPriority.Ignore && (favorites.isEmpty() || item.name in favorites)
        }
        .flatMap { item ->
            val checkedCount = item.components.count { it.checked }
            val missingCount = item.components.count { !it.checked }
            val priority = priorityFor(item.name, priorityMap)
            item.components.filterNot { it.checked }.map { component ->
                val matchingFissure = activeFissures.firstOrNull { fissure ->
                    relicTier(component.relic)?.let { tier ->
                        fissure.tier.equals(tier, ignoreCase = true)
                    } == true
                }
                val score = farmScore(
                    priority = priority,
                    activeFissure = matchingFissure != null,
                    checkedCount = checkedCount,
                    missingCount = missingCount,
                    hasRelic = component.relic.isNotBlank()
                )
                FarmRecommendation(
                    item = item,
                    component = component,
                    activeFissure = matchingFissure,
                    priority = priority,
                    reason = recommendationReason(
                        activeFissure = matchingFissure != null,
                        missingCount = missingCount,
                        hasRelic = component.relic.isNotBlank(),
                        priority = priority,
                        german = german
                    ),
                    score = score
                )
            }
        }
        .sortedWith(
            compareByDescending<FarmRecommendation> { it.score }
                .thenBy { it.item.name }
                .thenBy { it.component.name }
        )
}

private fun farmScore(
    priority: WatchPriority,
    activeFissure: Boolean,
    checkedCount: Int,
    missingCount: Int,
    hasRelic: Boolean
): Int {
    var score = checkedCount * 6
    score += priority.score
    if (activeFissure) score += 35
    if (missingCount == 1) score += 24
    if (hasRelic) score += 8
    return score
}

private fun relicTier(relic: String): String? {
    return relic
        .trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        ?.takeIf { it.isNotBlank() }
}

private fun recommendationReason(
    activeFissure: Boolean,
    missingCount: Int,
    hasRelic: Boolean,
    priority: WatchPriority,
    german: Boolean
): String {
    return when {
        priority == WatchPriority.Now -> if (german) "Von dir als Top-Priorität markiert" else "Marked as top priority"
        activeFissure -> if (german) "Aktive Void-Riss-Mission" else "Active Void Fissure"
        missingCount == 1 -> if (german) "Nur noch ein Teil fehlt" else "Only one part missing"
        hasRelic -> if (german) "Relikt bekannt" else "Known relic"
        else -> if (german) "Offenes Teil" else "Open part"
    }
}

private fun priorityFor(itemName: String, priorityMap: Map<String, String>): WatchPriority {
    return WatchPriority.fromKey(priorityMap[itemName])
}

private fun loadPriorityMap(prefs: SharedPreferences): Map<String, String> {
    return prefs.getStringSet("favorite_priorities", emptySet())
        .orEmpty()
        .mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank()) parts[0] to parts[1] else null
        }
        .toMap()
}

private fun savePriorityMap(prefs: SharedPreferences, priorities: Map<String, String>) {
    val stored = priorities.map { (name, priority) -> "$name|$priority" }.toSet()
    prefs.edit().putStringSet("favorite_priorities", stored).apply()
}

private fun estimateDucats(value: String): Int {
    return value
        .lines()
        .filter { it.isNotBlank() }
        .map { line ->
            val lower = line.lowercase()
            when {
                "rare" in lower || "gold" in lower || "selten" in lower -> 100
                "uncommon" in lower || "silver" in lower || "silber" in lower -> 45
                "bronze" in lower || "common" in lower -> 15
                else -> 45
            }
        }
        .sum()
}

internal fun masteryXpForItem(item: WarframeItem): Int = when (item.type.lowercase()) {
    "warframe", "companion" -> 6_000
    "weapon" -> 3_000
    else -> 0
}

private fun buildPurposes(): List<String> = listOf("steel_path", "farming", "support", "eidolon", "general")

private fun buildPurposeLabel(key: String, german: Boolean): String = when (key) {
    "steel_path" -> if (german) "Stahlpfad" else "Steel Path"
    "farming" -> if (german) "Farmen" else "Farming"
    "support" -> "Support"
    "eidolon" -> "Eidolon"
    "general" -> if (german) "Allgemein" else "General"
    else -> key
}

private fun progressionCategories(): List<String> = listOf("quests", "star_chart", "steel_path", "intrinsics", "custom")

private fun progressionCategoryLabel(key: String, german: Boolean): String = when (key) {
    "quests" -> if (german) "Quests" else "Quests"
    "star_chart" -> if (german) "Sternenkarte" else "Star Chart"
    "steel_path" -> if (german) "Stahlpfad" else "Steel Path"
    "intrinsics" -> "Intrinsics"
    "custom" -> if (german) "Eigenes Ziel" else "Custom goal"
    else -> key
}

private fun estimatePlatinum(value: String): Int {
    return Regex("""\b(\d{1,5})\s*(p|pt|platinum)?\b""", RegexOption.IGNORE_CASE)
        .findAll(value)
        .sumOf { match -> match.groupValues[1].toIntOrNull() ?: 0 }
}

private fun localNoteCount(values: List<String>): Int {
    return values.count { it.isNotBlank() }
}

private fun isExpired(expiry: String): Boolean {
    return runCatching {
        Instant.now().isAfter(Instant.parse(expiry))
    }.getOrDefault(true)
}
