package com.aistudio.qrgenerator.kmpzqr.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Region-to-currency display mapping.
 * No exchange-rate data is stored or presented as current market data.
 */
data class GoogleSupportedCurrency(
    val code: String,
    val symbol: String,
    val nameEn: String,
    val nameTh: String,
    val flagEmoji: String
)

object CurrencyManager {
    private const val PREFS_NAME = "region_currency_prefs"
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
        "KW" to "KWD",
        "AT" to "EUR", "BE" to "EUR", "HR" to "EUR", "CY" to "EUR",
        "EE" to "EUR", "FI" to "EUR", "FR" to "EUR", "DE" to "EUR",
        "GR" to "EUR", "IE" to "EUR", "IT" to "EUR", "LV" to "EUR",
        "LT" to "EUR", "LU" to "EUR", "MT" to "EUR", "NL" to "EUR",
        "PT" to "EUR", "SK" to "EUR", "SI" to "EUR", "ES" to "EUR"
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

    val ALL_CURRENCIES = listOf(
        GoogleSupportedCurrency("THB", "฿", "Thai Baht", "บาทไทย", "🇹🇭"),
        GoogleSupportedCurrency("USD", "$", "US Dollar", "ดอลลาร์สหรัฐ", "🇺🇸"),
        GoogleSupportedCurrency("EUR", "€", "Euro", "ยูโร", "🇪🇺"),
        GoogleSupportedCurrency("JPY", "¥", "Japanese Yen", "เยนญี่ปุ่น", "🇯🇵"),
        GoogleSupportedCurrency("GBP", "£", "British Pound", "ปอนด์สเตอร์ลิง", "🇬🇧"),
        GoogleSupportedCurrency("AUD", "A$", "Australian Dollar", "ดอลลาร์ออสเตรเลีย", "🇦🇺"),
        GoogleSupportedCurrency("CAD", "C$", "Canadian Dollar", "ดอลลาร์แคนาดา", "🇨🇦"),
        GoogleSupportedCurrency("SGD", "S$", "Singapore Dollar", "ดอลลาร์สิงคโปร์", "🇸🇬"),
        GoogleSupportedCurrency("HKD", "HK$", "Hong Kong Dollar", "ดอลลาร์ฮ่องกง", "🇭🇰"),
        GoogleSupportedCurrency("CNY", "¥", "Chinese Yuan", "หยวนจีน", "🇨🇳"),
        GoogleSupportedCurrency("KRW", "₩", "South Korean Won", "วอนเกาหลีใต้", "🇰🇷"),
        GoogleSupportedCurrency("TWD", "NT$", "New Taiwan Dollar", "ดอลลาร์ไต้หวันใหม่", "🇹🇼"),
        GoogleSupportedCurrency("INR", "₹", "Indian Rupee", "รูปีอินเดีย", "🇮🇳"),
        GoogleSupportedCurrency("MYR", "RM", "Malaysian Ringgit", "ริงกิตมาเลเซีย", "🇲🇾"),
        GoogleSupportedCurrency("PHP", "₱", "Philippine Peso", "เปโซฟิลิปปินส์", "🇵🇭"),
        GoogleSupportedCurrency("VND", "₫", "Vietnamese Dong", "ดงเวียดนาม", "🇻🇳"),
        GoogleSupportedCurrency("IDR", "Rp", "Indonesian Rupiah", "รูเปียห์อินโดนีเซีย", "🇮🇩"),
        GoogleSupportedCurrency("BRL", "R$", "Brazilian Real", "เรอัลบราซิล", "🇧🇷"),
        GoogleSupportedCurrency("MXN", "Mex$", "Mexican Peso", "เปโซเม็กซิโก", "🇲🇽"),
        GoogleSupportedCurrency("CHF", "CHF", "Swiss Franc", "ฟรังก์สวิส", "🇨🇭"),
        GoogleSupportedCurrency("AED", "AED", "UAE Dirham", "เดอร์แฮมสหรัฐอาหรับเอมิเรตส์", "🇦🇪"),
        GoogleSupportedCurrency("SAR", "SAR", "Saudi Riyal", "ริยาลซาอุดีอาระเบีย", "🇸🇦"),
        GoogleSupportedCurrency("ZAR", "R", "South African Rand", "แรนด์แอฟริกาใต้", "🇿🇦"),
        GoogleSupportedCurrency("NZD", "NZ$", "New Zealand Dollar", "ดอลลาร์นิวซีแลนด์", "🇳🇿"),
        GoogleSupportedCurrency("SEK", "kr", "Swedish Krona", "โครนาสวีเดน", "🇸🇪"),
        GoogleSupportedCurrency("NOK", "kr", "Norwegian Krone", "โครนนอร์เวย์", "🇳🇴"),
        GoogleSupportedCurrency("DKK", "kr", "Danish Krone", "โครนเดนมาร์ก", "🇩🇰"),
        GoogleSupportedCurrency("PLN", "zł", "Polish Zloty", "ซลอตีโปแลนด์", "🇵🇱"),
        GoogleSupportedCurrency("TRY", "₺", "Turkish Lira", "ลีราตุรกี", "🇹🇷"),
        GoogleSupportedCurrency("RUB", "₽", "Russian Ruble", "รูเบิลรัสเซีย", "🇷🇺"),
        GoogleSupportedCurrency("ILS", "₪", "Israeli New Shekel", "เชเกลอิสราเอล", "🇮🇱"),
        GoogleSupportedCurrency("CZK", "Kč", "Czech Koruna", "คอรูนาเช็ก", "🇨🇿"),
        GoogleSupportedCurrency("HUF", "Ft", "Hungarian Forint", "ฟอรินต์ฮังการี", "🇭🇺"),
        GoogleSupportedCurrency("CLP", "CLP$", "Chilean Peso", "เปโซชิลี", "🇨🇱"),
        GoogleSupportedCurrency("COP", "COL$", "Colombian Peso", "เปโซโคลอมเบีย", "🇨🇴"),
        GoogleSupportedCurrency("EGP", "E£", "Egyptian Pound", "ปอนด์อียิปต์", "🇪🇬"),
        GoogleSupportedCurrency("QAR", "QR", "Qatari Riyal", "ริยาลกาตาร์", "🇶🇦"),
        GoogleSupportedCurrency("KWD", "KD", "Kuwaiti Dinar", "ดีนาร์คูเวต", "🇰🇼")
    )

    private val _selectedCurrency = MutableStateFlow(ALL_CURRENCIES.first())
    val selectedCurrency: StateFlow<GoogleSupportedCurrency> = _selectedCurrency.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs?.getString(KEY_CURRENCY_CODE, null)
            val detectedCode =
                savedCode ?: COUNTRY_TO_CURRENCY[java.util.Locale.getDefault().country.uppercase()] ?: "USD"
            _selectedCurrency.value =
                ALL_CURRENCIES.find { it.code.equals(detectedCode, ignoreCase = true) }
                    ?: ALL_CURRENCIES.first { it.code == "USD" }
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
}
