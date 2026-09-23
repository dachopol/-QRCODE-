package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String
)

object LocalizationManager {
    private const val PREFS_NAME = "app_locale_prefs"
    private const val KEY_LANG_CODE = "key_lang_code"

    private var prefs: SharedPreferences? = null

    val ALL_LANGUAGES = listOf(
        SupportedLanguage("th", "Thai", "ภาษาไทย", "🇹🇭"),
        SupportedLanguage("en", "English", "English", "🇺🇸")
    )
    val AVAILABLE_UI_LANGUAGES: List<SupportedLanguage> = ALL_LANGUAGES

    private val _currentLanguage = MutableStateFlow(ALL_LANGUAGES.first())
    val currentLanguage: StateFlow<SupportedLanguage> = _currentLanguage.asStateFlow()

    fun effectiveLanguageCode(code: String = _currentLanguage.value.code): String =
        if (code == "th") "th" else "en"

    val isCurrentLanguageRtl: Boolean
        get() = false

    fun initialize(context: Context) {
        if (prefs != null) return

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs?.getString(KEY_LANG_CODE, null)
        val selected = if (savedCode != null) {
            AVAILABLE_UI_LANGUAGES.find { it.code.equals(savedCode, ignoreCase = true) }
        } else {
            val systemLanguage = java.util.Locale.getDefault().language.lowercase()
            AVAILABLE_UI_LANGUAGES.find { it.code == systemLanguage }
        } ?: AVAILABLE_UI_LANGUAGES.first { it.code == "en" }

        _currentLanguage.value = selected
        prefs?.edit()?.putString(KEY_LANG_CODE, selected.code)?.apply()
    }

    fun setLanguage(language: SupportedLanguage) {
        val effective = AVAILABLE_UI_LANGUAGES.find {
            it.code.equals(language.code, ignoreCase = true)
        } ?: AVAILABLE_UI_LANGUAGES.first { it.code == "en" }

        _currentLanguage.value = effective
        prefs?.edit()?.putString(KEY_LANG_CODE, effective.code)?.apply()
    }

    fun setLanguageByCode(code: String) {
        val found = AVAILABLE_UI_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
            ?: AVAILABLE_UI_LANGUAGES.first { it.code == "en" }
        setLanguage(found)
    }

    private val translations: Map<String, Map<String, String>> = mapOf(
        "nav_generate" to mapOf("th" to "สร้าง QR", "en" to "Generate QR"),
        "nav_card" to mapOf("th" to "นามบัตร", "en" to "Business Card"),
        "nav_scanner" to mapOf("th" to "สแกนเนอร์", "en" to "Scanner"),
        "nav_history" to mapOf("th" to "ประวัติ", "en" to "History"),
        "app_title" to mapOf("th" to "QuickQR Business", "en" to "QuickQR Business"),
        "contact_admin" to mapOf("th" to "ช่วยเหลือ", "en" to "Support"),
        "tab_language" to mapOf("th" to "เลือกภาษา", "en" to "Language"),
        "tab_currency" to mapOf("th" to "ภูมิภาค", "en" to "Region"),
        "search_language" to mapOf("th" to "ค้นหาภาษา...", "en" to "Search language..."),
        "search_currency" to mapOf(
            "th" to "ค้นหาประเทศ ภูมิภาค หรือสกุลเงิน...",
            "en" to "Search region or currency..."
        ),
        "google_certified_badge" to mapOf(
            "th" to "สกุลเงินถูกกำหนดตามภูมิภาค",
            "en" to "Currency follows the selected region"
        ),
        "cat_promptpay" to mapOf("th" to "พร้อมเพย์", "en" to "PromptPay"),
        "cat_wifi" to mapOf("th" to "Wi-Fi", "en" to "Wi-Fi"),
        "cat_store" to mapOf("th" to "ลิงก์ร้านค้า", "en" to "Store Link"),
        "cat_text" to mapOf("th" to "ข้อความ", "en" to "Text / URL"),
        "field_promptpay_id" to mapOf(
            "th" to "เบอร์โทรศัพท์ หรือ เลขบัตรประชาชน",
            "en" to "Phone Number or Citizen/Tax ID"
        ),
        "field_amount" to mapOf("th" to "จำนวนเงิน (ไม่บังคับ)", "en" to "Amount (Optional)"),
        "field_shop_name" to mapOf(
            "th" to "ชื่อร้านค้า หรือ บัญชีผู้รับ (บนป้ายตั้งโต๊ะ)",
            "en" to "Shop or Payee Name (for Standee)"
        ),
        "btn_generate_promptpay" to mapOf(
            "th" to "สร้าง QR พร้อมเพย์ & ป้ายตั้งโต๊ะ",
            "en" to "Generate PromptPay QR & Standee"
        ),
        "color_customizer_title" to mapOf(
            "th" to "ปรับแต่งสีและดีไซน์ QR Code",
            "en" to "QR Code Color & Design Customizer"
        ),
        "color_pattern" to mapOf("th" to "สีลวดลาย QR", "en" to "QR Pattern Color"),
        "color_bg" to mapOf("th" to "สีพื้นหลัง", "en" to "Background Color"),
        "color_live_preview" to mapOf("th" to "แสดงตัวอย่างสี", "en" to "Live Color Preview"),
        "close" to mapOf("th" to "ปิด", "en" to "Close"),
        "confirm" to mapOf("th" to "ยืนยัน", "en" to "Confirm")
    )

    fun getString(key: String, vararg args: Any): String {
        val code = effectiveLanguageCode()
        val entry = translations[key]
        val raw = entry?.get(code) ?: entry?.get("en") ?: entry?.get("th") ?: key

        return if (args.isNotEmpty()) {
            try {
                String.format(raw, *args)
            } catch (_: Exception) {
                raw
            }
        } else {
            raw
        }
    }
}

@Composable
fun localizedString(key: String, vararg args: Any): String {
    val lang by LocalizationManager.currentLanguage.collectAsState()
    lang.code
    return LocalizationManager.getString(key, *args)
}

@Composable
fun localizedText(th: String, en: String): String {
    val lang by LocalizationManager.currentLanguage.collectAsState()
    return if (LocalizationManager.effectiveLanguageCode(lang.code) == "th") th else en
}

fun localizedNow(th: String, en: String): String =
    if (LocalizationManager.effectiveLanguageCode() == "th") th else en
