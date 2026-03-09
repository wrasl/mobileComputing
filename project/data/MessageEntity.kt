package com.example.myapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val body: String?,
    val profileImage: String?,
    val imagePath: String? = null, // New field for sent images
    val timestamp: Long = System.currentTimeMillis()
)
