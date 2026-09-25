package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_items")
data class QrItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // PROMPTPAY, WIFI, STORE_LINK, LOCATION, VCARD, TEXT, SCAN_RESULT
    val title: String,
    val subtitle: String,
    val rawContent: String,
    val targetId: String? = null,
    val amount: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isScan: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "merchant_profiles")
data class MerchantProfileEntity(
    @PrimaryKey
    val id: Int = 1, // Default primary profile
    val fullName: String = "",
    val businessName: String = "",
    val profession: String = "",
    val phoneNumber: String = "",
    val promptPayId: String = "",
    val lineId: String = "",
    val facebook: String = "",
    val email: String = "",
    val services: String = "",
    val cardTheme: String = "NAVY_BLUE"
)
