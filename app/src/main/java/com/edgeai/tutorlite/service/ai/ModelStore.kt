package com.edgeai.tutorlite.service.ai

import android.content.Context
import com.edgeai.tutorlite.domain.model.ModelVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val root = File(context.filesDir, "models").apply { mkdirs() }

    fun fileFor(model: ModelVariant): File = File(root, model.fileName)

    fun isDownloaded(model: ModelVariant): Boolean = fileFor(model).exists()

    fun totalBytes(): Long = root.walkTopDown()
        .filter { it.isFile }
        .sumOf { it.length() }

    fun clearAllModels() {
        root.listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
    }
}
