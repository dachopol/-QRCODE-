package com.aistudio.qrgenerator.kmpzqr.model

enum class QrCategory(val titleTh: String, val subtitleTh: String) {
    PROMPTPAY("พร้อมเพย์", "รับเงินง่าย ระบุยอดเงินได้"),
    BUSINESS_CARD("นามบัตรดิจิทัล", "สำหรับร้านค้า ช่าง ฟรีแลนซ์"),
    WIFI("ไวไฟ (Wi-Fi)", "สแกนเชื่อมต่อทันที"),
    STORE_LINK("ลิงก์ร้านค้า", "Shopee, TikTok, LINE, เว็บไซต์"),
    LOCATION("พิกัดแผนที่", "สร้าง QR จากจุดปัจจุบัน"),
    TEXT("ข้อความทั่วไป", "ข้อความและรายละเอียด")
}

data class PromptPayModel(
    val targetId: String = "", // Mobile number (10 digits) or Citizen ID (13 digits)
    val amount: Double? = null,
    val note: String = ""
) {
    val isPhone: Boolean
        get() = targetId.replace("-", "").trim().length in 9..10

    val isCitizenId: Boolean
        get() = targetId.replace("-", "").trim().length == 13
}

data class WifiModel(
    val ssid: String = "",
    val password: String = "",
    val security: WifiSecurity = WifiSecurity.WPA,
    val isHidden: Boolean = false
)

enum class WifiSecurity(val label: String, val code: String) {
    WPA("WPA / WPA2 / WPA3", "WPA"),
    WEP("WEP", "WEP"),
    OPEN("ไม่มีรหัสผ่าน (Open)", "nopass")
}

enum class StorePlatform(val title: String, val defaultPrefix: String, val placeholder: String) {
    SHOPEE("Shopee", "https://shopee.co.th/", "username หรือ ลิงก์ร้าน"),
    LAZADA("Lazada", "https://www.lazada.co.th/shop/", "shop-name หรือ ลิงก์"),
    TIKTOK("TikTok Shop", "https://www.tiktok.com/@", "username"),
    LINE_OA("LINE Official / ID", "https://line.me/R/ti/p/~", "Line ID เช่น @myshop"),
    FACEBOOK("Facebook Page", "https://m.me/", "ชื่อเพจ หรือ ID"),
    INSTAGRAM("Instagram", "https://instagram.com/", "username"),
    WEBSITE("เว็บไซต์ร้านค้า", "https://", "www.mywebsite.com")
}

data class StoreLinkModel(
    val platform: StorePlatform = StorePlatform.LINE_OA,
    val value: String = ""
) {
    val fullUrl: String
        get() {
            val clean = value.trim()
            return if (clean.startsWith("http://") || clean.startsWith("https://")) {
                clean
            } else {
                "${platform.defaultPrefix}$clean"
            }
        }
}

data class DigitalBusinessCard(
    val fullName: String = "",
    val businessName: String = "",
    val profession: String = "", // e.g. ช่างรับเหมาต่อเติม, แม่ค้าออนไลน์, ช่างไฟฟ้า, กราฟิกดีไซเนอร์
    val phoneNumber: String = "",
    val promptPayId: String = "",
    val lineId: String = "",
    val facebook: String = "",
    val email: String = "",
    val address: String = "",
    val services: String = "", // ขอบเขตงาน / สินค้าแนะนำ
    val cardTheme: CardColorTheme = CardColorTheme.NAVY_BLUE
)

enum class CardColorTheme(
    val themeName: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val surfaceColorHex: Long
) {
    NAVY_BLUE("พร้อมเพย์ บลู (Navy)", 0xFF0B2853, 0xFF0284C7, 0xFFF0F6FF),
    LUXURY_GOLD("โกลด์ พรีเมียม (Gold)", 0xFF92400E, 0xFFF59E0B, 0xFFFFFBEB),
    EMERALD_SHOP("เอเมอรัลด์ พ่อค้าแม่ค้า (Green)", 0xFF065F46, 0xFF10B981, 0xFFECFDF5),
    MIDNIGHT_DARK("มิดไนท์ หรูหรา (Dark)", 0xFF18181B, 0xFF6366F1, 0xFFF4F4F5),
    ROSE_MODERN("โรส คอนแทรคเตอร์ (Coral)", 0xFF9F1239, 0xFFF43F5E, 0xFFFFF1F2)
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun isValid(): Boolean =
        latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
}

enum class ParsedQrType {
    PROMPTPAY,
    WIFI,
    GEO,
    URL,
    VCARD,
    PHONE,
    TEXT
}

data class ParsedQrResult(
    val rawText: String,
    val type: ParsedQrType,
    val title: String,
    val subtitle: String,
    val promptPayId: String? = null,
    val amount: Double? = null,
    val wifiSsid: String? = null,
    val wifiPass: String? = null,
    val wifiSecurity: String? = null,
    val url: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactOrg: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
