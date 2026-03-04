package com.edgeai.tutorlite.di

import android.content.Context
import androidx.room.Room
import com.edgeai.tutorlite.data.local.AnalyticsEventDao
import com.edgeai.tutorlite.data.local.EdgeAiDatabase
import com.edgeai.tutorlite.data.local.StudySessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EdgeAiDatabase {
        return Room.databaseBuilder(
            context,
            EdgeAiDatabase::class.java,
            "edge_ai_encrypted.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStudySessionDao(db: EdgeAiDatabase): StudySessionDao = db.studySessionDao()

    @Provides
    fun provideAnalyticsEventDao(db: EdgeAiDatabase): AnalyticsEventDao = db.analyticsEventDao()
}
