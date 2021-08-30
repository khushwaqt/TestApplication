package com.example.testapplication.repos

import android.content.Context
import com.example.testapplication.appdatabase.AppDatabase
import com.example.testapplication.dbmodels.Users

class UsersRepo {

   suspend fun insertUser(context: Context, users: Users): Long {
        val db = AppDatabase.getInstance(context)
        val dao = db.userDao()
        return dao.addUser(users)
    }
}