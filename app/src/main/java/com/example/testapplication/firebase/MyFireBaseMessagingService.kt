package com.example.testapplication.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.testapplication.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

const val fTag = "Firebase"

class MyFireBaseMessagingService : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        Timber.tag(fTag).d("New Token is:$token")
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.tag(fTag).d("Message Received...")
        showNotifications(remoteMessage)
    }

    private fun showNotifications(remoteMessage: RemoteMessage) {
        Timber.tag(fTag).d("Generating local notification...")
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
            val channelId = "91002169"
            val messageTitle = "This is the title of notification"
            val messageContent = "This is the content of notification message,"
            val vibrationArray = longArrayOf(100, 500, 100, 500, 100, 500)
            val uri = Uri.parse("android.resource://$packageName/raw/push_sound")
            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.data["promId"])
                    .setContentText(messageContent)
                    .setVibrate(vibrationArray)
                    .setSound(uri)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(10)//show actual number hare
                    .setLargeIcon(image)
                    .setLights(color, 100, 100)
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
                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    mChannel.vibrationPattern = vibrationArray
                    mChannel.enableVibration(true)
                    mChannel.enableLights(true)
                    mChannel.lightColor = color
                    mChannel.setShowBadge(true)
                    mChannel.setSound(uri, attributes)

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