package com.edgeai.tutorlite.service.storage

import android.content.Context
import com.edgeai.tutorlite.data.repository.AnalyticsRepository
import com.edgeai.tutorlite.service.ai.ModelStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AppStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelStore: ModelStore,
    private val analyticsRepository: AnalyticsRepository
) {
    data class StorageStats(
        val modelsBytes: Long,
        val cacheBytes: Long,
        val dbBytes: Long
    ) {
        val totalBytes: Long = modelsBytes + cacheBytes + dbBytes
    }

    suspend fun readStats(): StorageStats = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("edge_ai_encrypted.db")
        StorageStats(
            modelsBytes = modelStore.totalBytes(),
            cacheBytes = context.cacheDir.directorySizeBytes(),
            dbBytes = if (dbFile.exists()) dbFile.length() else 0L
        )
    }

    suspend fun clearUserData() = withContext(Dispatchers.IO) {
        modelStore.clearAllModels()
        analyticsRepository.clearAll()
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    private fun java.io.File.directorySizeBytes(): Long {
        if (!exists()) return 0L
        return walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }
}
