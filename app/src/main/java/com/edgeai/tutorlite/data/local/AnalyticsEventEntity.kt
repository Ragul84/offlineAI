package com.edgeai.tutorlite.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String,
    val subject: String?,
    val value: Double?,
    val timestamp: Long
)
