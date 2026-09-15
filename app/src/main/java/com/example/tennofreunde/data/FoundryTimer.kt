package com.example.tennofreunde.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tennofreunde.system.FoundryTimerWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit

data class FoundryTimer(
    val id: String,
    val name: String,
    val createdAt: Long,
    val finishesAt: Long
)

object FoundryTimerStore {
    private const val PREFS = "tenno_hub"
    private const val KEY = "foundry_timers_v1"
    private const val WORK_PREFIX = "tenno_foundry_"

    fun load(context: Context): List<FoundryTimer> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        return decode(json).sortedBy { it.finishesAt }
    }

    fun save(context: Context, timers: List<FoundryTimer>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, encode(timers.sortedBy { it.finishesAt }))
            .apply()
    }

    fun schedule(context: Context, timer: FoundryTimer) {
        val delay = (timer.finishesAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<FoundryTimerWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(FoundryTimerWorker.KEY_TIMER_ID, timer.id)
                    .putString(FoundryTimerWorker.KEY_TIMER_NAME, timer.name)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_PREFIX + timer.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, timerId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + timerId)
    }

    internal fun encode(timers: List<FoundryTimer>): String = Gson().toJson(timers.sortedBy { it.finishesAt })

    internal fun decode(json: String?): List<FoundryTimer> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<FoundryTimer>>() {}.type
        return runCatching { Gson().fromJson<List<FoundryTimer>>(json, type) }
            .getOrDefault(emptyList())
            .filter { it.id.isNotBlank() && it.name.isNotBlank() && it.finishesAt >= it.createdAt }
            .distinctBy { it.id }
            .sortedBy { it.finishesAt }
    }
}

internal fun foundryRemainingLabel(remainingMillis: Long, german: Boolean): String {
    if (remainingMillis <= 0L) return if (german) "Fertig" else "Ready"
    val totalMinutes = (remainingMillis + 59_999L) / 60_000L
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60
    return buildList {
        if (days > 0) add("${days}d")
        if (hours > 0 || days > 0) add("${hours}h")
        add("${minutes}m")
    }.joinToString(" ")
}
