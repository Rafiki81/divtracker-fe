package com.rafiki81.divtracker.data.api

import com.rafiki81.divtracker.data.model.AuthResponse
import com.rafiki81.divtracker.data.model.LoginRequest
import com.rafiki81.divtracker.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
}

