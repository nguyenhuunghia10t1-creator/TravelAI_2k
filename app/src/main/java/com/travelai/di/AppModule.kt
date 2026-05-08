package com.travelai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.travelai.BuildConfig
import com.travelai.data.api.ApiClient
import com.travelai.data.api.DeepSeekApi
import com.travelai.data.db.AppDatabase
import com.travelai.data.db.ChatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("DeepSeekApiKey")
    fun provideDeepSeekApiKey(): String = BuildConfig.DEEPSEEK_API_KEY

    @Provides
    @Singleton
    fun provideDeepSeekApi(
        @Named("DeepSeekApiKey") apiKey: String
    ): DeepSeekApi = ApiClient.create(apiKey)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "travelai.db"
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trip_profiles` (
                    `sessionId` INTEGER NOT NULL,
                    `destination` TEXT NOT NULL,
                    `days` INTEGER NOT NULL,
                    `budget` TEXT NOT NULL,
                    `people` INTEGER NOT NULL,
                    `travelStyle` TEXT NOT NULL,
                    `transport` TEXT NOT NULL,
                    `note` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`sessionId`),
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }
    }
}
