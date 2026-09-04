package com.edgereader.tts.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val bookId: String,
    val trackNumber: Int,
    val mediaStoreUri: String,
    val durationMs: Long,
    val sourceTextExcerpt: String // Для отладки/повторной генерации
)
