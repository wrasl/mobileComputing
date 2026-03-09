package com.example.myapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, MessageEntity::class, PhotoEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun photoDao(): PhotoDao
}
