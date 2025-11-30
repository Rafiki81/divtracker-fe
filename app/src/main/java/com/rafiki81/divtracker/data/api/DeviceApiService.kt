package com.rafiki81.divtracker.data.api

import com.rafiki81.divtracker.data.model.DeviceRegistrationRequest
import com.rafiki81.divtracker.data.model.DeviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApiService {
    
    @POST("api/v1/devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequest
    ): Response<DeviceResponse>

    @GET("api/v1/devices")
    suspend fun getDevices(): Response<List<DeviceResponse>>

    @DELETE("api/v1/devices/{deviceId}")
    suspend fun unregisterDevice(
        @Path("deviceId") deviceId: String
    ): Response<Unit>
}
