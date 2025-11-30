package com.rafiki81.divtracker.ui.ticker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafiki81.divtracker.data.model.TickerSearchResult
import com.rafiki81.divtracker.data.model.WatchlistItemRequest
import com.rafiki81.divtracker.ui.watchlist.WatchlistOperationState
import com.rafiki81.divtracker.ui.watchlist.WatchlistViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickerSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    searchViewModel: TickerSearchViewModel = viewModel(),
    watchlistViewModel: WatchlistViewModel = viewModel()
) {
    val searchState by searchViewModel.searchState.collectAsState()
    val operationState by watchlistViewModel.operationState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }
    
    // State to track which ticker is currently being added to show specific loading
    var addingTicker by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle Quick Add Success
    LaunchedEffect(operationState) {
        if (operationState is WatchlistOperationState.Created) {
            addingTicker = null // Reset loading state
            watchlistViewModel.resetOperationState()
            onNavigateToWatchlist()
        } else if (operationState is WatchlistOperationState.Error) {
            addingTicker = null // Reset loading state on error
            snackbarHostState.showSnackbar((operationState as WatchlistOperationState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add to Watchlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    searchJob?.cancel()
                    if (query.isNotBlank()) {
                        searchJob = scope.launch {
                            delay(500) // Debounce
                            searchViewModel.searchTickers(query)
                        }
                    } else {
                        searchViewModel.clearSearch()
                    }
                },
                label = { Text("Search Company or Ticker") },
                placeholder = { Text("e.g. Coca Cola, AAPL") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = searchState) {
                    is TickerSearchState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter))
                    }
                    is TickerSearchState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                    is TickerSearchState.Success -> {
                        if (state.results.isEmpty()) {
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "No results found",
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.results) { result ->
                                    TickerResultItem(
                                        result = result,
                                        isAdding = addingTicker == result.symbol,
                                        enabled = addingTicker == null, // Disable others while adding one
                                        onClick = { 
                                            // Set loading state for THIS ticker
                                            addingTicker = result.symbol
                                            watchlistViewModel.createItem(
                                                WatchlistItemRequest(ticker = result.symbol)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is TickerSearchState.Idle -> {
                        // Optional: Show popular tickers or instructions
                    }
                }
            }
        }
    }
}

@Composable
fun TickerResultItem(
    result: TickerSearchResult,
    isAdding: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && !isAdding, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isAdding) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                result.exchange?.let { exchange ->
                    Text(
                        text = exchange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Action Button Area
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onClick,
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
