package com.example.notesapp_sqlitedatabase.View


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp_sqlitedatabase.Model.Note
import com.example.notesapp_sqlitedatabase.ViewModel.NoteViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(noteId: Int, viewModel: NoteViewModel, onBack: () -> Unit) {
    val noteLiveData = viewModel.getNoteById(noteId).observeAsState()
    val note = noteLiveData.value

    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var isSaved by rememberSaveable { mutableStateOf(false) } // Kiểm soát trạng thái lưu

    // Cập nhật nội dung khi có dữ liệu ghi chú
    LaunchedEffect(note) {
        note?.let {
            title = it.title
            content = it.content
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == 0) "Thêm ghi chú" else "Chỉnh sửa ghi chú") },
                navigationIcon = {
                    IconButton(onClick = { saveAndExit(viewModel, noteId, title, content, isSaved, onBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { saveNote(viewModel, noteId, title, content, onSaved = { isSaved = true }) }) {
                        Icon(Icons.Default.Check, contentDescription = "Lưu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6E6FA),
                    unfocusedContainerColor = Color(0xFFE6E6FA)
                ),
                onValueChange = { title = it; isSaved = false }, // Nếu chỉnh sửa -> isSaved = false
                label = { Text("Tiêu đề") },
                textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = content,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6E6FA),
                    unfocusedContainerColor = Color(0xFFE6E6FA)
                ),
                onValueChange = { content = it; isSaved = false }, // Nếu chỉnh sửa -> isSaved = false
                label = { Text("Nội dung") },
                textStyle = TextStyle(fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

// Lưu và thoát
fun saveAndExit(viewModel: NoteViewModel, noteId: Int, title: String, content: String, isSaved: Boolean, onBack: () -> Unit) {
    if (!isSaved) { // Chỉ lưu nếu chưa lưu trước đó
        saveNote(viewModel, noteId, title, content) { }
    }
    onBack()
}

// Hàm lưu ghi chú
fun saveNote(viewModel: NoteViewModel, noteId: Int, title: String, content: String, onSaved: () -> Unit) {
    if (title.isNotBlank() || content.isNotBlank()) {
        val timestamp = System.currentTimeMillis()

        if (noteId == 0) {
            // Nếu là ghi chú mới, tạo mới
            val newNote = Note(0, title, content, timestamp)
            viewModel.insert(newNote)
            onSaved()
        } else {
            // Nếu là ghi chú cũ, lấy ra rồi cập nhật
            viewModel.getNoteByIdSync(noteId) { oldNote ->
                if (oldNote != null) {
                    val updatedNote = oldNote.copy(
                        title = title,
                        content = content,
                        timestamp = timestamp
                    )
                    viewModel.update(updatedNote)
                }
                onSaved()
            }
        }
    }
}






