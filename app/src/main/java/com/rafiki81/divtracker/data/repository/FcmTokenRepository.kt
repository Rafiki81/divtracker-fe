package com.rafiki81.divtracker.data.repository

import android.content.Context
import android.os.Build
import com.rafiki81.divtracker.data.api.DeviceApiService
import com.rafiki81.divtracker.data.model.DeviceRegistrationRequest
import com.rafiki81.divtracker.data.model.DeviceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FcmTokenRepository(
    private val apiService: DeviceApiService,
    private val context: Context
) {
    private val PREFS_NAME = "divtracker_prefs"
    private val KEY_FCM_TOKEN = "fcm_token"
    private val KEY_DEVICE_ID = "device_id"

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun registerToken(token: String): Result<DeviceResponse> = withContext(Dispatchers.IO) {
        try {
            val deviceId = getOrCreateDeviceId()
            val request = DeviceRegistrationRequest(
                fcmToken = token,
                deviceId = deviceId,
                platform = "ANDROID",
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            )
            val response = apiService.registerDevice(request)
            
            if (response.isSuccessful && response.body() != null) {
                // Guardar token localmente para saber que está sincronizado
                prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error registering device: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unregisterCurrentDevice(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deviceId = prefs.getString(KEY_DEVICE_ID, null)
                ?: return@withContext Result.failure(Exception("No device registered"))
            
            val response = apiService.unregisterDevice(deviceId)
            
            if (response.isSuccessful) {
                prefs.edit().remove(KEY_FCM_TOKEN).apply()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error unregistering device: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getOrCreateDeviceId(): String {
        return prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            newId
        }
    }

    /**
     * Registrar token FCM pendiente después del login.
     * Este método busca si hay un token almacenado localmente y lo envía al backend.
     */
    suspend fun registerPendingToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pendingToken = prefs.getString(KEY_FCM_TOKEN, null)
            if (pendingToken != null) {
                registerToken(pendingToken)
                Result.success(Unit)
            } else {
                // No hay token pendiente, intentar obtener uno nuevo de Firebase
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guardar token localmente sin enviar al backend (útil si el usuario no está logueado)
     */
    fun savePendingToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    /**
     * Limpiar datos locales del dispositivo (para logout)
     */
    fun clearLocalData() {
        prefs.edit()
            .remove(KEY_FCM_TOKEN)
            .apply()
    }
}
