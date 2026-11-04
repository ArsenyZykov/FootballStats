package com.example.footballstats.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.footballstats.domain.reminder.LessonReminderScheduler

class AndroidLessonReminderScheduler(
    context: Context
) : LessonReminderScheduler {

    private val context = context.applicationContext

    private val alarmManager = requireNotNull(
        this.context.getSystemService(AlarmManager::class.java)
    )

    init {
        LessonReminderReceiver.createNotificationChannel(this.context)
    }

    override fun areNotificationsEnabled(): Boolean =
        LessonReminderReceiver.areNotificationsEnabled(context)

    override fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

    override fun schedule(
        fullName: String,
        triggerAtMillis: Long
    ) {
        check(areNotificationsEnabled()) {
            "Разрешите уведомления приложения."
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            throw IllegalStateException(
                "Разрешите точные напоминания."
            )
        }

        require(triggerAtMillis > System.currentTimeMillis()) {
            "Выбранное время уже прошло."
        }

        val intent = Intent(
            context,
            LessonReminderReceiver::class.java
        ).apply {
            action = LessonReminderReceiver.ACTION_SHOW_REMINDER

            putExtra(
                LessonReminderReceiver.EXTRA_FULL_NAME,
                fullName
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private companion object {
        const val REMINDER_REQUEST_CODE = 7001
    }
}