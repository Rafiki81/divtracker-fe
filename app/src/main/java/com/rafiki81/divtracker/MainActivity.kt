package com.rafiki81.divtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.data.repository.FcmTokenRepository
import com.rafiki81.divtracker.navigation.AppNavigation
import com.rafiki81.divtracker.navigation.Screen
import com.rafiki81.divtracker.service.NotificationChannels
import com.rafiki81.divtracker.ui.theme.DivTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crear canales de notificación
        NotificationChannels.createChannels(this)
        
        // Solicitar permiso de notificaciones (Android 13+)
        askNotificationPermission()
        
        // Obtener y registrar token FCM actual
        registerFcmToken()

        // Determinar destino inicial
        val startDestination = if (TokenManager.hasToken()) {
            Screen.Watchlist.route
        } else {
            Screen.Login.route
        }

        enableEdgeToEdge()
        setContent {
            DivTrackerTheme {
                AppNavigation(startDestination = startDestination)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            
            // Register with backend
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = FcmTokenRepository(RetrofitClient.deviceApiService, applicationContext)
                    repository.registerToken(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
