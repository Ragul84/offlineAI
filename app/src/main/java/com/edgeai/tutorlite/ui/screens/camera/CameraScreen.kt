package com.edgeai.tutorlite.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.CameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@Composable
fun CameraScreen(
    paddingValues: PaddingValues,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var previewError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) previewError = "Camera permission is required for live scan."
    }

    val controller = remember(context) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    DisposableEffect(lifecycleOwner, hasPermission) {
        if (hasPermission) {
            controller.bindToLifecycle(lifecycleOwner)
        }
        onDispose { }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Camera-First Tutor", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Math", "Language", "Science", "History").forEach { subject ->
                FilterChip(
                    selected = state.subject == subject,
                    onClick = { viewModel.onSubjectChange(subject) },
                    label = { Text(subject) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            if (hasPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.controller = controller
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            } else {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Camera access is off.")
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Enable Camera")
                    }
                }
            }
        }

        Button(
            onClick = {
                val imageFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                val output = ImageCapture.OutputFileOptions.Builder(imageFile).build()
                controller.takePicture(
                    output,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            previewError = null
                            viewModel.onImageCaptured(outputFileResults.savedUri?.toString() ?: Uri.fromFile(imageFile).toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            previewError = exception.message ?: "Image capture failed"
                        }
                    }
                )
            },
            enabled = hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture Image")
        }

        OutlinedTextField(
            value = state.prompt,
            onValueChange = viewModel::onPromptChange,
            label = { Text("Ask about this image") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = viewModel::analyze, modifier = Modifier.fillMaxWidth()) {
            Text("Analyze Offline")
        }
        if (state.isLoading) CircularProgressIndicator()
        previewError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.imagePath?.let { Text("Captured: ${it.takeLast(42)}", style = MaterialTheme.typography.bodySmall) }
        if (state.result.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(state.result, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
