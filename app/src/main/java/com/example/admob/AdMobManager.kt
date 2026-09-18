package com.example.admob

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AdMobStats(
    val totalImpressions: Int = 0,
    val interstitialImpressions: Int = 0,
    val bannerImpressions: Int = 0,
    val totalSaves: Int = 0,
    val totalScans: Int = 0,
    val estimatedEarningsThb: Double = 0.0
)

object AdMobManager {

    // Official Google AdMob Test Ad Unit IDs
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    private val _stats = MutableStateFlow(AdMobStats())
    val stats: StateFlow<AdMobStats> = _stats.asStateFlow()

    // State for showing interstitial
    private val _activeInterstitial = MutableStateFlow<InterstitialRequest?>(null)
    val activeInterstitial: StateFlow<InterstitialRequest?> = _activeInterstitial.asStateFlow()

    data class InterstitialRequest(
        val title: String,
        val triggerReason: String, // "ใช้งานฟรี", "สร้าง QR Code ฟรี", "รับสิทธิ์ใช้งานฟรี 1 ครั้ง"
        val totalDurationSeconds: Int = 30,
        val isFreeAdReward: Boolean = true,
        val onDismiss: () -> Unit
    )

    fun recordBannerImpression() {
        val current = _stats.value
        val newImpressions = current.totalImpressions + 1
        val newBanner = current.bannerImpressions + 1
        val newEarnings = calculateEarnings(newImpressions)
        _stats.value = current.copy(
            totalImpressions = newImpressions,
            bannerImpressions = newBanner,
            estimatedEarningsThb = newEarnings
        )
    }

    /**
     * Triggers a 30-second AdMob ad required for free tier usage.
     * User watches 30 seconds and can then skip to proceed.
     */
    fun show30sFreeAd(
        triggerReason: String,
        onActionProceed: () -> Unit
    ) {
        _activeInterstitial.value = InterstitialRequest(
            title = "โฆษณาสนับสนุนผู้ใช้งานฟรี (30 วินาที)",
            triggerReason = triggerReason,
            totalDurationSeconds = 30,
            isFreeAdReward = true,
            onDismiss = {
                val current = _stats.value
                val newImpressions = current.totalImpressions + 1
                val newInterstitial = current.interstitialImpressions + 1
                val newEarnings = calculateEarnings(newImpressions)

                _stats.value = current.copy(
                    totalImpressions = newImpressions,
                    interstitialImpressions = newInterstitial,
                    estimatedEarningsThb = newEarnings
                )
                _activeInterstitial.value = null
                onActionProceed()
            }
        )
    }

    /**
     * Triggers an AdMob Interstitial Ad before executing the pending action.
     */
    fun showInterstitial(
        triggerReason: String,
        durationSeconds: Int = 30,
        onActionProceed: () -> Unit
    ) {
        _activeInterstitial.value = InterstitialRequest(
            title = "Google AdMob โฆษณาสำหรับผู้ใช้งานฟรี",
            triggerReason = triggerReason,
            totalDurationSeconds = durationSeconds,
            isFreeAdReward = true,
            onDismiss = {
                val current = _stats.value
                val newImpressions = current.totalImpressions + 1
                val newInterstitial = current.interstitialImpressions + 1
                val newSaves = if (triggerReason.contains("เซฟ") || triggerReason.contains("บันทึก")) current.totalSaves + 1 else current.totalSaves
                val newScans = if (triggerReason.contains("สแกน")) current.totalScans + 1 else current.totalScans
                val newEarnings = calculateEarnings(newImpressions)

                _stats.value = current.copy(
                    totalImpressions = newImpressions,
                    interstitialImpressions = newInterstitial,
                    totalSaves = newSaves,
                    totalScans = newScans,
                    estimatedEarningsThb = newEarnings
                )
                _activeInterstitial.value = null
                onActionProceed()
            }
        )
    }

    fun dismissInterstitialWithoutAction() {
        _activeInterstitial.value = null
    }

    private fun calculateEarnings(impressions: Int): Double {
        // Realistic Thailand eCPM estimation: ~48 THB per 1000 impressions
        val ecpmThb = 48.50
        return (impressions * ecpmThb) / 1000.0
    }

    fun formattedEarnings(): String {
        return String.format(Locale.US, "฿ %,.2f", _stats.value.estimatedEarningsThb)
    }
}
