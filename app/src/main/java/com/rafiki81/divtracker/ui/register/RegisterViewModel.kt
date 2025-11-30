package com.rafiki81.divtracker.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.data.repository.AuthRepository
import com.rafiki81.divtracker.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository(RetrofitClient.authApi)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            when (val result = repository.signup(email, password, firstName, lastName)) {
                is AuthResult.Success -> {
                    TokenManager.saveToken(result.authResponse.token)
                    _uiState.value = RegisterUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = RegisterUiState(errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
