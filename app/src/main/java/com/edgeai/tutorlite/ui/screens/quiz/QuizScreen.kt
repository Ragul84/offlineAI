package com.edgeai.tutorlite.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun QuizScreen(paddingValues: PaddingValues, viewModel: QuizViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Daily Micro-Quizzes", style = MaterialTheme.typography.headlineSmall)
        Text("Streak: ${state.streak} days")
        Text("Flashcards source: ${state.sourceLabel}", style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())

        Text("Flashcards", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.flashcards) { flashcard ->
                Card(modifier = Modifier.fillParentMaxWidth(0.85f)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(flashcard.front, style = MaterialTheme.typography.titleSmall)
                        Text(flashcard.back)
                    }
                }
            }
        }
        Button(onClick = { viewModel.markQuizCompleted(78) }, modifier = Modifier.fillMaxWidth()) {
            Text("Complete Quiz")
        }
    }
}
