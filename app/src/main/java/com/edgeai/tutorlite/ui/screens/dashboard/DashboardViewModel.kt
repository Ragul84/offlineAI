package com.edgeai.tutorlite.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeai.tutorlite.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    data class TopicStat(val name: String, val score: Int)

    data class UiState(
        val topics: List<TopicStat> = emptyList(),
        val tips: List<String> = listOf(
            "Revisit fractions with 10-minute practice.",
            "Use Tamil explanations for science keywords.",
            "Attempt one mixed quiz daily."
        ),
        val dailyEventCounts: List<Float> = emptyList(),
        val dailyAverageScores: List<Float> = emptyList()
    )

    val uiState: StateFlow<UiState> = combine(
        analyticsRepository.observeSubjectPerformance(),
        analyticsRepository.observeDailyEventCounts(),
        analyticsRepository.observeDailyScores()
    ) { subjectAverages, dailyEvents, dailyScores ->
        UiState(
            topics = subjectAverages.map {
                TopicStat(name = it.subject, score = it.avgScore.toInt().coerceIn(0, 100))
            },
            dailyEventCounts = dailyEvents.map { it.total.toFloat() }.reversed(),
            dailyAverageScores = dailyScores.map { it.avgScore.coerceIn(0f, 100f) }.reversed()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(
            topics = listOf(
                TopicStat("Math", 70),
                TopicStat("Science", 62),
                TopicStat("Language", 84),
                TopicStat("History", 54)
            ),
            dailyEventCounts = listOf(1f, 2f, 2f, 3f, 4f, 3f, 5f),
            dailyAverageScores = listOf(64f, 68f, 70f, 74f, 71f, 78f, 82f)
        )
    )
}
