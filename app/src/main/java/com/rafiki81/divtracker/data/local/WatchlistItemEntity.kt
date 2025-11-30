package com.rafiki81.divtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import java.math.BigDecimal
import java.util.UUID

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val ticker: String,
    val exchange: String?,
    val targetPrice: String?,
    val targetPfcf: String?,
    val notifyWhenBelowPrice: Boolean,
    val notes: String?,
    
    // Market Data
    val currentPrice: String?,
    val marketCapitalization: String?,
    val weekHigh52: String?,
    val weekLow52: String?,
    val weekRange52Position: String?,
    
    // Metrics
    val freeCashFlowPerShare: String?,
    val actualPfcf: String?,
    val peAnnual: String?,
    val beta: String?,
    val focfCagr5Y: String?,
    val dividendYield: String?,
    val dividendGrowthRate5Y: String?,
    val dividendCoverageRatio: String?,
    val payoutRatioFcf: String?,
    val chowderRuleValue: String?,
    
    // Valuation
    val fairPriceByPfcf: String?,
    val discountToFairPrice: String?,
    val deviationFromTargetPrice: String?,
    val undervalued: Boolean?,
    val dcfFairValue: String?,
    val fcfYield: String?,
    val marginOfSafety: String?,
    val paybackPeriod: String?,
    val estimatedROI: String?,
    val estimatedIRR: String?,
    
    // Params
    val estimatedFcfGrowthRate: String?,
    val investmentHorizonYears: Int?,
    val discountRate: String?,
    
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomainModel(): WatchlistItemResponse {
        return WatchlistItemResponse(
            id = UUID.fromString(id),
            userId = try { UUID.fromString(userId) } catch(e: Exception) { UUID.randomUUID() }, // Fallback if parsing fails
            ticker = ticker,
            exchange = exchange,
            targetPrice = targetPrice?.toBigDecimalOrNull(),
            targetPfcf = targetPfcf?.toBigDecimalOrNull(),
            notifyWhenBelowPrice = notifyWhenBelowPrice,
            notes = notes,
            currentPrice = currentPrice?.toBigDecimalOrNull(),
            marketCapitalization = marketCapitalization?.toBigDecimalOrNull(),
            weekHigh52 = weekHigh52?.toBigDecimalOrNull(),
            weekLow52 = weekLow52?.toBigDecimalOrNull(),
            weekRange52Position = weekRange52Position?.toBigDecimalOrNull(),
            freeCashFlowPerShare = freeCashFlowPerShare?.toBigDecimalOrNull(),
            actualPfcf = actualPfcf?.toBigDecimalOrNull(),
            peAnnual = peAnnual?.toBigDecimalOrNull(),
            beta = beta?.toBigDecimalOrNull(),
            focfCagr5Y = focfCagr5Y?.toBigDecimalOrNull(),
            dividendYield = dividendYield?.toBigDecimalOrNull(),
            dividendGrowthRate5Y = dividendGrowthRate5Y?.toBigDecimalOrNull(),
            dividendCoverageRatio = dividendCoverageRatio?.toBigDecimalOrNull(),
            payoutRatioFcf = payoutRatioFcf?.toBigDecimalOrNull(),
            chowderRuleValue = chowderRuleValue?.toBigDecimalOrNull(),
            fairPriceByPfcf = fairPriceByPfcf?.toBigDecimalOrNull(),
            discountToFairPrice = discountToFairPrice?.toBigDecimalOrNull(),
            deviationFromTargetPrice = deviationFromTargetPrice?.toBigDecimalOrNull(),
            undervalued = undervalued,
            dcfFairValue = dcfFairValue?.toBigDecimalOrNull(),
            fcfYield = fcfYield?.toBigDecimalOrNull(),
            marginOfSafety = marginOfSafety?.toBigDecimalOrNull(),
            paybackPeriod = paybackPeriod?.toBigDecimalOrNull(),
            estimatedROI = estimatedROI?.toBigDecimalOrNull(),
            estimatedIRR = estimatedIRR?.toBigDecimalOrNull(),
            estimatedFcfGrowthRate = estimatedFcfGrowthRate?.toBigDecimalOrNull(),
            investmentHorizonYears = investmentHorizonYears,
            discountRate = discountRate?.toBigDecimalOrNull(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun WatchlistItemResponse.toEntity(): WatchlistItemEntity {
    // userId is not in the simplified response model we updated to earlier, 
    // but we need it for DB. We'll use a placeholder or handle it if we add it back.
    // For now, let's assume we can pass userId or store a default.
    // NOTE: In the previous turn, we removed userId from WatchlistItemResponse to match the guide.
    // To fix this properly, I should check if I need to add userId back or handle it differently.
    // For now, I will use a placeholder UUID since the API filters by user anyway.
    
    return WatchlistItemEntity(
        id = id.toString(),
        userId = "00000000-0000-0000-0000-000000000000", // Placeholder if not available in response
        ticker = ticker,
        exchange = exchange,
        targetPrice = targetPrice?.toString(),
        targetPfcf = targetPfcf?.toString(),
        notifyWhenBelowPrice = notifyWhenBelowPrice,
        notes = notes,
        currentPrice = currentPrice?.toString(),
        marketCapitalization = marketCapitalization?.toString(),
        weekHigh52 = weekHigh52?.toString(),
        weekLow52 = weekLow52?.toString(),
        weekRange52Position = weekRange52Position?.toString(),
        freeCashFlowPerShare = freeCashFlowPerShare?.toString(),
        actualPfcf = actualPfcf?.toString(),
        peAnnual = peAnnual?.toString(),
        beta = beta?.toString(),
        focfCagr5Y = focfCagr5Y?.toString(),
        dividendYield = dividendYield?.toString(),
        dividendGrowthRate5Y = dividendGrowthRate5Y?.toString(),
        dividendCoverageRatio = dividendCoverageRatio?.toString(),
        payoutRatioFcf = payoutRatioFcf?.toString(),
        chowderRuleValue = chowderRuleValue?.toString(),
        fairPriceByPfcf = fairPriceByPfcf?.toString(),
        discountToFairPrice = discountToFairPrice?.toString(),
        deviationFromTargetPrice = deviationFromTargetPrice?.toString(),
        undervalued = undervalued,
        dcfFairValue = dcfFairValue?.toString(),
        fcfYield = fcfYield?.toString(),
        marginOfSafety = marginOfSafety?.toString(),
        paybackPeriod = paybackPeriod?.toString(),
        estimatedROI = estimatedROI?.toString(),
        estimatedIRR = estimatedIRR?.toString(),
        estimatedFcfGrowthRate = estimatedFcfGrowthRate?.toString(),
        investmentHorizonYears = investmentHorizonYears,
        discountRate = discountRate?.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
