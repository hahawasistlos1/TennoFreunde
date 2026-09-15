package com.example.tennofreunde.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.BuildConfig
import com.example.tennofreunde.R
import com.example.tennofreunde.system.TennoSystem
import com.example.tennofreunde.system.TennoWidgetProvider
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.*

@Composable
fun SettingsScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    darkMode: Boolean,
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
    val german = language == AppLanguage.GERMAN
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    var testDataMode by remember { mutableStateOf(prefs.getBoolean("test_data_mode", false)) }
    var localErrorLog by remember { mutableStateOf(prefs.getBoolean("local_error_log", true)) }
    var crashLog by remember { mutableStateOf(TennoSystem.readCrashLog(context)) }
    var notificationStatus by remember { mutableStateOf(TennoSystem.notificationsAllowed(context)) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationStatus = granted }
    var releaseChecks by remember { mutableStateOf(prefs.getStringSet("release_checks", emptySet()).orEmpty()) }
    var startPage by remember { mutableStateOf(prefs.getString("start_page", "home") ?: "home") }
    var backupReminder by remember { mutableStateOf(prefs.getBoolean("backup_reminder", true)) }
    val releaseChecklist = listOf(
        "build" to if (german) "Debug-Build läuft fehlerfrei" else "Debug build passes",
        "tests" to if (german) "Unit- und UI-Tests grün" else "Unit and UI tests pass",
        "scanner" to if (german) "Scanner prüft Treffer vor dem Übernehmen" else "Scanner reviews matches before applying",
        "backup" to if (german) "Import/Export besitzt Schutzdialoge" else "Import/export has guard dialogs",
        "notifications" to if (german) "Benachrichtigungskanäle aktiv" else "Notification channels active",
        "widget" to if (german) "Homescreen-Widget registriert" else "Home screen widget registered",
        "release" to if (german) "Signierter Release-Build erstellt" else "Signed release build created"
    )

    Box(Modifier.fillMaxSize().background(AppBrushes.MainBackground)) {
        Image(painterResource(R.drawable.warframe_bg), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.2f)
        Column(
            Modifier.fillMaxHeight().fillMaxWidth().widthIn(max = 920.dp).align(Alignment.TopCenter).padding(if (compactMode) 12.dp else 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (compactMode) 10.dp else 18.dp)
        ) {
            Text(if (german) "EINSTELLUNGEN" else "SETTINGS", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
            Text(if (german) "Tenno-Konfiguration" else "Tenno configuration", color = AppColors.TextPrimary, style = MaterialTheme.typography.headlineMedium)
            WarframeSettingCard(Icons.Default.Language, if (german) "Sprache" else "Language") {
                AppLanguage.entries.forEach { option ->
                    Row(Modifier.fillMaxWidth().clickable { onLanguageChange(option) }, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(option.displayNameFor(language), color = AppColors.TextPrimary)
                            if (option.displayNameFor(language) != option.nativeName) {
                                Text(option.nativeName, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        RadioButton(selected = language == option, onClick = { onLanguageChange(option) }, colors = RadioButtonDefaults.colors(selectedColor = AppColors.AccentBlue))
                    }
                }
                Text(
                    if (german) "Weitere vollständige App-Übersetzungen folgen schrittweise. Live-Daten werden schon mit dem jeweiligen Sprachcode angefragt, wenn die API sie unterstützt."
                    else "More complete app translations will be added step by step. Live data is already requested with the selected language code when the API supports it.",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            WarframeSettingCard(Icons.Default.DarkMode, if (german) "Darstellung" else "Appearance") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (german) "Dunkles Warframe-Design" else "Dark Warframe theme", color = AppColors.TextPrimary)
                    Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(if (german) "Größere Schrift" else "Larger text", color = AppColors.TextPrimary)
                        Text(
                            if (german) "Vergrößert Texte sofort in der ganzen App." else "Immediately enlarges text throughout the app.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = largeText, onCheckedChange = onLargeTextChange)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(if (german) "Kompakter Modus" else "Compact mode", color = AppColors.TextPrimary)
                        Text(
                            if (german) "Zeigt mehr Inhalt mit kleineren Abständen und kompakten Karten."
                            else "Shows more content with tighter spacing and compact cards.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = compactMode, onCheckedChange = onCompactModeChange)
                }
                Text(if (german) "Farbschema" else "Color scheme", color = AppColors.OrokinGold)
                listOf("orokin" to "Orokin", "corpus" to "Corpus", "stalker" to "Stalker").forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onColorStyleChange(key) }, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, color = MaterialTheme.colorScheme.onSurface)
                        RadioButton(selected = colorStyle == key, onClick = { onColorStyleChange(key) })
                    }
                }
            }
            WarframeSettingCard(Icons.Default.Home, if (german) "Startseite" else "Start page") {
                val startPages = listOf(
                    "home" to if (german) "Hauptseite (Live-Übersicht)" else "Home (live overview)",
                    "hub" to if (german) "Tenno-Zentrale" else "Tenno hub",
                    "collection" to if (german) "Sammlung" else "Collection"
                )
                startPages.forEach { (key, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            startPage = key
                            prefs.edit().putString("start_page", key).apply()
                        },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = startPage == key,
                            onClick = {
                                startPage = key
                                prefs.edit().putString("start_page", key).apply()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.AccentBlue)
                        )
                    }
                }
                Text(
                    if (german) "Die Auswahl gilt beim nächsten Öffnen der App. Android-Zurück führt weiterhin zur Hauptseite."
                    else "The selection applies the next time the app opens. Android Back still returns to Home.",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            WarframeSettingCard(Icons.Default.CloudOff, if (german) "Offline-Modus" else "Offline mode") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(if (german) "Nur gespeicherte Live-Daten verwenden" else "Use saved live data only", color = AppColors.TextPrimary)
                        Text(
                            if (german) "Verhindert Netzabfragen und zeigt den letzten verfügbaren Weltstatus deutlich als Offline-Stand."
                            else "Prevents network requests and clearly shows the last available world state as offline data.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = offlineMode, onCheckedChange = onOfflineModeChange)
                }
            }
            WarframeSettingCard(Icons.Default.Backup, if (german) "Sicherung" else "Backup") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(if (german) "Backup-Erinnerung" else "Backup reminder", color = AppColors.TextPrimary)
                        Text(
                            if (german) "Erinnert nach sieben Tagen ohne neue Sicherung. Ein erfolgreicher Export setzt die Frist zurück."
                            else "Reminds you after seven days without a new backup. A successful export resets the timer.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = backupReminder, onCheckedChange = {
                        backupReminder = it
                        TennoSystem.setBackupReminderEnabled(context, it)
                    })
                }
                val lastBackup = prefs.getLong("last_backup_at", 0L)
                Text(
                    if (lastBackup == 0L) {
                        if (german) "Noch keine Sicherung in der App registriert." else "No backup registered in the app yet."
                    } else {
                        val days = ((System.currentTimeMillis() - lastBackup) / 86_400_000L).coerceAtLeast(0)
                        if (german) "Letzte Sicherung vor $days Tag(en)." else "Last backup $days day(s) ago."
                    },
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            WarframeSettingCard(Icons.Default.Info, if (german) "App-Info" else "App info") {
                SettingLine(if (german) "Version" else "Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                SettingLine(if (german) "Paket" else "Package", BuildConfig.APPLICATION_ID)
                SettingLine(if (german) "Build-Typ" else "Build type", BuildConfig.BUILD_TYPE)
            }
            WarframeSettingCard(Icons.Default.PrivacyTip, if (german) "Datenschutz" else "Privacy") {
                Text(
                    if (german) "Sammlung, Profile, Scanner-Verlauf, OCR-Regeln und Einstellungen bleiben lokal auf deinem Gerät. Live-Daten werden nur von Warframe-Statusdiensten geladen."
                    else "Collection, profiles, scanner history, OCR rules, and settings stay local on your device. Live data is loaded only from Warframe status services.",
                    color = AppColors.TextSecondary
                )
            }
            WarframeSettingCard(Icons.Default.BugReport, if (german) "Diagnose" else "Diagnostics") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (german) "Lokales Fehlerprotokoll aktivieren" else "Enable local error log", color = AppColors.TextPrimary)
                    Switch(
                        checked = localErrorLog,
                        onCheckedChange = {
                            localErrorLog = it
                            prefs.edit().putBoolean("local_error_log", it).apply()
                        }
                    )
                }
                SettingLine(
                    if (german) "Datenprüfung" else "Data validation",
                    if (german) "Lokale Einstellungen lesbar" else "Local settings readable"
                )
                if (crashLog.isBlank()) {
                    Text(
                        if (german) "Kein lokaler Crash-Eintrag vorhanden." else "No local crash entry available.",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        crashLog.takeLast(900),
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            TennoSystem.clearCrashLog(context)
                            crashLog = ""
                        },
                        shape = AppShapes.Small
                    ) {
                        Icon(Icons.Default.DeleteSweep, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (german) "Fehlerprotokoll löschen" else "Clear error log")
                    }
                }
            }
            WarframeSettingCard(Icons.Default.NotificationsActive, if (german) "Benachrichtigungen" else "Notifications") {
                SettingLine(
                    if (german) "Android-Berechtigung" else "Android permission",
                    if (notificationStatus) {
                        if (german) "Erteilt" else "Granted"
                    } else {
                        if (german) "Noch nicht erteilt" else "Not granted yet"
                    }
                )
                Text(
                    if (german) "Die App erstellt eigene Kanäle für Updates, Farm-Erinnerungen und Diagnose. Neue GitHub-Versionen können dadurch auch bei geschlossener App gemeldet werden."
                    else "The app creates separate channels for updates, farm reminders, and diagnostics. New GitHub versions can be reported even while the app is closed.",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                if (!notificationStatus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(
                        onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        shape = AppShapes.Small
                    ) {
                        Icon(Icons.Default.NotificationsActive, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (german) "Benachrichtigungen erlauben" else "Allow notifications")
                    }
                }
                Button(
                    onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                        } else {
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                            )
                        }
                        context.startActivity(intent)
                    },
                    shape = AppShapes.Small
                ) {
                    Icon(Icons.Default.NotificationsActive, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (german) "Android-Einstellung öffnen" else "Open Android settings")
                }
                Button(
                    onClick = {
                        TennoSystem.createNotificationChannels(context)
                        TennoSystem.scheduleReminderWork(context)
                        TennoSystem.showReminderNotification(
                            context,
                            "TennoFreunde",
                            if (german) "Test-Hinweis ist aktiv." else "Test notification is active.",
                            6105
                        )
                        notificationStatus = TennoSystem.notificationsAllowed(context)
                    },
                    shape = AppShapes.Small
                ) {
                    Icon(Icons.Default.NotificationsActive, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (german) "Test-Hinweis senden" else "Send test notification")
                }
            }
            WarframeSettingCard(Icons.Default.Widgets, if (german) "Homescreen-Widget" else "Home screen widget") {
                Text(
                    if (german) "Das TennoFreunde-Widget zeigt Profil und priorisierte Farmziele und öffnet die App per Fingertipp."
                    else "The TennoFreunde widget shows profile and prioritized farm targets and opens the app with a tap.",
                    color = AppColors.TextSecondary
                )
                Button(
                    onClick = { TennoWidgetProvider.updateAll(context) },
                    shape = AppShapes.Small
                ) {
                    Icon(Icons.Default.Widgets, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (german) "Widget aktualisieren" else "Refresh widget")
                }
            }
            WarframeSettingCard(Icons.Default.Science, if (german) "Testdaten" else "Test data") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (german) "Testdaten-Modus" else "Test data mode", color = AppColors.TextPrimary)
                    Switch(
                        checked = testDataMode,
                        onCheckedChange = {
                            testDataMode = it
                            prefs.edit().putBoolean("test_data_mode", it).apply()
                        }
                    )
                }
                Text(
                    if (testDataMode) {
                        if (german) "Aktiv: neue QA-Ansichten können Beispielstände nutzen." else "Active: new QA views may use sample states."
                    } else {
                        if (german) "Aus: die App nutzt deine normalen lokalen Daten." else "Off: the app uses your normal local data."
                    },
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            WarframeSettingCard(Icons.AutoMirrored.Filled.FactCheck, if (german) "Release-Checkliste" else "Release checklist") {
                releaseChecklist.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = key in releaseChecks,
                            onCheckedChange = { checked ->
                                releaseChecks = if (checked) releaseChecks + key else releaseChecks - key
                                prefs.edit().putStringSet("release_checks", releaseChecks.toSet()).apply()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.AccentBlue)
                        )
                    }
                }
            }
            Text(
                if (german) "Die Sprache wird sofort gespeichert. Live-Daten werden beim Wechsel neu geladen."
                else "The language is saved immediately. Live data reloads when it changes.",
                color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppColors.TextSecondary)
        Text(value, color = AppColors.TextPrimary)
    }
}

@Composable
private fun WarframeSettingCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = AppShapes.Medium, colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = AppColors.AccentBlue)
                Text(title.uppercase(), color = AppColors.OrokinGold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = AppColors.CardBorder)
            content()
        }
    }
}
