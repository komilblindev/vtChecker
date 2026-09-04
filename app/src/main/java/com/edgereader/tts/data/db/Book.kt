package com.edgereader.tts.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val voiceShortName: String,
    val rate: Int,
    val pitch: Int,
    val createdAt: Long,
    val folderRelativePath: String // Oтносительный путь в MediaStore
)
