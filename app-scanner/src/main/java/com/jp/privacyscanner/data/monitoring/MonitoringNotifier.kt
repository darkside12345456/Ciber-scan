package com.jp.privacyscanner.data.monitoring

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
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import com.jp.privacyscanner.MainActivity
import com.jp.privacyscanner.R

/**
 * Cria o canal de notificações e emite avisos quando a monitorização deteta
 * novas permissões sensíveis. Só notifica quando há algo relevante — nunca
 * ruído "está tudo bem".
 */
object MonitoringNotifier {

    const val CHANNEL_ID = "privacy_monitoring"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorização de privacidade",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisa quando uma app passa a ter permissões sensíveis."
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** Emite uma notificação a resumir as alterações detetadas. */
    fun notifyChanges(context: Context, changes: List<MonitoringDiff.Change>) {
        if (changes.isEmpty()) return
        if (!hasPermission(context)) return

        val title = if (changes.size == 1) {
            "1 app com novas permissões sensíveis"
        } else {
            "${changes.size} apps com novas permissões sensíveis"
        }
        val body = changes.joinToString("\n") { change ->
            val perms = change.newPermissions.joinToString(", ") { it.substringAfterLast('.') }
            "• ${change.packageName}: $perms"
        }

        val openIntent = PendingIntentCompat.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Toca para rever.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
