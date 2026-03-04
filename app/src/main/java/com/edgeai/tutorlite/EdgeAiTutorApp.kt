package com.edgeai.tutorlite

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.edgeai.tutorlite.service.analytics.FirebaseConsentManager
import com.edgeai.tutorlite.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltAndroidApp
class EdgeAiTutorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var firebaseConsentManager: FirebaseConsentManager

    @Inject
    lateinit var workScheduler: WorkScheduler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        firebaseConsentManager.initialize()
        workScheduler.scheduleModelHealthCheck()
        appScope.launch {
            settingsRepository.streakNotificationsEnabled.collectLatest { enabled ->
                workScheduler.configureStreakReminder(enabled)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
