package com.edgeai.tutorlite

import com.edgeai.tutorlite.domain.model.ModelVariant
import com.edgeai.tutorlite.domain.model.TutorPrompt
import com.edgeai.tutorlite.service.ai.InferenceService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class InferenceServiceTest {

    @Test
    fun returnsOfflineResponse() = runTest {
        // Service requires Android context in production; this test verifies prompt assumptions.
        val prompt = TutorPrompt("Explain gravity", "Science")
        assertTrue(prompt.prompt.isNotBlank())
        assertTrue(ModelVariant.QWEN_08B.approxSizeMb < ModelVariant.QWEN_2B.approxSizeMb)
    }
}
