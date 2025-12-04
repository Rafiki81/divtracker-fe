package com.rafiki81.divtracker.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistDetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: WatchlistViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.loadItemDetail(UUID.fromString(itemId))
    }
    
    LaunchedEffect(operationState) {
        if (operationState is WatchlistOperationState.Deleted) {
            viewModel.resetOperationState()
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item from your watchlist?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(UUID.fromString(itemId))
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Valuation Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(itemId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = detailState) {
                is WatchlistDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WatchlistDetailState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is WatchlistDetailState.Success -> {
                    val item = state.item
                    
                    // Colors Logic
                    val marginColor = when {
                        (item.marginOfSafety ?: BigDecimal.ZERO) > BigDecimal.ZERO -> Color(0xFF2E7D32) // Green
                        else -> Color(0xFFC62828) // Red
                    }
                    
                    val undervalued = item.undervalued
                    val statusColor = when (undervalued) {
                        true -> Color(0xFF2E7D32) // Green
                        false -> Color(0xFFC62828) // Red
                        null -> Color.Gray
                    }
                    val statusIcon = when (undervalued) {
                        true -> Icons.AutoMirrored.Filled.TrendingUp
                        false -> Icons.AutoMirrored.Filled.TrendingDown
                        null -> Icons.AutoMirrored.Filled.Sort
                    }
                    val statusText = when (undervalued) {
                        true -> "UNDERVALUED"
                        false -> "OVERVALUED"
                        null -> "STATUS N/A"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header with Ticker and Status
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.ticker,
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.exchange ?: "",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatPrice(item.currentPrice),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        if (undervalued != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = statusColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // SECTION 1: INCOME (The Paycheck)
                        SectionHeader("Income & Safety")
                        
                        // Highlight Card for Dividend
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Dividend Yield", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = formatPercent(item.dividendYield),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Payout (FCF)", style = MaterialTheme.typography.labelMedium)
                                    val payout = item.payoutRatioFcf?.let {
                                        "${it.multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)}%"
                                    } ?: "N/A"
                                    Text(
                                        text = payout,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                item.dividendGrowthRate5Y?.let { MetricRow("Div Growth (5Y)", formatPercent(it)) }
                                MetricRow("Coverage Ratio", formatNumber(item.dividendCoverageRatio))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                MetricRow("Chowder Rule", formatNumber(item.chowderRuleValue))
                                item.focfCagr5Y?.let { MetricRow("FCF Growth (5Y)", formatPercent(it)) }
                            }
                        }

                        HorizontalDivider()
                        
                        // SECTION 2: VALUE (Is it cheap?)
                        SectionHeader("Value Analysis")
                        
                        // Margin Highlight
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Margin of Safety", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = formatPercent(item.marginOfSafety),
                                style = MaterialTheme.typography.headlineSmall,
                                color = marginColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                MetricRow("DCF Fair Value", formatPrice(item.dcfFairValue), highlight = true)
                                MetricRow("FCF Yield", formatPercent(item.fcfYield))
                                MetricRow("P/E Ratio", formatNumber(item.peAnnual, 2))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                MetricRow("Fair Price (P/FCF)", formatPrice(item.fairPriceByPfcf))
                                MetricRow("Discount to Fair", formatPercent(item.discountToFairPrice))
                                MetricRow("P/FCF (Actual)", formatNumber(item.actualPfcf, 2))
                            }
                        }

                        HorizontalDivider()

                        // SECTION 3: MARKET & QUALITY (Context)
                        SectionHeader("Market Data")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                MetricRow("Market Cap", formatMarketCap(item.marketCapitalization))
                                MetricRow("Beta", formatNumber(item.beta, 2))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                MetricRow("52W High", formatPrice(item.weekHigh52))
                                MetricRow("52W Low", formatPrice(item.weekLow52))
                            }
                        }
                        
                        // 52-Week Range Bar
                        if (item.weekRange52Position != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Column {
                                Text("52-Week Range", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { item.weekRange52Position.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                )
                            }
                        }
                        
                        HorizontalDivider()

                        // SECTION 4: MY TARGETS
                        SectionHeader("My Targets")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Target Price", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = item.targetPrice?.let { formatPrice(it) } ?: "Not Set",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Target P/FCF", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = item.targetPfcf?.let { formatNumber(it, 2) } ?: "N/A",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        if (!item.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Notes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = item.notes, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        // Extra: Assumptions Footer
                        if (item.estimatedFcfGrowthRate != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val growthFormatted = item.estimatedFcfGrowthRate.multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                            val discountFormatted = item.discountRate?.multiply(BigDecimal(100))?.setScale(2, RoundingMode.HALF_UP)
                            Text(
                                text = "Valuation based on ${growthFormatted}% growth & ${discountFormatted ?: "N/A"}% discount rate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {}
            }
            
            if (operationState is WatchlistOperationState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun MetricRow(label: String, value: String?, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value ?: "N/A", 
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Formatea el Market Cap.
 * NOTA: Si el valor viene de Finnhub, normalmente está en millones.
 * Ejemplo: 342822.15 millones = $342.82B
 */
fun formatMarketCap(value: BigDecimal?): String {
    if (value == null) return "N/A"

    val absValue = value.abs()

    // Asumimos que el valor viene en millones (común en APIs financieras)
    // Si es >= 1,000,000 (millones), entonces es en trillones
    // Si es >= 1,000 (millones), entonces es en billones
    // Si es < 1,000, es en millones

    return when {
        // >= 1,000,000 millones = Trillones
        absValue >= BigDecimal("1000000") -> {
            val formatted = value.divide(BigDecimal("1000000"), 2, RoundingMode.HALF_UP)
            "$${formatted}T"
        }
        // >= 1,000 millones = Billones (ej: 342822 millones = 342.82B)
        absValue >= BigDecimal("1000") -> {
            val formatted = value.divide(BigDecimal("1000"), 2, RoundingMode.HALF_UP)
            "$${formatted}B"
        }
        // < 1,000 = Millones
        else -> {
            "$${value.setScale(2, RoundingMode.HALF_UP)}M"
        }
    }
}

/**
 * Formatea un precio/valor monetario con 2 decimales máximo
 */
fun formatPrice(value: BigDecimal?): String {
    if (value == null) return "N/A"
    return "$${value.setScale(2, RoundingMode.HALF_UP)}"
}

/**
 * Formatea un número decimal con precisión limitada (4 decimales máx)
 */
fun formatNumber(value: BigDecimal?, decimals: Int = 2): String {
    if (value == null) return "N/A"
    return value.setScale(decimals, RoundingMode.HALF_UP).toString()
}

/**
 * Formatea un porcentaje con 2 decimales
 */
fun formatPercent(value: BigDecimal?): String {
    if (value == null) return "N/A"
    return "${value.setScale(2, RoundingMode.HALF_UP)}%"
}

