package com.example.tennofreunde.system

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tennofreunde.api.WarframeApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class TennoReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val hub = applicationContext.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
        val settings = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!TennoSystem.notificationsAllowed(applicationContext)) return Result.success()
        val now = System.currentTimeMillis()
        val platform = applicationContext.getSharedPreferences("live_settings", Context.MODE_PRIVATE).getString("platform", "ps4") ?: "ps4"
        val messages = mutableListOf<Pair<String, String>>()

        if (hub.getBoolean("alert_baro", true)) {
            runCatching { WarframeApi.api.getBaro(platform, "de") }.getOrNull()?.let { baro ->
                val activation = baro.activation.toEpochMillis()
                val expiry = baro.expiry.toEpochMillis()
                when {
                    isWithinUpcomingWindow(now, activation, TimeUnit.MINUTES.toMillis(60)) ->
                        messages.addOnce(hub, "baro_soon_$activation", "Baro Ki'Teer kommt in weniger als einer Stunde.")
                    activation != null && expiry != null && now in activation until expiry ->
                        messages.addOnce(hub, "baro_active_$expiry", "Baro Ki'Teer ist jetzt da${baro.location.takeIf { it.isNotBlank() }?.let { " – $it" }.orEmpty()}.")
                }
            }
        }
        if (hub.getBoolean("alert_eidolon", false)) {
            runCatching { WarframeApi.api.getCetusCycle(platform, "de") }.getOrNull()?.let { cycle ->
                val nightStart = cycle.expiry.toEpochMillis()
                if (cycle.isDay == true && isWithinUpcomingWindow(now, nightStart, TimeUnit.MINUTES.toMillis(30))) {
                    messages.addOnce(hub, "eidolon_night_$nightStart", eidolonNightMessage(now, nightStart!!))
                }
            }
        }
        val started = settings.getLong("backup_reminder_started_at", now)
        if (isBackupDue(settings.getBoolean("backup_reminder", true), now, settings.getLong("last_backup_at", 0L), started)) {
            messages.addOnce(hub, "backup_${LocalDate.now()}", "Deine letzte Sicherung ist mindestens sieben Tage her.")
        }
        val priorityCount = hub.getStringSet("favorite_priorities", emptySet()).orEmpty().count { it.endsWith("|now") }
        if (priorityCount > 0) messages.addOnce(hub, "farm_${LocalDate.now()}", "$priorityCount priorisierte Farmziele warten auf dich.")
        if (messages.isNotEmpty()) {
            TennoSystem.showReminderNotification(applicationContext, "TennoFreunde", messages.joinToString("\n") { it.second })
            val editor = hub.edit()
            messages.forEach { editor.putBoolean(it.first, true) }
            editor.apply()
        }
        TennoWidgetProvider.updateAll(applicationContext)
        return Result.success()
    }
}

private fun MutableList<Pair<String, String>>.addOnce(prefs: android.content.SharedPreferences, key: String, message: String) {
    if (!prefs.getBoolean(key, false)) add(key to message)
}

private fun String?.toEpochMillis(): Long? = this?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }

internal fun isWithinUpcomingWindow(now: Long, event: Long?, window: Long): Boolean = event != null && event >= now && event - now <= window

internal fun eidolonNightMessage(now: Long, nightStart: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val remainingSeconds = ((nightStart - now).coerceAtLeast(0L) + 999L) / 1_000L
    val minutes = remainingSeconds / 60L
    val seconds = remainingSeconds % 60L
    val exactTime = Instant.ofEpochMilli(nightStart)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    val remaining = when {
        minutes > 0L && seconds > 0L -> "$minutes Min $seconds Sek"
        minutes > 0L -> "$minutes Min"
        else -> "$seconds Sek"
    }
    return "Eidolon-Nacht beginnt in $remaining (um $exactTime Uhr)."
}

internal fun isBackupDue(enabled: Boolean, now: Long, lastBackup: Long, reminderStarted: Long): Boolean {
    if (!enabled) return false
    val reference = if (lastBackup > 0L) lastBackup else reminderStarted
    return now - reference >= TimeUnit.DAYS.toMillis(7)
}
