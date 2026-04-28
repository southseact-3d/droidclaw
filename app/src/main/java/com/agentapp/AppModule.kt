package com.agentapp

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.agentapp.data.db.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.LlmProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AgentDatabase =
        Room.databaseBuilder(context, AgentDatabase::class.java, "agent_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMessageDao(db: AgentDatabase) = db.messageDao()
    @Provides fun provideSkillDao(db: AgentDatabase) = db.skillDao()
    @Provides fun provideScheduledJobDao(db: AgentDatabase) = db.scheduledJobDao()
    @Provides fun provideSessionDao(db: AgentDatabase) = db.sessionDao()

    @Provides
    @Singleton
    fun provideLlmClient() = LlmProviderClient()

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) =
        SettingsRepository(context)
}

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
