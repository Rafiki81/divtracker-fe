package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class DeviceRegistrationRequest(
    @SerializedName("fcmToken")
    val fcmToken: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("platform")
    val platform: String = "ANDROID",
    
    @SerializedName("deviceName")
    val deviceName: String? = null
)

data class DeviceResponse(
    @SerializedName("id")
    val id: UUID,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("platform")
    val platform: String,
    
    @SerializedName("deviceName")
    val deviceName: String?,
    
    @SerializedName("isActive")
    val isActive: Boolean,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("lastUsedAt")
    val lastUsedAt: String?
)
