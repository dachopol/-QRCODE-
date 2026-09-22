package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

/**
 * Currency recognized and supported by Google Play Store billing worldwide.
 */
data class GoogleSupportedCurrency(
    val code: String,              // e.g. "USD", "THB", "EUR"
    val symbol: String,            // e.g. "$", "฿", "€"
    val nameEn: String,            // e.g. "US Dollar"
    val nameTh: String,            // e.g. "ดอลลาร์สหรัฐ"
    val flagEmoji: String,         // e.g. "🇺🇸"
    val rateToThb: Double,         // 1 THB = rateToThb in this currency
    val decimalPlaces: Int = 2,    // standard decimal places (0 for JPY, KRW, VND, IDR)
    val isGooglePlayCertified: Boolean = true
) {
    fun convertFromThb(amountThb: Double): Double {
        return amountThb * rateToThb
    }

    fun format(amountThb: Double): String {
        val converted = convertFromThb(amountThb)
        return if (decimalPlaces == 0) {
            val df = DecimalFormat("#,##0")
            "$symbol${df.format(converted)}"
        } else {
            val df = DecimalFormat("#,##0.00")
            "$symbol${df.format(converted)}"
        }
    }
}

object CurrencyManager {
    private const val PREFS_NAME = "google_currency_prefs"
    private const val KEY_CURRENCY_CODE = "key_currency_code"

    private var prefs: SharedPreferences? = null

    private val COUNTRY_TO_CURRENCY = mapOf(
        "TH" to "THB", "US" to "USD", "JP" to "JPY", "KR" to "KRW",
        "GB" to "GBP", "AU" to "AUD", "CA" to "CAD", "SG" to "SGD",
        "HK" to "HKD", "CN" to "CNY", "TW" to "TWD", "IN" to "INR",
        "MY" to "MYR", "PH" to "PHP", "VN" to "VND", "ID" to "IDR",
        "BR" to "BRL", "MX" to "MXN", "CH" to "CHF", "AE" to "AED",
        "SA" to "SAR", "ZA" to "ZAR", "NZ" to "NZD", "SE" to "SEK",
        "NO" to "NOK", "DK" to "DKK", "PL" to "PLN", "TR" to "TRY",
        "RU" to "RUB", "IL" to "ILS", "CZ" to "CZK", "HU" to "HUF",
        "CL" to "CLP", "CO" to "COP", "EG" to "EGP", "QA" to "QAR",
        "KW" to "KWD"
    )

    private val REGION_NAME_EN = mapOf(
        "THB" to "Thailand", "USD" to "United States", "EUR" to "Euro area",
        "JPY" to "Japan", "GBP" to "United Kingdom", "AUD" to "Australia",
        "CAD" to "Canada", "SGD" to "Singapore", "HKD" to "Hong Kong",
        "CNY" to "China", "KRW" to "South Korea", "TWD" to "Taiwan",
        "INR" to "India", "MYR" to "Malaysia", "PHP" to "Philippines",
        "VND" to "Vietnam", "IDR" to "Indonesia", "BRL" to "Brazil",
        "MXN" to "Mexico", "CHF" to "Switzerland", "AED" to "United Arab Emirates",
        "SAR" to "Saudi Arabia", "ZAR" to "South Africa", "NZD" to "New Zealand",
        "SEK" to "Sweden", "NOK" to "Norway", "DKK" to "Denmark",
        "PLN" to "Poland", "TRY" to "Türkiye", "RUB" to "Russia",
        "ILS" to "Israel", "CZK" to "Czechia", "HUF" to "Hungary",
        "CLP" to "Chile", "COP" to "Colombia", "EGP" to "Egypt",
        "QAR" to "Qatar", "KWD" to "Kuwait"
    )

