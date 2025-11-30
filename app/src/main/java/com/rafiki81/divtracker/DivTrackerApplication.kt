package com.rafiki81.divtracker

import android.app.Application
import com.rafiki81.divtracker.data.api.TokenManager

class DivTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar almacenamiento seguro de tokens
        TokenManager.init(this)
    }
}
