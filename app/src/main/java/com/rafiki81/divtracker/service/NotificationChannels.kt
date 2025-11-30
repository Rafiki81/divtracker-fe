package com.rafiki81.divtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val DEFAULT_CHANNEL = "default_channel"
    const val PRICE_ALERTS = "price_alerts"
    const val MARGIN_ALERTS = "margin_alerts"
    const val DAILY_SUMMARY = "daily_summary"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Price Alerts - Alta importancia
            val priceChannel = NotificationChannel(
                PRICE_ALERTS,
                "Price Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a stock hits your target price"
                enableVibration(true)
            }

            // Margin Alerts - Alta importancia
            val marginChannel = NotificationChannel(
                MARGIN_ALERTS,
                "Margin of Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a stock is deeply undervalued"
                enableVibration(true)
            }

            // Daily Summary - Baja importancia
            val summaryChannel = NotificationChannel(
                DAILY_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily update of your watchlist"
            }
            
            // Default Channel
            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannels(
                listOf(priceChannel, marginChannel, summaryChannel, defaultChannel)
            )
        }
    }
}
