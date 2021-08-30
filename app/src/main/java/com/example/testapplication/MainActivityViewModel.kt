package com.example.testapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.testapplication.dbmodels.Users
import com.example.testapplication.repos.UsersRepo
import java.lang.Exception

class MainActivityViewModel : ViewModel() {

    fun addUser(context: Context, users: Users) = liveData {
        emit("Loading...")
        try {
            val userRepo = UsersRepo()
            val response = userRepo.insertUser(context, users)
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(e.message)
        }
    }


}