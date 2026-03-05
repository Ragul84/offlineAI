package com.edgeai.tutorlite.service.ai

import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.domain.model.ModelVariant
import com.edgeai.tutorlite.domain.model.TutorPrompt
import com.edgeai.tutorlite.domain.model.TutorResponse
import com.edgeai.tutorlite.service.device.BatteryThermalMonitor
import com.edgeai.tutorlite.service.ai.runtime.LlamaCppRuntime
import com.edgeai.tutorlite.service.ai.runtime.OnnxRuntimeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InferenceService @Inject constructor(
    private val modelStore: ModelStore,
    private val llamaCppRuntime: LlamaCppRuntime,
    private val onnxRuntimeEngine: OnnxRuntimeEngine,
    private val settingsRepository: SettingsRepository,
    private val batteryThermalMonitor: BatteryThermalMonitor
) {

    suspend fun runPrompt(prompt: TutorPrompt, model: ModelVariant): Result<TutorResponse> = withContext(Dispatchers.Default) {
        runCatching {
            val start = System.currentTimeMillis()
            val modelFile = modelStore.fileFor(model)
            require(modelFile.exists()) { "Model file missing. Download ${model.name} in Settings." }
            batteryThermalMonitor.refresh()
            val thermalState = batteryThermalMonitor.state.value

            val normalizedPrompt = normalizePrompt(prompt, thermalState.shouldThrottle)
            val modelPath = modelFile.absolutePath
            val responseText = when {
                modelPath.endsWith(".gguf", ignoreCase = true) -> {
                    if (llamaCppRuntime.isAvailable()) {
                        llamaCppRuntime.run(modelPath, normalizedPrompt)
                    } else {
                        buildOfflineFallback(normalizedPrompt, "Native runtime unavailable")
                    }
                }
                modelPath.endsWith(".onnx", ignoreCase = true) -> {
                    runCatching {
                        onnxRuntimeEngine.initialize(modelPath)
                        onnxRuntimeEngine.run(normalizedPrompt)
                    }.getOrElse {
                        buildOfflineFallback(normalizedPrompt, "ONNX generation unavailable")
                    }
                }
                else -> buildOfflineFallback(normalizedPrompt, "Unsupported model format")
            }
            TutorResponse(
                text = responseText,
                confidence = estimateConfidence(responseText),
                latencyMs = System.currentTimeMillis() - start,
                usedModel = model
            )
        }
    }

    private fun normalizePrompt(prompt: TutorPrompt, thermalThrottle: Boolean): TutorPrompt {
        val lowRamEnabled = settingsRepository.lowRamModeEnabled.value
        if (!lowRamEnabled && !thermalThrottle) return prompt
        val limit = if (thermalThrottle) THERMAL_PROMPT_CHAR_LIMIT else LOW_RAM_PROMPT_CHAR_LIMIT
        return prompt.copy(prompt = prompt.prompt.take(limit))
    }

    private fun estimateConfidence(responseText: String): Float {
        if (responseText.isBlank()) return 0.0f
        if (responseText.length < 12) return 0.2f
        return 0.72f
    }

    private fun buildOfflineFallback(prompt: TutorPrompt, reason: String): String {
        val cleaned = prompt.prompt.trim().ifBlank { "Share your question." }
        return """
            Offline fallback mode ($reason)
            1) I understood your question: "$cleaned"
            2) Break it into known facts and unknowns.
            3) Solve step-by-step in simple language.
            4) Verify the final answer with one quick check.
            5) Ask me to explain in Tamil/Hindi/English.
        """.trimIndent()
    }

    companion object {
        private const val LOW_RAM_PROMPT_CHAR_LIMIT = 1_200
        private const val THERMAL_PROMPT_CHAR_LIMIT = 700
    }
}
