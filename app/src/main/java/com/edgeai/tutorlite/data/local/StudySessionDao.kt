package com.edgeai.tutorlite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<StudySessionEntity>>

    @Query(
        """
        SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch', 'localtime') AS day,
               AVG(score) AS avgScore
        FROM study_sessions
        GROUP BY day
        ORDER BY day DESC
        LIMIT :days
        """
    )
    fun observeDailyAverageScore(days: Int = 14): Flow<List<DailyScorePoint>>

    @Query(
        """
        SELECT subject AS subject,
               AVG(score) AS avgScore
        FROM study_sessions
        GROUP BY subject
        ORDER BY avgScore DESC
        """
    )
    fun observeSubjectAverages(): Flow<List<SubjectAveragePoint>>

    @Query("SELECT MAX(timestamp) FROM study_sessions")
    suspend fun latestSessionTimestamp(): Long?

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}

data class DailyScorePoint(
    val day: String,
    val avgScore: Float
)

data class SubjectAveragePoint(
    val subject: String,
    val avgScore: Float
)
