package com.example.testapplication

import android.app.Application
import timber.log.Timber

class BaseClass : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}