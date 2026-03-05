package com.edgeai.tutorlite.service.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.domain.model.ModelVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelStore: ModelStore,
    private val settingsRepository: SettingsRepository
) {
    data class DownloadState(
        val isDownloading: Boolean = false,
        val progressPercent: Int = 0,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    suspend fun download(model: ModelVariant, retries: Int = 3): Boolean = withContext(Dispatchers.IO) {
        if (modelStore.isDownloaded(model)) {
            _state.value = DownloadState(isDownloading = false, progressPercent = 100)
            return@withContext true
        }
        if (settingsRepository.wifiOnlyDownloads.value && !isOnUnmeteredNetwork()) {
            _state.value = DownloadState(errorMessage = "Wi-Fi-only is enabled. Disable it in Settings to use mobile data.")
            return@withContext false
        }

        val targetFile = modelStore.fileFor(model)
        val requiredBytes = model.approxSizeMb * 1024L * 1024L
        val availableBytes = targetFile.parentFile?.usableSpace ?: 0L
        if (availableBytes < (requiredBytes + SAFETY_BUFFER_BYTES)) {
            _state.value = DownloadState(errorMessage = "Not enough storage. Free at least ${model.approxSizeMb + 256} MB.")
            return@withContext false
        }

        repeat(retries) { attempt ->
            try {
                _state.value = DownloadState(isDownloading = true, progressPercent = 0)
                val sourceUrl = ModelCatalog.huggingFaceUrls.getValue(model)

                val connection = URL(sourceUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 20_000
                connection.readTimeout = 60_000
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "EdgeAITutorLite/1.0 Android")
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IllegalStateException("HTTP $responseCode ${connection.responseMessage}")
                }

                val total = connection.contentLengthLong.coerceAtLeast(1L)
                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var read: Int
                        var copied = 0L
                        while (input.read(buffer).also { read = it } >= 0) {
                            output.write(buffer, 0, read)
                            copied += read
                            val progress = ((copied * 100L) / total).toInt().coerceIn(1, 99)
                            _state.value = DownloadState(isDownloading = true, progressPercent = progress)
                        }
                    }
                }
                _state.value = DownloadState(isDownloading = false, progressPercent = 100)
                return@withContext true
            } catch (t: Throwable) {
                targetFile.delete()
                val reason = t.message?.take(120) ?: t::class.simpleName.orEmpty()
                _state.value = DownloadState(errorMessage = "Retry ${attempt + 1} failed: $reason")
                delay(500L)
            }
        }
        _state.value = DownloadState(errorMessage = "Model download failed after retries")
        return@withContext false
    }

    private fun isOnUnmeteredNetwork(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    companion object {
        private const val SAFETY_BUFFER_BYTES = 256L * 1024L * 1024L
    }
}
