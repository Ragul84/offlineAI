package com.edgeai.tutorlite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AnalyticsEventEntity)

    @Query(
        """
        SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch', 'localtime') AS day,
               COUNT(*) AS total
        FROM analytics_events
        GROUP BY day
        ORDER BY day DESC
        LIMIT :days
        """
    )
    fun observeDailyEventCounts(days: Int = 14): Flow<List<DailyEventCount>>

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<AnalyticsEventEntity>>

    @Query("DELETE FROM analytics_events")
    suspend fun clearAll()
}

data class DailyEventCount(
    val day: String,
    val total: Int
)
