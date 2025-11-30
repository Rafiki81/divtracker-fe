package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName

data class TickerSearchResult(
    @SerializedName("symbol")
    val symbol: String,  // Ej: "AAPL"
    
    @SerializedName("description")
    val description: String,  // Ej: "Apple Inc"
    
    @SerializedName("type")
    val type: String?,  // Ej: "Common Stock"
    
    @SerializedName("exchange")
    val exchange: String?,  // Ej: "NASDAQ"
    
    @SerializedName("currency")
    val currency: String?,  // Ej: "USD"
    
    @SerializedName("figi")
    val figi: String?  // Código FIGI
)
