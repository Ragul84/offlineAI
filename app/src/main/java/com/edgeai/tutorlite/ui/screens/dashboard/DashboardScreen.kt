package com.edgeai.tutorlite.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Topics", "Quizzes", "Tips")

    val eventChartProducer = remember { CartesianChartModelProducer() }
    val scoreChartProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(state.dailyEventCounts) {
        eventChartProducer.runTransaction {
            columnSeries { series(state.dailyEventCounts) }
        }
    }

    LaunchedEffect(state.dailyAverageScores) {
        scoreChartProducer.runTransaction {
            lineSeries { series(state.dailyAverageScores) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Progress Dashboard", style = MaterialTheme.typography.headlineSmall)
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
            }
        }

        when (selectedTab) {
            0 -> {
                state.topics.forEach {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(it.name)
                            LinearProgressIndicator(progress = { it.score / 100f }, modifier = Modifier.fillMaxWidth())
                            Text("${it.score}% mastery")
                        }
                    }
                }
            }

            1 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Daily Learning Activity")
                        CartesianChartHost(
                            chart = rememberCartesianChart(rememberColumnCartesianLayer()),
                            modelProducer = eventChartProducer,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Daily Average Score")
                        CartesianChartHost(
                            chart = rememberCartesianChart(rememberLineCartesianLayer()),
                            modelProducer = scoreChartProducer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            2 -> {
                state.tips.forEach { tip ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(tip, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}
