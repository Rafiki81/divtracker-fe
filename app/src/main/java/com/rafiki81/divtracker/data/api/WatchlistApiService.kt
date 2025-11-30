package com.rafiki81.divtracker.data.api

import com.rafiki81.divtracker.data.model.TickerSearchResult
import com.rafiki81.divtracker.data.model.WatchlistItemRequest
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import com.rafiki81.divtracker.data.model.WatchlistPage
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface WatchlistApiService {

    // Búsqueda de Ticker (Universal) - Recomendado
    @GET("api/v1/tickers/search")
    suspend fun searchTickers(
        @Query("q") query: String
    ): Response<List<TickerSearchResult>>

    // Búsqueda Estricta por Símbolo (Lookup)
    @GET("api/v1/tickers/lookup")
    suspend fun lookupSymbol(
        @Query("symbol") symbol: String
    ): Response<List<TickerSearchResult>>
    
    /**
     * Listar items del watchlist con paginación y ordenamiento
     */
    @GET("api/v1/watchlist")
    suspend fun listWatchlistItems(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("direction") direction: String = "DESC"
    ): Response<WatchlistPage>
    
    /**
     * Obtener detalles completos de un item por ID
     */
    @GET("api/v1/watchlist/{id}")
    suspend fun getWatchlistItemById(
        @Path("id") id: UUID
    ): Response<WatchlistItemResponse>
    
    /**
     * Crear nuevo item en el watchlist
     */
    @POST("api/v1/watchlist")
    suspend fun createWatchlistItem(
        @Body request: WatchlistItemRequest
    ): Response<WatchlistItemResponse>
    
    /**
     * Actualizar item existente (PATCH - actualización parcial)
     */
    @PATCH("api/v1/watchlist/{id}")
    suspend fun updateWatchlistItem(
        @Path("id") id: UUID,
        @Body request: WatchlistItemRequest
    ): Response<WatchlistItemResponse>
    
    /**
     * Eliminar item del watchlist
     */
    @DELETE("api/v1/watchlist/{id}")
    suspend fun deleteWatchlistItem(
        @Path("id") id: UUID
    ): Response<Unit>
}
