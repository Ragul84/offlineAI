package com.edgeai.tutorlite.ui.screens.scanner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun ScannerScreen(
    paddingValues: PaddingValues,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ocrLoading by remember { mutableStateOf(false) }
    var ocrError by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        if (uri == null) return@rememberLauncherForActivityResult
        ocrLoading = true
        ocrError = null
        runCatching { InputImage.fromFilePath(context, uri) }
            .onFailure {
                ocrLoading = false
                ocrError = it.message ?: "Could not read selected image."
            }
            .onSuccess { inputImage ->
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(inputImage)
                    .addOnSuccessListener { recognized ->
                        ocrLoading = false
                        viewModel.onOcrExtracted(recognized.text.trim())
                    }
                    .addOnFailureListener { ex ->
                        ocrLoading = false
                        ocrError = ex.message ?: "OCR failed."
                    }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Handwritten Scanner", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scan note image with ML Kit OCR")
                Button(onClick = { pickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Pick Note Image")
                }
                selectedImageUri?.let {
                    Text("Selected: ${it.toString().takeLast(44)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedTextField(
            value = state.rawText,
            onValueChange = viewModel::onRawTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("OCR text") }
        )
        Button(onClick = viewModel::cleanupNotes, modifier = Modifier.fillMaxWidth()) {
            Text("Cleanup Notes")
        }
        if (ocrLoading || state.isProcessing) CircularProgressIndicator()
        ocrError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Before", style = MaterialTheme.typography.labelLarge)
                Text(state.rawText.ifBlank { "-" })
                Text("After", style = MaterialTheme.typography.labelLarge)
                Text(state.cleanText.ifBlank { "-" })
            }
        }
    }
}
