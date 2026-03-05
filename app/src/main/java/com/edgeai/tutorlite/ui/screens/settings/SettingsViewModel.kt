package com.edgeai.tutorlite.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.domain.model.TutorPrompt
import com.edgeai.tutorlite.domain.model.ModelVariant
import com.edgeai.tutorlite.service.ai.InferenceService
import com.edgeai.tutorlite.service.ai.ModelDownloader
import com.edgeai.tutorlite.service.analytics.AnalyticsService
import com.edgeai.tutorlite.service.storage.AppStorageManager
import com.edgeai.tutorlite.worker.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelDownloader: ModelDownloader,
    private val inferenceService: InferenceService,
    private val analyticsService: AnalyticsService,
    private val appStorageManager: AppStorageManager,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private data class PrefsSnapshot(
        val model: ModelVariant,
        val analyticsEnabled: Boolean,
        val language: String,
        val lowRamModeEnabled: Boolean,
        val wifiOnlyDownloads: Boolean,
        val streakNotificationsEnabled: Boolean
    )

    data class UiState(
        val model: ModelVariant,
        val analyticsEnabled: Boolean,
        val language: String,
        val lowRamModeEnabled: Boolean,
        val wifiOnlyDownloads: Boolean,
        val streakNotificationsEnabled: Boolean,
        val downloading: Boolean,
        val progress: Int,
        val error: String?,
        val storageUsedBytes: Long,
        val storageModelBytes: Long,
        val storageCacheBytes: Long,
        val storageDbBytes: Long,
        val benchmarkMs: Long?,
        val benchmarkError: String?
    )

    private val storageStats = MutableStateFlow(
        AppStorageManager.StorageStats(modelsBytes = 0L, cacheBytes = 0L, dbBytes = 0L)
    )
    private val benchmarkMs = MutableStateFlow<Long?>(null)
    private val benchmarkError = MutableStateFlow<String?>(null)
    private val prefsSnapshot: StateFlow<PrefsSnapshot> = combine(
        settingsRepository.selectedModel,
        settingsRepository.analyticsEnabled,
        settingsRepository.selectedLanguage,
        settingsRepository.lowRamModeEnabled,
        settingsRepository.wifiOnlyDownloads
    ) { model, analytics, language, lowRam, wifiOnly ->
        PrefsSnapshot(
            model = model,
            analyticsEnabled = analytics,
            language = language,
            lowRamModeEnabled = lowRam,
            wifiOnlyDownloads = wifiOnly,
            streakNotificationsEnabled = true
        )
    }.combine(settingsRepository.streakNotificationsEnabled) { current, streak ->
        current.copy(
            streakNotificationsEnabled = streak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PrefsSnapshot(
            model = ModelVariant.QWEN_08B,
            analyticsEnabled = false,
            language = "auto",
            lowRamModeEnabled = true,
            wifiOnlyDownloads = false,
            streakNotificationsEnabled = true
        )
    )

    val uiState: StateFlow<UiState> = combine(
        prefsSnapshot,
        storageStats,
        benchmarkMs,
        benchmarkError,
        modelDownloader.state
    ) { prefs, storage, runMs, runError, dl ->
        UiState(
            model = prefs.model,
            analyticsEnabled = prefs.analyticsEnabled,
            language = prefs.language,
            lowRamModeEnabled = prefs.lowRamModeEnabled,
            wifiOnlyDownloads = prefs.wifiOnlyDownloads,
            streakNotificationsEnabled = prefs.streakNotificationsEnabled,
            downloading = dl.isDownloading,
            progress = dl.progressPercent,
            error = dl.errorMessage,
            storageUsedBytes = storage.totalBytes,
            storageModelBytes = storage.modelsBytes,
            storageCacheBytes = storage.cacheBytes,
            storageDbBytes = storage.dbBytes,
            benchmarkMs = runMs,
            benchmarkError = runError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(
            model = ModelVariant.QWEN_08B,
            analyticsEnabled = false,
            language = "auto",
            lowRamModeEnabled = true,
            wifiOnlyDownloads = false,
            streakNotificationsEnabled = true,
            downloading = false,
            progress = 0,
            error = null,
            storageUsedBytes = 0L,
            storageModelBytes = 0L,
            storageCacheBytes = 0L,
            storageDbBytes = 0L,
            benchmarkMs = null,
            benchmarkError = null
        )
    )

    init {
        refreshStorageStats()
    }

    fun setModel(model: ModelVariant) {
        viewModelScope.launch {
            settingsRepository.setModel(model)
            analyticsService.logEvent("model_variant_selected", model.name, null)
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(code)
            analyticsService.logEvent("language_changed", code, null)
        }
    }

    fun setAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnalyticsEnabled(enabled)
            analyticsService.logEvent("analytics_consent_changed", null, if (enabled) 1.0 else 0.0)
        }
    }

    fun setLowRamMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLowRamModeEnabled(enabled)
            analyticsService.logEvent("low_ram_mode_changed", null, if (enabled) 1.0 else 0.0)
        }
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWifiOnlyDownloads(enabled)
            analyticsService.logEvent("wifi_only_downloads_changed", null, if (enabled) 1.0 else 0.0)
        }
    }

    fun setStreakNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStreakNotificationsEnabled(enabled)
            workScheduler.configureStreakReminder(enabled)
            analyticsService.logEvent("streak_notifications_changed", null, if (enabled) 1.0 else 0.0)
        }
    }

    fun downloadModel() {
        viewModelScope.launch {
            analyticsService.logEvent("model_download_started", uiState.value.model.name, null)
            modelDownloader.download(uiState.value.model)
            refreshStorageStats()
        }
    }

    fun clearData() {
        viewModelScope.launch {
            settingsRepository.setModel(ModelVariant.QWEN_08B)
            settingsRepository.setAnalyticsEnabled(false)
            settingsRepository.setLanguage("auto")
            settingsRepository.setLowRamModeEnabled(true)
            settingsRepository.setWifiOnlyDownloads(false)
            settingsRepository.setStreakNotificationsEnabled(true)
            workScheduler.configureStreakReminder(true)
            appStorageManager.clearUserData()
            refreshStorageStats()
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            storageStats.value = appStorageManager.readStats()
        }
    }

    fun runQuickBenchmark() {
        viewModelScope.launch {
            benchmarkError.value = null
            benchmarkMs.value = null
            val start = System.currentTimeMillis()
            val result = inferenceService.runPrompt(
                TutorPrompt(
                    prompt = "Explain fraction 3/4 in one line.",
                    subject = "Math",
                    mode = "benchmark"
                ),
                uiState.value.model
            )
            if (result.isSuccess) {
                benchmarkMs.value = System.currentTimeMillis() - start
                analyticsService.logEvent("quick_benchmark_success", uiState.value.model.name, benchmarkMs.value?.toDouble())
            } else {
                benchmarkError.value = result.exceptionOrNull()?.message ?: "Benchmark failed. Download model first."
                analyticsService.logEvent("quick_benchmark_failed", uiState.value.model.name, null)
            }
        }
    }
}
