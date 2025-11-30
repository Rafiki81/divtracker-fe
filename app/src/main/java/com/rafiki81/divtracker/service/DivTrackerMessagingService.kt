package com.rafiki81.divtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rafiki81.divtracker.MainActivity
import com.rafiki81.divtracker.R
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.repository.FcmTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DivTrackerMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val repository by lazy { 
        FcmTokenRepository(RetrofitClient.deviceApiService, applicationContext) 
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Enviar token al backend
        scope.launch {
            repository.registerToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            showNotification(
                it.title ?: "DivTracker",
                it.body ?: "New alert",
                NotificationChannels.DEFAULT_CHANNEL
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val title = data["title"]
        val body = data["body"]

        when (type) {
            "PRICE_ALERT" -> {
                if (title != null && body != null) {
                    showNotification(title, body, NotificationChannels.PRICE_ALERTS)
                }
            }
            "MARGIN_ALERT" -> {
                if (title != null && body != null) {
                    showNotification(title, body, NotificationChannels.MARGIN_ALERTS)
                }
            }
            "DAILY_SUMMARY" -> {
                if (title != null && body != null) {
                    showNotification(title, body, NotificationChannels.DAILY_SUMMARY)
                }
            }
            "PRICE_UPDATE" -> {
                // Silent update - TODO: Implement logic to update local DB/Cache
                Log.d("FCM", "Silent price update received for ${data["ticker"]}")
            }
        }
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Asegúrate de tener un icono válido
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Channels are handled in NotificationChannels helper, ensure they are created before showing
        NotificationChannels.createChannels(this)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
