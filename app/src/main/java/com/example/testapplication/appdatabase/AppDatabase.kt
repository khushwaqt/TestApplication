package com.example.testapplication.appdatabase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.testapplication.dao.UsersDao
import com.example.testapplication.dbmodels.Users


@Database(entities = [Users::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDao

    companion object {
        private const val logTag = "AppDataBase"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            Log.d(logTag, "Getting Instance")
            return instance ?: synchronized(this) {
                Log.d(logTag, "Building Database")
                instance ?: buildDatabase(context).also { instance = it }
            }
        }


        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .fallbackToDestructiveMigration().build()
        }

    }
}