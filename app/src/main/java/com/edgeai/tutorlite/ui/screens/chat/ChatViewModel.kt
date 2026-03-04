package com.edgeai.tutorlite.ui.screens.chat

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val inferenceService: InferenceService,
    private val settingsRepository: SettingsRepository,
    private val analyticsService: AnalyticsService,
    private val studyPipelineStore: StudyPipelineStore
) : ViewModel() {

    data class Message(val text: String, val fromUser: Boolean)

    data class UiState(
        val messages: List<Message> = emptyList(),
        val draft: String = "",
        val voiceOnly: Boolean = false,
        val loading: Boolean = false,
        val languageCode: String = "auto"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.selectedLanguage.collectLatest { code ->
                _uiState.update { it.copy(languageCode = code) }
            }
        }
    }

    fun setDraft(text: String) = _uiState.update { it.copy(draft = text) }
    fun toggleVoiceOnly() = _uiState.update { it.copy(voiceOnly = !it.voiceOnly) }

    fun send() {
        val draft = _uiState.value.draft.trim()
        if (draft.isBlank()) return

        _uiState.update {
            it.copy(
                messages = it.messages + Message(draft, true),
                draft = "",
                loading = true
            )
        }

        viewModelScope.launch {
            val model = settingsRepository.selectedModel.value
            val result = inferenceService.runPrompt(
                TutorPrompt(prompt = draft, subject = "General", mode = "chat"),
                model
            )

            val text = result.getOrNull()?.text ?: "I could not process that offline."
            _uiState.update {
                it.copy(messages = it.messages + Message(text, false), loading = false)
            }
            if (result.isSuccess) {
                studyPipelineStore.publishArtifact(
                    StudyPipelineStore.Artifact(
                        source = "chat",
                        subject = "General",
                        rawInput = draft,
                        processedOutput = text
                    )
                )
                analyticsService.logEvent("chat_response_success", "General", 1.0)
                analyticsService.logStudySession("General", durationMinutes = 2, score = 74)
            } else {
                analyticsService.logEvent("chat_response_failed", "General", null)
            }
        }
    }
}
