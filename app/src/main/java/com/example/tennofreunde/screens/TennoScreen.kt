package com.example.tennofreunde.screens

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.IconButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tennofreunde.components.WarframeList
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.WarframeItem
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import com.example.tennofreunde.screens.UpdateDialog
import com.example.tennofreunde.screens.FinishedScreen
import androidx.compose.material3.NavigationDrawerItemDefaults
import com.example.tennofreunde.ui.theme.AppBrushes
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.toMutableStateList
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.api.WarframeApi
import kotlinx.coroutines.delay
import com.example.tennofreunde.data.CollectionSeedCatalog
import com.example.tennofreunde.data.AssetCollectionCatalog
import com.example.tennofreunde.data.PrimeCatalog
import com.example.tennofreunde.data.ParsedTennoBackup
import com.example.tennofreunde.data.PortablePreferences
import com.example.tennofreunde.data.TennoBackup
import com.example.tennofreunde.data.TennoBackupCodec
import com.example.tennofreunde.data.CloudRestoreExtras
import com.example.tennofreunde.data.cloudProgressFromDocument
import com.example.tennofreunde.data.decodeCloudExtras
import com.example.tennofreunde.data.encodeCloudExtras
import com.example.tennofreunde.data.mergeCloudExtras
import com.example.tennofreunde.data.RemoteCollectionCatalog
import com.example.tennofreunde.data.LatestPrimeCatalog
import com.example.tennofreunde.data.SharedCollectionItem
import com.example.tennofreunde.data.decodeSharedCollectionItems
import com.example.tennofreunde.data.encodeSharedCollectionItems
import com.example.tennofreunde.data.mergeSharedCollectionItems
import com.example.tennofreunde.data.WarframeAcquisitionCatalog
import com.example.tennofreunde.data.WeaponRelicLoader
import com.example.tennofreunde.data.WeaponGenerator
import com.example.tennofreunde.data.CollectionPlacementRules
import com.example.tennofreunde.data.ScannerAddedItem
import com.example.tennofreunde.data.analyzeCloudProgress
import com.example.tennofreunde.data.applyCloudProgress
import com.example.tennofreunde.data.preserveLocalCollection
import com.example.tennofreunde.BuildConfig
import com.example.tennofreunde.ScreenshotScannerScreen
import com.example.tennofreunde.R
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.system.TennoSystem

private fun cloudFailureMessage(error: Throwable, german: Boolean): String {
    val firestore = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    val code = firestore?.code
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> if (german) {
            "Firebase hat den Zugriff abgelehnt. Bitte die Firestore-Regeln für dein Konto prüfen. (PERMISSION_DENIED)"
        } else "Firebase denied access. Check the Firestore rules for your account. (PERMISSION_DENIED)"
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> if (german) {
            "Deine Google-Anmeldung ist abgelaufen. Bitte abmelden und erneut anmelden."
        } else "Your Google sign-in expired. Sign out and sign in again."
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
        FirebaseFirestoreException.Code.INVALID_ARGUMENT -> if (german) {
            "Die Cloud-Sicherung war zu groß oder ungültig. Die App verwendet nun das kompakte Sicherungsformat."
        } else "The cloud backup was too large or invalid. The app now uses the compact backup format."
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> if (german) {
            "Firebase ist gerade nicht erreichbar. Bitte Internetverbindung prüfen und erneut versuchen."
        } else "Firebase is unavailable. Check the internet connection and try again."
        else -> if (german) {
            "Cloud-Sicherung fehlgeschlagen${code?.let { " (${it.name})" }.orEmpty()}."
        } else "Cloud backup failed${code?.let { " (${it.name})" }.orEmpty()}."
    }
}

private fun capturePortablePreferences(
    hub: SharedPreferences,
    settings: SharedPreferences
): PortablePreferences {
    fun isPortableHubKey(key: String): Boolean = listOf(
        "baro_soon_", "baro_active_", "eidolon_night_", "backup_", "farm_"
    ).none(key::startsWith)

    fun strings(source: SharedPreferences, filter: (String) -> Boolean = { true }) = source.all
        .mapNotNull { (key, value) -> (value as? String)?.takeIf { filter(key) }?.let { key to it } }
        .toMap()
    fun booleans(source: SharedPreferences, filter: (String) -> Boolean = { true }) = source.all
        .mapNotNull { (key, value) -> (value as? Boolean)?.takeIf { filter(key) }?.let { key to it } }
        .toMap()
    val hubSets = hub.all.mapNotNull { (key, value) ->
        if (!isPortableHubKey(key)) return@mapNotNull null
        val set = value as? Set<*> ?: return@mapNotNull null
        key to set.filterIsInstance<String>()
    }.toMap()
    return PortablePreferences(
        hubStrings = strings(hub, ::isPortableHubKey),
        hubStringSets = hubSets,
        hubBooleans = booleans(hub, ::isPortableHubKey),
        settingsStrings = strings(settings),
        settingsBooleans = booleans(settings)
    )
}

