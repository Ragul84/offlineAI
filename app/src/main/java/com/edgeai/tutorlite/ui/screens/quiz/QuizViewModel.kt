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
    data class QuizQuestion(
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        val selectedIndex: Int? = null,
        val submitted: Boolean = false
    )

    data class UiState(
        val streak: Int = 4,
        val progress: Float = 0.62f,
        val sourceLabel: String = "General",
        val flashcards: List<Flashcard> = listOf(
            Flashcard("What is photosynthesis?", "Plants convert light into energy."),
            Flashcard("2x + 4 = 10", "x = 3"),
            Flashcard("Capital of Tamil Nadu", "Chennai")
        ),
        val quizQuestion: QuizQuestion? = null,
        val quizResult: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studyPipelineStore.latestArtifact.collectLatest { artifact ->
                if (artifact == null) return@collectLatest
                val cards = generateFlashcards(artifact.processedOutput)
                _uiState.update {
                    it.copy(
                        sourceLabel = "${artifact.source.uppercase()} - ${artifact.subject}",
                        flashcards = cards,
                        quizQuestion = buildQuestion(cards),
                        quizResult = null
                    )
                }
            }
        }
        _uiState.update { it.copy(quizQuestion = buildQuestion(it.flashcards)) }
    }

    fun selectAnswer(index: Int) {
        val current = _uiState.value.quizQuestion ?: return
        if (current.submitted) return
        val isCorrect = index == current.correctIndex
        _uiState.update {
            it.copy(
                quizQuestion = current.copy(
                    selectedIndex = index,
                    submitted = true
                ),
                quizResult = if (isCorrect) "Correct! Streak updated." else "Not correct. Try next."
            )
        }

        if (!isCorrect) return
        viewModelScope.launch {
            val score = 80
            _uiState.update {
                it.copy(
                    streak = it.streak + 1,
                    progress = ((it.progress * 100f + score) / 200f).coerceIn(0f, 1f)
                )
            }
            analyticsService.logEvent("quiz_completed", "Quiz", score.toDouble())
            analyticsService.logStudySession("Quiz", durationMinutes = 5, score = score)
        }
    }

    fun nextQuestion() {
        _uiState.update {
            it.copy(
                quizQuestion = buildQuestion(it.flashcards),
                quizResult = null
            )
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

    private fun buildQuestion(cards: List<Flashcard>): QuizQuestion {
        val primary = cards.firstOrNull() ?: Flashcard("No question", "No answer")
        val distractors = cards.drop(1).map { it.back }.filter { it != primary.back }.take(3).toMutableList()
        while (distractors.size < 3) {
            distractors.add("Review notes and try again")
        }
        val options = (distractors + primary.back).shuffled()
        val correctIndex = options.indexOf(primary.back)
        return QuizQuestion(
            prompt = primary.front,
            options = options,
            correctIndex = correctIndex
        )
    }
}
