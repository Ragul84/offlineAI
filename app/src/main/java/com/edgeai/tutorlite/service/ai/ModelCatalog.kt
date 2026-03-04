package com.edgeai.tutorlite.service.ai

import com.edgeai.tutorlite.domain.model.ModelVariant

object ModelCatalog {
    // Use direct artifact endpoints to avoid downloading HTML landing pages.
    val huggingFaceUrls: Map<ModelVariant, String> = mapOf(
        ModelVariant.QWEN_08B to "https://huggingface.co/Qwen/Qwen3.5-0.8B-Instruct-GGUF/resolve/main/qwen3.5-0.8b-instruct-q4.gguf?download=true",
        ModelVariant.QWEN_2B to "https://huggingface.co/Qwen/Qwen3.5-2B-Instruct-GGUF/resolve/main/qwen3.5-2b-instruct-q4.gguf?download=true"
    )
}
