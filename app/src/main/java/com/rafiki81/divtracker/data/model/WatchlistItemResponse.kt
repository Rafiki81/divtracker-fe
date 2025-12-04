package com.rafiki81.divtracker.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.UUID

data class WatchlistItemResponse(
    @SerializedName("id")
    val id: UUID,
    
    @SerializedName("userId")
    val userId: UUID? = null,

    @SerializedName("ticker")
    val ticker: String,
    
    @SerializedName("exchange")
    val exchange: String?,
    
    // Datos Usuario
    @SerializedName("targetPrice")
    val targetPrice: BigDecimal?,
    
    @SerializedName("targetPfcf")
    val targetPfcf: BigDecimal?,
    
    @SerializedName("notifyWhenBelowPrice")
    val notifyWhenBelowPrice: Boolean,
    
    @SerializedName("notes")
    val notes: String?,
    
    // Datos Mercado (Automáticos)
    @SerializedName("currentPrice")
    val currentPrice: BigDecimal?,
    
    @SerializedName("dailyChangePercent")
    val dailyChangePercent: BigDecimal?,

    @SerializedName("marketCapitalization")
    val marketCapitalization: BigDecimal?,
    
    @SerializedName("weekHigh52")
    val weekHigh52: BigDecimal?,
    
    @SerializedName("weekLow52")
    val weekLow52: BigDecimal?,
    
    @SerializedName("weekRange52Position")
    val weekRange52Position: BigDecimal?, // 0.0 a 1.0
    
    @SerializedName("freeCashFlowPerShare")
    val freeCashFlowPerShare: BigDecimal?,
    
    @SerializedName("actualPfcf")
    val actualPfcf: BigDecimal?,

    // Métricas de Dividendos y Valoración
    @SerializedName("peAnnual")
    val peAnnual: BigDecimal?,
    
    @SerializedName("beta")
    val beta: BigDecimal?,
    
    @SerializedName("focfCagr5Y")
    val focfCagr5Y: BigDecimal?,
    
    @SerializedName("dividendYield")
    val dividendYield: BigDecimal?,
    
    @SerializedName("dividendGrowthRate5Y")
    val dividendGrowthRate5Y: BigDecimal?, // Crecimiento Dividendo 5Y
    
    @SerializedName("dividendCoverageRatio")
    val dividendCoverageRatio: BigDecimal?, // Cobertura FCF/Div
    
    @SerializedName("payoutRatioFcf")
    val payoutRatioFcf: BigDecimal?,
    
    @SerializedName("chowderRuleValue")
    val chowderRuleValue: BigDecimal?,
    
    // Cálculos Backend (Automáticos)
    @SerializedName("fairPriceByPfcf")
    val fairPriceByPfcf: BigDecimal?,
    
    @SerializedName("discountToFairPrice")
    val discountToFairPrice: BigDecimal?,
    
    @SerializedName("deviationFromTargetPrice")
    val deviationFromTargetPrice: BigDecimal?,
    
    @SerializedName("undervalued")
    val undervalued: Boolean?,
    
    @SerializedName("dcfFairValue")
    val dcfFairValue: BigDecimal?,
    
    @SerializedName("fcfYield")
    val fcfYield: BigDecimal?,
    
    @SerializedName("marginOfSafety")
    val marginOfSafety: BigDecimal?,
    
    @SerializedName("paybackPeriod")
    val paybackPeriod: BigDecimal?,
    
    @SerializedName("estimatedROI")
    val estimatedROI: BigDecimal?,
    
    @SerializedName("estimatedIRR")
    val estimatedIRR: BigDecimal?,
    
    @SerializedName("estimatedFcfGrowthRate")
    val estimatedFcfGrowthRate: BigDecimal?,

    @SerializedName("investmentHorizonYears")
    val investmentHorizonYears: Int?,

    @SerializedName("discountRate")
    val discountRate: BigDecimal?,
    
    @SerializedName("createdAt")
    val createdAt: String, 
    
    @SerializedName("updatedAt")
    val updatedAt: String
)
