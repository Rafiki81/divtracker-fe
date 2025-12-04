package com.rafiki81.divtracker.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.data.repository.AuthRepository
import com.rafiki81.divtracker.data.repository.AuthResult
import com.rafiki81.divtracker.data.repository.FcmTokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(RetrofitClient.authApi)
    private val fcmTokenRepository = FcmTokenRepository(
        RetrofitClient.deviceApiService,
        application.applicationContext
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    TokenManager.saveToken(result.authResponse.token)

                    // Registrar token FCM después del login exitoso
                    registerFcmToken()

                    _uiState.value = LoginUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun registerFcmToken() {
        try {
            // Primero intentar registrar token pendiente
            fcmTokenRepository.registerPendingToken()

            // Si no hay token pendiente, obtener uno nuevo de Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            fcmTokenRepository.registerToken(token)
            Log.d("LoginViewModel", "FCM token registered successfully")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error registering FCM token: ${e.message}")
            // No fallar el login por esto, es secundario
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
