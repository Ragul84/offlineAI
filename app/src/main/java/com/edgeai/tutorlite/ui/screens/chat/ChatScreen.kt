package com.edgeai.tutorlite.ui.screens.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun ChatScreen(
    paddingValues: PaddingValues,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permissionError by remember { mutableStateOf<String?>(null) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var lastSpokenReplyIndex by remember { mutableIntStateOf(-1) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = resolveLocale(state.languageCode)
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()
        if (text.isNotBlank()) {
            viewModel.setDraft(text)
        } else {
            voiceError = "No speech detected."
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            permissionError = "Microphone permission is needed for voice input."
            return@rememberLauncherForActivityResult
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, resolveLocale(state.languageCode))
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        speechLauncher.launch(intent)
    }

    LaunchedEffect(state.messages.size, state.voiceOnly) {
        if (!state.voiceOnly) return@LaunchedEffect
        val latestReplyIndex = state.messages.indexOfLast { !it.fromUser }
        if (latestReplyIndex <= lastSpokenReplyIndex || latestReplyIndex < 0) return@LaunchedEffect
        val latestReply = state.messages[latestReplyIndex].text
        tts?.speak(latestReply, TextToSpeech.QUEUE_FLUSH, null, "assistant_reply")
        lastSpokenReplyIndex = latestReplyIndex
    }

    LaunchedEffect(state.languageCode) {
        tts?.language = resolveLocale(state.languageCode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.voiceOnly,
                onClick = viewModel::toggleVoiceOnly,
                label = { Text("Voice-only mode") }
            )
            Button(onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, resolveLocale(state.languageCode))
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    }
                    speechLauncher.launch(intent)
                } else {
                    recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) {
                Text("Speak")
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.messages) { msg ->
                val align = if (msg.fromUser) Alignment.CenterEnd else Alignment.CenterStart
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (msg.fromUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(10.dp)
                    ) {
                        Text(msg.text)
                    }
                }
            }
        }

        if (!state.voiceOnly) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::setDraft,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ask anything") }
            )
        }
        Button(onClick = viewModel::send, modifier = Modifier.fillMaxWidth()) { Text("Send") }
        if (state.loading) CircularProgressIndicator()
        permissionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        voiceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

private fun resolveLocale(languageCode: String): Locale {
    return when (languageCode.lowercase()) {
        "ta" -> Locale("ta", "IN")
        "hi" -> Locale("hi", "IN")
        "en" -> Locale("en", "IN")
        else -> Locale.getDefault()
    }
}
