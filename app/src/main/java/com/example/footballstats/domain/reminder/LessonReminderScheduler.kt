package com.example.footballstats.domain.reminder

interface LessonReminderScheduler {

    fun areNotificationsEnabled(): Boolean

    fun canScheduleExactAlarms(): Boolean

    fun schedule(
        fullName: String,
        triggerAtMillis: Long
    )
}