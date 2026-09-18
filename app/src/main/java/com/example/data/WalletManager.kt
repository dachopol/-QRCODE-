package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WalletState(
    val freeUsesLeft: Int = 3,
    val paidCredits: Int = 0,
    val walletBalance: Double = 0.0,
    val isMonthlyVip: Boolean = false,
    val vipExpiryTimestamp: Long = 0L
) {
    val isUnlimitedVip: Boolean
        get() = isMonthlyVip && System.currentTimeMillis() < vipExpiryTimestamp

    val totalAvailableUses: Int
        get() = if (isUnlimitedVip) Int.MAX_VALUE else (freeUsesLeft + paidCredits)

    val displayStatusText: String
        get() = when {
            isUnlimitedVip -> "VIP รายเดือน (ไม่จำกัด)"
            paidCredits > 0 -> "เครดิตคงเหลือ: $paidCredits ครั้ง"
            freeUsesLeft > 0 -> "ทดลองใช้ฟรี: เหลือ $freeUsesLeft/3 ครั้ง"
            else -> "สิทธิ์หมดแล้ว (กรุณาเติมเงิน หรือดูโฆษณา)"
        }
}

data class PricingPlan(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceThb: Double,
    val originalPriceThb: Double? = null,
    val badge: String? = null,
    val isMonthlySubscription: Boolean = false,
    val durationDays: Int = 0,
    val creditsGranted: Int = 0
)

sealed class BillingSupportResult {
    object Supported : BillingSupportResult()
    data class NotSupported(val reason: String) : BillingSupportResult()
}

sealed class SlipVerificationResult {
    data class Success(val message: String) : SlipVerificationResult()
    data class Unsupported(val reason: String) : SlipVerificationResult()
    data class Failed(val error: String) : SlipVerificationResult()
}

object WalletManager {
    private const val PREFS_NAME = "qr_wallet_prefs"
    private const val KEY_FREE_USES = "key_free_uses"
    private const val KEY_PAID_CREDITS = "key_paid_credits"
    private const val KEY_WALLET_BALANCE = "key_wallet_balance"
    private const val KEY_IS_MONTHLY_VIP = "key_is_monthly_vip"
    private const val KEY_VIP_EXPIRY = "key_vip_expiry"

    const val INITIAL_FREE_USES = 3
    const val SUPPORT_EMAIL = "chenkung12@gmail.com"

