package com.edgeai.tutorlite.ui.screens.scanner

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
class ScannerViewModel @Inject constructor(
    private val inferenceService: InferenceService,
    private val settingsRepository: SettingsRepository,
    private val analyticsService: AnalyticsService,
    private val studyPipelineStore: StudyPipelineStore
) : ViewModel() {

    data class UiState(
        val rawText: String = "",
        val cleanText: String = "",
        val isProcessing: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onRawTextChange(value: String) = _uiState.update { it.copy(rawText = value, error = null) }
    fun onOcrExtracted(value: String) = _uiState.update { it.copy(rawText = value, error = null) }

    fun cleanupNotes() {
        viewModelScope.launch {
            if (_uiState.value.rawText.isBlank()) {
                _uiState.update { it.copy(error = "Scan text first or paste OCR content.") }
                return@launch
            }
            _uiState.update { it.copy(isProcessing = true, error = null) }
            val model = settingsRepository.selectedModel.value
            val response = inferenceService.runPrompt(
                TutorPrompt(
                    prompt = "Cleanup and structure: ${_uiState.value.rawText}",
                    subject = "Language",
                    mode = "note_cleanup"
                ),
                model
            )
            val cleaned = response.getOrNull()?.text ?: "Could not clean notes"
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    cleanText = cleaned,
                    error = response.exceptionOrNull()?.message
                )
            }
            if (response.isSuccess) {
                analyticsService.logEvent("scanner_cleanup_success", "Language", 1.0)
                analyticsService.logStudySession("Language", durationMinutes = 3, score = 76)
                studyPipelineStore.publishArtifact(
                    StudyPipelineStore.Artifact(
                        source = "scanner",
                        subject = "Language",
                        rawInput = _uiState.value.rawText,
                        processedOutput = cleaned
                    )
                )
            } else {
                analyticsService.logEvent("scanner_cleanup_failed", "Language", null)
            }
        }
    }
}
