package com.edgeai.tutorlite.data.repository

import com.edgeai.tutorlite.data.local.AnalyticsEventDao
import com.edgeai.tutorlite.data.local.AnalyticsEventEntity
import com.edgeai.tutorlite.data.local.DailyEventCount
import com.edgeai.tutorlite.data.local.DailyScorePoint
import com.edgeai.tutorlite.data.local.StudySessionDao
import com.edgeai.tutorlite.data.local.StudySessionEntity
import com.edgeai.tutorlite.data.local.SubjectAveragePoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomAnalyticsRepository @Inject constructor(
    private val analyticsEventDao: AnalyticsEventDao,
    private val studySessionDao: StudySessionDao
) : AnalyticsRepository {

    override suspend fun trackEvent(eventName: String, subject: String?, value: Double?) {
        analyticsEventDao.insert(
            AnalyticsEventEntity(
                eventName = eventName,
                subject = subject,
                value = value,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun trackStudySession(subject: String, durationMinutes: Int, score: Int) {
        val now = System.currentTimeMillis()
        studySessionDao.insert(
            StudySessionEntity(
                subject = subject,
                durationMinutes = durationMinutes,
                score = score,
                timestamp = now
            )
        )
        trackEvent(eventName = "study_session", subject = subject, value = score.toDouble())
    }

    override fun observeDailyEventCounts(days: Int): Flow<List<DailyEventCount>> =
        analyticsEventDao.observeDailyEventCounts(days)

    override fun observeDailyScores(days: Int): Flow<List<DailyScorePoint>> =
        studySessionDao.observeDailyAverageScore(days)

    override fun observeSubjectPerformance(): Flow<List<SubjectAveragePoint>> =
        studySessionDao.observeSubjectAverages()

    override fun observeRecentEvents(limit: Int): Flow<List<AnalyticsEventEntity>> =
        analyticsEventDao.observeRecent(limit)

    override suspend fun latestStudyTimestamp(): Long? = studySessionDao.latestSessionTimestamp()

    override suspend fun clearAll() {
        analyticsEventDao.clearAll()
        studySessionDao.clearAll()
    }
}
