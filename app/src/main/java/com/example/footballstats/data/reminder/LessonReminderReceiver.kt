package com.example.footballstats.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.footballstats.MainActivity
import com.example.footballstats.R

class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return

        createNotificationChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!areNotificationsEnabled(context)) return

        val fullName = intent
            .getStringExtra(EXTRA_FULL_NAME)
            .orEmpty()
            .ifBlank { "Пользователь" }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val message = "$fullName, начинается ваша любимая пара!"

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_lesson_notification)
            .setContentTitle("Время любимой пары")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID,
                notification
            )
        } catch (_: SecurityException) {
            // Разрешение могло быть отозвано после проверки.
        }
    }

    companion object {
        internal const val ACTION_SHOW_REMINDER =
            "com.example.footballstats.action.SHOW_LESSON_REMINDER"

        internal const val EXTRA_FULL_NAME = "extra_full_name"

        private const val CHANNEL_ID = "lesson_reminders"
        private const val NOTIFICATION_ID = 7002
        private const val OPEN_APP_REQUEST_CODE = 7003

        internal fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val manager = context.getSystemService(
                NotificationManager::class.java
            ) ?: return

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о паре",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Уведомления о начале любимой пары"
            }

            manager.createNotificationChannel(channel)
        }

        internal fun areNotificationsEnabled(context: Context): Boolean {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            if (
                !NotificationManagerCompat
                    .from(context)
                    .areNotificationsEnabled()
            ) {
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(
                    NotificationManager::class.java
                ) ?: return false

                val channel = manager.getNotificationChannel(CHANNEL_ID)

                if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                    return false
                }
            }

            return true
        }
    }
}