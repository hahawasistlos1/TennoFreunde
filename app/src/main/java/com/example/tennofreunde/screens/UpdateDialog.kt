package com.example.tennofreunde.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import java.text.DateFormat
import java.util.Date

@Composable
fun UpdateDialog(
    showDialog: Boolean,
    updateTitle: String,
    updateMessage: String,
    updateUrl: String,
    german: Boolean,
    lastBackupAt: Long,
    onExportBackup: () -> Unit,
    onDismiss: () -> Unit
) {

    if (!showDialog) return

    val context = LocalContext.current
    val opensApk = updateUrl.contains(".apk", ignoreCase = true)
    val backupIsRecent = lastBackupAt > 0L && System.currentTimeMillis() - lastBackupAt < 7L * 24L * 60L * 60L * 1000L

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            shape = AppShapes.Large,
            colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel)
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Update $updateTitle",

                    style =
                        MaterialTheme.typography.headlineSmall,
                    color = AppColors.OrokinGold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(updateMessage, color = AppColors.TextSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (backupIsRecent) {
                        val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastBackupAt))
                        if (german) "✓ Letzte vollständige Sicherung: $date" else "✓ Last full backup: $date"
                    } else if (german) {
                        "Vor dem Update wird eine vollständige Sicherung empfohlen. So bleiben Sammlung, Profile, Tabs und Favoriten geschützt."
                    } else {
                        "A full backup is recommended before updating. This protects your collection, profiles, tabs, and favorites."
                    },
                    color = if (backupIsRecent) AppColors.EnergyCyan else AppColors.OrokinGold
                )

                if (!backupIsRecent) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onExportBackup,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.Large
                    ) {
                        Text(if (german) "Jetzt vollständig sichern" else "Create full backup now")
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            onDismiss()
                        },

                        modifier = Modifier.weight(1f)
                    ) {

                        Text(if (german) "Später" else "Later")
                    }

                    Button(
                        onClick = {

                            if (updateUrl.isBlank()) return@Button

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(updateUrl)
                            )

                            context.startActivity(intent)
                        },

                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            if (german) {
                                if (opensApk) "APK laden" else "GitHub öffnen"
                            } else {
                                if (opensApk) "Download APK" else "Open GitHub"
                            }
                        )
                    }
                }
            }
        }
    }
}