private fun applyPortablePreferences(
    hub: SharedPreferences,
    settings: SharedPreferences,
    snapshot: PortablePreferences,
    mergeSets: Boolean
) {
    hub.edit().apply {
        snapshot.hubStrings.forEach(::putString)
        snapshot.hubBooleans.forEach(::putBoolean)
        snapshot.hubStringSets.forEach { (key, values) ->
            val restored = if (mergeSets) hub.getStringSet(key, emptySet()).orEmpty() + values else values.toSet()
            putStringSet(key, restored)
        }
    }.apply()
    settings.edit().apply {
        snapshot.settingsStrings.forEach(::putString)
        snapshot.settingsBooleans.forEach(::putBoolean)
    }.apply()
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TennoScreen(
    darkMode: Boolean,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    largeText: Boolean,
    onLargeTextChange: (Boolean) -> Unit,
    offlineMode: Boolean,
    onOfflineModeChange: (Boolean) -> Unit,
    compactMode: Boolean,
    onCompactModeChange: (Boolean) -> Unit,
    colorStyle: String,
    onColorStyleChange: (String) -> Unit
) {


    var updateTitle by remember {
        mutableStateOf("")
    }

    var updateMessage by remember {
        mutableStateOf("")
    }

    var updateUrl by remember {
        mutableStateOf("")
    }


    val context = LocalContext.current

    val gson = Gson()

    val db = Firebase.firestore

    val sharedPreferences = context.getSharedPreferences(
        "tenno_data",
        Context.MODE_PRIVATE
    )
    val appSettings = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }
    val configuredStartPage = remember {
        appSettings.getString("start_page", "home") ?: "home"
    }
    val hubPreferences = remember {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
    }
    var accountAutoSync by remember {
        mutableStateOf(appSettings.getBoolean("account_auto_sync", true))
    }
    val initialPage = remember {
        if (hubPreferences.getBoolean("remember_last_page", true)) {
            hubPreferences.getString("last_page", configuredStartPage) ?: configuredStartPage
        } else configuredStartPage
    }
    var activeProfile by rememberSaveable {
        mutableStateOf(sharedPreferences.getString("active_profile", "Tenno") ?: "Tenno")
    }


    val items = remember {
        mutableStateListOf<WarframeItem>()
    }

    fun saveLocalProgress() {

        val progressMap = mutableMapOf<String, Boolean>()

        items.forEach { item ->

            item.components.forEach { component ->

                val key =
                    "${item.name}_${component.name}"

                progressMap[key] = component.checked


            }
        }

        val json = gson.toJson(progressMap)

        sharedPreferences
            .edit()
            .putString("local_progress_$activeProfile", json)
            .apply()

        Firebase.auth.currentUser?.uid?.takeIf { accountAutoSync }?.let { uid ->

            db.collection("user_progress")
                .document("${uid}_$activeProfile")
                .set(
                    mapOf(
                        "formatVersion" to 2,
                        "appVersion" to BuildConfig.VERSION_NAME,
                        "updatedAt" to System.currentTimeMillis(),
                        "progress" to progressMap
                    ),
                    SetOptions.merge()
                )
        }

        sharedPreferences.edit().putString(
            "scanner_added_items",
            gson.toJson(items.filter { it.isNew }.map(ScannerAddedItem::from))
        ).apply()
    }

    fun loadProfileProgress(profile: String) {
        val json = sharedPreferences.getString("local_progress_$profile", null)
        val progress: Map<String, Boolean> = if (json.isNullOrBlank()) emptyMap() else runCatching {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            gson.fromJson<Map<String, Boolean>>(json, type)
        }.getOrDefault(emptyMap())
        items.forEach { item ->
            item.components.forEachIndexed { index, component ->
                item.components[index] = component.copy(
                    checked = progress["${item.name}_${component.name}"] ?: false
                )
            }
        }
    }


    val auth = Firebase.auth

    var selectedTab by remember {
        mutableIntStateOf(0)
    }
    var tabReorderMenuIndex by remember {
        mutableStateOf<Int?>(null)
    }
    var subTabReorderMenuIndex by remember {
        mutableStateOf<Int?>(null)
    }

    var currentUser by remember {
        mutableStateOf(auth.currentUser)
    }
    var accountSyncing by remember { mutableStateOf(false) }
    var lastAccountSyncAt by remember {
        mutableLongStateOf(appSettings.getLong("last_account_sync_at_$activeProfile", 0L))
    }
    var lastLocalBackupAt by remember {
        mutableLongStateOf(appSettings.getLong("last_backup_at", 0L))
    }
    var pendingCloudProgress by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var pendingCloudExtras by remember { mutableStateOf(CloudRestoreExtras()) }
    var showCloudRestorePreview by rememberSaveable { mutableStateOf(false) }
    var showCloudDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(activeProfile) {
        lastAccountSyncAt = appSettings.getLong("last_account_sync_at_$activeProfile", 0L)
    }

    var selectedSubTab by remember {
        mutableIntStateOf(0)
    }


    val subTabs = remember {
        mutableStateListOf<SubTabItem>()
    }


    var sortAZ by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var collectionCategory by rememberSaveable { mutableStateOf("all") }
    var onlyMissing by rememberSaveable { mutableStateOf(true) }
    var onlyAlmostDone by rememberSaveable { mutableStateOf(false) }
    var onlyFavorites by rememberSaveable { mutableStateOf(false) }
    var collectionFiltersExpanded by rememberSaveable { mutableStateOf(false) }
    var favoriteNames by remember { mutableStateOf(context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getStringSet("favorites", emptySet()) ?: emptySet()) }
    var archivedNames by remember { mutableStateOf(sharedPreferences.getStringSet("archived_items", emptySet()) ?: emptySet()) }
    var changeLog by remember { mutableStateOf(sharedPreferences.getStringSet("change_log", emptySet()) ?: emptySet()) }

    var fissuresData by remember {
        mutableStateOf<List<FissureResponse>>(emptyList())
    }

    var fabExpanded by remember {
        mutableStateOf(false)
    }

    var showAddTabDialog by remember {

        mutableStateOf(false)
    }

    var newTabName by remember {

        mutableStateOf("")
    }


    var showAddSubTabDialog by remember {
        mutableStateOf(false)
    }

    var newSubTabName by remember {
        mutableStateOf("")
    }

    var showFinishedScreen by rememberSaveable {
        mutableStateOf(initialPage == "finished")
    }

    var showLiveScreen by rememberSaveable {
        mutableStateOf(initialPage == "home")
    }

    var showScreenshotScanner by rememberSaveable {
        mutableStateOf(initialPage == "scanner")
    }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    var showTennoHub by rememberSaveable { mutableStateOf(initialPage == "hub") }
    var showAccountScreen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showLiveScreen, showTennoHub, showFinishedScreen, showScreenshotScanner, showSettingsScreen, showAccountScreen) {
        if (showSettingsScreen || showAccountScreen || !hubPreferences.getBoolean("remember_last_page", true)) return@LaunchedEffect
        val page = when {
            showLiveScreen -> "home"
            showTennoHub -> "hub"
            showFinishedScreen -> "finished"
            showScreenshotScanner -> "scanner"
            else -> "collection"
        }
        hubPreferences.edit().putString("last_page", page).apply()
    }

    fun addChangeLog(message: String) {
        val stamped = "${System.currentTimeMillis()}|$message"
        val next = (changeLog + stamped).sortedDescending().take(30).toSet()
        changeLog = next
        sharedPreferences.edit().putStringSet("change_log", next).apply()
    }

    var showUpdateDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteTabDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteSubTabDialog by remember {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    val itemSaveRevision = remember { AtomicInteger(0) }

    val tabs = remember {
        mutableStateListOf<TabItem>()
    }
    val currentVersion = BuildConfig.VERSION_NAME
    var pendingImportItems by remember { mutableStateOf<List<WarframeItem>>(emptyList()) }
    var pendingBackup by remember { mutableStateOf<ParsedTennoBackup?>(null) }
    var hasImportRecovery by remember { mutableStateOf(sharedPreferences.contains("import_recovery_backup")) }
    var showImportPreview by remember { mutableStateOf(false) }

    fun createFullBackup(): TennoBackup {
        val progressType = object : TypeToken<Map<String, Boolean>>() {}.type
        val progressByProfile = sharedPreferences.all
            .filterKeys { it.startsWith("local_progress_") }
            .mapNotNull { (key, value) ->
                val profile = key.removePrefix("local_progress_")
                val progress = (value as? String)?.let { stored ->
                    runCatching { gson.fromJson<Map<String, Boolean>>(stored, progressType) }.getOrNull()
                }
                if (profile.isBlank() || progress == null) null else profile to progress
            }.toMap().toMutableMap()
        progressByProfile[activeProfile] = items.flatMap { item ->
            item.components.map { component -> "${item.name}_${component.name}" to component.checked }
        }.toMap()

        val scannerType = object : TypeToken<List<ScannerAddedItem>>() {}.type
        val scannerItems = sharedPreferences.getString("scanner_added_items", null)?.let { stored ->
            runCatching { gson.fromJson<List<ScannerAddedItem>>(stored, scannerType) }.getOrNull()
        }.orEmpty()
        return TennoBackup(
            appVersion = BuildConfig.VERSION_NAME,
            exportedAt = System.currentTimeMillis(),
            activeProfile = activeProfile,
            profiles = hubPreferences.getStringSet("profiles", setOf(activeProfile)).orEmpty().toList(),
            items = items.toList(),
            tabs = tabs.toList(),
            subTabs = subTabs.toList(),
            progressByProfile = progressByProfile,
            favorites = hubPreferences.getStringSet("favorites", emptySet()).orEmpty().toList(),
            scannerAddedItems = scannerItems,
            preferences = capturePortablePreferences(hubPreferences, appSettings)
        )
    }



    val exportLauncher = rememberLauncherForActivityResult(

        contract =
            ActivityResultContracts.CreateDocument(
                "application/json"
            )

    ) { uri ->

        uri?.let {

            try {

                val json = TennoBackupCodec.encode(gson, createFullBackup())

                val written = context.contentResolver
                    .openOutputStream(it)
                    ?.use { output ->

                        output.write(
                            json.toByteArray()
                        )
                        true
                    } ?: false
                if (written) {
                    TennoSystem.markBackupCreated(context)
                    lastLocalBackupAt = System.currentTimeMillis()
                }

            } catch (e: Exception) {
                TennoSystem.appendCrashLog(context, e)
                Toast.makeText(
                    context,
                    if (language == AppLanguage.GERMAN) "Sicherung konnte nicht gespeichert werden." else "Backup could not be saved.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(

        contract = OpenDocument()

    ) { uri ->

        uri?.let {

            try {

                val json =
                    context.contentResolver
                        .openInputStream(it)
                        ?.bufferedReader()
                        ?.use { reader ->
                            reader.readText()
                        }

                if (json != null) {

                    val parsed = TennoBackupCodec.decode(gson, json)
                    pendingBackup = parsed
                    pendingImportItems = parsed.items
                    showImportPreview = true


                }

            } catch (e: Exception) {
                TennoSystem.appendCrashLog(context, e)
                Toast.makeText(
                    context,
                    if (language == AppLanguage.GERMAN) "Die Sicherungsdatei konnte nicht gelesen werden." else "The backup file could not be read.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    val credentialManager = remember { CredentialManager.create(context) }

    fun startGoogleSignIn() {
        if (accountSyncing) return
        accountSyncing = true
        scope.launch {
            try {
                val googleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = context.getString(R.string.default_web_client_id)
                ).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val customCredential = result.credential as? CustomCredential
                require(customCredential?.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    "Unsupported Google credential"
                }
                val googleCredential = GoogleIdTokenCredential.createFrom(customCredential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { authResult ->
                        accountSyncing = false
                        if (authResult.isSuccessful) {
                            currentUser = auth.currentUser
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.GERMAN) "Erfolgreich angemeldet." else "Signed in successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            authResult.exception?.let { TennoSystem.appendCrashLog(context, it) }
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.GERMAN) "Google-Anmeldung fehlgeschlagen. Prüfe Internet und Google-Konto." else "Google sign-in failed. Check your connection and Google account.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } catch (_: GetCredentialCancellationException) {
                accountSyncing = false
                Toast.makeText(
                    context,
                    if (language == AppLanguage.GERMAN) "Google-Anmeldung abgebrochen." else "Google sign-in cancelled.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                accountSyncing = false
                TennoSystem.appendCrashLog(context, e)
                Toast.makeText(
                    context,
                    if (language == AppLanguage.GERMAN) "Google-Anmeldung fehlgeschlagen. Versuche es erneut." else "Google sign-in failed. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun saveItems() {
        val snapshot = items.toList()
        val revision = itemSaveRevision.incrementAndGet()
        scope.launch(Dispatchers.IO) {
            val json = gson.toJson(snapshot)
            if (itemSaveRevision.get() == revision) {
                sharedPreferences
                    .edit()
                    .putString("warframe_items", json)
                    .apply()
            }
        }
    }


    fun saveTabs() {

        val json = gson.toJson(tabs)

        sharedPreferences
            .edit()
            .putString("tab_data", json)
            .apply()

        val firebaseTabs = tabs.map { tab ->

            hashMapOf(
                "name" to tab.name
            )
        }

        db.collection("tabs")
            .document("shared_tabs")
            .set(hashMapOf("data" to firebaseTabs))
    }


    fun saveSubTabs() {

        sharedPreferences
            .edit()
            .putString("subtab_data", gson.toJson(subTabs))
            .apply()

        val firebaseSubTabs = subTabs.toList().map { subTab ->

            hashMapOf(

                "name" to subTab.name,

                "parentTab" to subTab.parentTab
            )
        }

        db.collection("subtabs")
            .document("shared_subtabs")
            .set(hashMapOf("data" to firebaseSubTabs))
    }

    fun syncSharedCollectionCatalog(candidates: List<WarframeItem>) {
        val records = candidates
            .asSequence()
            .filter { it.name.isNotBlank() && !it.name.equals("Neuer Eintrag", ignoreCase = true) }
            .filter {
                it.catalogSource in setOf(
                    "wfcd", "wfcd_latest", "scanner_discovered", "bundled", "shared_catalog"
                )
            }
            .map(SharedCollectionItem::from)
            .distinctBy { CollectionPlacementRules.normalizedKey(it.name) }
            .toList()
        if (records.isNotEmpty()) {
            val reference = db.collection("items").document("shared_items")
            db.runTransaction { transaction ->
                val stored = decodeSharedCollectionItems(
                    gson,
                    transaction.get(reference).getString("catalogPayloadGzip")
                )
                val merged = mergeSharedCollectionItems(stored, records)
                if (merged != stored) {
                    transaction.set(
                        reference,
                        mapOf(
                            "catalogFormatVersion" to 1,
                            "catalogUpdatedAt" to System.currentTimeMillis(),
                            "catalogItemCount" to merged.size,
                            "catalogPayloadGzip" to encodeSharedCollectionItems(gson, merged)
                        ),
                        SetOptions.merge()
                    )
                }
            }.addOnSuccessListener {
                sharedPreferences.edit()
                    .putLong("shared_catalog_last_upload", System.currentTimeMillis())
                    .apply()
            }.addOnFailureListener { TennoSystem.appendCrashLog(context, it) }
        }
    }

    fun normalizeCollectionPlacement(): Boolean {
        var changed = false

        tabs.forEach { tab ->
            val canonicalName = CollectionPlacementRules.canonicalTabName(tab.name)
            if (tab.name != canonicalName) {
                tab.name = canonicalName
                changed = true
            }
        }

        items.forEach { item ->
            changed = CollectionPlacementRules.applyTo(item) || changed
        }

        subTabs.forEach { subTab ->
            val placement = CollectionPlacementRules.forSubTab(subTab.parentTab, subTab.name)
            if (subTab.parentTab != placement.tabName || subTab.name != placement.subTabName) {
                subTab.parentTab = placement.tabName
                subTab.name = placement.subTabName
                changed = true
            }
        }

        val uniqueTabs = mutableSetOf<String>()
        if (tabs.removeAll { !uniqueTabs.add(CollectionPlacementRules.normalizedKey(it.name)) }) {
            changed = true
        }

        subTabs.forEach { subTab ->
            if (tabs.none { it.name.equals(subTab.parentTab, ignoreCase = true) }) {
                tabs.add(TabItem(subTab.parentTab))
                changed = true
            }
        }

        CollectionPlacementRules.defaultWeaponSubTabs.forEach { weaponSubTab ->
            if (subTabs.none {
                    CollectionPlacementRules.sameKey(it.parentTab, "Waffen") &&
                        CollectionPlacementRules.sameKey(it.name, weaponSubTab)
                }
            ) {
                subTabs.add(SubTabItem(weaponSubTab, "Waffen"))
                changed = true
            }
        }

        CollectionPlacementRules.defaultResourceSubTabs.forEach { resourceSubTab ->
            if (subTabs.none {
                    CollectionPlacementRules.sameKey(it.parentTab, "Ressourcen") &&
                        CollectionPlacementRules.sameKey(it.name, resourceSubTab)
                }
            ) {
                subTabs.add(SubTabItem(resourceSubTab, "Ressourcen"))
                changed = true
            }
        }

        CollectionPlacementRules.defaultModSubTabs.forEach { modSubTab ->
            if (subTabs.none {
                    CollectionPlacementRules.sameKey(it.parentTab, "Mods") &&
                        CollectionPlacementRules.sameKey(it.name, modSubTab)
                }
            ) {
                subTabs.add(SubTabItem(modSubTab, "Mods"))
                changed = true
            }
        }

        val uniqueSubTabs = mutableSetOf<Pair<String, String>>()
        if (subTabs.removeAll {
                !uniqueSubTabs.add(
                    CollectionPlacementRules.normalizedKey(it.parentTab) to
                        CollectionPlacementRules.normalizedKey(it.name)
                )
            }
        ) {
            changed = true
        }

        items.forEach { item ->
            if (tabs.none { it.name.equals(item.tabName, ignoreCase = true) }) {
                tabs.add(TabItem(item.tabName))
                changed = true
            }
            if (item.subTabName.isNotBlank() &&
                subTabs.none {
                    it.name.equals(item.subTabName, ignoreCase = true) &&
                        it.parentTab.equals(item.tabName, ignoreCase = true)
                }
            ) {
                subTabs.add(SubTabItem(item.subTabName, item.tabName))
                changed = true
            }
        }

        val usedTabs = items.map { CollectionPlacementRules.normalizedKey(it.tabName) }.toSet()
        val usedSubTabs = items.map {
            CollectionPlacementRules.normalizedKey(it.tabName) to
                CollectionPlacementRules.normalizedKey(it.subTabName)
        }.toSet()
        val removedWrongTabs = tabs.removeAll {
            (it.name.equals("Warframe", ignoreCase = true) ||
                it.name.equals("Warframes", ignoreCase = true)) &&
                CollectionPlacementRules.normalizedKey(it.name) !in usedTabs
        }
        val removedWrongSubTabs = subTabs.removeAll {
            it.parentTab.equals("Warframe", ignoreCase = true) ||
                it.parentTab.equals("Warframes", ignoreCase = true) ||
                (
                    it.parentTab.equals("Waffen", ignoreCase = true) &&
                        it.name.equals("Prime Waffen", ignoreCase = true) &&
                        (
                            CollectionPlacementRules.normalizedKey(it.parentTab) to
                                CollectionPlacementRules.normalizedKey(it.name)
                            ) !in usedSubTabs
                    )
        }

        return changed || removedWrongTabs || removedWrongSubTabs
    }

    fun mergeCatalogItems(catalogItems: List<WarframeItem>): Boolean {
        var changed = false
        val existingByName = items.associateByTo(linkedMapOf()) {
            CollectionPlacementRules.normalizedKey(it.name)
        }
        val additions = mutableListOf<WarframeItem>()
        catalogItems.forEach { catalogItem ->
            val normalizedName = CollectionPlacementRules.normalizedKey(catalogItem.name)
            val existing = existingByName[normalizedName]
            if (existing == null) {
                additions.add(catalogItem)
                existingByName[normalizedName] = catalogItem
                changed = true
            } else {
                catalogItem.components.forEach { catalogComponent ->
                    if (existing.components.none {
                            CollectionPlacementRules.sameKey(it.name, catalogComponent.name)
                        }
                    ) {
                        existing.components.add(catalogComponent.copy(checked = false))
                        changed = true
                    }
                }
                if (existing.catalogSource == "scanner_discovered" &&
                    catalogItem.catalogSource in setOf("wfcd", "wfcd_latest", "shared_catalog")
                ) {
                    existing.type = catalogItem.type
                    existing.tabName = catalogItem.tabName
                    existing.subTabName = catalogItem.subTabName
                    existing.imageName = catalogItem.imageName.ifBlank { existing.imageName }
                    existing.catalogSource = catalogItem.catalogSource
                    changed = true
                }
            }
        }
        if (additions.isNotEmpty()) items.addAll(additions)

        return changed
    }

    fun moveMainTab(fromIndex: Int, offset: Int) {
        if (fromIndex !in tabs.indices) return
        val toIndex = (fromIndex + offset).coerceIn(tabs.indices)
        if (fromIndex == toIndex) return

        val tab = tabs.removeAt(fromIndex)
        tabs.add(toIndex, tab)
        selectedTab = toIndex
        selectedSubTab = 0
        tabReorderMenuIndex = null
        saveTabs()
    }

    fun moveSubTab(parentTab: String, fromIndex: Int, offset: Int) {
        val siblings = subTabs.filter { it.parentTab.equals(parentTab, ignoreCase = true) }
        if (fromIndex !in siblings.indices) return

        val toIndex = (fromIndex + offset).coerceIn(siblings.indices)
        if (fromIndex == toIndex) return

        val reordered = siblings.toMutableList()
        val moved = reordered.removeAt(fromIndex)
        reordered.add(toIndex, moved)

        var nextSiblingIndex = 0
        subTabs.forEachIndexed { index, subTab ->
            if (subTab.parentTab.equals(parentTab, ignoreCase = true)) {
                subTabs[index] = reordered[nextSiblingIndex]
                nextSiblingIndex++
            }
        }

        selectedSubTab = toIndex
        subTabReorderMenuIndex = null
        saveSubTabs()
    }

    LaunchedEffect(Unit) {
        val (cachedItems, cachedNavigation) = withContext(Dispatchers.IO) {
            val storedItems = sharedPreferences.getString("warframe_items", null)?.let { json ->
                runCatching {
                    val type = object : TypeToken<MutableList<WarframeItem>>() {}.type
                    gson.fromJson<MutableList<WarframeItem>>(json, type)
                }.getOrNull()
            }.orEmpty()
            val storedTabs = sharedPreferences.getString("tab_data", null)?.let { json ->
                runCatching {
                    val type = object : TypeToken<MutableList<TabItem>>() {}.type
                    gson.fromJson<MutableList<TabItem>>(json, type)
                }.getOrNull()
            }.orEmpty()
            val storedSubTabs = sharedPreferences.getString("subtab_data", null)?.let { json ->
                runCatching {
                    val type = object : TypeToken<MutableList<SubTabItem>>() {}.type
                    gson.fromJson<MutableList<SubTabItem>>(json, type)
                }.getOrNull()
            }.orEmpty()
            storedItems to (storedTabs to storedSubTabs)
        }
        val (cachedTabs, cachedSubTabs) = cachedNavigation

        if (items.isEmpty()) items.addAll(cachedItems)
        if (tabs.isEmpty()) tabs.addAll(cachedTabs)
        if (subTabs.isEmpty()) subTabs.addAll(cachedSubTabs)
        if (items.isEmpty()) items.addAll(CollectionSeedCatalog.items())
        val builtInPrimeItems = withContext(Dispatchers.IO) {
            runCatching { PrimeCatalog.load(context).map { it.item } }
                .getOrDefault(emptyList())
        }
        val builtInAssetItems = withContext(Dispatchers.IO) {
            runCatching { AssetCollectionCatalog.load(context) }
                .getOrDefault(emptyList())
        }
        val builtInCatalogAdded = mergeCatalogItems(builtInPrimeItems) or
            mergeCatalogItems(builtInAssetItems)
        normalizeCollectionPlacement()
        if (builtInCatalogAdded || cachedItems.isEmpty()) {
            saveItems()
            saveTabs()
            saveSubTabs()
        }
        syncSharedCollectionCatalog(items)

        val (remoteItems, latestCollectionItems) = withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val remote = runCatching {
                RemoteCollectionCatalog.load(client)
            }.getOrDefault(emptyList())
            val latest = runCatching {
                LatestPrimeCatalog.load(context).map { it.item }
            }.getOrDefault(emptyList())
            remote to latest
        }

        val onlineCatalogItems = remoteItems + latestCollectionItems
        if (onlineCatalogItems.isNotEmpty()) {
            val onlineCatalogAdded = mergeCatalogItems(onlineCatalogItems)
            if (normalizeCollectionPlacement() || onlineCatalogAdded) {
                saveItems()
                saveTabs()
                saveSubTabs()
            }
            syncSharedCollectionCatalog(onlineCatalogItems)
        }

        val acquisitionSources = withContext(Dispatchers.IO) {
            runCatching {
                WarframeAcquisitionCatalog.load(OkHttpClient())
            }.getOrDefault(emptyMap())
        }
        if (acquisitionSources.isNotEmpty()) {
            val acquisitionsAdded = items.fold(false) { anyChanged, item ->
                WarframeAcquisitionCatalog.enrich(item, acquisitionSources) || anyChanged
            }
            if (acquisitionsAdded) {
                normalizeCollectionPlacement()
                saveItems()
            }
        }
    }

    LaunchedEffect(Unit) {

        db.collection("subtabs")
            .document("shared_subtabs")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val data = value?.get("data").asStringAnyMaps()
                if (!data.isNullOrEmpty()) {

                    data.forEach { map ->
                        val placement = CollectionPlacementRules.forSubTab(
                            map["parentTab"].toString(),
                            map["name"].toString()
                        )
                        if (subTabs.none {
                                CollectionPlacementRules.sameKey(it.parentTab, placement.tabName) &&
                                    CollectionPlacementRules.sameKey(it.name, placement.subTabName)
                            }
                        ) subTabs.add(SubTabItem(placement.subTabName, placement.tabName))
                    }

                }
            }
    }

    LaunchedEffect(Unit) {

        db.collection("tabs")
            .document("shared_tabs")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val data = value?.get("data").asStringAnyMaps()

                if (!data.isNullOrEmpty()) {

                    data.forEach { map ->
                        val tabName = CollectionPlacementRules.canonicalTabName(map["name"].toString())
                        if (tabs.none { CollectionPlacementRules.sameKey(it.name, tabName) }) {
                            tabs.add(TabItem(name = tabName))
                        }
                    }
                }
            }
    }







    LaunchedEffect(Unit) {

        db.collection("items")
            .document("shared_items")
            .addSnapshotListener { value, error ->


                if (error != null) {
                    return@addSnapshotListener
                }

                val sharedCatalogItems = decodeSharedCollectionItems(
                    gson,
                    value?.getString("catalogPayloadGzip")
                ).map { it.toWarframeItem() }
                if (sharedCatalogItems.isNotEmpty()) {
                    sharedPreferences.edit()
                        .putLong("shared_catalog_last_receive", System.currentTimeMillis())
                        .apply()

                    val sharedCatalogAdded = mergeCatalogItems(sharedCatalogItems)
                    if (normalizeCollectionPlacement() || sharedCatalogAdded) {
                        saveItems()
                        saveTabs()
                        saveSubTabs()
                    }

                    // The compressed catalog supersedes the very large legacy `data` array.
                    // Parsing both on every snapshot blocked the UI long enough to trigger ANRs.
                    return@addSnapshotListener
                }

                val data = value?.get("data").asStringAnyMaps()
                if (!data.isNullOrEmpty()) {

                    val protectedLocalItems = items.toList()
                    items.clear()

                    val existingNames = mutableSetOf<String>()

                    val storedProgressJson = sharedPreferences.getString(
                        "local_progress_$activeProfile",
                        sharedPreferences.getString("local_progress", null)
                    )
                    val storedProgress: Map<String, Boolean> = if (storedProgressJson.isNullOrBlank()) {
                        emptyMap()
                    } else {
                        runCatching {
                            val progressType = object : TypeToken<Map<String, Boolean>>() {}.type
                            gson.fromJson<Map<String, Boolean>>(storedProgressJson, progressType)
                        }.getOrDefault(emptyMap())
                    }

                    val weaponEntries =
                        WeaponRelicLoader.loadWeapons(context)

                    val generatedWeapons =
                        WeaponGenerator.generateWeapons(
                            weaponEntries
                        )
                    generatedWeapons.forEach { weapon ->

                        if (!existingNames.contains(weapon.name)) {

                            existingNames.add(weapon.name)

                            items.add(

                                WarframeItem(

                                    name = weapon.name,

                                    type = "weapon",

                                    tabName = "Waffen",

                                    subTabName = weapon.category,

                                    infoFields = mutableStateListOf(),

                                    components =
                                        weapon.components
                                            .toMutableStateList(),

                                    isNew = false
                                )
                            )
                        }
                    }


                    data.forEach { map ->

                        val infoFields =
                            mutableListOf<InfoField>()

                        val infoList = map["infoFields"].asStringAnyMaps()

                        infoList?.forEach { info ->

                            infoFields.add(
                                InfoField(
                                    title =
                                        info["title"].toString(),

                                    value =
                                        info["value"].toString()
                                )
                            )
                        }

                        val components =
                            mutableListOf<ComponentItem>()

                        val componentList = map["components"].asStringAnyMaps()

                        componentList?.forEach { component ->

                            val componentName =
                                component["name"].toString()

                            val key = "${map["name"]}_${componentName}"
                            val checkedState = storedProgress[key] ?: false

                            components.add(

                                ComponentItem(

                                    name = componentName,

                                    checked = checkedState,

                                    farmLocation =
                                        component["farmLocation"]?.toString() ?: "",

                                    relic =
                                        component["relic"]?.toString() ?: "",

                                    rotation =
                                        component["rotation"]?.toString() ?: "",

                                    activeMission =
                                        component["activeMission"]?.toString() ?: ""
                                )
                            )
                        }


                        val itemName = map["name"].toString()

                        if (!existingNames.contains(itemName)) {

                            existingNames.add(itemName)

                            items.add(

                                WarframeItem(

                                    name = itemName,

                                    type = map["type"]?.toString() ?: "warframe",

                                    tabName =
                                        map["tabName"].toString(),

                                    subTabName =
                                        map["subTabName"]?.toString() ?: "",

                                    infoFields = infoFields,

                                    components = components.toMutableStateList(),

                                    isNew = false,

                                    imageName =
                                        map["imageName"]?.toString() ?: "",

                                    catalogSource =
                                        map["catalogSource"]?.toString() ?: "shared"
                                )

                            )
                        }

                    }

                    preserveLocalCollection(items, protectedLocalItems)

                    var collectionSeedsAdded = false
                    collectionSeedsAdded = mergeCatalogItems(CollectionSeedCatalog.items())
                    val sharedCatalogAdded = mergeCatalogItems(sharedCatalogItems)

                    val scannerJson = sharedPreferences.getString("scanner_added_items", null)
                    if (!scannerJson.isNullOrBlank()) {
                        val scannerType = object : TypeToken<List<ScannerAddedItem>>() {}.type
                        val scannerItems: List<ScannerAddedItem> = runCatching {
                            gson.fromJson<List<ScannerAddedItem>>(scannerJson, scannerType)
                        }.getOrDefault(emptyList())
                        scannerItems.forEach { saved ->
                            val restored = saved.toWarframeItem()
                            if (existingNames.add(restored.name)) items.add(restored)
                            if (tabs.none { it.name.equals(restored.tabName, ignoreCase = true) }) {
                                tabs.add(TabItem(restored.tabName))
                            }
                            if (restored.subTabName.isNotBlank() &&
                                subTabs.none {
                                    it.name.equals(restored.subTabName, ignoreCase = true) &&
                                        it.parentTab.equals(restored.tabName, ignoreCase = true)
                                }
                            ) {
                                subTabs.add(SubTabItem(restored.subTabName, restored.tabName))
                            }
                        }
                    }

                    if (normalizeCollectionPlacement() || collectionSeedsAdded || sharedCatalogAdded) {
                        saveLocalProgress()
                        saveItems()
                        saveTabs()
                        saveSubTabs()
                    }

                }
            }
    }

    LaunchedEffect(Unit) {

        withContext(Dispatchers.IO) {

            try {

                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("https://api.github.com/repos/hahawasistlos1/TennoFreunde/releases/latest")
                    .build()

                val response =
                    client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext

                val json =
                    JSONObject(response.body?.string() ?: "")

                val latestTag =
                    json.optString("tag_name")

                val releaseTitle =
                    json.optString("name").ifBlank { latestTag }

                val latestVersion =
                    releaseVersionNumber(latestTag.ifBlank { releaseTitle })

                if (
                    latestVersion.isNotBlank() &&
                    isRemoteVersionNewer(latestVersion, currentVersion)
                ) {
                    val apkUrl = githubApkDownloadUrl(json)
                    updateUrl =
                        apkUrl ?: json.optString("html_url")

                    updateMessage =
                        githubUpdateMessage(
                            body = json.optString("body"),
                            hasDirectApk = apkUrl != null,
                            german = language == AppLanguage.GERMAN
                        )

                    updateTitle = releaseTitle.ifBlank { latestTag }

                    showUpdateDialog = true
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            try {

                fissuresData =
                    WarframeApi.api.getFissures()

            } catch (e: Exception) {

                e.printStackTrace()
            }

            delay(120000)
        }
    }

    BackHandler(enabled = drawerState.isOpen || fabExpanded || showAddTabDialog ||
        showAddSubTabDialog || showDeleteTabDialog || showDeleteSubTabDialog ||
        showImportPreview || showCloudRestorePreview || showCloudDeleteConfirm || showFinishedScreen || showScreenshotScanner || showSettingsScreen || showAccountScreen || showTennoHub || !showLiveScreen) {

        when {

            drawerState.isOpen -> {

                scope.launch {
                    drawerState.close()
                }
            }

            fabExpanded -> {

                fabExpanded = false
            }

            showAddTabDialog -> {

                showAddTabDialog = false
            }
            showAddSubTabDialog -> showAddSubTabDialog = false
            showDeleteTabDialog -> showDeleteTabDialog = false
            showDeleteSubTabDialog -> showDeleteSubTabDialog = false
            showImportPreview -> {
                pendingImportItems = emptyList()
                pendingBackup = null
                showImportPreview = false
            }
            showCloudRestorePreview -> {
                pendingCloudProgress = emptyMap()
                pendingCloudExtras = CloudRestoreExtras()
                showCloudRestorePreview = false
            }
            showCloudDeleteConfirm -> showCloudDeleteConfirm = false
            else -> {
                showFinishedScreen = false
                showScreenshotScanner = false
                showSettingsScreen = false
                showAccountScreen = false
                showTennoHub = false
                showLiveScreen = true
                searchText = ""
            }
        }
    }


    if (showImportPreview) {
        AlertDialog(
            containerColor = AppColors.Card,
            shape = AppShapes.Large,
            titleContentColor = AppColors.TextPrimary,
            textContentColor = AppColors.TextSecondary,
            onDismissRequest = { showImportPreview = false },
            title = { Text(if (language == AppLanguage.GERMAN) "Import prüfen" else "Review import") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (language == AppLanguage.GERMAN) {
                            "Die Datei enthält ${pendingImportItems.size} Einträge. Deine aktuelle Sammlung mit ${items.size} Einträgen wird erst nach Bestätigung ersetzt."
                        } else {
                            "The file contains ${pendingImportItems.size} items. Your current collection with ${items.size} items will only be replaced after confirmation."
                        }
                    )
                    Text(
                        pendingImportItems.take(5).joinToString("\n") { it.name }.ifBlank {
                            if (language == AppLanguage.GERMAN) "Keine Einträge erkannt." else "No items detected."
                        },
                        color = AppColors.OrokinGold
                    )
                    pendingBackup?.takeIf { !it.isLegacy }?.let { backup ->
                        Text(
                            if (language == AppLanguage.GERMAN) {
                                "Vollständige Sicherung: ${backup.profiles.size} Profil(e), ${backup.tabs.size} Haupttabs, ${backup.favorites.size} Favorit(en)."
                            } else {
                                "Full backup: ${backup.profiles.size} profile(s), ${backup.tabs.size} main tabs, ${backup.favorites.size} favorite(s)."
                            },
                            color = AppColors.EnergyCyan
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val backup = pendingBackup
                        sharedPreferences.edit()
                            .putString("import_recovery_backup", TennoBackupCodec.encode(gson, createFullBackup()))
                            .apply()
                        hasImportRecovery = true
                        items.clear()
                        items.addAll(pendingImportItems)
                        if (backup != null && !backup.isLegacy) {
                            if (backup.tabs.isNotEmpty()) {
                                tabs.clear()
                                tabs.addAll(backup.tabs)
                            }
                            if (backup.subTabs.isNotEmpty()) {
                                subTabs.clear()
                                subTabs.addAll(backup.subTabs)
                            }
                            val progressEditor = sharedPreferences.edit()
                            backup.progressByProfile.forEach { (profile, progress) ->
                                progressEditor.putString("local_progress_$profile", gson.toJson(progress))
                            }
                            progressEditor
                                .putString("active_profile", backup.activeProfile)
                                .putString("scanner_added_items", gson.toJson(backup.scannerAddedItems))
                                .apply()
                            hubPreferences.edit()
                                .putStringSet("profiles", backup.profiles.toSet())
                                .putStringSet("favorites", backup.favorites.toSet())
                                .apply()
                            applyPortablePreferences(
                                hubPreferences,
                                appSettings,
                                backup.preferences,
                                mergeSets = false
                            )
                            activeProfile = backup.activeProfile
                            favoriteNames = backup.favorites.toSet()
                        }
                        normalizeCollectionPlacement()
                        pendingImportItems = emptyList()
                        pendingBackup = null
                        showImportPreview = false
                        saveLocalProgress()
                        saveItems()
                        saveTabs()
                        saveSubTabs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                    shape = AppShapes.Large
                ) {
                    Text(if (language == AppLanguage.GERMAN) "Import übernehmen" else "Apply import")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingImportItems = emptyList()
                        pendingBackup = null
                        showImportPreview = false
                    },
                    shape = AppShapes.Large
                ) {
                    Text(if (language == AppLanguage.GERMAN) "Abbrechen" else "Cancel")
                }
            }
        )
    }

    if (showCloudRestorePreview) {
        val restoreSummary = analyzeCloudProgress(items, pendingCloudProgress)
        AlertDialog(
            containerColor = AppColors.Card,
            shape = AppShapes.Large,
            onDismissRequest = {
                pendingCloudProgress = emptyMap()
                pendingCloudExtras = CloudRestoreExtras()
                showCloudRestorePreview = false
            },
            title = { Text(if (language == AppLanguage.GERMAN) "Cloud-Sicherung prüfen" else "Review cloud backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (language == AppLanguage.GERMAN) {
                            "Lokal abgehakt: ${restoreSummary.localChecked}\nIn der Cloud abgehakt: ${restoreSummary.cloudChecked}\nBekannte Komponenten: ${restoreSummary.matchingComponents}\nTatsächliche Änderungen: ${restoreSummary.changedComponents}\nUnbekannte Cloud-Einträge: ${restoreSummary.unknownCloudEntries}\n\nNur bekannte Komponenten werden übernommen. Neuere lokale Einträge bleiben erhalten."
                        } else {
                            "Checked locally: ${restoreSummary.localChecked}\nChecked in cloud: ${restoreSummary.cloudChecked}\nKnown components: ${restoreSummary.matchingComponents}\nActual changes: ${restoreSummary.changedComponents}\nUnknown cloud entries: ${restoreSummary.unknownCloudEntries}\n\nOnly known components are applied. Newer local entries remain available."
                        },
                        color = AppColors.TextSecondary
                    )
                    if (pendingCloudExtras.hasFullBackup) {
                        Text(
                            if (language == AppLanguage.GERMAN) {
                                "Vollständige Cloud-Sicherung: ${pendingCloudExtras.profiles.size} Profil(e), ${pendingCloudExtras.tabs.size} Tabs, ${pendingCloudExtras.favorites.size} Favorit(en), ${pendingCloudExtras.scannerAddedItems.size} Scanner-Einträge sowie Hub-Werkzeuge und Einstellungen."
                            } else {
                                "Full cloud backup: ${pendingCloudExtras.profiles.size} profile(s), ${pendingCloudExtras.tabs.size} tabs, ${pendingCloudExtras.favorites.size} favorite(s), ${pendingCloudExtras.scannerAddedItems.size} scanner item(s)."
                            },
                            color = AppColors.EnergyCyan
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pendingCloudExtras.hasFullBackup) {
                            val mergeResult = mergeCloudExtras(
                                items = items,
                                tabs = tabs,
                                subTabs = subTabs,
                                localProfiles = hubPreferences.getStringSet("profiles", setOf(activeProfile)).orEmpty(),
                                localFavorites = favoriteNames,
                                extras = pendingCloudExtras
                            )
                            hubPreferences.edit()
                                .putStringSet("profiles", mergeResult.profiles)
                                .putStringSet("favorites", mergeResult.favorites)
                                .apply()
                            applyPortablePreferences(
                                hubPreferences,
                                appSettings,
                                pendingCloudExtras.preferences,
                                mergeSets = true
                            )
                            favoriteNames = mergeResult.favorites
                            val profileProgressEditor = sharedPreferences.edit()
                            pendingCloudExtras.progressByProfile.forEach { (profile, progress) ->
                                profileProgressEditor.putString("local_progress_$profile", gson.toJson(progress))
                            }
                            profileProgressEditor.apply()
                            normalizeCollectionPlacement()
                            saveItems()
                            saveTabs()
                            saveSubTabs()
                            syncSharedCollectionCatalog(
                                items.filter { it.catalogSource == "scanner_discovered" }
                            )
                            Firebase.auth.currentUser?.uid?.takeIf { accountAutoSync }?.let { uid ->
                                db.collection("user_progress")
                                    .document("${uid}_$activeProfile")
                                    .set(
                                        mapOf(
                                            "formatVersion" to 3,
                                            "appVersion" to BuildConfig.VERSION_NAME,
                                            "updatedAt" to System.currentTimeMillis(),
                                            "scannerAddedItemsJson" to gson.toJson(
                                                items.filter { it.isNew }.map(ScannerAddedItem::from)
                                            ),
                                            "tabsJson" to gson.toJson(tabs.toList()),
                                            "subTabsJson" to gson.toJson(subTabs.toList())
                                        ),
                                        SetOptions.merge()
                                    )
                                    .addOnFailureListener { TennoSystem.appendCrashLog(context, it) }
                            }
                        }
                        val changed = applyCloudProgress(items, pendingCloudProgress)
                        saveItems()
                        pendingCloudProgress = emptyMap()
                        pendingCloudExtras = CloudRestoreExtras()
                        showCloudRestorePreview = false
                        saveLocalProgress()
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.GERMAN) "$changed Komponenten aktualisiert." else "$changed components updated.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                    shape = AppShapes.Large
                ) { Text(if (language == AppLanguage.GERMAN) "Wiederherstellen" else "Restore") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingCloudProgress = emptyMap()
                        pendingCloudExtras = CloudRestoreExtras()
                        showCloudRestorePreview = false
                    },
                    shape = AppShapes.Large
                ) { Text(if (language == AppLanguage.GERMAN) "Abbrechen" else "Cancel") }
            }
        )
    }

    if (showCloudDeleteConfirm) {
        AlertDialog(
            containerColor = AppColors.Card,
            shape = AppShapes.Large,
            onDismissRequest = { showCloudDeleteConfirm = false },
            title = { Text(if (language == AppLanguage.GERMAN) "Cloud-Sicherung löschen?" else "Delete cloud backup?") },
            text = {
                Text(
                    if (language == AppLanguage.GERMAN) {
                        "Die Cloud-Sicherung für das Profil „$activeProfile“ wird gelöscht. Deine Sammlung und dein Fortschritt auf diesem Gerät bleiben erhalten."
                    } else {
                        "The cloud backup for profile “$activeProfile” will be deleted. Your collection and progress on this device remain available."
                    },
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloudDeleteConfirm = false
                        val user = currentUser
                        if (user != null) {
                            accountSyncing = true
                            db.collection("user_progress")
                                .document("${user.uid}_$activeProfile")
                                .delete()
                                .addOnSuccessListener {
                                    accountSyncing = false
                                    lastAccountSyncAt = 0L
                                    appSettings.edit().remove("last_account_sync_at_$activeProfile").apply()
                                    Toast.makeText(
                                        context,
                                        if (language == AppLanguage.GERMAN) "Cloud-Sicherung gelöscht. Lokale Daten bleiben erhalten." else "Cloud backup deleted. Local data remains available.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { error ->
                                    accountSyncing = false
                                    TennoSystem.appendCrashLog(context, error)
                                    Toast.makeText(
                                        context,
                                        if (language == AppLanguage.GERMAN) "Cloud-Sicherung konnte nicht gelöscht werden." else "Cloud backup could not be deleted.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                    shape = AppShapes.Large
                ) { Text(if (language == AppLanguage.GERMAN) "Cloud-Daten löschen" else "Delete cloud data") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCloudDeleteConfirm = false }, shape = AppShapes.Large) {
                    Text(if (language == AppLanguage.GERMAN) "Abbrechen" else "Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet(

                drawerContainerColor = AppColors.BackgroundTop,
                drawerContentColor = AppColors.TextPrimary,

                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .verticalScroll(rememberScrollState())
                    .background(
                        brush = AppBrushes.MainBackground
                    )
            ){

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(

                    text = "TENNOFREUNDE",

                    style = MaterialTheme.typography.headlineMedium,

                    color = AppColors.OrokinGold,

                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(

                    text =

                        if (currentUser != null)
                            currentUser?.email ?: ""
                        else
                            if (language == AppLanguage.GERMAN) "Nicht angemeldet" else "Not signed in",

                    color = AppColors.TextSecondary,


                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    if (language == AppLanguage.GERMAN) "NAVIGATION" else "NAVIGATION",
                    color = AppColors.EnergyCyan,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, null, tint = AppColors.AccentBlue) },
                    label = { Text(if (language == AppLanguage.GERMAN) "Hauptseite" else "Home") },
                    selected = showLiveScreen,
                    onClick = {
                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showSettingsScreen = false
                        showAccountScreen = false
                        showTennoHub = false
                        showLiveScreen = true
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = AppColors.Card,
                        unselectedTextColor = AppColors.TextPrimary,
                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = AppShapes.Large
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DashboardCustomize, null, tint = AppColors.AccentBlue) },
                    label = { Text(if (language == AppLanguage.GERMAN) "Tenno-Zentrale" else "Tenno hub") },
                    selected = showTennoHub,
                    onClick = {
                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showSettingsScreen = false
                        showAccountScreen = false
                        showLiveScreen = false
                        showTennoHub = true
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = AppColors.Card,
                        unselectedTextColor = AppColors.TextPrimary,
                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.Settings, null, tint = AppColors.AccentBlue) },
                    label = { Text(if (language == AppLanguage.GERMAN) "Einstellungen" else "Settings") },
                    selected = showSettingsScreen,
                    onClick = {
                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showLiveScreen = false
                        showSettingsScreen = true
                        showAccountScreen = false
                        showTennoHub = false
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = AppColors.Card,
                        unselectedTextColor = AppColors.TextPrimary,
                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.DoneAll, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Fertige Sachen" else "Completed items")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = true
                        showScreenshotScanner = false
                        showSettingsScreen = false
                        showAccountScreen = false
                        showTennoHub = false

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.DocumentScanner, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Screenshot-Scanner" else "Screenshot scanner")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = false
                        showLiveScreen = false
                        showScreenshotScanner = true
                        showSettingsScreen = false
                        showAccountScreen = false
                        showTennoHub = false

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.Inventory2, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Sammlung" else "Collection")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showSettingsScreen = false
                        showAccountScreen = false
                        showTennoHub = false
                        showLiveScreen = false

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.AutoMirrored.Filled.Login, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Konto & Cloud" else "Account & cloud")
                    },

                    selected = showAccountScreen,

                    onClick = {
                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showSettingsScreen = false
                        showTennoHub = false
                        showLiveScreen = false
                        showAccountScreen = true
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.SortByAlpha, null, tint = AppColors.AccentBlue) },
                    label = {

                        Text(

                            if (sortAZ)
                                if (language == AppLanguage.GERMAN) "A-Z Sortierung AN" else "A-Z sorting ON"
                            else
                                if (language == AppLanguage.GERMAN) "A-Z Sortierung AUS" else "A-Z sorting OFF"
                        )
                    },

                    selected = sortAZ,

                    onClick = {
                        sortAZ = !sortAZ
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.FileDownload, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Importieren" else "Import")
                    },

                    selected = false,

                    onClick = {

                        importLauncher.launch(
                            arrayOf("application/json")
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    icon = { Icon(Icons.Default.FileUpload, null, tint = AppColors.AccentBlue) },
                    label = {
                        Text(if (language == AppLanguage.GERMAN) "Exportieren" else "Export")
                    },

                    selected = false,

                    onClick = {

                        exportLauncher.launch(
                            buildBackupFileName()
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )
            }
        }
    ) {

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().background(AppColors.BackgroundTop).statusBarsPadding()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Seitenmenü öffnen", tint = AppColors.AccentBlue)
                    }
                    Text(
                        when {
                            showFinishedScreen -> "Fertige Sachen"
                            showScreenshotScanner -> "Screenshot-Scanner"
                            showSettingsScreen -> if (language == AppLanguage.GERMAN) "Einstellungen" else "Settings"
                            showAccountScreen -> if (language == AppLanguage.GERMAN) "Konto & Cloud" else "Account & cloud"
                            showTennoHub -> if (language == AppLanguage.GERMAN) "Tenno-Zentrale" else "Tenno hub"
                            showLiveScreen -> "Übersicht"
                            else -> if (language == AppLanguage.GERMAN) "Sammlung" else "Collection"
                        },
                        modifier = Modifier.weight(1f),
                        color = AppColors.TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (showFinishedScreen || showScreenshotScanner || showSettingsScreen || showAccountScreen || showTennoHub || !showLiveScreen) {
                        IconButton(onClick = {
                            showFinishedScreen = false
                            showScreenshotScanner = false
                            showSettingsScreen = false
                            showAccountScreen = false
                            showTennoHub = false
                            showLiveScreen = true
                            fabExpanded = false
                            searchText = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück zur Hauptseite", tint = AppColors.AccentBlue)
                        }
                    }

                }
            },

            floatingActionButton = {

                if (!showLiveScreen && !showFinishedScreen && !showScreenshotScanner && !showSettingsScreen && !showAccountScreen && !showTennoHub) {

                    Box {

                        FloatingActionButton(

                            onClick = {
                                fabExpanded = !fabExpanded
                            },

                            containerColor = AppColors.Card,

                            contentColor = AppColors.Accent,

                            shape = AppShapes.Large,

                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 10.dp
                            ),

                            modifier = Modifier

                                .padding(bottom = 8.dp)

                                .border(
                                    width = 1.dp,
                                    color = AppColors.OrokinGold.copy(alpha = 0.75f),
                                    shape = AppShapes.Large
                                )
                        ) {

                            Icon(

                                imageVector = Icons.Default.Add,

                                contentDescription = if (language == AppLanguage.GERMAN) "Sammlung bearbeiten" else "Edit collection",

                                modifier = Modifier.size(30.dp)
                            )
                        }

                        DropdownMenu(

                            expanded = fabExpanded,

                            onDismissRequest = {
                                fabExpanded = false
                            },

                            modifier = Modifier
                                .background(
                                    AppColors.Card,
                                    AppShapes.Large
                                )
                        ) {


                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Tab hinzufügen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, tint = AppColors.EnergyCyan) },

                                onClick = {

                                    newTabName = ""

                                    showAddTabDialog = true

                                    fabExpanded = false
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Eintrag hinzufügen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, null, tint = AppColors.EnergyCyan) },

                                onClick = {

                                    if (tabs.isNotEmpty()) {


                                        val newItem = WarframeItem(

                                            name = "Neuer Eintrag",

                                            type = "warframe",

                                            tabName = tabs[selectedTab].name,

                                            subTabName =
                                                subTabs
                                                    .filter {
                                                        it.parentTab == tabs[selectedTab].name
                                                    }
                                                    .getOrNull(selectedSubTab)
                                                    ?.name ?: "",

                                            infoFields = mutableListOf(),

                                            components = mutableStateListOf(),

                                            isNew = true
                                        )


                                        items.add(0, newItem)

                                        saveItems()
                                    }

                                    fabExpanded = false
                                }
                            )


                            DropdownMenuItem(

                                text = {
                                    Text("Untertab hinzufügen", color = AppColors.TextPrimary)
                                },

                                leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, tint = AppColors.EnergyCyan) },

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        newSubTabName = ""

                                        showAddSubTabDialog = true
                                    }

                                    fabExpanded = false
                                }
                            )
                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Untertab entfernen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFFF9B8F)) },

                                onClick = {

                                    showDeleteSubTabDialog = true

                                    fabExpanded = false
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Tab entfernen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFFF9B8F)) },

                                onClick = {

                                    showDeleteTabDialog = true

                                    fabExpanded = false
                                }
                            )

                        }
                    }
                }
            }


        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = AppBrushes.MainBackground
                    )
                    .padding(paddingValues)
            ) {
                Image(
                    painter = painterResource(R.drawable.warframe_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.14f
                )

                if (showFinishedScreen) {

                    FinishedScreen(

                        items = items,

                        onBack = {
                            showFinishedScreen = false
                            showScreenshotScanner = false
                            showSettingsScreen = false
                            showAccountScreen = false
                            showLiveScreen = true
                        },

                        onResetItem = { item ->

                            item.components.forEachIndexed { index, component ->
                                item.components[index] = component.copy(checked = false)
                            }

                            saveLocalProgress()
                        },
                        onResetComponent = { item, componentIndex ->
                            item.components.getOrNull(componentIndex)?.let { component ->
                                item.components[componentIndex] = component.copy(checked = false)
                            }
                            saveLocalProgress()
                        }
                    )

                } else if (showScreenshotScanner) {

                    ScreenshotScannerScreen(
                        items = items,
                        language = language,
                        onProgressChanged = { saveLocalProgress() },
                        onCatalogItemsAdded = { addedItems ->
                            addedItems.forEach(CollectionPlacementRules::applyTo)
                            normalizeCollectionPlacement()
                            addedItems.forEach { added ->
                                if (tabs.none { it.name.equals(added.tabName, ignoreCase = true) }) {
                                    tabs.add(TabItem(added.tabName))
                                }
                                if (added.subTabName.isNotBlank() &&
                                    subTabs.none {
                                        it.name.equals(added.subTabName, ignoreCase = true) &&
                                            it.parentTab.equals(added.tabName, ignoreCase = true)
                                    }
                                ) {
                                    subTabs.add(SubTabItem(added.subTabName, added.tabName))
                                }
                            }
                            saveItems()
                            saveTabs()
                            saveSubTabs()
                            syncSharedCollectionCatalog(addedItems)
                        }
                    )

                } else if (showSettingsScreen) {

                    SettingsScreen(language, onLanguageChange, darkMode, onDarkModeChange, largeText, onLargeTextChange, offlineMode, onOfflineModeChange, compactMode, onCompactModeChange, colorStyle, onColorStyleChange)

                } else if (showAccountScreen) {

                    AccountScreen(
                        language = language,
                        signedIn = currentUser != null,
                        displayName = currentUser?.displayName,
                        email = currentUser?.email,
                        activeProfile = activeProfile,
                        itemCount = items.size,
                        completedComponents = items.sumOf { item -> item.components.count { it.checked } },
                        totalComponents = items.sumOf { it.components.size },
                        syncing = accountSyncing,
                        lastSyncAt = lastAccountSyncAt,
                        lastLocalBackupAt = lastLocalBackupAt,
                        hasImportRecovery = hasImportRecovery,
                        autoSyncEnabled = accountAutoSync,
                        onSignIn = { startGoogleSignIn() },
                        onSignOut = {
                            auth.signOut()
                            scope.launch {
                                runCatching {
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                }.onFailure { TennoSystem.appendCrashLog(context, it) }
                                currentUser = null
                                Toast.makeText(
                                    context,
                                    if (language == AppLanguage.GERMAN) "Sicher abgemeldet. Lokale Daten bleiben erhalten." else "Signed out safely. Local data remains available.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onSyncNow = {
                            val user = currentUser
                            if (user != null && !accountSyncing) {
                                accountSyncing = true
                                val progressMap = buildMap<String, Boolean> {
                                    items.forEach { item ->
                                        item.components.forEach { component ->
                                            put("${item.name}_${component.name}", component.checked)
                                        }
                                    }
                                }
                                val scannerItems = items.filter { it.isNew }.map(ScannerAddedItem::from)
                                val fullSnapshot = createFullBackup()
                                val cloudExtras = CloudRestoreExtras(
                                    formatVersion = 3,
                                    profiles = fullSnapshot.profiles,
                                    favorites = fullSnapshot.favorites,
                                    tabs = fullSnapshot.tabs,
                                    subTabs = fullSnapshot.subTabs,
                                    scannerAddedItems = scannerItems,
                                    progressByProfile = fullSnapshot.progressByProfile,
                                    preferences = fullSnapshot.preferences ?: PortablePreferences()
                                )
                                val cloudDocument = mapOf(
                                    "formatVersion" to 3,
                                    "appVersion" to BuildConfig.VERSION_NAME,
                                    "updatedAt" to System.currentTimeMillis(),
                                    "activeProfile" to activeProfile,
                                    "backupPayloadGzip" to encodeCloudExtras(gson, cloudExtras),
                                    "progress" to progressMap
                                )
                                db.collection("user_progress")
                                    .document("${user.uid}_$activeProfile")
                                    .set(cloudDocument)
                                    .addOnSuccessListener {
                                        accountSyncing = false
                                        lastAccountSyncAt = System.currentTimeMillis()
                                        appSettings.edit().putLong("last_account_sync_at_$activeProfile", lastAccountSyncAt).apply()
                                        Toast.makeText(
                                            context,
                                            if (language == AppLanguage.GERMAN) "Cloud-Sicherung erfolgreich." else "Cloud backup completed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { error ->
                                        accountSyncing = false
                                        TennoSystem.appendCrashLog(context, error)
                                        Toast.makeText(
                                            context,
                                            cloudFailureMessage(error, language == AppLanguage.GERMAN),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        onRestoreFromCloud = {
                            val user = currentUser
                            if (user != null && !accountSyncing) {
                                accountSyncing = true
                                db.collection("user_progress")
                                    .document("${user.uid}_$activeProfile")
                                    .get()
                                    .addOnSuccessListener { document ->
                                        accountSyncing = false
                                        val documentData = document.data
                                        val cloudProgress = cloudProgressFromDocument(documentData)
                                        if (cloudProgress.isNullOrEmpty()) {
                                            Toast.makeText(
                                                context,
                                                if (language == AppLanguage.GERMAN) "Für dieses Profil wurde noch keine Cloud-Sicherung gefunden." else "No cloud backup was found for this profile.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val tabType = object : TypeToken<List<TabItem>>() {}.type
                                            val subTabType = object : TypeToken<List<SubTabItem>>() {}.type
                                            val scannerType = object : TypeToken<List<ScannerAddedItem>>() {}.type
                                            val profileProgressType = object : TypeToken<Map<String, Map<String, Boolean>>>() {}.type
                                            val legacyExtras = CloudRestoreExtras(
                                                formatVersion = (documentData?.get("formatVersion") as? Number)?.toInt() ?: 1,
                                                profiles = (documentData?.get("profiles") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                                                favorites = (documentData?.get("favorites") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                                                tabs = (documentData?.get("tabsJson") as? String)?.let { stored ->
                                                    runCatching { gson.fromJson<List<TabItem>>(stored, tabType) }.getOrNull()
                                                }.orEmpty(),
                                                subTabs = (documentData?.get("subTabsJson") as? String)?.let { stored ->
                                                    runCatching { gson.fromJson<List<SubTabItem>>(stored, subTabType) }.getOrNull()
                                                }.orEmpty(),
                                                scannerAddedItems = (documentData?.get("scannerAddedItemsJson") as? String)?.let { stored ->
                                                    runCatching { gson.fromJson<List<ScannerAddedItem>>(stored, scannerType) }.getOrNull()
                                                }.orEmpty(),
                                                progressByProfile = (documentData?.get("progressByProfileJson") as? String)?.let { stored ->
                                                    runCatching { gson.fromJson<Map<String, Map<String, Boolean>>>(stored, profileProgressType) }.getOrNull()
                                                }.orEmpty()
                                            )
                                            pendingCloudExtras = decodeCloudExtras(
                                                gson,
                                                documentData?.get("backupPayloadGzip") as? String
                                            ) ?: legacyExtras
                                            pendingCloudProgress = cloudProgress
                                            showCloudRestorePreview = true
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        accountSyncing = false
                                        TennoSystem.appendCrashLog(context, error)
                                        Toast.makeText(
                                            context,
                                            cloudFailureMessage(error, language == AppLanguage.GERMAN),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        onDeleteCloudBackup = {
                            if (!accountSyncing) showCloudDeleteConfirm = true
                        },
                        onExportBackup = {
                            exportLauncher.launch(buildBackupFileName())
                        },
                        onRestoreImportRecovery = {
                            sharedPreferences.getString("import_recovery_backup", null)?.let { stored ->
                                runCatching { TennoBackupCodec.decode(gson, stored) }
                                    .onSuccess { recovery ->
                                        pendingBackup = recovery
                                        pendingImportItems = recovery.items
                                        showImportPreview = true
                                    }
                                    .onFailure {
                                        TennoSystem.appendCrashLog(context, it)
                                        Toast.makeText(
                                            context,
                                            if (language == AppLanguage.GERMAN) "Der vorherige Import-Stand konnte nicht gelesen werden." else "The previous import state could not be read.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        onAutoSyncChange = { enabled ->
                            accountAutoSync = enabled
                            appSettings.edit().putBoolean("account_auto_sync", enabled).apply()
                            Toast.makeText(
                                context,
                                if (enabled) {
                                    if (language == AppLanguage.GERMAN) "Automatische Cloud-Synchronisierung aktiviert." else "Automatic cloud sync enabled."
                                } else {
                                    if (language == AppLanguage.GERMAN) "Automatische Cloud-Synchronisierung deaktiviert." else "Automatic cloud sync disabled."
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                } else if (showTennoHub) {

                    TennoHubScreen(items, fissuresData, language, activeProfile) { profile ->
                        saveLocalProgress()
                        activeProfile = profile
                        sharedPreferences.edit().putString("active_profile", profile).apply()
                        loadProfileProgress(profile)
                    }

                } else if (showLiveScreen) {

                    LiveScreen(language, offlineMode)

                } else {

                    if (showAddSubTabDialog) {
                        AlertDialog(

                            containerColor = AppColors.Card,

                            shape = AppShapes.Large,

                            titleContentColor = AppColors.TextPrimary,

                            textContentColor = AppColors.TextSecondary,

                            onDismissRequest = {

                                showAddSubTabDialog = false
                            },

                            confirmButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,


                                    onClick = {


                                        if (
                                            newSubTabName.isNotEmpty() &&
                                            tabs.isNotEmpty()
                                        ) {

                                            subTabs.add(

                                                SubTabItem(

                                                    name = newSubTabName,

                                                    parentTab =
                                                        tabs[selectedTab].name
                                                )
                                            )

                                            saveSubTabs()
                                        }

                                        showAddSubTabDialog = false

                                    }
                                ) {

                                    Text("Erstellen")
                                }
                            },

                            dismissButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

                                    onClick = {

                                        showAddSubTabDialog = false
                                    }
                                ) {

                                    Text("Abbrechen")
                                }
                            },

                            title = {

                                Text(
                                    "Neuer Untertab",
                                    color = AppColors.TextPrimary
                                )
                            },

                            text = {

                                OutlinedTextField(

                                    colors = OutlinedTextFieldDefaults.colors(

                                        focusedBorderColor = AppColors.Accent,

                                        unfocusedBorderColor =
                                            AppColors.TextPrimary.copy(alpha = 0.2f),

                                        focusedTextColor = AppColors.TextPrimary,

                                        unfocusedTextColor = AppColors.TextPrimary,

                                        cursorColor = AppColors.Accent
                                    ),

                                    shape = AppShapes.Large,

                                    value = newSubTabName,

                                    onValueChange = {

                                        newSubTabName = it
                                    },

                                    singleLine = true,

                                    label = {

                                        Text(
                                            "Untertab Name",
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                )
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                    if (tabs.isNotEmpty()) {

                        ScrollableTabRow(

                            selectedTabIndex = selectedTab,

                            containerColor = Color.Transparent,

                            edgePadding = 12.dp,

                            divider = { },

                            indicator = { }

                        ) {

                            tabs.forEachIndexed { index, tab ->

                                val tabItems =
                                    items.filter {
                                        it.tabName == tab.name
                                    }

                                val totalTabComponents =
                                    tabItems.sumOf {
                                        it.components.size
                                    }

                                val checkedTabComponents =
                                    tabItems.sumOf { item ->

                                        item.components.count {
                                            it.checked
                                        }
                                    }

                                val tabProgress =
                                    if (totalTabComponents > 0)
                                        checkedTabComponents * 100 / totalTabComponents
                                    else
                                        0

                                val newItemsCount =
                                    tabItems.count {
                                        it.isNew
                                    }

                                val selected =
                                    selectedTab == index

                                Tab(

                                    selected = selected,

                                    onClick = {
                                        selectedTab = index
                                    },

                                    text = {

                                        Box(

                                            modifier = Modifier
                                                .pointerInput(index) {
                                                    detectTapGestures(
                                                        onTap = {
                                                            selectedTab = index
                                                        },
                                                        onLongPress = {
                                                            selectedTab = index
                                                            tabReorderMenuIndex = index
                                                        }
                                                    )
                                                }

                                                .background(

                                                    if (selected)
                                                        Color(0xFFC8A8FF)
                                                    else
                                                        AppColors.TextPrimary.copy(alpha = 0.12f),

                                                    AppShapes.Large
                                                )

                                                .padding(
                                                    horizontal = 18.dp,
                                                    vertical = 10.dp
                                                )
                                        ) {

                                            Column(

                                                horizontalAlignment =
                                                    androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {

                                                Text(

                                                    text =

                                                        if (newItemsCount > 0)
                                                            "${tab.name} • $newItemsCount"
                                                        else
                                                            tab.name,

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            Color.White
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(4.dp)
                                                )

                                                Text(

                                                    text = "$tabProgress%",

                                                    color =

                                                        if (selected)
                                                            Color.Black.copy(alpha = 0.7f)
                                                        else
                                                            Color.LightGray,

                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                LinearProgressIndicator(

                                                    progress = {
                                                        tabProgress / 100f
                                                    },

                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .height(6.dp),

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            AppColors.Accent,

                                                    trackColor =
                                                        Color.White.copy(alpha = 0.15f),

                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = tabReorderMenuIndex == index,
                                                onDismissRequest = { tabReorderMenuIndex = null }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (language == AppLanguage.GERMAN) "Nach vorne" else "Move left"
                                                        )
                                                    },
                                                    enabled = index > 0,
                                                    onClick = { moveMainTab(index, -1) }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (language == AppLanguage.GERMAN) "Nach hinten" else "Move right"
                                                        )
                                                    },
                                                    enabled = index < tabs.lastIndex,
                                                    onClick = { moveMainTab(index, 1) }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }


                    val currentSubTabs =

                        if (tabs.isNotEmpty()) {

                            subTabs.filter {

                                it.parentTab ==
                                        tabs[selectedTab].name
                            }

                        } else {

                            emptyList()
                        }

                    if (currentSubTabs.isNotEmpty()) {

                        ScrollableTabRow(

                            selectedTabIndex =
                                selectedSubTab.coerceAtMost(
                                    currentSubTabs.lastIndex
                                ),

                            containerColor = Color.Transparent,

                            edgePadding = 12.dp,

                            divider = { },

                            indicator = { }
                        ) {

                            currentSubTabs.forEachIndexed { index, subTab ->

                                val subTabItems =
                                    items.filter {
                                        it.subTabName == subTab.name
                                    }

                                val totalSubTabComponents =
                                    subTabItems.sumOf {
                                        it.components.size
                                    }

                                val checkedSubTabComponents =
                                    subTabItems.sumOf { item ->

                                        item.components.count {
                                            it.checked
                                        }
                                    }

                                val subTabProgress =
                                    if (totalSubTabComponents > 0)
                                        checkedSubTabComponents * 100 / totalSubTabComponents
                                    else
                                        0

                                Tab(

                                    selected = selectedSubTab == index,

                                    onClick = {
                                        selectedSubTab = index
                                    },

                                    text = {

                                        val newSubItemsCount =
                                            subTabItems.count {
                                                it.isNew
                                            }

                                        val selected =
                                            selectedSubTab == index

                                        Box(

                                            modifier = Modifier
                                                .pointerInput(index, subTab.name) {
                                                    detectTapGestures(
                                                        onTap = {
                                                            selectedSubTab = index
                                                        },
                                                        onLongPress = {
                                                            selectedSubTab = index
                                                            subTabReorderMenuIndex = index
                                                        }
                                                    )
                                                }

                                                .background(

                                                    if (selected)
                                                        AppColors.Accent
                                                    else
                                                        AppColors.TextPrimary.copy(alpha = 0.08f),

                                                    RoundedCornerShape(50)
                                                )

                                                .padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                        ) {

                                            Column(

                                                horizontalAlignment =
                                                    androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {

                                                Text(

                                                    text =

                                                        if (newSubItemsCount > 0)
                                                            "${subTab.name} • $newSubItemsCount"
                                                        else
                                                            subTab.name,

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            Color.White
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(4.dp)
                                                )

                                                Text(

                                                    text = "$subTabProgress%",

                                                    color =

                                                        if (selected)
                                                            Color.Black.copy(alpha = 0.7f)
                                                        else
                                                            Color.LightGray,

                                                    style = MaterialTheme.typography.labelSmall
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                LinearProgressIndicator(

                                                    progress = {
                                                        subTabProgress / 100f
                                                    },

                                                    modifier = Modifier
                                                        .width(50.dp)
                                                        .height(5.dp),

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            AppColors.Accent,

                                                    trackColor =
                                                        Color.White.copy(alpha = 0.15f),

                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = subTabReorderMenuIndex == index,
                                                onDismissRequest = { subTabReorderMenuIndex = null }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (language == AppLanguage.GERMAN) "Nach vorne" else "Move left"
                                                        )
                                                    },
                                                    enabled = index > 0,
                                                    onClick = {
                                                        moveSubTab(
                                                            tabs[selectedTab].name,
                                                            index,
                                                            -1
                                                        )
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (language == AppLanguage.GERMAN) "Nach hinten" else "Move right"
                                                        )
                                                    },
                                                    enabled = index < currentSubTabs.lastIndex,
                                                    onClick = {
                                                        moveSubTab(
                                                            tabs[selectedTab].name,
                                                            index,
                                                            1
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }




                        val totalComponents = items.sumOf {
                            it.components.size
                        }

                        val checkedComponents = items.sumOf {
                            it.components.count { component ->
                                component.checked
                            }
                        }

                        val progressPercent =
                            if (totalComponents > 0)
                                (checkedComponents * 100) / totalComponents
                            else
                                0



                        OutlinedTextField(

                            colors = OutlinedTextFieldDefaults.colors(

                                focusedBorderColor = AppColors.Accent,

                                unfocusedBorderColor =
                                    AppColors.TextPrimary.copy(alpha = 0.2f),

                                focusedTextColor = AppColors.TextPrimary,

                                unfocusedTextColor = AppColors.TextPrimary,

                                cursorColor = AppColors.Accent
                            ),

                            shape = AppShapes.Large,

                            value = searchText,

                            onValueChange = {
                                searchText = it
                            },

                            singleLine = true,

                            label = {
                                Text(
                                    "Suchen",
                                    color = AppColors.TextSecondary
                                )
                            },


                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        val visibleCollectionItems = items.filter { item ->
                            val currentSubTab = currentSubTabs.getOrNull(selectedSubTab)?.name ?: ""
                            val checkedCount = item.components.count { component -> component.checked }
                            item.tabName.trim() == tabs[selectedTab].name.trim() &&
                                item.name.contains(searchText, ignoreCase = true) &&
                                (currentSubTab.isEmpty() || item.subTabName.trim() == currentSubTab.trim()) &&
                                item.name !in archivedNames &&
                                (collectionCategory == "all" ||
                                    item.type.equals(collectionCategory, ignoreCase = true) ||
                                    item.tabName.contains(collectionCategory, ignoreCase = true) ||
                                    item.subTabName.contains(collectionCategory, ignoreCase = true)) &&
                                (!onlyFavorites || item.name in favoriteNames) &&
                                (!onlyAlmostDone || (item.components.size > 1 && checkedCount > 0 && checkedCount < item.components.size)) &&
                                (!onlyMissing || item.components.isEmpty() || !item.components.all { component -> component.checked })
                        }

                        OutlinedButton(
                            onClick = {
                                collectionFiltersExpanded = !collectionFiltersExpanded
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = AppColors.EnergyCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (collectionFiltersExpanded) {
                                    if (language == AppLanguage.GERMAN) "Filter ausblenden" else "Hide filters"
                                } else {
                                    if (language == AppLanguage.GERMAN) "Filter anzeigen" else "Show filters"
                                },
                                color = AppColors.TextPrimary
                            )
                        }

                        if (collectionFiltersExpanded) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "all" to if (language == AppLanguage.GERMAN) "Alle" else "All",
                                    "warframe" to "Warframes",
                                    "weapon" to if (language == AppLanguage.GERMAN) "Waffen" else "Weapons",
                                    "mod" to "Mods",
                                    "ressource" to if (language == AppLanguage.GERMAN) "Ressourcen" else "Resources"
                                ).forEach { (key, label) ->
                                    AssistChip(
                                        onClick = { collectionCategory = key },
                                        label = { Text(label) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (collectionCategory == key) AppColors.EnergyCyan.copy(alpha = 0.18f) else AppColors.Card,
                                            labelColor = if (collectionCategory == key) AppColors.EnergyCyan else AppColors.TextSecondary
                                        )
                                    )
                                }
                            }

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AssistChip(
                                    onClick = { onlyMissing = !onlyMissing },
                                    label = { Text(if (language == AppLanguage.GERMAN) "Nur fehlende" else "Missing only") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (onlyMissing) AppColors.EnergyCyan.copy(alpha = 0.18f) else AppColors.Card,
                                        labelColor = if (onlyMissing) AppColors.EnergyCyan else AppColors.TextSecondary
                                    )
                                )
                                AssistChip(
                                    onClick = { onlyAlmostDone = !onlyAlmostDone },
                                    label = { Text(if (language == AppLanguage.GERMAN) "Fast fertig" else "Almost done") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (onlyAlmostDone) AppColors.EnergyCyan.copy(alpha = 0.18f) else AppColors.Card,
                                        labelColor = if (onlyAlmostDone) AppColors.EnergyCyan else AppColors.TextSecondary
                                    )
                                )
                                AssistChip(
                                    onClick = {
                                        favoriteNames = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
                                            .getStringSet("favorites", emptySet()) ?: emptySet()
                                        onlyFavorites = !onlyFavorites
                                    },
                                    label = { Text(if (language == AppLanguage.GERMAN) "Favoriten" else "Favorites") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (onlyFavorites) AppColors.EnergyCyan.copy(alpha = 0.18f) else AppColors.Card,
                                        labelColor = if (onlyFavorites) AppColors.EnergyCyan else AppColors.TextSecondary
                                    )
                                )
                            }

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AssistChip(
                                    onClick = {
                                        visibleCollectionItems.forEach { item ->
                                        item.components.forEachIndexed { index, component ->
                                            item.components[index] = component.copy(checked = true)
                                        }
                                        }
                                        saveLocalProgress()
                                        addChangeLog("Massen-Abhaken: ${visibleCollectionItems.size} Einträge")
                                    },
                                    label = { Text(if (language == AppLanguage.GERMAN) "Sichtbare abhaken" else "Check visible") }
                                )
                                AssistChip(
                                    onClick = {
                                        val nextArchived = archivedNames + visibleCollectionItems.map { it.name }
                                        archivedNames = nextArchived
                                        sharedPreferences.edit().putStringSet("archived_items", nextArchived).apply()
                                        addChangeLog("Archiviert: ${visibleCollectionItems.size} Einträge")
                                    },
                                    label = { Text(if (language == AppLanguage.GERMAN) "Sichtbare archivieren" else "Archive visible") }
                                )
                            }
                        }

                        Text(
                            text = if (language == AppLanguage.GERMAN) {
                                "Kategorie-Fortschritt: ${visibleCollectionItems.sumOf { item -> item.components.count { it.checked } }}/${visibleCollectionItems.sumOf { it.components.size }.coerceAtLeast(1)} · Profil: $activeProfile"
                            } else {
                                "Category progress: ${visibleCollectionItems.sumOf { item -> item.components.count { it.checked } }}/${visibleCollectionItems.sumOf { it.components.size }.coerceAtLeast(1)} · Profile: $activeProfile"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        WarframeList(

                            items = items,

                            fissuresData = fissuresData,

                            currentTab = tabs[selectedTab].name,

                            currentSubTab =
                                currentSubTabs
                                    .getOrNull(selectedSubTab)
                                    ?.name ?: "",

                            saveItems = {
                                saveItems()
                            },
                            saveLocalProgress = {
                                saveLocalProgress()
                            },

                            sortAZ = sortAZ,

                            searchText = searchText,

                            categoryFilter = collectionCategory,

                            onlyMissing = onlyMissing,

                            onlyAlmostDone = onlyAlmostDone,

                            onlyFavorites = onlyFavorites,

                            favoriteNames = favoriteNames,

                            pinFavorites = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
                                .getBoolean("pin_favorites", true),

                            archivedNames = archivedNames,

                            german = language == AppLanguage.GERMAN,

                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 6.dp)
                        )
                    }
                    }


                    if (showAddTabDialog) {

                        AlertDialog(

                            containerColor = AppColors.Card,

                            shape = AppShapes.Large,

                            titleContentColor = AppColors.TextPrimary,

                            textContentColor = AppColors.TextSecondary,

                            onDismissRequest = {
                                showAddTabDialog = false
                            },

                            confirmButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

                                    onClick = {

                                        if (newTabName.isNotEmpty()) {

                                            tabs.add(
                                                TabItem(newTabName)
                                            )

                                            selectedTab = tabs.lastIndex

                                            saveTabs()
                                        }

                                        showAddTabDialog = false
                                    }
                                ) {

                                    Text("Erstellen")
                                }
                            },

                            dismissButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

                                    onClick = {
                                        showAddTabDialog = false
                                    }
                                ) {

                                    Text("Abbrechen")
                                }
                            },

                            title = {
                                Text("Neuer Tab")
                            },

                            text = {

                                OutlinedTextField(

                                    colors = OutlinedTextFieldDefaults.colors(

                                        focusedBorderColor = AppColors.Accent,

                                        unfocusedBorderColor =
                                            AppColors.TextPrimary.copy(alpha = 0.2f),

                                        focusedTextColor = AppColors.TextPrimary,

                                        unfocusedTextColor = AppColors.TextPrimary,

                                        cursorColor = AppColors.Accent
                                    ),

                                    shape = AppShapes.Large,

                                    value = newTabName,

                                    onValueChange = {
                                        newTabName = it
                                    },

                                    singleLine = true,

                                    label = {
                                        Text("Tab Name",
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                )
                            }
                        )
                    }

                }

                UpdateDialog(
                    showDialog = showUpdateDialog,
                    updateTitle = updateTitle,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    german = language == AppLanguage.GERMAN,
                    lastBackupAt = lastLocalBackupAt,
                    onExportBackup = {
                        showUpdateDialog = false
                        exportLauncher.launch(buildBackupFileName())
                    },
                    onDismiss = { showUpdateDialog = false }
                )

                if (showDeleteTabDialog) {

                    AlertDialog(

                        containerColor = AppColors.Card,

                        shape = AppShapes.Large,

                        titleContentColor = AppColors.TextPrimary,

                        textContentColor = AppColors.TextSecondary,

                        onDismissRequest = {
                            showDeleteTabDialog = false
                        },

                        confirmButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        val currentTabName =
                                            tabs[selectedTab].name

                                        items.removeAll {
                                            it.tabName == currentTabName
                                        }

                                        subTabs.removeAll {
                                            it.parentTab == currentTabName
                                        }

                                        tabs.removeAt(selectedTab)

                                        if (tabs.isEmpty()) {

                                            selectedTab = 0

                                        } else if (selectedTab > tabs.lastIndex) {

                                            selectedTab = tabs.lastIndex
                                        }

                                        saveItems()
                                        saveTabs()
                                        saveSubTabs()
                                    }

                                    showDeleteTabDialog = false
                                }
                            ) {

                                Text("Löschen")
                            }
                        },

                        dismissButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {
                                    showDeleteTabDialog = false
                                }
                            ) {

                                Text("Abbrechen")
                            }
                        },

                        title = {
                            Text("Tab löschen",
                                color = AppColors.TextPrimary
                            )
                        },

                        text = {

                            Text(
                                "Willst du den Tab \"${tabs[selectedTab].name}\" wirklich löschen?"
                            )
                        }
                    )
                }



                if (showDeleteSubTabDialog) {

                    val currentSubTab =
                        subTabs
                            .filter {
                                it.parentTab == tabs[selectedTab].name
                            }
                            .getOrNull(selectedSubTab)

                    AlertDialog(

                        containerColor = AppColors.Card,

                        shape = AppShapes.Large,

                        titleContentColor = AppColors.TextPrimary,

                        textContentColor = AppColors.TextSecondary,

                        onDismissRequest = {
                            showDeleteSubTabDialog = false
                        },

                        confirmButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {

                                    if (currentSubTab != null) {

                                        items.removeAll {
                                            it.subTabName == currentSubTab.name
                                        }

                                        subTabs.remove(currentSubTab)

                                        saveItems()
                                        saveSubTabs()

                                        if (selectedSubTab > 0) {
                                            selectedSubTab--
                                        }
                                    }

                                    showDeleteSubTabDialog = false
                                }
                            ) {

                                Text("Löschen")
                            }
                        },

                        dismissButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {
                                    showDeleteSubTabDialog = false
                                }
                            ) {

                                Text("Abbrechen")
                            }
                        },

                        title = {
                            Text("Untertab löschen",
                                color = AppColors.TextPrimary
                            )
                        },

                        text = {

                            Text(
                                "Willst du den Untertab \"${currentSubTab?.name}\" wirklich löschen?"
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun githubApkDownloadUrl(releaseJson: JSONObject): String? {
    val assets = releaseJson.optJSONArray("assets") ?: return null
    for (index in 0 until assets.length()) {
        val asset = assets.optJSONObject(index) ?: continue
        val name = asset.optString("name")
        val downloadUrl = asset.optString("browser_download_url")
        if (name.endsWith(".apk", ignoreCase = true) && downloadUrl.isNotBlank()) {
            return downloadUrl
        }
    }
    return null
}

private fun Any?.asBooleanMap(): Map<String, Boolean>? {
    val source = this as? Map<*, *> ?: return null
    return source.entries.mapNotNull { (rawKey, rawValue) ->
        val key = rawKey as? String ?: return@mapNotNull null
        val value = rawValue as? Boolean ?: return@mapNotNull null
        key to value
    }.toMap()
}

private fun Any?.asStringAnyMaps(): List<Map<String, Any?>>? {
    val source = this as? List<*> ?: return null
    return source.mapNotNull outer@{ rawMap ->
        val map = rawMap as? Map<*, *> ?: return@outer null
        map.entries.mapNotNull entry@{ (rawKey, value) ->
            val key = rawKey as? String ?: return@entry null
            key to value
        }.toMap()
    }
}

private fun releaseVersionNumber(value: String): String {
    return Regex("""\d+(?:\.\d+){0,3}""").find(value)?.value.orEmpty()
}

private fun isRemoteVersionNewer(remote: String, current: String): Boolean {
    val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
    val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
    val maxSize = maxOf(remoteParts.size, currentParts.size)
    for (index in 0 until maxSize) {
        val remotePart = remoteParts.getOrElse(index) { 0 }
        val currentPart = currentParts.getOrElse(index) { 0 }
        if (remotePart > currentPart) return true
        if (remotePart < currentPart) return false
    }
    return false
}

private fun githubUpdateMessage(body: String, hasDirectApk: Boolean, german: Boolean): String {
    val cleanBody = body.trim().ifBlank {
        if (german) "Für diese Version wurde kein Changelog hinterlegt." else "No changelog was provided for this version."
    }
    val hint = if (hasDirectApk) {
        if (german) "Die APK wird direkt aus dem GitHub-Release geöffnet. Android fragt dich danach nach der Installationsbestätigung."
        else "The APK opens directly from the GitHub release. Android will ask you to confirm the installation."
    } else {
        if (german) "Im GitHub-Release ist noch keine APK-Datei hinterlegt. Der Button öffnet deshalb die Release-Seite."
        else "No APK asset is attached to this GitHub release yet. The button opens the release page instead."
    }
    return "$cleanBody\n\n$hint"
}

private fun buildBackupFileName(): String {
    val stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").format(LocalDateTime.now())
    return "TennoFreundeBackup_$stamp.json"
}
