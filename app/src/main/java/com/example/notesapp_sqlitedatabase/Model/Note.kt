package com.example.notesapp_sqlitedatabase.Model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long, //Thoi gian tao va cap nhat
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false, //Trang thai yeu thich
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false //Trang thai hoan thanh
)
