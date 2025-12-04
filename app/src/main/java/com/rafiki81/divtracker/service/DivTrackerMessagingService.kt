package com.rafiki81.divtracker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rafiki81.divtracker.MainActivity
import com.rafiki81.divtracker.R
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.local.AppDatabase
import com.rafiki81.divtracker.data.repository.FcmTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DivTrackerMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val repository by lazy { 
        FcmTokenRepository(RetrofitClient.deviceApiService, applicationContext) 
    }
    private val watchlistDao by lazy {
        AppDatabase.getDatabase(applicationContext).watchlistDao()
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
        
        Log.d("FCM", "🔔 Message received from: ${remoteMessage.from}")
        Log.d("FCM", "🔔 Message ID: ${remoteMessage.messageId}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "📦 Data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        } else {
            Log.d("FCM", "📦 No data payload")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "📢 Notification: ${it.title} - ${it.body}")
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

        Log.d("FCM", "📨 Processing message type: $type")

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
                // Silent update - Actualizar la base de datos local
                handlePriceUpdate(data)
            }
            else -> {
                // Si no hay type pero hay ticker y price, tratar como actualización de precio
                val ticker = data["ticker"]
                val price = data["price"]
                if (ticker != null && price != null) {
                    Log.d("FCM", "📨 No type specified, but found ticker=$ticker, price=$price - treating as price update")
                    handlePriceUpdate(data)
                }
            }
        }
    }

    private fun handlePriceUpdate(data: Map<String, String>) {
        val ticker = data["ticker"] ?: run {
            Log.w("FCM", "⚠️ No ticker in price update")
            return
        }
        val price = data["price"] ?: run {
            Log.w("FCM", "⚠️ No price in price update for $ticker")
            return
        }
        val changePercent = data["changePercent"]

        Log.d("FCM", "💰 Price update for $ticker: $$price (${changePercent ?: "N/A"}%)")

        scope.launch {
            try {
                watchlistDao.updatePriceByTicker(
                    ticker = ticker,
                    currentPrice = price,
                    dailyChangePercent = changePercent
                )
                Log.d("FCM", "✅ Successfully updated price for $ticker in local DB")
            } catch (e: Exception) {
                Log.e("FCM", "❌ Error updating price for $ticker: ${e.message}", e)
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
