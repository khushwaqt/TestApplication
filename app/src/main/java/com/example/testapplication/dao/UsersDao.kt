package com.example.testapplication.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.testapplication.dbmodels.Users

@Dao
interface UsersDao {


    @Insert
    suspend fun addUser(user: Users): Long
}