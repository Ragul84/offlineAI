package com.edgeai.tutorlite.service.ai.runtime

import com.edgeai.tutorlite.domain.model.TutorPrompt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlamaCppRuntime @Inject constructor() {

    private val nativeAvailable: Boolean = runCatching {
        System.loadLibrary("edgeai_llama")
        true
    }.getOrElse { false }
    private var initializedModelPath: String? = null

    fun isAvailable(): Boolean = nativeAvailable

    @Synchronized
    fun initialize(modelPath: String) {
        check(nativeAvailable) { "Native llama runtime unavailable on this build." }
        if (initializedModelPath == modelPath) return
        runCatching { nativeRelease() }
        val ok = nativeInit(modelPath)
        check(ok) { "Failed to initialize native llama model at $modelPath" }
        initializedModelPath = modelPath
    }

    @Synchronized
    fun run(modelPath: String, prompt: TutorPrompt): String {
        check(nativeAvailable) { "llama.cpp native library unavailable" }
        initialize(modelPath)
        return nativeRun(modelPath, prompt.prompt, prompt.subject, prompt.mode, prompt.imagePath)
    }

    @Synchronized
    fun release() {
        if (!nativeAvailable) return
        runCatching { nativeRelease() }
        initializedModelPath = null
    }

    private external fun nativeInit(modelPath: String): Boolean

    private external fun nativeRun(
        modelPath: String,
        prompt: String,
        subject: String,
        mode: String,
        imagePath: String?
    ): String

    private external fun nativeRelease()
}
