package com.edgeai.tutorlite.ui.screens.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyProofScreen(
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Privacy Proof", style = MaterialTheme.typography.headlineSmall)

        ProofCard(
            title = "On-device tutoring",
            body = "Answers are generated locally by the downloaded model. No cloud tutoring API is called."
        )
        ProofCard(
            title = "Analytics off by default",
            body = "Firebase Analytics and Crashlytics stay disabled until user opt-in."
        )
        ProofCard(
            title = "Local data control",
            body = "You can clear local models, cache, and progress data from Settings at any time."
        )
        ProofCard(
            title = "Minimal permissions",
            body = "Only camera, microphone, and notifications are requested for learning features."
        )
    }
}

@Composable
private fun ProofCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
