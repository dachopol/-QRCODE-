package com.aistudio.qrgenerator.kmpzqr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {

    @Query("""
        SELECT id, type, title, subtitle, rawContent, targetId, amount, timestamp, isScan, isFavorite
        FROM qr_items
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getAllQrItems(limit: Int): Flow<List<QrItemEntity>>

    @Query("""
        SELECT id, type, title, subtitle, rawContent, targetId, amount, timestamp, isScan, isFavorite
        FROM qr_items
        WHERE isScan = 0
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getGeneratedItems(limit: Int): Flow<List<QrItemEntity>>

    @Query("""
        SELECT id, type, title, subtitle, rawContent, targetId, amount, timestamp, isScan, isFavorite
        FROM qr_items
        WHERE isScan = 1
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getScannedItems(limit: Int): Flow<List<QrItemEntity>>

    @Query("SELECT COUNT(*) FROM qr_items")
    fun getQrItemCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrItem(item: QrItemEntity): Long

    @Delete
    suspend fun deleteQrItem(item: QrItemEntity)

    @Query("DELETE FROM qr_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM qr_items WHERE isScan = :isScan")
    suspend fun clearHistory(isScan: Boolean)

    @Update
    suspend fun updateQrItem(item: QrItemEntity)

    // Merchant Profile
    @Query("SELECT * FROM merchant_profiles WHERE id = 1 LIMIT 1")
    fun getMerchantProfile(): Flow<MerchantProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMerchantProfile(profile: MerchantProfileEntity)
}
