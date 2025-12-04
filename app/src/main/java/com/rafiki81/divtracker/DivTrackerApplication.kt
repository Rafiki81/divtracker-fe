package com.rafiki81.divtracker

import android.app.Application
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.service.NotificationChannels

class DivTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar almacenamiento seguro de tokens
        TokenManager.init(this)
        // Crear canales de notificación
        NotificationChannels.createChannels(this)
    }
}
