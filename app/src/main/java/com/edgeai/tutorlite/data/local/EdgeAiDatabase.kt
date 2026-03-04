package com.edgeai.tutorlite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StudySessionEntity::class, AnalyticsEventEntity::class],
    version = 2,
    exportSchema = true
)
abstract class EdgeAiDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun analyticsEventDao(): AnalyticsEventDao
}