    private val REGION_NAME_TH = mapOf(
        "THB" to "ประเทศไทย", "USD" to "สหรัฐอเมริกา", "EUR" to "เขตยูโร",
        "JPY" to "ญี่ปุ่น", "GBP" to "สหราชอาณาจักร", "AUD" to "ออสเตรเลีย",
        "CAD" to "แคนาดา", "SGD" to "สิงคโปร์", "HKD" to "ฮ่องกง",
        "CNY" to "จีน", "KRW" to "เกาหลีใต้", "TWD" to "ไต้หวัน",
        "INR" to "อินเดีย", "MYR" to "มาเลเซีย", "PHP" to "ฟิลิปปินส์",
        "VND" to "เวียดนาม", "IDR" to "อินโดนีเซีย", "BRL" to "บราซิล",
        "MXN" to "เม็กซิโก", "CHF" to "สวิตเซอร์แลนด์", "AED" to "สหรัฐอาหรับเอมิเรตส์",
        "SAR" to "ซาอุดีอาระเบีย", "ZAR" to "แอฟริกาใต้", "NZD" to "นิวซีแลนด์",
        "SEK" to "สวีเดน", "NOK" to "นอร์เวย์", "DKK" to "เดนมาร์ก",
        "PLN" to "โปแลนด์", "TRY" to "ตุรกี", "RUB" to "รัสเซีย",
        "ILS" to "อิสราเอล", "CZK" to "เช็ก", "HUF" to "ฮังการี",
        "CLP" to "ชิลี", "COP" to "โคลอมเบีย", "EGP" to "อียิปต์",
        "QAR" to "กาตาร์", "KWD" to "คูเวต"
    )


    // Comprehensive list of Google Play accepted billing currencies worldwide
    val ALL_CURRENCIES = listOf(
        GoogleSupportedCurrency("THB", "฿", "Thai Baht", "บาทไทย", "🇹🇭", 1.0, 2),
        GoogleSupportedCurrency("USD", "$", "US Dollar", "ดอลลาร์สหรัฐ", "🇺🇸", 0.028, 2),
        GoogleSupportedCurrency("EUR", "€", "Euro", "ยูโร", "🇪🇺", 0.026, 2),
        GoogleSupportedCurrency("JPY", "¥", "Japanese Yen", "เยนญี่ปุ่น", "🇯🇵", 4.15, 0),
        GoogleSupportedCurrency("GBP", "£", "British Pound", "ปอนด์สเตอร์ลิง", "🇬🇧", 0.022, 2),
        GoogleSupportedCurrency("AUD", "A$", "Australian Dollar", "ดอลลาร์ออสเตรเลีย", "🇦🇺", 0.043, 2),
        GoogleSupportedCurrency("CAD", "C$", "Canadian Dollar", "ดอลลาร์แคนาดา", "🇨🇦", 0.038, 2),
        GoogleSupportedCurrency("SGD", "S$", "Singapore Dollar", "ดอลลาร์สิงคโปร์", "🇸🇬", 0.037, 2),
        GoogleSupportedCurrency("HKD", "HK$", "Hong Kong Dollar", "ดอลลาร์ฮ่องกง", "🇭🇰", 0.22, 2),
        GoogleSupportedCurrency("CNY", "¥", "Chinese Yuan", "หยวนจีน", "🇨🇳", 0.20, 2),
        GoogleSupportedCurrency("KRW", "₩", "South Korean Won", "วอนเกาหลีใต้", "🇰🇷", 37.5, 0),
        GoogleSupportedCurrency("TWD", "NT$", "New Taiwan Dollar", "ดอลลาร์ไต้หวันใหม่", "🇹🇼", 0.89, 2),
        GoogleSupportedCurrency("INR", "₹", "Indian Rupee", "รูปีอินเดีย", "🇮🇳", 2.35, 2),
        GoogleSupportedCurrency("MYR", "RM", "Malaysian Ringgit", "ริงกิตมาเลเซีย", "🇲🇾", 0.13, 2),
        GoogleSupportedCurrency("PHP", "₱", "Philippine Peso", "เปโซฟิลิปปินส์", "🇵🇭", 1.58, 2),
        GoogleSupportedCurrency("VND", "₫", "Vietnamese Dong", "ดงเวียดนาม", "🇻🇳", 710.0, 0),
        GoogleSupportedCurrency("IDR", "Rp", "Indonesian Rupiah", "รูเปียห์อินโดนีเซีย", "🇮🇩", 440.0, 0),
        GoogleSupportedCurrency("BRL", "R$", "Brazilian Real", "เรอัลบราซิล", "🇧🇷", 0.15, 2),
        GoogleSupportedCurrency("MXN", "Mex$", "Mexican Peso", "เปโซเม็กซิโก", "🇲🇽", 0.52, 2),
        GoogleSupportedCurrency("CHF", "CHF", "Swiss Franc", "ฟรังก์สวิส", "🇨🇭", 0.025, 2),
        GoogleSupportedCurrency("AED", "AED", "UAE Dirham", "เดอร์แฮมสหรัฐอาหรับเอมิเรตส์", "🇦🇪", 0.10, 2),
        GoogleSupportedCurrency("SAR", "SAR", "Saudi Riyal", "ริยาลซาอุดีอาระเบีย", "🇸🇦", 0.105, 2),
        GoogleSupportedCurrency("ZAR", "R", "South African Rand", "แรนด์แอฟริกาใต้", "🇿🇦", 0.51, 2),
        GoogleSupportedCurrency("NZD", "NZ$", "New Zealand Dollar", "ดอลลาร์นิวซีแลนด์", "🇳🇿", 0.046, 2),
        GoogleSupportedCurrency("SEK", "kr", "Swedish Krona", "โครนาสวีเดน", "🇸🇪", 0.29, 2),
        GoogleSupportedCurrency("NOK", "kr", "Norwegian Krone", "โครนนอร์เวย์", "🇳🇴", 0.30, 2),
        GoogleSupportedCurrency("DKK", "kr", "Danish Krone", "โครนเดนมาร์ก", "🇩🇰", 0.19, 2),
        GoogleSupportedCurrency("PLN", "zł", "Polish Zloty", "ซลอตีโปแลนด์", "🇵🇱", 0.11, 2),
        GoogleSupportedCurrency("TRY", "₺", "Turkish Lira", "ลีราตุรกี", "🇹🇷", 0.95, 2),
        GoogleSupportedCurrency("RUB", "₽", "Russian Ruble", "รูเบิลรัสเซีย", "🇷🇺", 2.50, 2),
        GoogleSupportedCurrency("ILS", "₪", "Israeli New Shekel", "เชเกลอิสราเอล", "🇮🇱", 0.105, 2),
        GoogleSupportedCurrency("CZK", "Kč", "Czech Koruna", "คอรูนาเช็ก", "🇨🇿", 0.65, 2),
        GoogleSupportedCurrency("HUF", "Ft", "Hungarian Forint", "ฟอรินต์ฮังการี", "🇭🇺", 10.2, 0),
        GoogleSupportedCurrency("CLP", "CLP$", "Chilean Peso", "เปโซชิลี", "🇨🇱", 26.5, 0),
        GoogleSupportedCurrency("COP", "COL$", "Colombian Peso", "เปโซโคลอมเบีย", "🇨🇴", 115.0, 0),
        GoogleSupportedCurrency("EGP", "E£", "Egyptian Pound", "ปอนด์อียิปต์", "🇪🇬", 1.35, 2),
        GoogleSupportedCurrency("QAR", "QR", "Qatari Riyal", "ริยาลกาตาร์", "🇶🇦", 0.102, 2),
        GoogleSupportedCurrency("KWD", "KD", "Kuwaiti Dinar", "ดีนาร์คูเวต", "🇰🇼", 0.0086, 3)
    )

