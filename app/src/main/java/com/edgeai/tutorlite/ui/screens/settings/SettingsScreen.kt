package com.edgeai.tutorlite.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgeai.tutorlite.domain.model.ModelVariant
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Model")
                ModelVariant.entries.forEach { model ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.model == model,
                            onClick = { viewModel.setModel(model) }
                        )
                        Text("${model.name} (~${model.approxSizeMb} MB)")
                    }
                }
                Button(onClick = viewModel::downloadModel, modifier = Modifier.fillMaxWidth()) {
                    Text("Download Selected Model")
                }
                if (state.downloading) {
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${state.progress}%")
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Language")
                listOf("auto", "en", "ta", "hi").forEach { code ->
                    Button(onClick = { viewModel.setLanguage(code) }, modifier = Modifier.fillMaxWidth()) {
                        Text(code.uppercase())
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsToggleRow(
                    label = "Anonymous analytics + crash reporting",
                    checked = state.analyticsEnabled,
                    onCheckedChange = viewModel::setAnalytics
                )
                SettingsToggleRow(
                    label = "Low-RAM performance mode",
                    checked = state.lowRamModeEnabled,
                    onCheckedChange = viewModel::setLowRamMode
                )
                SettingsToggleRow(
                    label = "Wi-Fi only model downloads",
                    checked = state.wifiOnlyDownloads,
                    onCheckedChange = viewModel::setWifiOnlyDownloads
                )
                SettingsToggleRow(
                    label = "Daily streak reminders",
                    checked = state.streakNotificationsEnabled,
                    onCheckedChange = viewModel::setStreakNotifications
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage", style = MaterialTheme.typography.titleMedium)
                Text("Total used: ${state.storageUsedBytes.toMbString()}")
                Text("Models: ${state.storageModelBytes.toMbString()}")
                Text("Cache: ${state.storageCacheBytes.toMbString()}")
                Text("Local DB: ${state.storageDbBytes.toMbString()}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::refreshStorageStats, modifier = Modifier.weight(1f)) {
                        Text("Refresh")
                    }
                    Button(onClick = viewModel::clearData, modifier = Modifier.weight(1f)) {
                        Text("Clear Local Data")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Device Quick Benchmark", style = MaterialTheme.typography.titleMedium)
                Text("Run a local one-prompt test on selected model.")
                Button(onClick = viewModel::runQuickBenchmark, modifier = Modifier.fillMaxWidth()) {
                    Text("Run Benchmark")
                }
                state.benchmarkMs?.let { Text("Latest latency: ${it} ms") }
                state.benchmarkError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun Long.toMbString(): String {
    val mb = this / (1024.0 * 1024.0)
    return "${mb.roundToInt()} MB"
}
