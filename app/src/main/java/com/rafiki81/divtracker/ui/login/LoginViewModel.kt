package com.rafiki81.divtracker.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.data.repository.AuthRepository
import com.rafiki81.divtracker.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository(RetrofitClient.authApi)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    TokenManager.saveToken(result.authResponse.token)
                    _uiState.value = LoginUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
