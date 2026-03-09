package com.example.myapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: Int,
    val username: String,
    val imagePath: String?
)