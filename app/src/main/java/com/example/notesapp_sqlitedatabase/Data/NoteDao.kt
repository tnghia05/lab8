package com.example.notesapp_sqlitedatabase.Data

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Update
import com.example.notesapp_sqlitedatabase.Model.Note

@Dao
interface NoteDao {
    @Query("""
        SELECT * FROM notes 
        WHERE (:favorite IS NULL OR is_favorite = :favorite) 
        AND (:completed IS NULL OR is_completed = :completed)
        ORDER BY 
            CASE WHEN :sortByTitle = 1 THEN title END ASC,
            CASE WHEN :sortByTime = 1 THEN timestamp END DESC,
            CASE WHEN :sortById = 1 THEN id END DESC
    """)
    fun getFilteredNotes(
        favorite: Boolean?,
        completed: Boolean?,
        sortByTitle: Boolean,
        sortByTime: Boolean,
        sortById: Boolean
    ): LiveData<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: Int): LiveData<Note>

    @Query("SELECT * FROM notes")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun getNoteByIdSync(noteId: Int): Note?

}