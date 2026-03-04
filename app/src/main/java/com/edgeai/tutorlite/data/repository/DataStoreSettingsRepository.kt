package com.edgeai.tutorlite.data.repository

import android.app.ActivityManager
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.edgeai.tutorlite.domain.model.ModelVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val defaultLowRamMode = isBudgetDevice(context)

    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { File(context.filesDir, "settings.preferences_pb") }
    )

    private val _selectedModel = MutableStateFlow(ModelVariant.QWEN_08B)
    private val _analyticsEnabled = MutableStateFlow(false)
    private val _selectedLanguage = MutableStateFlow("auto")
    private val _lowRamModeEnabled = MutableStateFlow(defaultLowRamMode)
    private val _wifiOnlyDownloads = MutableStateFlow(false)
    private val _streakNotificationsEnabled = MutableStateFlow(true)

    override val selectedModel: StateFlow<ModelVariant> = _selectedModel.asStateFlow()
    override val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()
    override val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    override val lowRamModeEnabled: StateFlow<Boolean> = _lowRamModeEnabled.asStateFlow()
    override val wifiOnlyDownloads: StateFlow<Boolean> = _wifiOnlyDownloads.asStateFlow()
    override val streakNotificationsEnabled: StateFlow<Boolean> = _streakNotificationsEnabled.asStateFlow()

    init {
        scope.launch {
            val prefs = dataStore.data.first()
            _selectedModel.value = prefs[KEY_MODEL]
                ?.let { runCatching { ModelVariant.valueOf(it) }.getOrNull() }
                ?: ModelVariant.QWEN_08B
            _analyticsEnabled.value = prefs[KEY_ANALYTICS] ?: false
            _selectedLanguage.value = prefs[KEY_LANGUAGE] ?: "auto"
            _lowRamModeEnabled.value = prefs[KEY_LOW_RAM_MODE] ?: defaultLowRamMode
            _wifiOnlyDownloads.value = prefs[KEY_WIFI_ONLY_DOWNLOADS] ?: false
            _streakNotificationsEnabled.value = prefs[KEY_STREAK_NOTIFICATIONS] ?: true
        }
    }

    override suspend fun setModel(model: ModelVariant) {
        dataStore.edit { it[KEY_MODEL] = model.name }
        _selectedModel.value = model
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_ANALYTICS] = enabled }
        _analyticsEnabled.value = enabled
    }

    override suspend fun setLanguage(languageCode: String) {
        dataStore.edit { it[KEY_LANGUAGE] = languageCode }
        _selectedLanguage.value = languageCode
    }

    override suspend fun setLowRamModeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_LOW_RAM_MODE] = enabled }
        _lowRamModeEnabled.value = enabled
    }

    override suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        dataStore.edit { it[KEY_WIFI_ONLY_DOWNLOADS] = enabled }
        _wifiOnlyDownloads.value = enabled
    }

    override suspend fun setStreakNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_STREAK_NOTIFICATIONS] = enabled }
        _streakNotificationsEnabled.value = enabled
    }

    companion object {
        private val KEY_MODEL = stringPreferencesKey("selected_model")
        private val KEY_ANALYTICS = booleanPreferencesKey("analytics_enabled")
        private val KEY_LANGUAGE = stringPreferencesKey("selected_language")
        private val KEY_LOW_RAM_MODE = booleanPreferencesKey("low_ram_mode_enabled")
        private val KEY_WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        private val KEY_STREAK_NOTIFICATIONS = booleanPreferencesKey("streak_notifications_enabled")

        private fun isBudgetDevice(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            return am.isLowRamDevice || totalRamGb <= 4.5
        }
    }
}
