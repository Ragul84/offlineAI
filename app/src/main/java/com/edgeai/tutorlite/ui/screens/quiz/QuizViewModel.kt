package com.edgeai.tutorlite.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class QuizViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val studyPipelineStore: StudyPipelineStore
) : ViewModel() {
    data class Flashcard(val front: String, val back: String)

    data class UiState(
        val streak: Int = 4,
        val progress: Float = 0.62f,
        val sourceLabel: String = "General",
        val flashcards: List<Flashcard> = listOf(
            Flashcard("What is photosynthesis?", "Plants convert light into energy."),
            Flashcard("2x + 4 = 10", "x = 3"),
            Flashcard("Capital of Tamil Nadu", "Chennai")
        )
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studyPipelineStore.latestArtifact.collectLatest { artifact ->
                if (artifact == null) return@collectLatest
                _uiState.update {
                    it.copy(
                        sourceLabel = "${artifact.source.uppercase()} - ${artifact.subject}",
                        flashcards = generateFlashcards(artifact.processedOutput)
                    )
                }
            }
        }
    }

    fun markQuizCompleted(score: Int) {
        viewModelScope.launch {
            val clamped = score.coerceIn(0, 100)
            _uiState.update {
                it.copy(
                    streak = it.streak + 1,
                    progress = ((it.progress * 100f + clamped) / 200f).coerceIn(0f, 1f)
                )
            }
            analyticsService.logEvent("quiz_completed", "Quiz", clamped.toDouble())
            analyticsService.logStudySession("Quiz", durationMinutes = 5, score = clamped)
        }
    }

    private fun generateFlashcards(text: String): List<Flashcard> {
        val cleaned = text
            .replace("\n", ". ")
            .split(".")
            .map { it.trim() }
            .filter { it.length > 12 }
            .take(6)
        if (cleaned.isEmpty()) {
            return listOf(Flashcard("No session content yet", "Run Camera or Scanner first."))
        }
        return cleaned.mapIndexed { index, sentence ->
            Flashcard(
                front = "Key Point ${index + 1}",
                back = sentence.take(180)
            )
        }
    }
}
