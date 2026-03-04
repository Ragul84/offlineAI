package com.edgeai.tutorlite.data.repository

import com.edgeai.tutorlite.data.local.AnalyticsEventEntity
import com.edgeai.tutorlite.data.local.DailyEventCount
import com.edgeai.tutorlite.data.local.DailyScorePoint
import com.edgeai.tutorlite.data.local.SubjectAveragePoint
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun trackEvent(eventName: String, subject: String? = null, value: Double? = null)
    suspend fun trackStudySession(subject: String, durationMinutes: Int, score: Int)

    fun observeDailyEventCounts(days: Int = 14): Flow<List<DailyEventCount>>
    fun observeDailyScores(days: Int = 14): Flow<List<DailyScorePoint>>
    fun observeSubjectPerformance(): Flow<List<SubjectAveragePoint>>
    fun observeRecentEvents(limit: Int = 50): Flow<List<AnalyticsEventEntity>>

    suspend fun latestStudyTimestamp(): Long?
    suspend fun clearAll()
}
