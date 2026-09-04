package com.edgereader.tts.di

import android.content.Context
import androidx.room.Room
import com.edgereader.tts.data.db.AppDatabase
import com.edgereader.tts.data.db.BookDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "edgereader_tts_db"
        ).build()
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }
}
