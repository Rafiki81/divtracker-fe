package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName

data class WatchlistPage(
    @SerializedName("content")
    val content: List<WatchlistItemResponse>,
    
    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int,  // Página actual (0-indexed)
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("empty")
    val empty: Boolean
)
