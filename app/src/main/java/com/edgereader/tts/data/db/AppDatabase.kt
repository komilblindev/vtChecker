package com.edgereader.tts.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Book::class, Track::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
