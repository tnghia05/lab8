package com.example.notesapp_sqlitedatabase.ViewModel

import androidx.lifecycle.*
import com.example.notesapp_sqlitedatabase.Model.Note
import com.example.notesapp_sqlitedatabase.Repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val filteredNotes = MediatorLiveData<List<Note>>()

    private val _filters = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val filters: LiveData<Map<String, Boolean>> = _filters

    private val _searchQuery = MutableLiveData<String>("") // Lưu từ khóa tìm kiếm
    val searchQuery: LiveData<String> = _searchQuery

    init {
        val source = repository.getAllNotes()

        filteredNotes.addSource(source) { notes ->
            filteredNotes.value = applyFilters(notes, _filters.value ?: emptyMap(), _searchQuery.value ?: "")
        }

        filteredNotes.addSource(_filters) { selectedFilters ->
            filteredNotes.value = applyFilters(source.value ?: emptyList(), selectedFilters, _searchQuery.value ?: "")
        }

        filteredNotes.addSource(_searchQuery) { query ->
            filteredNotes.value = applyFilters(source.value ?: emptyList(), _filters.value ?: emptyMap(), query)
        }
    }

    private fun applyFilters(notes: List<Note>, filters: Map<String, Boolean>, query: String): List<Note> {
        var filteredList = notes

        // Lọc theo từ khóa tìm kiếm
        if (query.isNotBlank()) {
            filteredList = filteredList.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (filters["ALL"] == true) return filteredList

        if (filters["FAVORITE"] == true) {
            filteredList = filteredList.filter { it.isFavorite }
        }
        if (filters["COMPLETED"] == true) {
            filteredList = filteredList.filter { it.isCompleted }
        }
        if (filters["TITLE_A-Z"] == true) {
            filteredList = filteredList.sortedBy { it.title.lowercase() }
        }

        return filteredList.sortedWith(
            compareByDescending<Note> { filters["COMPLETED"] == true && it.isCompleted }
                .thenByDescending { filters["FAVORITE"] == true && it.isFavorite }
                .thenByDescending { if (filters["TIME"] == true) it.timestamp else 0L }
        )
    }

    fun toggleFilter(filter: String) {
        val currentFilters = _filters.value?.toMutableMap() ?: mutableMapOf()

        if (filter == "ALL") {
            currentFilters.clear()
            currentFilters["ALL"] = true
        } else {
            currentFilters.remove("ALL")

            if (filter == "TITLE_A-Z") {
                currentFilters.remove("TIME")
            } else if (filter == "TIME") {
                currentFilters.remove("TITLE_A-Z")
            }

            if (currentFilters.containsKey(filter)) {
                currentFilters.remove(filter)
            } else {
                currentFilters[filter] = true
            }
        }

        _filters.value = currentFilters
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleComplete(note: Note) = viewModelScope.launch {
        val updatedNote = note.copy(isCompleted = !note.isCompleted)
        repository.update(updatedNote)
    }

    fun toggleFavorite(note: Note) = viewModelScope.launch {
        val updatedNote = note.copy(isFavorite = !note.isFavorite)
        repository.update(updatedNote)
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun getNoteById(id: Int): LiveData<Note> {
        return repository.getNoteById(id)
    }

    fun getNoteByIdSync(noteId: Int, onResult: (Note?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = repository.getNoteByIdSync(noteId)
            withContext(Dispatchers.Main) {
                onResult(note)
            }
        }
    }
}





