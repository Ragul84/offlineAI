package com.edgeai.tutorlite.ui.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.domain.model.TutorPrompt
import com.edgeai.tutorlite.service.ai.InferenceService
import com.edgeai.tutorlite.service.analytics.AnalyticsService
import com.edgeai.tutorlite.service.study.StudyPipelineStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val inferenceService: InferenceService,
    private val settingsRepository: SettingsRepository,
    private val analyticsService: AnalyticsService,
    private val studyPipelineStore: StudyPipelineStore
) : ViewModel() {

    data class UiState(
        val prompt: String = "",
        val subject: String = "Math",
        val result: String = "",
        val imagePath: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onPromptChange(value: String) = _uiState.update { it.copy(prompt = value) }
    fun onSubjectChange(value: String) = _uiState.update { it.copy(subject = value) }
    fun onImageCaptured(path: String) = _uiState.update { it.copy(imagePath = path, error = null) }

    fun analyze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val model = settingsRepository.selectedModel.value
            val promptText = _uiState.value.prompt.ifBlank { "Explain what is in this photo for a student." }
            val result = inferenceService.runPrompt(
                TutorPrompt(
                    prompt = promptText,
                    subject = _uiState.value.subject,
                    imagePath = _uiState.value.imagePath,
                    mode = "camera_explain"
                ),
                model
            )
            result.onSuccess {
                analyticsService.logEvent("camera_analyze_success", _uiState.value.subject, it.confidence.toDouble())
                analyticsService.logStudySession(
                    subject = _uiState.value.subject,
                    durationMinutes = 2,
                    score = (it.confidence * 100f).toInt()
                )
                studyPipelineStore.publishArtifact(
                    StudyPipelineStore.Artifact(
                        source = "camera",
                        subject = _uiState.value.subject,
                        rawInput = promptText,
                        processedOutput = it.text
                    )
                )
                _uiState.update { state ->
                    state.copy(isLoading = false, result = it.text)
                }
            }.onFailure {
                analyticsService.logEvent("camera_analyze_failed", _uiState.value.subject, null)
                _uiState.update { state ->
                    state.copy(isLoading = false, error = it.message ?: "Analysis failed")
                }
            }
        }
    }
}
