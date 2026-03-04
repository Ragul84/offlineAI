package com.edgeai.tutorlite.data.repository

import com.edgeai.tutorlite.domain.model.ModelVariant
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val selectedModel: StateFlow<ModelVariant>
    val analyticsEnabled: StateFlow<Boolean>
    val selectedLanguage: StateFlow<String>
    val lowRamModeEnabled: StateFlow<Boolean>
    val wifiOnlyDownloads: StateFlow<Boolean>
    val streakNotificationsEnabled: StateFlow<Boolean>

    suspend fun setModel(model: ModelVariant)
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setLanguage(languageCode: String)
    suspend fun setLowRamModeEnabled(enabled: Boolean)
    suspend fun setWifiOnlyDownloads(enabled: Boolean)
    suspend fun setStreakNotificationsEnabled(enabled: Boolean)
}
