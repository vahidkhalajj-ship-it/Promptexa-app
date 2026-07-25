package ir.promptexa.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Receives push notifications (routed through Firebase Cloud Messaging,
 * as configured by Webpushr) and displays them. Tapping a notification
 * opens the associated URL inside MainActivity's WebView.
 *
 * Webpushr dashboard: connect this app's Firebase project (Constants above)
 * and Webpushr will deliver title / message / image / url via FCM data payload.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "promptexa_notifications"
        private const val CHANNEL_NAME = "Promptexa"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: getString(R.string.app_name)

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: ""

        val imageUrl = remoteMessage.notification?.imageUrl?.toString()
            ?: remoteMessage.data["image"]

        val targetUrl = remoteMessage.data["url"] ?: Constants.URL_HOME

        // Image download must not run on the main thread.
        Thread { showNotification(title, body, imageUrl, targetUrl) }.start()
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Webpushr's Firebase-based SDK/API handles token registration server-side.
        // If a manual sync is needed, send `token` to your backend / Webpushr API here.
    }

    private fun showNotification(title: String, body: String, imageUrl: String?, targetUrl: String) {
        createChannelIfNeeded()

        val intent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_NOTIFICATION_URL, targetUrl)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigPicture: Bitmap? = imageUrl?.let { downloadBitmap(it) }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (bigPicture != null) {
            builder.setLargeIcon(bigPicture)
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigPicture))
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpsURLConnection
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}
