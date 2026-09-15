package com.example.tennofreunde.system

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.tennofreunde.MainActivity
import com.example.tennofreunde.R
import com.example.tennofreunde.data.FoundryTimerStore
import com.example.tennofreunde.data.foundryRemainingLabel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TennoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> manager.updateAppWidget(id, buildViews(context)) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TennoWidgetProvider::class.java))
            ids.forEach { id -> manager.updateAppWidget(id, buildViews(context)) }
        }

        private fun buildViews(context: Context): RemoteViews {
            val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val data = context.getSharedPreferences("tenno_data", Context.MODE_PRIVATE)
            val hub = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE)
            val profile = data.getString("active_profile", "Tenno") ?: "Tenno"
            val language = settings.getString("app_language", "de") ?: "de"
            val german = language == "de"
            val priorities = hub.getStringSet("favorite_priorities", emptySet()).orEmpty().count { it.endsWith("|now") }
            val baseSubtitle = if (german) {
                "Profil $profile · $priorities Farmziele"
            } else {
                "Profile $profile · $priorities farm targets"
            }
            val now = System.currentTimeMillis()
            val timers = FoundryTimerStore.load(context)
            val readyCount = timers.count { it.finishesAt <= now }
            val foundry = when {
                readyCount > 0 -> if (german) " · Foundry: $readyCount fertig" else " · Foundry: $readyCount ready"
                timers.isNotEmpty() -> {
                    val next = timers.first()
                    " · ${next.name}: ${foundryRemainingLabel(next.finishesAt - now, german)}"
                }
                else -> ""
            }
            val subtitle = baseSubtitle + foundry
            val updated = DateTimeFormatter.ofPattern("HH:mm").format(LocalTime.now())
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            return RemoteViews(context.packageName, R.layout.tenno_widget).apply {
                setTextViewText(R.id.widget_title, "TennoFreunde")
                setTextViewText(R.id.widget_subtitle, subtitle)
                setTextViewText(R.id.widget_footer, if (german) "Aktualisiert $updated" else "Updated $updated")
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }
        }
    }
}
