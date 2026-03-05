package com.edgeai.tutorlite.service.ai

import com.edgeai.tutorlite.domain.model.ModelVariant

object ModelCatalog {
    // Public GGUF endpoints validated for direct mobile download.
    // Note: using Qwen2.5 mirrors until reliable public Qwen3.5 small GGUF links are available.
    val huggingFaceUrls: Map<ModelVariant, String> = mapOf(
        ModelVariant.QWEN_08B to "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true",
        ModelVariant.QWEN_2B to "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf?download=true"
    )
}
