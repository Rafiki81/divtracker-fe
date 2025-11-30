package com.rafiki81.divtracker.ui.watchlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: WatchlistViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Sorting state (Default: Margin Descending)
    var sortOption by remember { mutableStateOf(SortOption.MARGIN_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Refresh State
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Cache items
    var cachedItems by remember { mutableStateOf<List<WatchlistItemResponse>?>(null) }

    // Update cache and stop refreshing
    LaunchedEffect(listState) {
        if (listState is WatchlistListState.Success) {
            cachedItems = (listState as WatchlistListState.Success).watchlistPage.content
            isRefreshing = false
        } else if (listState is WatchlistListState.Error) {
            isRefreshing = false
        }
    }

    // Load data on first composition (Always load from API fresh)
    LaunchedEffect(Unit) {
        viewModel.loadWatchlist()
    }
    
    // When sort option changes, sort in memory (don't reload from API)
    LaunchedEffect(sortOption) {
        viewModel.sortWatchlistInMemory(sortOption)
    }
    
    // Reset operation state when entering this screen
    LaunchedEffect(Unit) {
        viewModel.resetOperationState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Watchlist") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Most Undervalued (Margin)") },
                            onClick = { 
                                sortOption = SortOption.MARGIN_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Yield") },
                            onClick = { 
                                sortOption = SortOption.YIELD_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ticker (A-Z)") },
                            onClick = { 
                                sortOption = SortOption.TICKER_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Newest First") },
                            onClick = { 
                                sortOption = SortOption.CREATED_DESC
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = {
                isRefreshing = true
                viewModel.loadWatchlist() // API reload
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val itemsToShow = cachedItems
                val isLoading = listState is WatchlistListState.Loading
                val isError = listState is WatchlistListState.Error
                
                if (itemsToShow != null) {
                    if (itemsToShow.isEmpty()) {
                        if (!isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No items in watchlist. Add one!")
                            }
                        }
                    } else {
                        WatchlistList(
                            items = itemsToShow,
                            onItemClick = { item -> onNavigateToDetail(item.id.toString()) }
                        )
                    }
                } else if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (isError) {
                    val errorMessage = (listState as WatchlistListState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadWatchlist() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

enum class SortOption(val field: String, val direction: String) {
    CREATED_DESC("createdAt", "DESC"),
    MARGIN_DESC("marginOfSafety", "DESC"),
    YIELD_DESC("fcfYield", "DESC"),
    TICKER_ASC("ticker", "ASC")
}

@Composable
fun WatchlistList(
    items: List<WatchlistItemResponse>,
    onItemClick: (WatchlistItemResponse) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            WatchlistItemCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun WatchlistItemCard(
    item: WatchlistItemResponse,
    onClick: () -> Unit
) {
    val marginOfSafety = item.marginOfSafety ?: BigDecimal.ZERO
    val fcfYield = item.fcfYield ?: BigDecimal.ZERO
    val targetPrice = item.targetPrice
    val currentPrice = item.currentPrice ?: BigDecimal.ZERO
    
    // Logic for buy signal: Current Price <= Target Price
    val isBelowTarget = targetPrice != null && currentPrice <= targetPrice
    val targetHitColor = Color(0xFF1976D2) // Material Blue 700
    
    // Logic for colors
    val marginColor = when {
        marginOfSafety > BigDecimal.ZERO -> Color(0xFF2E7D32) // Green
        else -> Color(0xFFC62828) // Red
    }
    
    val yieldColor = when {
        fcfYield >= BigDecimal("4.0") -> Color(0xFF2E7D32) // Green
        fcfYield > BigDecimal.ZERO -> Color(0xFFF57F17) // Orange/Dark Yellow
        else -> Color(0xFFC62828) // Red
    }
    
    // Undervalued Status Logic
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
        null -> "N/A"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row: Ticker | Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.ticker,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.exchange ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Status Indicator
                    Spacer(modifier = Modifier.height(4.dp))
                    if (undervalued != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(
                                 imageVector = statusIcon,
                                 contentDescription = null,
                                 tint = statusColor,
                                 modifier = Modifier.size(14.dp)
                             )
                             Spacer(modifier = Modifier.width(4.dp))
                             Text(
                                 text = statusText,
                                 style = MaterialTheme.typography.labelSmall,
                                 color = statusColor,
                                 fontWeight = FontWeight.Bold
                             )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.currentPrice != null) "$${item.currentPrice}" else "N/A",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isBelowTarget) targetHitColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (isBelowTarget) {
                        Text(
                            text = "BELOW TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = targetHitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // REORGANIZED Metrics Row: Focus on Dividend Investing
            // 1. Dividend Yield (What you get paid)
            // 2. Margin of Safety (Is it cheap?)
            // 3. FCF Yield (Is it sustainable/cash rich?)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Div Yield", 
                    value = item.dividendYield?.let { "${it}%" } ?: "N/A",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                
                MetricItem(
                    label = "Margin", 
                    value = item.marginOfSafety?.let { "${it}%" } ?: "N/A",
                    valueColor = if (item.marginOfSafety != null) marginColor else MaterialTheme.colorScheme.onSurface
                )
                
                MetricItem(
                    label = "FCF Yield", 
                    value = item.fcfYield?.let { "${it}%" } ?: "N/A",
                    valueColor = if (item.fcfYield != null) yieldColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String, 
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
