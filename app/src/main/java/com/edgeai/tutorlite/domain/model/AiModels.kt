package com.edgeai.tutorlite.domain.model

enum class ModelVariant(val fileName: String, val approxSizeMb: Int) {
    QWEN_08B("qwen3.5-0.8b-instruct-q4.gguf", 520),
    QWEN_2B("qwen3.5-2b-instruct-q4.gguf", 1450)
}

data class TutorPrompt(
    val prompt: String,
    val subject: String,
    val imagePath: String? = null,
    val mode: String = "explain"
)

data class TutorResponse(
    val text: String,
    val confidence: Float,
    val latencyMs: Long,
    val usedModel: ModelVariant
)
