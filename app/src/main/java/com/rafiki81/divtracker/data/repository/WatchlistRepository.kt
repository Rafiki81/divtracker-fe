package com.rafiki81.divtracker.data.repository

import com.rafiki81.divtracker.data.api.WatchlistApiService
import com.rafiki81.divtracker.data.local.WatchlistDao
import com.rafiki81.divtracker.data.local.toEntity
import com.rafiki81.divtracker.data.model.TickerSearchResult
import com.rafiki81.divtracker.data.model.WatchlistItemRequest
import com.rafiki81.divtracker.data.model.WatchlistItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.UUID

class WatchlistRepository(
    private val apiService: WatchlistApiService,
    private val watchlistDao: WatchlistDao
) {

    // Fuente de verdad: Base de datos local
    val watchlistItems: Flow<List<WatchlistItemResponse>> = watchlistDao.getAllItems()
        .map { entities -> entities.map { it.toDomainModel() } }

    /**
     * Refrescar datos desde la API y guardarlos en local.
     */
    suspend fun refreshWatchlist(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        direction: String = "DESC"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.listWatchlistItems(page, size, sortBy, direction)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.content
                watchlistDao.replaceAll(items.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error fetching data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Métodos existentes (CRUD Directo a API + Actualización Local Optimista) ---

    suspend fun lookupSymbol(symbol: String): Result<List<TickerSearchResult>> =
        withContext(Dispatchers.IO) {
            try {
                if (symbol.isBlank()) return@withContext Result.success(emptyList())
                val response = apiService.lookupSymbol(symbol)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun searchTickers(query: String): Result<List<TickerSearchResult>> =
        withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) return@withContext Result.success(emptyList())
                val response = apiService.searchTickers(query)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    suspend fun listItems(page: Int, size: Int, sortBy: String, direction: String) = refreshWatchlist(page, size, sortBy, direction)

    suspend fun getItemById(id: UUID): Result<WatchlistItemResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWatchlistItemById(id)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createItem(request: WatchlistItemRequest): Result<WatchlistItemResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createWatchlistItem(request)
                val result = handleResponse(response)
                result.onSuccess { watchlistDao.insert(it.toEntity()) } // Guardar en local
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    suspend fun updateItem(id: UUID, request: WatchlistItemRequest): Result<WatchlistItemResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateWatchlistItem(id, request)
                val result = handleResponse(response)
                result.onSuccess { watchlistDao.insert(it.toEntity()) } // Actualizar local
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    suspend fun deleteItem(id: UUID): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteWatchlistItem(id)
            if (response.isSuccessful) {
                watchlistDao.deleteById(id.toString()) // Borrar local
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            // Intentar extraer el mensaje de error del cuerpo de la respuesta
            val errorMessage = try {
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    // El backend devuelve JSON como: {"message":"Ticker 'PG' already exists..."}
                    val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
                    regex.find(errorBody)?.groupValues?.get(1) ?: "Error ${response.code()}: ${response.message()}"
                } else {
                    "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                "Error ${response.code()}: ${response.message()}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}
