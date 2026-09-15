package com.example.tennofreunde.system

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FoundryTimerWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val name = inputData.getString(KEY_TIMER_NAME)?.trim().orEmpty()
        if (name.isBlank()) return Result.failure()
        val german = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("app_language", "de") == "de"
        TennoSystem.showReminderNotification(
            applicationContext,
            if (german) "Foundry fertig" else "Foundry ready",
            if (german) "$name kann jetzt abgeholt werden." else "$name is ready to claim.",
            6200 + (inputData.getString(KEY_TIMER_ID)?.hashCode() ?: name.hashCode()).and(0x3FF)
        )
        TennoWidgetProvider.updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        const val KEY_TIMER_ID = "timer_id"
        const val KEY_TIMER_NAME = "timer_name"
    }
}
