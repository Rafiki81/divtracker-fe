package com.rafiki81.divtracker.ui.ticker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiki81.divtracker.data.api.RetrofitClient
import com.rafiki81.divtracker.data.model.TickerSearchResult
import com.rafiki81.divtracker.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TickerSearchState {
    object Idle : TickerSearchState()
    object Loading : TickerSearchState()
    data class Success(val results: List<TickerSearchResult>) : TickerSearchState()
    data class Error(val message: String) : TickerSearchState()
}

class TickerSearchViewModel : ViewModel() {
    
    private val repository = WatchlistRepository(RetrofitClient.watchlistApiService)
    
    private val _searchState = MutableStateFlow<TickerSearchState>(TickerSearchState.Idle)
    val searchState: StateFlow<TickerSearchState> = _searchState
    
    /**
     * Symbol Lookup - Exact search (Recommended)
     */
    fun lookupSymbol(symbol: String) {
        if (symbol.isBlank()) {
            _searchState.value = TickerSearchState.Idle
            return
        }
        
        viewModelScope.launch {
            _searchState.value = TickerSearchState.Loading
            
            val result = repository.lookupSymbol(symbol)
            
            _searchState.value = result.fold(
                onSuccess = { TickerSearchState.Success(it) },
                onFailure = { TickerSearchState.Error(it.message ?: "Unknown error") }
            )
        }
    }
    
    /**
     * Search by Name - Fuzzy search
     */
    fun searchTickers(query: String) {
        if (query.isBlank()) {
            _searchState.value = TickerSearchState.Idle
            return
        }
        
        viewModelScope.launch {
            _searchState.value = TickerSearchState.Loading
            
            val result = repository.searchTickers(query)
            
            _searchState.value = result.fold(
                onSuccess = { TickerSearchState.Success(it) },
                onFailure = { TickerSearchState.Error(it.message ?: "Unknown error") }
            )
        }
    }
    
    fun clearSearch() {
        _searchState.value = TickerSearchState.Idle
    }
}
