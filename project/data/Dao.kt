package com.example.myapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(user: User)

    @Query("SELECT * FROM User WHERE uid = 0")
    fun getUser(): User?
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM MessageEntity ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM MessageEntity")
    suspend fun deleteAllMessages()
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM PhotoEntity ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Insert
    suspend fun insertPhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}
