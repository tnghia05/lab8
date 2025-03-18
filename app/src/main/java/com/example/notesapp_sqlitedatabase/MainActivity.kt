package com.example.notesapp_sqlitedatabase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.notesapp_sqlitedatabase.Data.NoteDatabase
import com.example.notesapp_sqlitedatabase.View.NoteDetailScreen
import com.example.notesapp_sqlitedatabase.View.NoteScreen
import com.example.notesapp_sqlitedatabase.ViewModel.NoteViewModel
import com.example.notesapp_sqlitedatabase.ViewModel.NoteViewModelFactory
import com.example.notesapp_sqlitedatabase.Repository.NoteRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            val database = NoteDatabase.getDatabase(this)
            val noteDao = database.noteDao()

            // Tạo repository (tuỳ vào cách bạn triển khai, có thể truyền Database vào đây)
            val repository = NoteRepository(noteDao)

            // Tạo ViewModel bằng Factory
            val viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(repository))

            NavHost(navController, startDestination = "note_screen") {
                composable("note_screen") {
                    NoteScreen(viewModel) { noteId ->
                        navController.navigate("note_detail_screen/$noteId")
                    }
                }
                composable("note_detail_screen/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
                    if (noteId != null) {
                        NoteDetailScreen(noteId, viewModel) { navController.popBackStack() }
                    } else {
                        Log.e("Navigation", "Invalid noteId received")
                    }
                }
            }
        }
    }
}
