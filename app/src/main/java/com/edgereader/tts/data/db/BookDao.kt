package com.edgereader.tts.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: String)

    @Query("SELECT * FROM tracks WHERE bookId = :bookId ORDER BY trackNumber ASC")
    fun getTracksForBook(bookId: String): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks WHERE bookId = :bookId")
    suspend fun deleteTracksForBook(bookId: String)
}
