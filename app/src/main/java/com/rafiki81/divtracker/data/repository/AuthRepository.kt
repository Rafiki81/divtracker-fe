package com.rafiki81.divtracker.data.repository

import com.google.gson.Gson
import com.rafiki81.divtracker.data.api.AuthApiService
import com.rafiki81.divtracker.data.model.AuthResponse
import com.rafiki81.divtracker.data.model.ErrorResponse
import com.rafiki81.divtracker.data.model.LoginRequest
import com.rafiki81.divtracker.data.model.SignupRequest
import okhttp3.ResponseBody

sealed class AuthResult {
    data class Success(val authResponse: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val authApi: AuthApiService) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error(parseError(response.errorBody(), response.message()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun signup(email: String, password: String, firstName: String, lastName: String): AuthResult {
        return try {
            val response = authApi.signup(SignupRequest(email, password, firstName, lastName))
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error(parseError(response.errorBody(), response.message()))
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    private fun parseError(errorBody: ResponseBody?, defaultMessage: String?): String {
        return try {
            errorBody?.string()?.let { jsonString ->
                val errorResponse = Gson().fromJson(jsonString, ErrorResponse::class.java)
                if (!errorResponse.errors.isNullOrEmpty()) {
                    errorResponse.errors.values.joinToString("\n")
                } else {
                    errorResponse.message.takeIf { it.isNotEmpty() } ?: (defaultMessage ?: "Unknown error")
                }
            } ?: (defaultMessage ?: "Unknown error")
        } catch (e: Exception) {
            defaultMessage ?: "Unknown error"
        }
    }
}
