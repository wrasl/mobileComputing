package com.example.myapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis()
)
