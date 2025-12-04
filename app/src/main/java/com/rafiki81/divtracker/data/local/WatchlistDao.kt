package com.rafiki81.divtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<WatchlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items")
    suspend fun clearAll()
    
    @Query("DELETE FROM watchlist_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Transaction
    suspend fun replaceAll(items: List<WatchlistItemEntity>) {
        clearAll()
        insertAll(items)
    }
    
    // Para actualizaciones parciales de precios (desde FCM por ejemplo)
    @Query("""
        UPDATE watchlist_items 
        SET currentPrice = :currentPrice, 
            dailyChangePercent = :dailyChangePercent,
            updatedAt = :updatedAt
        WHERE UPPER(ticker) = UPPER(:ticker)
    """)
    suspend fun updatePriceByTicker(
        ticker: String,
        currentPrice: String,
        dailyChangePercent: String?,
        updatedAt: String = System.currentTimeMillis().toString()
    )

    @Query("SELECT * FROM watchlist_items WHERE UPPER(ticker) = UPPER(:ticker) LIMIT 1")
    suspend fun getByTicker(ticker: String): WatchlistItemEntity?
}
