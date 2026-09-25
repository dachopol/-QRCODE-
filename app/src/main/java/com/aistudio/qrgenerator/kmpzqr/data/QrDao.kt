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

    @Query("SELECT * FROM qr_items ORDER BY timestamp DESC")
    fun getAllQrItems(): Flow<List<QrItemEntity>>

    @Query("SELECT * FROM qr_items WHERE isScan = 0 ORDER BY timestamp DESC")
    fun getGeneratedItems(): Flow<List<QrItemEntity>>

    @Query("SELECT * FROM qr_items WHERE isScan = 1 ORDER BY timestamp DESC")
    fun getScannedItems(): Flow<List<QrItemEntity>>

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
