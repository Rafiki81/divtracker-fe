package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("status")
    val status: Int,
    
    @SerializedName("error")
    val error: String,
    
    @SerializedName("message")
    val message: String,

    @SerializedName("errors")
    val errors: Map<String, String>?,
    
    @SerializedName("path")
    val path: String
)
