package com.edgeai.tutorlite.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.domain.model.ModelVariant
import com.edgeai.tutorlite.domain.model.TutorPrompt
import com.edgeai.tutorlite.service.ai.InferenceService
import com.edgeai.tutorlite.service.ai.ModelDownloader
import com.edgeai.tutorlite.service.ai.ModelStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ModelSetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelDownloader: ModelDownloader,
    private val modelStore: ModelStore,
    private val inferenceService: InferenceService
) : ViewModel() {

    data class UiState(
        val selectedModel: ModelVariant = ModelVariant.QWEN_08B,
        val downloading: Boolean = false,
        val progress: Int = 0,
        val error: String? = null,
        val downloaded: Boolean = false,
        val testResult: String? = null,
        val testing: Boolean = false
    )

    val uiState: StateFlow<UiState> = combine(
        settingsRepository.selectedModel,
        modelDownloader.state
    ) { model, dl ->
        UiState(
            selectedModel = model,
            downloading = dl.isDownloading,
            progress = dl.progressPercent,
            error = dl.errorMessage,
            downloaded = modelStore.isDownloaded(model)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    private val _testResult = MutableStateFlow<String?>(null)
    private val _testing = MutableStateFlow(false)

    val mergedUiState: StateFlow<UiState> = combine(uiState, _testResult, _testing) { base, test, testing ->
        base.copy(testResult = test, testing = testing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    fun selectModel(model: ModelVariant) {
        viewModelScope.launch {
            settingsRepository.setModel(model)
            _testResult.value = null
        }
    }

    fun downloadSelectedModel() {
        viewModelScope.launch {
            _testResult.value = null
            modelDownloader.download(uiState.value.selectedModel)
        }
    }

    fun testModel() {
        viewModelScope.launch {
            _testing.value = true
            val result = inferenceService.runPrompt(
                TutorPrompt(
                    prompt = "Say hello in one line.",
                    subject = "General",
                    mode = "setup_test"
                ),
                uiState.value.selectedModel
            )
            _testResult.value = if (result.isSuccess) {
                "Model ready."
            } else {
                "Model test failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            }
            _testing.value = false
        }
    }
}

