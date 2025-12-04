package com.rafiki81.divtracker.ui.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.local.AppDatabase
import com.rafiki81.divtracker.data.model.WatchlistPage
import com.rafiki81.divtracker.data.model.WatchlistItemRequest
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import com.rafiki81.divtracker.data.repository.WatchlistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = WatchlistRepository(RetrofitClient.watchlistApiService, database.watchlistDao())
    
    // State para lista de items
    private val _listState = MutableStateFlow<WatchlistListState>(WatchlistListState.Idle)
    val listState: StateFlow<WatchlistListState> = _listState.asStateFlow()
    
    // State para detalle de un item
    private val _detailState = MutableStateFlow<WatchlistDetailState>(WatchlistDetailState.Idle)
    val detailState: StateFlow<WatchlistDetailState> = _detailState.asStateFlow()
    
    // State para operaciones de creación/actualización/eliminación
    private val _operationState = MutableStateFlow<WatchlistOperationState>(WatchlistOperationState.Idle)
    val operationState: StateFlow<WatchlistOperationState> = _operationState.asStateFlow()
    
    // State para indicar si el refresh de red está en progreso
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Configuración actual de ordenamiento
    private var currentSortOption = SortOption.MARGIN_DESC

    // Job para auto-refresh
    private var autoRefreshJob: Job? = null

    // Intervalo de auto-refresh (30 segundos)
    companion object {
        private const val AUTO_REFRESH_INTERVAL_MS = 30_000L
    }

    init {
        // Observar cambios en la base de datos local
        viewModelScope.launch {
            repository.watchlistItems.collectLatest { items ->
                if (items.isNotEmpty()) {
                    // Aplicar ordenamiento en memoria a los datos locales
                    val sortedItems = sortItems(items, currentSortOption)
                    
                    // Crear un objeto WatchlistPage ficticio para mantener compatibilidad con el UI State existente
                    // (En una app real, el UI State debería usar List<Item> directamente si es offline-first)
                    val page = WatchlistPage(
                        content = sortedItems,
                        totalElements = items.size.toLong(),
                        totalPages = 1,
                        size = items.size,
                        number = 0,
                        numberOfElements = items.size,
                        first = true,
                        last = true,
                        empty = false
                    )
                    _listState.value = WatchlistListState.Success(page)
                } else if (_listState.value is WatchlistListState.Loading) {
                    // Si sigue cargando y no hay datos locales, esperar a la red
                } else {
                     // Empty state
                     val page = WatchlistPage(emptyList(), 0, 0, 0, 0, 0, true, true, true)
                     _listState.value = WatchlistListState.Success(page)
                }
            }
        }
    }

    /**
     * Cargar lista (Sincronizar con API)
     */
    fun loadWatchlist(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt", // Default API sort
        direction: String = "DESC"    // Default API direction
    ) {
        viewModelScope.launch {
            // Solo mostrar loading si no hay datos ya mostrados (para no parpadear)
            if (_listState.value is WatchlistListState.Idle) {
                _listState.value = WatchlistListState.Loading
            }
            
            _isRefreshing.value = true

            repository.refreshWatchlist(page, size, sortBy, direction)
                .onSuccess {
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _isRefreshing.value = false
                    // Si falla la red pero tenemos datos locales, no mostramos error bloqueante
                    if (_listState.value !is WatchlistListState.Success) {
                        _listState.value = WatchlistListState.Error(
                            error.message ?: "Error al cargar watchlist"
                        )
                    }
                }
        }
    }

    /**
     * Ordenar la lista actual en memoria
     */
    fun sortWatchlistInMemory(sortOption: SortOption) {
        currentSortOption = sortOption
        val currentState = _listState.value
        if (currentState is WatchlistListState.Success) {
            val sortedItems = sortItems(currentState.watchlistPage.content, sortOption)
            val sortedPage = currentState.watchlistPage.copy(content = sortedItems)
            _listState.value = WatchlistListState.Success(sortedPage)
        }
    }
    
    private fun sortItems(items: List<WatchlistItemResponse>, sortOption: SortOption): List<WatchlistItemResponse> {
        return when (sortOption) {
            SortOption.MARGIN_DESC -> items.sortedByDescending { it.marginOfSafety ?: BigDecimal("-999") }
            SortOption.YIELD_DESC -> items.sortedByDescending { it.fcfYield ?: BigDecimal.ZERO }
            SortOption.TICKER_ASC -> items.sortedBy { it.ticker }
            SortOption.CREATED_DESC -> items.sortedByDescending { it.createdAt }
        }
    }
    
    // --- Métodos CRUD (delegan al repositorio que actualiza API y DB) ---
    
    fun loadItemDetail(id: UUID) {
        viewModelScope.launch {
            _detailState.value = WatchlistDetailState.Loading
            repository.getItemById(id)
                .onSuccess { item -> _detailState.value = WatchlistDetailState.Success(item) }
                .onFailure { error -> _detailState.value = WatchlistDetailState.Error(error.message ?: "Error") }
        }
    }
    
    fun createItem(request: WatchlistItemRequest) {
        viewModelScope.launch {
            _operationState.value = WatchlistOperationState.Loading
            repository.createItem(request)
                .onSuccess { item -> 
                    _operationState.value = WatchlistOperationState.Created(item)
                    // No need to call loadWatchlist, DB flow will update automatically
                }
                .onFailure { error -> 
                    _operationState.value = WatchlistOperationState.Error(error.message ?: "Error") 
                }
        }
    }
    
    fun updateItem(id: UUID, request: WatchlistItemRequest) {
        viewModelScope.launch {
            _operationState.value = WatchlistOperationState.Loading
            repository.updateItem(id, request)
                .onSuccess { item -> _operationState.value = WatchlistOperationState.Updated(item) }
                .onFailure { error -> _operationState.value = WatchlistOperationState.Error(error.message ?: "Error") }
        }
    }
    
    fun deleteItem(id: UUID) {
        viewModelScope.launch {
            _operationState.value = WatchlistOperationState.Loading
            repository.deleteItem(id)
                .onSuccess { _operationState.value = WatchlistOperationState.Deleted }
                .onFailure { error -> _operationState.value = WatchlistOperationState.Error(error.message ?: "Error") }
        }
    }
    
    fun resetListState() { _listState.value = WatchlistListState.Idle }
    fun resetDetailState() { _detailState.value = WatchlistDetailState.Idle }
    fun resetOperationState() { _operationState.value = WatchlistOperationState.Idle }

    /**
     * Iniciar auto-refresh de precios cada 30 segundos
     */
    fun startAutoRefresh() {
        // Cancelar job anterior si existe
        autoRefreshJob?.cancel()

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                refreshSilently()
            }
        }
    }

    /**
     * Detener auto-refresh (cuando la pantalla no está visible)
     */
    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    /**
     * Refresh silencioso sin mostrar indicador de carga
     * Usado para actualizaciones automáticas de precios
     */
    private suspend fun refreshSilently() {
        repository.refreshWatchlist(0, 20, "createdAt", "DESC")
        // No mostramos errores en refresh silencioso para no molestar al usuario
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