    // Preset Pricing Plans
    val PLANS = listOf(
        PricingPlan(
            id = "monthly_vip",
            title = "แพ็กเกจรายเดือน (VIP)",
            subtitle = "ใช้งานได้ไม่จำกัด 30 วัน ไม่มีโฆษณา บันทึกภาพชัดพิเศษ",
            priceThb = 59.0,
            originalPriceThb = 99.0,
            badge = "ยอดนิยม 🔥",
            isMonthlySubscription = true,
            durationDays = 30
        ),
        PricingPlan(
            id = "annual_vip",
            title = "แพ็กเกจรายปี (สุดคุ้ม)",
            subtitle = "ใช้งานได้ไม่จำกัด 365 วัน เฉลี่ยเพียง ฿40/เดือน",
            priceThb = 490.0,
            originalPriceThb = 708.0,
            badge = "ประหยัด 30%",
            isMonthlySubscription = true,
            durationDays = 365
        ),
        PricingPlan(
            id = "credits_100",
            title = "เติมเครดิต 120 ครั้ง",
            subtitle = "เหมาะสำหรับร้านค้าที่สร้างคิวอาร์บ่อย ใช้งานเมื่อไหร่ก็ได้ ไม่มีวันหมดอายุ",
            priceThb = 100.0,
            originalPriceThb = 150.0,
            badge = "แถม 20 ครั้ง",
            creditsGranted = 120
        ),
        PricingPlan(
            id = "credits_50",
            title = "เติมเครดิต 50 ครั้ง",
            subtitle = "สำหรับใช้งานทั่วไป ปริมาณกำลังดี",
            priceThb = 50.0,
            creditsGranted = 50
        ),
        PricingPlan(
            id = "credits_20",
            title = "เติมเครดิต 15 ครั้ง",
            subtitle = "เริ่มต้นใช้งานแบบสบายกระเป๋า",
            priceThb = 20.0,
            creditsGranted = 15
        )
    )

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadState()
        }
    }

    private fun loadState() {
        val p = prefs
        val free = p?.getInt(KEY_FREE_USES, INITIAL_FREE_USES) ?: INITIAL_FREE_USES
        val credits = p?.getInt(KEY_PAID_CREDITS, 0) ?: 0
        val balance = p?.getFloat(KEY_WALLET_BALANCE, 0f)?.toDouble() ?: 0.0
        val isVip = p?.getBoolean(KEY_IS_MONTHLY_VIP, false) ?: false
        val expiry = p?.getLong(KEY_VIP_EXPIRY, 0L) ?: 0L

        val activeVip = isVip && System.currentTimeMillis() < expiry

        _walletState.value = WalletState(
            freeUsesLeft = free,
            paidCredits = credits,
            walletBalance = balance,
            isMonthlyVip = activeVip,
            vipExpiryTimestamp = expiry
        )
    }

    /**
     * Real check for Google Play Billing availability.
     * Returns real result; if unsupported, clearly states not supported.
     */
    fun checkGooglePlayBillingSupport(context: Context): BillingSupportResult {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES)
            // Found Google Play Store package, but In-App Billing library requires Google Play Merchant account
            BillingSupportResult.NotSupported(
                "Google Play Billing: ยังไม่พร้อมให้บริการในบิลด์นี้ (ยังไม่ได้เชื่อมต่อ Google Play Console Merchant Account)"
            )
        } catch (_: PackageManager.NameNotFoundException) {
            BillingSupportResult.NotSupported(
                "Google Play Billing: ไม่รองรับในอุปกรณ์นี้ (ไม่พบแอปพลิเคชัน Google Play Store)"
            )
        } catch (e: Exception) {
            BillingSupportResult.NotSupported(
                "Google Play Billing: ไม่สามารถตรวจสอบได้ (${e.localizedMessage ?: "ข้อผิดพลาดระบบ"})"
            )
        }
    }

    /**
     * Real check for online bank slip verification.
     * Returns Unsupported if automated backend slip server is not configured.
     */
    fun verifySlipOnline(refCode: String): SlipVerificationResult {
        return SlipVerificationResult.Unsupported(
            "ระบบตรวจสอบสลิปอัตโนมัติ: ยังไม่รองรับในอุปกรณ์/บิลด์ออฟไลน์นี้ (กรุณาส่งหลักฐานสลิปโอนเงินพร้อมรหัสอ้างอิง $refCode ไปที่อีเมล $SUPPORT_EMAIL เพื่อให้อนุมัติสิทธิ์)"
        )
    }

    /**
     * Checks if user has remaining usage quota or active VIP subscription.
     */
    fun canUse(): Boolean {
        val state = _walletState.value
        if (state.isUnlimitedVip) return true
        if (state.paidCredits > 0) return true
        return state.freeUsesLeft > 0
    }

    /**
     * Consumes 1 use if on free trial or paid credits.
     * VIP users don't have credits deducted.
     * Returns true if successfully consumed, false if quota exhausted.
     */
    fun consumeUsage(): Boolean {
        val state = _walletState.value
        if (state.isUnlimitedVip) {
            return true
        }
        if (state.freeUsesLeft > 0) {
            val newFree = state.freeUsesLeft - 1
            prefs?.edit()?.putInt(KEY_FREE_USES, newFree)?.apply()
            _walletState.value = state.copy(freeUsesLeft = newFree)
            return true
        }
        if (state.paidCredits > 0) {
            val newCredits = state.paidCredits - 1
            prefs?.edit()?.putInt(KEY_PAID_CREDITS, newCredits)?.apply()
            _walletState.value = state.copy(paidCredits = newCredits)
            return true
        }
        return false
    }

    /**
     * Rewards user with free uses after watching a rewarded ad.
     */
    fun addFreeUse(amount: Int = 1) {
        val state = _walletState.value
        val newFree = state.freeUsesLeft + amount
        prefs?.edit()?.putInt(KEY_FREE_USES, newFree)?.apply()
        _walletState.value = state.copy(freeUsesLeft = newFree)
    }

    /**
     * Top-up balance into wallet (from manual admin approval or voucher).
     */
    fun addWalletBalance(amount: Double) {
        val state = _walletState.value
        val newBalance = state.walletBalance + amount
        prefs?.edit()?.putFloat(KEY_WALLET_BALANCE, newBalance.toFloat())?.apply()
        _walletState.value = state.copy(walletBalance = newBalance)
    }

    /**
     * Activates a plan when real payment is verified or confirmed by admin.
     */
    fun activatePlan(plan: PricingPlan): Boolean {
        val state = _walletState.value
        val editor = prefs?.edit() ?: return false

        if (plan.isMonthlySubscription) {
            val currentExpiry = if (state.isUnlimitedVip) state.vipExpiryTimestamp else System.currentTimeMillis()
            val addedDuration = plan.durationDays * 24L * 60L * 60L * 1000L
            val newExpiry = currentExpiry + addedDuration

            editor.putBoolean(KEY_IS_MONTHLY_VIP, true)
            editor.putLong(KEY_VIP_EXPIRY, newExpiry)
            editor.apply()

            _walletState.value = state.copy(
                isMonthlyVip = true,
                vipExpiryTimestamp = newExpiry
            )
            return true
        } else {
            val newCredits = state.paidCredits + plan.creditsGranted
            editor.putInt(KEY_PAID_CREDITS, newCredits)
            editor.apply()

            _walletState.value = state.copy(paidCredits = newCredits)
            return true
        }
    }

    /**
     * Pay for a plan using available Wallet Balance.
     */
    fun payPlanWithWallet(plan: PricingPlan): Boolean {
        val state = _walletState.value
        if (state.walletBalance < plan.priceThb) return false

        val remainingBalance = state.walletBalance - plan.priceThb
        prefs?.edit()?.putFloat(KEY_WALLET_BALANCE, remainingBalance.toFloat())?.apply()
        _walletState.value = state.copy(walletBalance = remainingBalance)

        return activatePlan(plan)
    }
}
