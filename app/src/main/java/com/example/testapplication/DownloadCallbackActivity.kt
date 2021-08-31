package com.example.testapplication

import android.app.DownloadManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.testapplication.databinding.ActivityMainBinding
import com.example.testapplication.databinding.ActivityTestBinding

class DownloadCallbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request =
            DownloadManager.Request(Uri.parse("https://zongitapps-stg.zong.com.pk/MyZongv3/ApkFiles/MainActivity.zip"))
        dm.enqueue(request)

    }
}