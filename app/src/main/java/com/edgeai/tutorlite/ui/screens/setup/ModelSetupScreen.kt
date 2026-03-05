package com.edgeai.tutorlite.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgeai.tutorlite.domain.model.ModelVariant

@Composable
fun ModelSetupScreen(
    paddingValues: PaddingValues,
    onContinue: () -> Unit,
    viewModel: ModelSetupViewModel = hiltViewModel()
) {
    val state by viewModel.mergedUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Model Setup", style = MaterialTheme.typography.headlineSmall)
        Text("Download and test your model once before using app features.")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModelVariant.entries.forEach { model ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedModel == model,
                            onClick = { viewModel.selectModel(model) }
                        )
                        Text("${model.name} (~${model.approxSizeMb} MB)")
                    }
                }
                Button(onClick = viewModel::downloadSelectedModel, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.downloaded) "Re-download Model" else "Download Model")
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

        Button(
            onClick = viewModel::testModel,
            enabled = state.downloaded && !state.testing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.testing) "Testing..." else "Test Model Connection")
        }
        state.testResult?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(
            onClick = onContinue,
            enabled = state.downloaded,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to App")
        }
    }
}

