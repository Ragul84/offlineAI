package com.edgeai.tutorlite.service.analytics

import android.os.Bundle
import com.edgeai.tutorlite.data.repository.AnalyticsRepository
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class AnalyticsService @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository,
    private val firebaseAnalytics: FirebaseAnalytics
) {

    val analyticsEnabled: StateFlow<Boolean> = settingsRepository.analyticsEnabled

    suspend fun logEvent(name: String, subject: String? = null, value: Double? = null) {
        analyticsRepository.trackEvent(name, subject, value)
        if (!settingsRepository.analyticsEnabled.value) return

        val bundle = Bundle().apply {
            if (subject != null) putString("subject", subject)
            if (value != null) putDouble("value", value)
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    suspend fun logStudySession(subject: String, durationMinutes: Int, score: Int) {
        analyticsRepository.trackStudySession(subject, durationMinutes, score)
        if (!settingsRepository.analyticsEnabled.value) return

        val bundle = Bundle().apply {
            putString("subject", subject)
            putLong("duration_minutes", durationMinutes.toLong())
            putLong("score", score.toLong())
        }
        firebaseAnalytics.logEvent("study_session", bundle)
    }
}
