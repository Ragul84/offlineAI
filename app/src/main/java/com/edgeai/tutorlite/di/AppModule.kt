package com.edgeai.tutorlite.di

import com.edgeai.tutorlite.data.repository.DataStoreSettingsRepository
import com.edgeai.tutorlite.data.repository.AnalyticsRepository
import com.edgeai.tutorlite.data.repository.RoomAnalyticsRepository
import com.edgeai.tutorlite.data.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: DataStoreSettingsRepository
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: RoomAnalyticsRepository
    ): AnalyticsRepository
}
