package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WatchlistItemRequest(
    @SerializedName("ticker")
    val ticker: String, // Solo requerido en creación
    
    @SerializedName("exchange")
    val exchange: String? = null,
    
    @SerializedName("targetPrice")
    val targetPrice: BigDecimal? = null, // Opcional
    
    @SerializedName("targetPfcf")
    val targetPfcf: BigDecimal? = null,  // Opcional
    
    @SerializedName("notifyWhenBelowPrice")
    val notifyWhenBelowPrice: Boolean? = false,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("estimatedFcfGrowthRate")
    val estimatedFcfGrowthRate: BigDecimal? = null, 
    
    @SerializedName("investmentHorizonYears")
    val investmentHorizonYears: Int? = null,
    
    @SerializedName("discountRate")
    val discountRate: BigDecimal? = null
)
