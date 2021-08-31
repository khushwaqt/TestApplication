package com.example.testapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DownloadListenerService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("got here")
        Log.d("BCL", "Download completed")
    }
}