package com.rafiki81.divtracker.ui.watchlist

import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import com.rafiki81.divtracker.data.model.WatchlistPage

/**
 * Estados para la lista de watchlist
 */
sealed class WatchlistListState {
    object Idle : WatchlistListState()
    object Loading : WatchlistListState()
    data class Success(val watchlistPage: WatchlistPage) : WatchlistListState()
    data class Error(val message: String) : WatchlistListState()
}

/**
 * Estados para el detalle de un item
 */
sealed class WatchlistDetailState {
    object Idle : WatchlistDetailState()
    object Loading : WatchlistDetailState()
    data class Success(val item: WatchlistItemResponse) : WatchlistDetailState()
    data class Error(val message: String) : WatchlistDetailState()
}

/**
 * Estados para operaciones CRUD
 */
sealed class WatchlistOperationState {
    object Idle : WatchlistOperationState()
    object Loading : WatchlistOperationState()
    data class Created(val item: WatchlistItemResponse) : WatchlistOperationState()
    data class Updated(val item: WatchlistItemResponse) : WatchlistOperationState()
    object Deleted : WatchlistOperationState()
    data class Error(val message: String) : WatchlistOperationState()
}
