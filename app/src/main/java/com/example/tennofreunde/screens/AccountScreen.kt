package com.example.tennofreunde.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import java.text.DateFormat
import java.util.Date

@Composable
fun AccountScreen(
    language: AppLanguage,
    signedIn: Boolean,
    displayName: String?,
    email: String?,
    activeProfile: String,
    itemCount: Int,
    completedComponents: Int,
    totalComponents: Int,
    syncing: Boolean,
    lastSyncAt: Long,
    lastLocalBackupAt: Long,
    hasImportRecovery: Boolean,
    autoSyncEnabled: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onDeleteCloudBackup: () -> Unit,
    onExportBackup: () -> Unit,
    onRestoreImportRecovery: () -> Unit,
    onAutoSyncChange: (Boolean) -> Unit
) {
    val german = language == AppLanguage.GERMAN
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (german) "Tenno-Konto" else "Tenno account",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.OrokinGold
            )
            Text(
                if (german) "Verwalte deine Anmeldung und sichere deinen Sammlungsfortschritt in der Cloud."
                else "Manage your sign-in and back up your collection progress to the cloud.",
                color = AppColors.TextSecondary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.Card),
                shape = AppShapes.Large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (signedIn) {
                            if (german) "Angemeldet" else "Signed in"
                        } else {
                            if (german) "Nicht angemeldet" else "Not signed in"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (signedIn) AppColors.EnergyCyan else AppColors.TextPrimary
                    )
                    if (signedIn) {
                        displayName?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                        }
                        email?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = AppColors.TextSecondary)
                        }
                        Text(
                            if (german) "Dein Fortschritt wird zusätzlich unter deiner Konto-ID und dem aktiven Profil gespeichert."
                            else "Your progress is also saved under your account ID and active profile.",
                            color = AppColors.TextSecondary
                        )
                    } else {
                        Text(
                            if (german) "Deine Sammlung bleibt auch ohne Konto vollständig auf diesem Gerät gespeichert."
                            else "Your collection remains fully stored on this device without an account.",
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.Card),
                shape = AppShapes.Large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(if (german) "Cloud-Übersicht" else "Cloud overview", style = MaterialTheme.typography.titleMedium, color = AppColors.OrokinGold)
                    AccountValue(if (german) "Aktives Profil" else "Active profile", activeProfile)
                    AccountValue(if (german) "Sammlungseinträge" else "Collection items", itemCount.toString())
                    AccountValue(if (german) "Komponentenfortschritt" else "Component progress", "$completedComponents/$totalComponents")
                    AccountValue(
                        if (german) "Letzte manuelle Cloud-Sicherung" else "Last manual cloud backup",
                        if (lastSyncAt > 0L) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastSyncAt))
                        else if (german) "Noch nicht durchgeführt" else "Not completed yet"
                    )
                    AccountValue(
                        if (german) "Letzte Datei-Sicherung" else "Last file backup",
                        if (lastLocalBackupAt > 0L) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastLocalBackupAt))
                        else if (german) "Noch nicht durchgeführt" else "Not completed yet"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(if (german) "Automatisch synchronisieren" else "Automatic sync", color = AppColors.TextPrimary)
                            Text(
                                if (german) "Änderungen am Fortschritt direkt in der Cloud sichern."
                                else "Save progress changes directly to the cloud.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = onAutoSyncChange,
                            enabled = signedIn && !syncing
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.Card),
                shape = AppShapes.Large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (german) "Lokale Notfallsicherung" else "Local emergency backup", style = MaterialTheme.typography.titleMedium, color = AppColors.OrokinGold)
                    Text(
                        if (german) "Speichert Sammlung, Profile, Tabs, Favoriten und Scanner-Ergänzungen als Datei auf deinem Gerät. Diese Sicherung funktioniert auch ohne Google-Konto."
                        else "Stores collection, profiles, tabs, favorites, and scanner additions as a file on your device. This backup works without a Google account.",
                        color = AppColors.TextSecondary
                    )
                    OutlinedButton(onClick = onExportBackup, enabled = !syncing, modifier = Modifier.fillMaxWidth(), shape = AppShapes.Large) {
                        Text(if (german) "Vollständige Sicherung exportieren" else "Export full backup")
                    }
                    if (hasImportRecovery) {
                        OutlinedButton(onClick = onRestoreImportRecovery, enabled = !syncing, modifier = Modifier.fillMaxWidth(), shape = AppShapes.Large) {
                            Text(if (german) "Stand vor letztem Import wiederherstellen" else "Restore state before last import")
                        }
                    }
                }
            }

            if (signedIn) {
                Button(
                    onClick = onSyncNow,
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                    shape = AppShapes.Large
                ) {
                    Text(
                        if (syncing) {
                            if (german) "Synchronisierung läuft …" else "Syncing …"
                        } else {
                            if (german) "Jetzt synchronisieren" else "Sync now"
                        }
                    )
                }
                OutlinedButton(
                    onClick = onRestoreFromCloud,
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Large
                ) {
                    Text(if (german) "Aus Cloud wiederherstellen" else "Restore from cloud")
                }
                OutlinedButton(
                    onClick = onDeleteCloudBackup,
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Large
                ) {
                    Text(
                        if (german) "Cloud-Sicherung dieses Profils löschen" else "Delete this profile's cloud backup",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                OutlinedButton(onClick = onSignOut, enabled = !syncing, modifier = Modifier.fillMaxWidth(), shape = AppShapes.Large) {
                    Text(if (german) "Sicher abmelden" else "Sign out safely")
                }
            } else {
                Button(
                    onClick = onSignIn,
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                    shape = AppShapes.Large
                ) {
                    Text(
                        if (syncing) {
                            if (german) "Google-Konto wird geöffnet …" else "Opening Google account …"
                        } else if (german) "Mit Google anmelden" else "Sign in with Google"
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountValue(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppColors.TextSecondary)
        Text(value, color = AppColors.TextPrimary)
    }
}
