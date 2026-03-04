package com.edgeai.tutorlite.service.analytics

import com.edgeai.tutorlite.data.repository.SettingsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Singleton
class FirebaseConsentManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun initialize() {
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        crashlytics.setCrashlyticsCollectionEnabled(false)

        scope.launch {
            settingsRepository.analyticsEnabled
                .collect { enabled ->
                    firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
                    crashlytics.setCrashlyticsCollectionEnabled(enabled)
                }
        }
    }
}
