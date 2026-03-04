package com.edgeai.tutorlite.service.ai.runtime

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.edgeai.tutorlite.domain.model.TutorPrompt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnnxRuntimeEngine @Inject constructor() {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var sessionModelPath: String? = null

    @Synchronized
    fun initialize(modelPath: String) {
        require(modelPath.endsWith(".onnx", ignoreCase = true)) {
            "ONNX runtime requires a .onnx model file. Found: $modelPath"
        }
        if (session != null && sessionModelPath == modelPath) return
        release()
        val options = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(2)
            setInterOpNumThreads(1)
        }
        session = environment.createSession(modelPath, options)
        sessionModelPath = modelPath
    }

    @Synchronized
    fun run(prompt: TutorPrompt): String {
        checkNotNull(session) {
            "ONNX model session is not initialized."
        }
        throw UnsupportedOperationException(
            "ONNX generation pipeline is not configured yet (tokenizer + decode loop required)."
        )
    }

    @Synchronized
    fun release() {
        runCatching { session?.close() }
        session = null
        sessionModelPath = null
    }
}
