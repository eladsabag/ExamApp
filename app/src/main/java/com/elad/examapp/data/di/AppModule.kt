package com.elad.examapp.data.di

import android.content.Context
import androidx.room.Room
import com.elad.examapp.data.room.AppDatabase
import com.elad.examapp.data.room.LocationDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase
            = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "exam_app_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideLocationDao(appDatabase: AppDatabase): LocationDAO
            = appDatabase.locationDAO()

    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext appContext: Context
    ): Context = appContext
}