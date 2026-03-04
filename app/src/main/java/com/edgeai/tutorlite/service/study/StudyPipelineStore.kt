package com.edgeai.tutorlite.service.study

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class StudyPipelineStore @Inject constructor() {

    data class Artifact(
        val source: String,
        val subject: String,
        val rawInput: String,
        val processedOutput: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _latestArtifact = MutableStateFlow<Artifact?>(null)
    val latestArtifact: StateFlow<Artifact?> = _latestArtifact.asStateFlow()

    fun publishArtifact(artifact: Artifact) {
        _latestArtifact.value = artifact
    }
}