    private val _selectedCurrency = MutableStateFlow(ALL_CURRENCIES.first())
    val selectedCurrency: StateFlow<GoogleSupportedCurrency> = _selectedCurrency.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs?.getString(KEY_CURRENCY_CODE, null)
            val detectedCode = savedCode ?: COUNTRY_TO_CURRENCY[java.util.Locale.getDefault().country.uppercase()] ?: "USD"
            val match = ALL_CURRENCIES.find { it.code.equals(detectedCode, ignoreCase = true) } ?: ALL_CURRENCIES.first()
            _selectedCurrency.value = match
        }
    }

    fun setCurrency(currency: GoogleSupportedCurrency) {
        _selectedCurrency.value = currency
        prefs?.edit()?.putString(KEY_CURRENCY_CODE, currency.code)?.apply()
    }

    fun regionNameEn(currency: GoogleSupportedCurrency): String =
        REGION_NAME_EN[currency.code] ?: currency.nameEn

    fun regionNameTh(currency: GoogleSupportedCurrency): String =
        REGION_NAME_TH[currency.code] ?: currency.nameTh

    fun setCurrencyByCode(code: String) {
        val found = ALL_CURRENCIES.find { it.code.equals(code, ignoreCase = true) } ?: return
        setCurrency(found)
    }

    fun formatPrice(thbAmount: Double): String {
        return _selectedCurrency.value.format(thbAmount)
    }
}
