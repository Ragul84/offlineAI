package com.edgeai.tutorlite.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.edgeai.tutorlite.data.repository.AnalyticsRepository
import com.edgeai.tutorlite.service.notification.StreakNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val analyticsRepository: AnalyticsRepository,
    private val notificationManager: StreakNotificationManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        notificationManager.ensureChannel()

        val latestTimestamp = analyticsRepository.latestStudyTimestamp() ?: return Result.success()
        val latestDate = Instant.ofEpochMilli(latestTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()

        if (latestDate.isBefore(today)) {
            val streak = max(1, inputData.getInt(KEY_CURRENT_STREAK, 1))
            notificationManager.notifyStreakReminder(streak)
        }
        return Result.success()
    }

    companion object {
        const val KEY_CURRENT_STREAK = "current_streak"
    }
}
