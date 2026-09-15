package com.example.tennofreunde.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tennofreunde.MainActivity
import com.example.tennofreunde.R
import com.example.tennofreunde.data.ScannerQueueStore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

object TennoSystem {
    const val CHANNEL_REMINDERS = "tennofreunde_reminders"
    const val CHANNEL_DIAGNOSTICS = "tennofreunde_diagnostics"
    const val CHANNEL_UPDATES = "tennofreunde_updates"
    const val CHANNEL_SCANNER = "tennofreunde_scanner"
    private const val REMINDER_WORK = "tennofreunde_periodic_reminders"
    private const val CURRENT_LOCAL_SCHEMA = 3

    fun initialize(context: Context) {
        ScannerQueueStore.setForegroundActive(context, false)
        runLocalMigrations(context)
        createNotificationChannels(context)
        installCrashLogger(context)
        scheduleReminderWork(context)
        ScannerQueueWork.scheduleIfNeeded(context)
        FirebaseMessaging.getInstance().subscribeToTopic("app_updates")
            .addOnFailureListener { appendCrashLog(context, it) }
    }

    fun runLocalMigrations(context: Context) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val current = prefs.getInt("local_schema_version", 0)
        if (current >= CURRENT_LOCAL_SCHEMA) return

        val editor = prefs.edit()
        if (current < 1) {
            editor.putBoolean("local_error_log", prefs.getBoolean("local_error_log", true))
        }
        if (current < 2) {
            editor.putString("release_channel", prefs.getString("release_channel", "github"))
            editor.putBoolean("github_update_only", prefs.getBoolean("github_update_only", true))
        }
        if (current < 3) {
            val hubPrefs = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
            editor.putBoolean("backup_reminder", hubPrefs.getBoolean("backup_reminder", true))
            editor.putLong("backup_reminder_started_at", System.currentTimeMillis())
        }
        editor.putInt("local_schema_version", CURRENT_LOCAL_SCHEMA).apply()
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            "TennoFreunde Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Hinweise fuer Baro, Void-Risse und persoenliche Farmziele."
        }
        val diagnostics = NotificationChannel(
            CHANNEL_DIAGNOSTICS,
            "TennoFreunde Diagnose",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Technische Hinweise und lokale Fehlerdiagnose."
        }
        val updates = NotificationChannel(
            CHANNEL_UPDATES,
            "TennoFreunde Updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Benachrichtigt über neue TennoFreunde-Versionen auf GitHub."
        }
        val scanner = NotificationChannel(
            CHANNEL_SCANNER,
            "TennoFreunde Scanner",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Fortschritt der Screenshot-Texterkennung im Hintergrund."
        }
        manager.createNotificationChannels(listOf(reminders, diagnostics, updates, scanner))
    }

    fun scheduleReminderWork(context: Context) {
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val hub = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
        val hasAnyReminder = settings.getBoolean("backup_reminder", true) ||
            hub.getBoolean("alert_baro", true) ||
            hub.getBoolean("alert_eidolon", false) ||
            hub.getBoolean("alert_fissure", false) ||
            hub.getStringSet("favorite_priorities", emptySet()).orEmpty().any { it.endsWith("|now") }
        if (!hasAnyReminder) {
            WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK)
            return
        }
        if (!settings.contains("backup_reminder_started_at")) {
            settings.edit().putLong("backup_reminder_started_at", System.currentTimeMillis()).apply()
        }
        val request = PeriodicWorkRequestBuilder<TennoReminderWorker>(30, TimeUnit.MINUTES)
            .addTag(REMINDER_WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun setBackupReminderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putBoolean("backup_reminder", enabled)
            .apply()
        scheduleReminderWork(context)
    }

    fun markBackupCreated(context: Context) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putLong("last_backup_at", System.currentTimeMillis())
            .apply()
    }

    fun showReminderNotification(context: Context, title: String, message: String, notificationId: Int = 6104) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (error: SecurityException) {
            appendCrashLog(context, error)
        }
    }

    fun showScannerNotification(
        context: Context,
        completed: Int,
        total: Int,
        ready: Boolean,
        failed: Int = 0
    ) {
        if (!notificationsAllowed(context)) return
        val german = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("app_language", "de") == "de"
        val intent = PendingIntent.getActivity(
            context,
            6300,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (ready) {
            if (german) "Scanner-Ergebnisse bereit" else "Scanner results ready"
        } else if (german) "Screenshots werden gelesen" else "Reading screenshots"
        val message = if (ready) {
            if (german) "$completed Bild(er) vorbereitet${if (failed > 0) ", $failed fehlgeschlagen" else ""}. Öffne den Scanner zum Übernehmen."
            else "$completed image(s) prepared${if (failed > 0) ", $failed failed" else ""}. Open the scanner to apply them."
        } else {
            if (german) "$completed von $total Bildern gelesen" else "$completed of $total images read"
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_SCANNER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(intent)
            .setOnlyAlertOnce(true)
            .setOngoing(!ready)
            .setAutoCancel(ready)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (!ready) {
            builder.setProgress(
                total.coerceAtLeast(1),
                completed.coerceAtMost(total.coerceAtLeast(1)),
                false
            )
        }
        try {
            NotificationManagerCompat.from(context).notify(6300, builder.build())
        } catch (error: SecurityException) {
            appendCrashLog(context, error)
        }
    }

    fun notificationsAllowed(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun appendCrashLog(context: Context, error: Throwable) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("local_error_log", true)) return
        val logFile = File(context.filesDir, "tennofreunde-crash.log")
        val text = buildString {
            appendLine("Zeit: ${Instant.now()}")
            appendLine("Fehler: ${error::class.java.name}")
            appendLine(error.message.orEmpty())
            appendLine(error.stackTraceToString())
            appendLine()
        }
        runCatching { logFile.appendText(text) }
    }

    fun readCrashLog(context: Context): String {
        return runCatching { File(context.filesDir, "tennofreunde-crash.log").readText() }.getOrDefault("")
    }

    fun clearCrashLog(context: Context) {
        runCatching { File(context.filesDir, "tennofreunde-crash.log").delete() }
    }

    private fun installCrashLogger(context: Context) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (previousHandler is TennoCrashHandler) return
        Thread.setDefaultUncaughtExceptionHandler(TennoCrashHandler(context.applicationContext, previousHandler))
    }
}

private class TennoCrashHandler(
    private val context: Context,
    private val previousHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, error: Throwable) {
        TennoSystem.appendCrashLog(context, error)
        previousHandler?.uncaughtException(thread, error)
    }
}
