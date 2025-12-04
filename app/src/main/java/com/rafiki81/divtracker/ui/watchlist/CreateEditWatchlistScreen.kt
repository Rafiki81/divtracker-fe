package com.rafiki81.divtracker.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafiki81.divtracker.data.model.WatchlistItemRequest
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditWatchlistScreen(
    itemId: String? = null,
    initialTicker: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: WatchlistViewModel = viewModel()
) {
    val isEditMode = itemId != null
    val operationState by viewModel.operationState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()

    // Form state
    var ticker by remember { mutableStateOf(initialTicker ?: "") }
    var exchange by remember { mutableStateOf("") }
    var targetPrice by remember { mutableStateOf("") }
    var targetPfcf by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var notifyWhenBelowPrice by remember { mutableStateOf(false) }
    
    // Advanced fields (Inputs allowed by Request model)
    var estimatedFcfGrowthRate by remember { mutableStateOf("") }
    var investmentHorizonYears by remember { mutableStateOf("") }
    var discountRate by remember { mutableStateOf("") }
    
    var showAdvanced by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para mostrar el diálogo de error
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    // Load data if in edit mode
    LaunchedEffect(itemId) {
        if (isEditMode && itemId != null) {
            viewModel.loadItemDetail(UUID.fromString(itemId))
        } else {
            viewModel.resetDetailState()
        }
    }
    
    // Populate form when detail loaded (Edit Mode)
    LaunchedEffect(detailState) {
        if (isEditMode && detailState is WatchlistDetailState.Success) {
            val item = (detailState as WatchlistDetailState.Success).item
            ticker = item.ticker
            exchange = item.exchange ?: ""
            targetPrice = item.targetPrice?.toString() ?: ""
            targetPfcf = item.targetPfcf?.toString() ?: ""
            notes = item.notes ?: ""
            notifyWhenBelowPrice = item.notifyWhenBelowPrice
            
            // Note: Advanced parameters are not part of the WatchlistItemResponse in the Master Guide,
            // so we cannot pre-fill them from the server data. They start empty.
            // If the user wants to override the server's calculated values, they can enter new ones here.
        }
    }

    // Handle operation success
    LaunchedEffect(operationState) {
        if (operationState is WatchlistOperationState.Created || operationState is WatchlistOperationState.Updated) {
            viewModel.resetOperationState()
            onNavigateBack()
        } else if (operationState is WatchlistOperationState.Error) {
            errorDialogMessage = (operationState as WatchlistOperationState.Error).message
            viewModel.resetOperationState()
        }
    }

    // Diálogo de error
    if (errorDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Error") },
            text = { Text(errorDialogMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Item" else "Add Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (ticker.isBlank()) {
                                // Show error
                                return@IconButton
                            }
                            
                            val tPrice = targetPrice.toBigDecimalOrNull()
                            val tPfcf = targetPfcf.toBigDecimalOrNull()
                            
                            // Request construction strictly following WatchlistItemRequest in Master Guide
                            val request = WatchlistItemRequest(
                                ticker = ticker,
                                exchange = exchange.takeIf { it.isNotBlank() },
                                targetPrice = tPrice,
                                targetPfcf = tPfcf,
                                notifyWhenBelowPrice = notifyWhenBelowPrice,
                                notes = notes.takeIf { it.isNotBlank() },
                                // Advanced params are optional in the Request
                                estimatedFcfGrowthRate = estimatedFcfGrowthRate.toBigDecimalOrNull(),
                                investmentHorizonYears = investmentHorizonYears.toIntOrNull(),
                                discountRate = discountRate.toBigDecimalOrNull()
                            )

                            if (isEditMode && itemId != null) {
                                viewModel.updateItem(UUID.fromString(itemId), request)
                            } else {
                                viewModel.createItem(request)
                            }
                        },
                        enabled = operationState !is WatchlistOperationState.Loading
                    ) {
                        if (operationState is WatchlistOperationState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            OutlinedTextField(
                value = ticker,
                onValueChange = { ticker = it },
                label = { Text("Ticker (e.g. AAPL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isEditMode && initialTicker == null // Disable if editing or pre-filled
            )

            OutlinedTextField(
                value = exchange,
                onValueChange = { exchange = it },
                label = { Text("Exchange (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()
            
            Column {
                Text("Targets & Valuation", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "Info", 
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leave targets blank to auto-calculate from market data.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            OutlinedTextField(
                value = targetPrice,
                onValueChange = { targetPrice = it },
                label = { Text("Target Price ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { Text("Fill to calc Target P/FCF") }
            )

            OutlinedTextField(
                value = targetPfcf,
                onValueChange = { targetPfcf = it },
                label = { Text("Target P/FCF") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { Text("Fill to calc Target Price") }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = notifyWhenBelowPrice,
                    onCheckedChange = { notifyWhenBelowPrice = it }
                )
                Text("Notify when below price")
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide Advanced" else "Show Advanced Valuation Settings")
            }

            if (showAdvanced) {
                Text(
                    text = "Override system defaults:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                OutlinedTextField(
                    value = estimatedFcfGrowthRate,
                    onValueChange = { estimatedFcfGrowthRate = it },
                    label = { Text("Est. FCF Growth (0.08 = 8%)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = investmentHorizonYears,
                    onValueChange = { investmentHorizonYears = it },
                    label = { Text("Horizon (Years)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = discountRate,
                    onValueChange = { discountRate = it },
                    label = { Text("Discount Rate (0.10 = 10%)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        }
    }
}
