package com.example.tennofreunde.system

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.tennofreunde.BuildConfig
import com.example.tennofreunde.MainActivity
import com.example.tennofreunde.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TennoUpdateMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseMessaging.getInstance().subscribeToTopic(UPDATE_TOPIC)
            .addOnFailureListener { TennoSystem.appendCrashLog(this, it) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val version = message.data["version"].orEmpty().trim().removePrefix("v")
        if (version.isBlank() || !isNewerVersion(version, BuildConfig.VERSION_NAME)) return

        val preferences = getSharedPreferences("settings", MODE_PRIVATE)
        if (preferences.getString("last_update_push_version", "") == version) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        TennoSystem.createNotificationChannels(this)

        val releaseUrl = message.data["url"].orEmpty()
        val intent = if (releaseUrl.startsWith("https://")) {
            Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
        } else {
            Intent(this, MainActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            version.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = message.data["title"].orEmpty().ifBlank { "TennoFreunde $version" }
        val body = message.data["body"].orEmpty().ifBlank {
            "Eine neue App-Version ist auf GitHub verfügbar."
        }
        val notification = NotificationCompat.Builder(this, TennoSystem.CHANNEL_UPDATES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify(UPDATE_NOTIFICATION_ID, notification)
        preferences.edit().putString("last_update_push_version", version).apply()
    }

    companion object {
        const val UPDATE_TOPIC = "app_updates"
        private const val UPDATE_NOTIFICATION_ID = 6317

        internal fun isNewerVersion(remote: String, current: String): Boolean {
            val remoteParts = remote.versionParts()
            val currentParts = current.versionParts()
            val count = maxOf(remoteParts.size, currentParts.size)
            return (0 until count).firstNotNullOfOrNull { index ->
                val remotePart = remoteParts.getOrElse(index) { 0 }
                val currentPart = currentParts.getOrElse(index) { 0 }
                when {
                    remotePart > currentPart -> true
                    remotePart < currentPart -> false
                    else -> null
                }
            } ?: false
        }

        private fun String.versionParts(): List<Int> =
            trim().removePrefix("v").split(".").map { part ->
                part.takeWhile(Char::isDigit).toIntOrNull() ?: 0
            }
    }
}
