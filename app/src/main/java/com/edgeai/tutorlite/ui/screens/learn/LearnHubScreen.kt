package com.edgeai.tutorlite.ui.screens.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edgeai.tutorlite.ui.navigation.Route

private data class LearnFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: Route
)

@Composable
fun LearnHubScreen(
    paddingValues: PaddingValues,
    onNavigate: (Route) -> Unit
) {
    val features = listOf(
        LearnFeature("Camera Tutor", "Capture and solve from image", Icons.Default.CameraAlt, Route.Camera),
        LearnFeature("Note Scanner", "Clean up handwritten notes", Icons.Default.AutoStories, Route.Scanner),
        LearnFeature("Daily Quiz", "Generate quick revision tests", Icons.Default.Quiz, Route.Quiz),
        LearnFeature("Share Cards", "Share explainers to WhatsApp/IG", Icons.Default.IosShare, Route.Share)
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Learn", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Choose a quick mode to start studying.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(features) { feature ->
            Card(
                onClick = { onNavigate(feature.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(feature.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        feature.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
