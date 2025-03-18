package com.example.notesapp_sqlitedatabase.View


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp_sqlitedatabase.Model.Note
import com.example.notesapp_sqlitedatabase.ViewModel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteScreen(viewModel: NoteViewModel, onNavigateToDetail: (Int) -> Unit) {
    val filters by viewModel.filters.observeAsState(initial = emptyMap<String, Boolean>())
    val notes by viewModel.filteredNotes.observeAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState(initial = "")

    var expanded by remember { mutableStateOf(false) }
    val selectedFilters = remember { mutableStateListOf<String>() }

    val filterOptions = listOf(
        "Tất cả" to "ALL",
        "Yêu thích" to "FAVORITE",
        "Hoàn thành" to "COMPLETED",
        "A-Z" to "TITLE_A-Z",
        "Mới nhất" to "TIME",
    )

    // Hiển thị text tương ứng với bộ lọc đã chọn
    val selectedText = when {
        "Tất cả" in selectedFilters -> "Tất cả"
        selectedFilters.isEmpty() -> "Sắp xếp"
        else -> selectedFilters.joinToString(", ")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToDetail(0) }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ghi chú")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize() // Đảm bảo Column mở rộng hết màn hình
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                // Dropdown để chọn tiêu chí sắp xếp
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedText)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filterOptions.forEach { (label, key) ->
                            val isChecked = filters[key] == true

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                updateFilters(label, key, checked, selectedFilters, viewModel)
                                            }
                                        )
                                        Text(label, modifier = Modifier.padding(start = 8.dp))
                                    }
                                },
                                onClick = {
                                    val newState = !isChecked
                                    updateFilters(label, key, newState, selectedFilters, viewModel)
                                }
                            )
                        }
                    }
                }
                // Thanh tìm kiếm
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = { Text("Tìm kiếm...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.Gray // Đổi màu icon
                        )
                    },
                    modifier = Modifier
                        .width(195.dp) // Giảm chiều ngang
                        .height(55.dp) // Giảm chiều dọc
                        .padding(start = 15.dp),
                    shape = RoundedCornerShape(12.dp), // Bo tròn góc
                    singleLine = true,
                )

            }

            // Danh sách ghi chú
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp) // Để tránh đè lên Row phía trên
            ) {
                items(notes) { note ->
                    NoteItem(note, onNavigateToDetail, viewModel)
                }
            }
        }
    }
}


// Hàm xử lý cập nhật bộ lọc
fun updateFilters(filterLabel: String, filterKey: String, isChecked: Boolean, selectedFilters: SnapshotStateList<String>, viewModel: NoteViewModel) {
    if (filterKey == "ALL") {
        // Nếu chọn "Tất cả", xóa hết các bộ lọc khác
        selectedFilters.clear()
        if (isChecked) selectedFilters.add(filterLabel)
    } else {
        // Nếu chọn bộ lọc khác, loại bỏ "Tất cả"
        selectedFilters.remove("Tất cả")
        // Đảm bảo chỉ có một bộ lọc sắp xếp được chọn
        if (filterKey == "TITLE_A-Z") {
            selectedFilters.remove("Mới nhất") // Loại bỏ "TIME"
        } else if (filterKey == "TIME") {
            selectedFilters.remove("A-Z") // Loại bỏ "TITLE_A-Z"
        }
        if (isChecked) {
            selectedFilters.add(filterLabel)

        } else {
            selectedFilters.remove(filterLabel)
        }
    }

    // Cập nhật ViewModel
    viewModel.toggleFilter(filterKey)
}

@Composable
fun NoteItem(note: Note, onNavigateToDetail: (Int) -> Unit, viewModel: NoteViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable { onNavigateToDetail(note.id) },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E6FA)) // Lavender
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Tiêu đề với giới hạn 1 dòng
                Text(
                    text = note.title.uppercase(),
                    style = TextStyle(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333) // Màu chữ đậm, dễ nhìn
                    ),
                    modifier = Modifier
                        .weight(1f) // Chiếm phần còn lại, tránh đẩy icon
                        .padding(end = 4.dp), // Tạo khoảng cách với icon
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Hiển thị "..." nếu quá dài
                )

                // Nhóm icon góc trên phải
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { // Khoảng cách hợp lý
                    IconButton(onClick = { onNavigateToDetail(note.id) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa", tint = Color(0xFF007AFF))
                    }
                    IconButton(onClick = { viewModel.toggleComplete(note) }, modifier = Modifier.size(20.dp)) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Hoàn thành",
                            tint = if (note.isCompleted) Color(0xFF34C759) else Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFavorite(note) }, modifier = Modifier.size(20.dp)) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Yêu thích",
                            tint = if (note.isFavorite) Color(0xFFFF3B30) else Color.Gray
                        )
                    }
                    IconButton(onClick = { viewModel.delete(note) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFFF9500))
                    }
                }
            }

            // Nội dung ghi chú
            Text(
                text = note.content,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF555555) // Màu chữ tối hơn chút để dễ đọc
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Thời gian cập nhật
            Text(
                text = "Cập nhật: ${formatDate(note.timestamp)}",
                style = TextStyle(
                    fontSize = 13.sp, // Tăng kích thước cho dễ đọc hơn
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF777777) // Màu nhạt hơn để không lấn át nội dung chính
                ),
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}




fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



