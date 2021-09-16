package com.example.testapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.testapplication.SecureDataFactory.decrypt
import com.example.testapplication.SecureDataFactory.encrypt
import com.example.testapplication.databinding.ActivityMainBinding
import com.example.testapplication.dbmodels.Users
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            addUser()
        }

        showNotifications()
    }

    private fun addUser() {
        val user = Users(userName = "Test User")
        viewModel.addUser(applicationContext, user).observe(this, Observer { userId ->
            Log.d("MainActivity", "Inserted User Id is $userId")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun showNotifications() {
        var image: Bitmap? = null
//        val color: Int = Color.parseColor("#f3f3f3")
        val color: Int = ContextCompat.getColor(this, R.color.red)
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color shr 0 and 0xFF

        CoroutineScope(Dispatchers.IO).launch {
            try {
                image =
                    getBitmapFromURL("https://image.freepik.com/free-photo/closeup-person-filling-out-questionary-form_1262-2259.jpg")
            } catch (e: IOException) {
                println(e)
            }
            val channelId = "10999"
            val messageTitle = "This is the title of notification"
            val messageContent = "This is the content of notification message,"
            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageContent)
                    .setNumber(10)
                    .setVibrate(longArrayOf(100, 5000, 100, 5000, 100))
                    .setLargeIcon(image)
                    .setLights(0xff0000, 10000000, 100)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(image).setSummaryText(messageContent)
                    )
                    .build()


                val notificationManager: NotificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val mChannel = NotificationChannel(
                        channelId,
                        "My Application",
                        importance
                    )
                    mChannel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                    mChannel.enableVibration(true)
                    mChannel.lightColor = color
                    mChannel.enableLights(true)
                    mChannel.setShowBadge(true)
                    notificationManager.createNotificationChannel(mChannel)
                }
                notificationManager.notify(1, notification)
            }
        }
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}