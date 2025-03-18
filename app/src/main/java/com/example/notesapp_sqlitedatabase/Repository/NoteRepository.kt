package com.example.notesapp_sqlitedatabase.Repository


import androidx.lifecycle.LiveData
import com.example.notesapp_sqlitedatabase.Data.NoteDao
import com.example.notesapp_sqlitedatabase.Model.Note

class NoteRepository(private val noteDao: NoteDao) {
    fun getFilteredNotes(filters: Map<String, Boolean>): LiveData<List<Note>> {
        return noteDao.getFilteredNotes(
            favorite = filters["FAVORITE"],
            completed = filters["COMPLETED"],
            sortByTitle = filters["TITLE"] ?: false,
            sortByTime = filters["TIME"] ?: false,
            sortById = filters["ID"] ?: false
        )
    }

    fun getNoteByIdSync(noteId: Int): Note? {
        return noteDao.getNoteByIdSync(noteId) // Viết thêm trong DAO
    }


    fun getNoteById(id: Int): LiveData<Note> {
        return noteDao.getNoteById(id)
    }

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }
}