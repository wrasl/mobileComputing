package com.example.myapp.data

import androidx.room.*


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(user: User)

    @Query("SELECT * FROM User WHERE uid = 0")
    fun getUser(): User?
}