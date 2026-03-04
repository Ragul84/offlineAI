package com.edgeai.tutorlite.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleModelHealthCheck() {
        val request = PeriodicWorkRequestBuilder<ModelHealthWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MODEL_HEALTH_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleStreakReminder() {
        val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STREAK_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelStreakReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(STREAK_REMINDER_WORK)
    }

    fun configureStreakReminder(enabled: Boolean) {
        if (enabled) scheduleStreakReminder() else cancelStreakReminder()
    }

    companion object {
        const val MODEL_HEALTH_WORK = "model_health_work"
        const val STREAK_REMINDER_WORK = "streak_reminder_work"
    }
}
