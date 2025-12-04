package com.rafiki81.divtracker.ui.watchlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.api.TokenManager
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import com.rafiki81.divtracker.data.repository.FcmTokenRepository
import com.rafiki81.divtracker.util.ColorUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onLogout: () -> Unit,
    viewModel: WatchlistViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // FCM Token Repository para desregistrar dispositivo en logout
    val fcmTokenRepository = remember {
        FcmTokenRepository(RetrofitClient.deviceApiService, context)
    }

    // Sorting state (Default: Margin Descending)
    var sortOption by remember { mutableStateOf(SortOption.MARGIN_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Refresh State from ViewModel
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Cache items
    var cachedItems by remember { mutableStateOf<List<WatchlistItemResponse>?>(null) }

    // Update cache when list state changes
    LaunchedEffect(listState) {
        if (listState is WatchlistListState.Success) {
            cachedItems = (listState as WatchlistListState.Success).watchlistPage.content
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
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FAB pequeño para logout
                SmallFloatingActionButton(
                    onClick = { showLogoutDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                }
                // FAB principal para añadir
                FloatingActionButton(onClick = onNavigateToCreate) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Cerrar sesión") },
                text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            coroutineScope.launch {
                                // 1. Desregistrar dispositivo del backend
                                fcmTokenRepository.unregisterCurrentDevice()
                                // 2. Limpiar datos locales FCM
                                fcmTokenRepository.clearLocalData()
                                // 3. Limpiar token de autenticación
                                TokenManager.clearToken()
                                // 4. Navegar a login
                                onLogout()
                            }
                        }
                    ) {
                        Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = {
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
                    // Precio con animación de pulso cuando cambia
                    AnimatedPriceText(
                        price = item.currentPrice,
                        dailyChangePercent = item.dailyChangePercent,
                        isBelowTarget = isBelowTarget,
                        targetHitColor = targetHitColor
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

/**
 * Precio animado con efecto de pulso cuando cambia el valor.
 * También muestra el cambio diario en porcentaje.
 */
@Composable
fun AnimatedPriceText(
    price: BigDecimal?,
    dailyChangePercent: BigDecimal?,
    isBelowTarget: Boolean,
    targetHitColor: Color
) {
    // Escala animada para el efecto de pulso
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Recordar el precio anterior para detectar cambios
    var previousPrice by remember { mutableStateOf(price) }

    // Color del cambio diario
    val changeColor = ColorUtils.getDailyChangeColor(dailyChangePercent)

    // Detectar cambio de precio y animar
    LaunchedEffect(price) {
        if (previousPrice != null && price != null && previousPrice != price) {
            // Precio cambió - ejecutar animación de pulso
            coroutineScope.launch {
                // Escalar hacia arriba
                scale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(durationMillis = 150)
                )
                // Volver al tamaño normal
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
        }
        previousPrice = price
    }

    Column(horizontalAlignment = Alignment.End) {
        // Precio principal con animación de escala
        Text(
            text = if (price != null) "$${price}" else "N/A",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isBelowTarget) targetHitColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.scale(scale.value)
        )

        // Cambio diario en porcentaje
        if (dailyChangePercent != null) {
            val sign = if (dailyChangePercent >= BigDecimal.ZERO) "+" else ""
            Text(
                text = "$sign${dailyChangePercent}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

