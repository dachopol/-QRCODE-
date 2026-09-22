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
    val code: String,          // e.g. "th", "en", "zh_CN"
    val displayName: String,   // in English
    val nativeName: String,    // in native language script
    val flagEmoji: String
)

object LocalizationManager {
    private const val PREFS_NAME = "app_locale_prefs"
    private const val KEY_LANG_CODE = "key_lang_code"

    private var prefs: SharedPreferences? = null

    // Comprehensive list of worldwide supported languages
    val ALL_LANGUAGES = listOf(
        SupportedLanguage("th", "Thai", "ภาษาไทย", "🇹🇭"),
        SupportedLanguage("en", "English", "English", "🇺🇸"),
        SupportedLanguage("zh_CN", "Chinese (Simplified)", "简体中文", "🇨🇳"),
        SupportedLanguage("zh_TW", "Chinese (Traditional)", "繁體中文", "🇹🇼"),
        SupportedLanguage("ja", "Japanese", "日本語", "🇯🇵"),
        SupportedLanguage("ko", "Korean", "한국어", "🇰🇷"),
        SupportedLanguage("es", "Spanish", "Español", "🇪🇸"),
        SupportedLanguage("fr", "French", "Français", "🇫🇷"),
        SupportedLanguage("de", "German", "Deutsch", "🇩🇪"),
        SupportedLanguage("ru", "Russian", "Русский", "🇷🇺"),
        SupportedLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
        SupportedLanguage("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        SupportedLanguage("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
        SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦"),
        SupportedLanguage("pt", "Portuguese", "Português", "🇧🇷"),
        SupportedLanguage("it", "Italian", "Italiano", "🇮🇹"),
        SupportedLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
        SupportedLanguage("nl", "Dutch", "Nederlands", "🇳🇱"),
        SupportedLanguage("tl", "Filipino", "Tagalog", "🇵🇭"),
        SupportedLanguage("pl", "Polish", "Polski", "🇵🇱"),
        SupportedLanguage("uk", "Ukrainian", "Українська", "🇺🇦"),
        SupportedLanguage("sv", "Swedish", "Svenska", "🇸🇪"),
        SupportedLanguage("my", "Burmese", "မြန်မာဘာသာ", "🇲🇲")
    )

    private val _currentLanguage = MutableStateFlow(ALL_LANGUAGES.first())
    val currentLanguage: StateFlow<SupportedLanguage> = _currentLanguage.asStateFlow()

    private val COMPLETE_UI_LANGUAGES = setOf("th", "en")
    val AVAILABLE_UI_LANGUAGES: List<SupportedLanguage> =
        ALL_LANGUAGES.filter { it.code in COMPLETE_UI_LANGUAGES }

    fun effectiveLanguageCode(code: String = _currentLanguage.value.code): String =
        if (code in COMPLETE_UI_LANGUAGES) code else "en"

    val isCurrentLanguageRtl: Boolean
        get() = _currentLanguage.value.code in listOf("ar", "fa", "he", "ur")

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs?.getString(KEY_LANG_CODE, null)
            if (savedCode != null) {
                val saved = ALL_LANGUAGES.find { it.code.equals(savedCode, ignoreCase = true) }
                _currentLanguage.value =
                    if (saved != null && saved.code in COMPLETE_UI_LANGUAGES) saved
                    else AVAILABLE_UI_LANGUAGES.first { it.code == "en" }
            } else {
                // Auto detect based on system locale
                val systemLocale = java.util.Locale.getDefault()
                val sysLang = systemLocale.language.lowercase()
                val sysCountry = systemLocale.country.uppercase()
                val candidateCode = when {
                    sysLang == "zh" && (sysCountry == "TW" || sysCountry == "HK") -> "zh_TW"
                    sysLang == "zh" -> "zh_CN"
                    else -> sysLang
                }
                val match = AVAILABLE_UI_LANGUAGES.find { it.code.equals(candidateCode, ignoreCase = true) }
                    ?: AVAILABLE_UI_LANGUAGES.find { it.code.startsWith(sysLang, ignoreCase = true) }
                    ?: AVAILABLE_UI_LANGUAGES.first { it.code == "en" }
                _currentLanguage.value = match
            }
        }
    }

    fun setLanguage(language: SupportedLanguage) {
        val effective = if (language.code in COMPLETE_UI_LANGUAGES) {
            language
        } else {
            AVAILABLE_UI_LANGUAGES.first { it.code == "en" }
        }
        _currentLanguage.value = effective
        prefs?.edit()?.putString(KEY_LANG_CODE, effective.code)?.apply()
    }

    fun setLanguageByCode(code: String) {
        val found = ALL_LANGUAGES.find { it.code.equals(code, ignoreCase = true) } ?: return
        setLanguage(found)
    }

    // Multilingual Translations Table
    private val translations: Map<String, Map<String, String>> = mapOf(
        // Navigation & Titles
        "nav_generate" to mapOf(
            "th" to "สร้าง QR", "en" to "Generate QR", "zh_CN" to "生成二维码", "zh_TW" to "產生QR碼",
            "ja" to "QR作成", "ko" to "QR 생성", "es" to "Crear QR", "fr" to "Créer QR",
            "de" to "QR erstellen", "ru" to "Создать QR", "vi" to "Tạo QR", "id" to "Buat QR",
            "ms" to "Cipta QR", "hi" to "क्यूआर बनाएं", "ar" to "إنشاء QR", "pt" to "Criar QR",
            "it" to "Crea QR", "tr" to "QR Oluştur", "nl" to "QR Maken", "tl" to "Gumawa ng QR"
        ),
        "nav_card" to mapOf(
            "th" to "นามบัตร", "en" to "Business Card", "zh_CN" to "电子名片", "zh_TW" to "電子名片",
            "ja" to "デジタル名刺", "ko" to "디지털 명함", "es" to "Tarjeta Digital", "fr" to "Carte de Visite",
            "de" to "Visitenkarte", "ru" to "Визитка", "vi" to "Danh thiếp", "id" to "Kartu Nama",
            "ms" to "Kad Perniagaan", "hi" to "विजिटिंग कार्ड", "ar" to "بطاقة عمل", "pt" to "Cartão Digital",
            "it" to "Biglietto da Visita", "tr" to "Kartvizit", "nl" to "Visitekaartje", "tl" to "Business Card"
        ),
        "nav_scanner" to mapOf(
            "th" to "สแกนเนอร์", "en" to "Scanner", "zh_CN" to "扫码器", "zh_TW" to "掃描器",
            "ja" to "スキャナー", "ko" to "스캐너", "es" to "Escáner", "fr" to "Scanner",
            "de" to "Scanner", "ru" to "Сканер", "vi" to "Quét mã", "id" to "Pemindai",
            "ms" to "Pengimbas", "hi" to "स्कैनर", "ar" to "الماسح", "pt" to "Escanear",
            "it" to "Scanner", "tr" to "Tarayıcı", "nl" to "Scanner", "tl" to "Scanner"
        ),
        "nav_history" to mapOf(
            "th" to "ประวัติ", "en" to "History", "zh_CN" to "历史记录", "zh_TW" to "歷史記錄",
            "ja" to "履歴", "ko" to "기록", "es" to "Historial", "fr" to "Historique",
            "de" to "Verlauf", "ru" to "История", "vi" to "Lịch sử", "id" to "Riwayat",
            "ms" to "Sejarah", "hi" to "इतिहास", "ar" to "السجل", "pt" to "Histórico",
            "it" to "Cronologia", "tr" to "Geçmiş", "nl" to "Geschiedenis", "tl" to "Kasaysayan"
        ),
        "app_title" to mapOf(
            "th" to "QuickQR Business",
            "en" to "QuickQR Business"
        ),
        "app_subtitle" to mapOf(
            "th" to "พร้อมเพย์ & นามบัตรดิจิทัล", "en" to "PromptPay & Digital Cards", "zh_CN" to "PromptPay与数字名片",
            "zh_TW" to "PromptPay與數位名片", "ja" to "プロンプトペイ＆名刺", "ko" to "프롬프트페이 및 디지털 명함",
            "es" to "PromptPay y Tarjetas Digitales", "fr" to "PromptPay et Cartes Numériques", "de" to "PromptPay & Visitenkarten"
        ),
        "contact_admin" to mapOf(
            "th" to "ช่วยเหลือ",
            "en" to "Support"
        ),

        // Language & Currency Selector Dialog
        "tab_language" to mapOf(
            "th" to "เลือกภาษา (Languages)", "en" to "Languages", "zh_CN" to "选择语言", "zh_TW" to "選擇語言",
            "ja" to "言語選択", "ko" to "언어 선택", "es" to "Idioma", "fr" to "Langue", "de" to "Sprache",
            "ru" to "Язык", "vi" to "Ngôn ngữ", "id" to "Bahasa", "ms" to "Bahasa", "hi" to "भाषा"
        ),
        "tab_currency" to mapOf(
            "th" to "ภูมิภาค", "en" to "Region"
        ),
        "search_language" to mapOf(
            "th" to "ค้นหาภาษา...", "en" to "Search language...", "zh_CN" to "搜索语言...", "zh_TW" to "搜尋語言...",
            "ja" to "言語を検索...", "ko" to "언어 검색...", "es" to "Buscar idioma...", "fr" to "Rechercher langue..."
        ),
        "search_currency" to mapOf(
            "th" to "ค้นหาประเทศ ภูมิภาค หรือสกุลเงิน...", "en" to "Search region or currency..."
        ),
        "google_certified_badge" to mapOf(
            "th" to "สกุลเงินถูกกำหนดตามภูมิภาค", "en" to "Currency follows the selected region"
        ),

        // Ad and Free Tier
        "free_uses_left" to mapOf(
            "th" to "ทดลองใช้ฟรี: เหลือ %d/3 ครั้ง", "en" to "Free Trial: %d/3 left", "zh_CN" to "免费试用: 剩余 %d/3 次",
            "zh_TW" to "免費試用: 剩餘 %d/3 次", "ja" to "無料お試し: 残り %d/3 回", "ko" to "무료 체험: %d/3회 남음",
            "es" to "Prueba gratis: %d/3 restantes", "fr" to "Essai gratuit : %d/3 restants", "de" to "Kostenlose Testversion: %d/3 übrig"
        ),
        "unlimited_vip" to mapOf(
            "th" to "VIP รายเดือน (ไม่จำกัด)", "en" to "VIP Monthly (Unlimited)", "zh_CN" to "月度VIP (无限制)",
            "zh_TW" to "月度VIP (無限次)", "ja" to "VIP 月額 (無制限)", "ko" to "VIP 월간 (무제한)",
            "es" to "VIP Mensual (Ilimitado)", "fr" to "VIP Mensuel (Illimité)", "de" to "VIP Monatlich (Unbegrenzt)"
        ),
        "watch_ad_30s_title" to mapOf(
            "th" to "โฆษณาสนับสนุนผู้ใช้งานฟรี (30 วินาที)", "en" to "Free Tier Ad (30 Seconds)",
            "zh_CN" to "免费用户赞助广告 (30秒)", "zh_TW" to "免費用戶贊助廣告 (30秒)",
            "ja" to "無料ユーザー向け広告 (30秒)", "ko" to "무료 사용자 광고 (30초)"
        ),
        "watch_ad_wait" to mapOf(
            "th" to "รอชมโฆษณา %d วินาที เพื่อกดข้ามได้", "en" to "Watch for %d seconds to skip",
            "zh_CN" to "观看 %d 秒后可跳过", "zh_TW" to "觀看 %d 秒後可跳過",
            "ja" to "あと %d 秒でスキップできます", "ko" to "%d초 후 건너뛰기 가능"
        ),
        "skip_ad_button" to mapOf(
            "th" to "กดข้ามได้", "en" to "Skip Ad", "zh_CN" to "跳过广告", "zh_TW" to "跳過廣告",
            "ja" to "スキップする", "ko" to "광고 건너뛰기", "es" to "Saltar anuncio", "fr" to "Passer la pub"
        ),
        "watch_ad_free_reward" to mapOf(
            "th" to "🎬 ดูโฆษณา 30 วิ เพื่อรับสิทธิ์ฟรี (+1 ครั้ง)", "en" to "🎬 Watch 30s ad for +1 free use",
            "zh_CN" to "🎬 观看30秒广告获得免费次数(+1次)", "zh_TW" to "🎬 觀看30秒廣告獲得免費次數(+1次)",
            "ja" to "🎬 30秒広告を見て無料利用回数を獲得(+1回)", "ko" to "🎬 30초 광고 시청하고 무료 이용권 받기(+1회)"
        ),
        "free_tier_policy_note" to mapOf(
            "th" to "ใช้ฟรี: ดู 30 วิ แล้วกดข้ามได้ | สมาชิก VIP ไม่ต้องดูโฆษณา",
            "en" to "Free: Watch 30s then skip | VIP members enjoy ad-free instant use",
            "zh_CN" to "免费用户: 观看30秒后跳过 | VIP免广告即刻生成",
            "zh_TW" to "免費用戶: 觀看30秒後跳過 | VIP免廣告即刻產生",
            "ja" to "無料利用: 30秒視聴でスキップ可能 | VIPは広告なし即時作成",
            "ko" to "무료 이용: 30초 시청 후 건너뛰기 | VIP는 광고 없이 즉시 생성"
        ),

        // Generator Categories
        "cat_promptpay" to mapOf(
            "th" to "พร้อมเพย์", "en" to "PromptPay", "zh_CN" to "PromptPay", "zh_TW" to "PromptPay",
            "ja" to "PromptPay", "ko" to "프롬프트페이"
        ),
        "cat_wifi" to mapOf(
            "th" to "Wi-Fi", "en" to "Wi-Fi", "zh_CN" to "Wi-Fi无线网络", "zh_TW" to "Wi-Fi無線網絡",
            "ja" to "Wi-Fi接続", "ko" to "Wi-Fi 연결"
        ),
        "cat_store" to mapOf(
            "th" to "ลิงก์ร้านค้า", "en" to "Store Link", "zh_CN" to "店铺链接", "zh_TW" to "店鋪連結",
            "ja" to "ショップリンク", "ko" to "스토어 링크"
        ),
        "cat_text" to mapOf(
            "th" to "ข้อความ", "en" to "Text / URL", "zh_CN" to "文本 / 网址", "zh_TW" to "文字 / 網址",
            "ja" to "テキスト / URL", "ko" to "텍스트 / URL"
        ),

        // PromptPay Fields
        "field_promptpay_id" to mapOf(
            "th" to "เบอร์โทรศัพท์ หรือ เลขบัตรประชาชน", "en" to "Phone Number or Citizen/Tax ID",
            "zh_CN" to "电话号码或身份证/税号", "zh_TW" to "電話號碼或身分證/稅號",
            "ja" to "電話番号 または ID番号", "ko" to "전화번호 또는 주민/사업자등록번호"
        ),
        "field_amount" to mapOf(
            "th" to "จำนวนเงิน (ไม่บังคับ)", "en" to "Amount (Optional)", "zh_CN" to "金额 (选填)",
            "zh_TW" to "金額 (選填)", "ja" to "金額 (任意)", "ko" to "금액 (선택 사항)"
        ),
        "field_shop_name" to mapOf(
            "th" to "ชื่อร้านค้า หรือ บัญชีผู้รับ (บนป้ายตั้งโต๊ะ)", "en" to "Shop or Payee Name (for Standee)",
            "zh_CN" to "店铺名称或收款人 (展示在立牌上)", "zh_TW" to "店鋪名稱或收款人 (展示在立牌上)",
            "ja" to "店舗名 または 受取人名 (卓上スタンド用)", "ko" to "상호명 또는 수취인명 (스탠드 표시용)"
        ),
        "btn_generate_promptpay" to mapOf(
            "th" to "สร้าง QR พร้อมเพย์ & ป้ายตั้งโต๊ะ", "en" to "Generate PromptPay QR & Standee",
            "zh_CN" to "生成PromptPay二维码与立牌", "zh_TW" to "產生PromptPay QR碼與立牌",
            "ja" to "PromptPay QR & 卓上スタンド作成", "ko" to "PromptPay QR 및 스탠드 생성"
        ),

        // Color Customizer
        "color_customizer_title" to mapOf(
            "th" to "ปรับแต่งสีและดีไซน์ QR Code", "en" to "QR Code Color & Design Customizer",
            "zh_CN" to "定制二维码颜色与外观", "zh_TW" to "自訂QR碼顏色與外觀",
            "ja" to "QRコードの色とデザインをカスタマイズ", "ko" to "QR 코드 색상 및 디자인 맞춤 설정"
        ),
        "color_pattern" to mapOf(
            "th" to "สีลวดลาย QR (Pattern Color)", "en" to "QR Pattern Color", "zh_CN" to "二维码图案颜色",
            "zh_TW" to "QR碼圖案顏色", "ja" to "QRパターンカラー", "ko" to "QR 패턴 색상"
        ),
        "color_bg" to mapOf(
            "th" to "จุดเปลี่ยนสีพื้นหลัง (Background Dots)", "en" to "Background Color Dots",
            "zh_CN" to "背景颜色选择点", "zh_TW" to "背景顏色選擇點", "ja" to "背景色選択ドット", "ko" to "배경색 선택 도트"
        ),
        "color_live_preview" to mapOf(
            "th" to "แสดงตัวอย่างสีแบบเรียลไทม์ (Live Preview)", "en" to "Live Color Preview",
            "zh_CN" to "实时颜色预览", "zh_TW" to "即時顏色預覽", "ja" to "リアルタイムカラープレビュー", "ko" to "실시간 색상 미리보기"
        ),

        // Top Up and VIP
        "topup_dialog_title" to mapOf(
            "th" to "เติมเงิน & อัปเกรด VIP", "en" to "Top Up & Upgrade VIP", "zh_CN" to "充值与升级VIP",
            "zh_TW" to "儲值與升級VIP", "ja" to "チャージ＆VIPアップグレード", "ko" to "충전 및 VIP 업그레이드"
        ),
        "btn_select_package" to mapOf(
            "th" to "เลือกแพ็กเกจนี้", "en" to "Select This Plan", "zh_CN" to "选择此套餐", "zh_TW" to "選擇此方案",
            "ja" to "このプランを選択", "ko" to "이 요금제 선택"
        ),
        "vip_unlimited_badge" to mapOf(
            "th" to "ใช้งานไม่จำกัด", "en" to "Unlimited Access", "zh_CN" to "无限次使用", "zh_TW" to "無限次使用",
            "ja" to "無制限アクセス", "ko" to "무제한 이용"
        ),
        "best_value_badge" to mapOf(
            "th" to "คุ้มค่าที่สุด", "en" to "Best Value", "zh_CN" to "超值推荐", "zh_TW" to "超值推薦",
            "ja" to "一番お得", "ko" to "최고 가치"
        ),
        "save_percent" to mapOf(
            "th" to "ประหยัด 30%", "en" to "Save 30%", "zh_CN" to "立省 30%", "zh_TW" to "現省 30%",
            "ja" to "30%オフ", "ko" to "30% 할인"
        ),
        "pay_confirm_title" to mapOf(
            "th" to "สแกน QR เพื่อชำระเงิน", "en" to "Scan QR to Pay", "zh_CN" to "扫码付款", "zh_TW" to "掃碼付款",
            "ja" to "QRをスキャンして支払う", "ko" to "QR 스캔하여 결제"
        ),
        "btn_confirm_paid" to mapOf(
            "th" to "✅ ฉันโอนเงินเรียบร้อยแล้ว (เปิดใช้งาน)", "en" to "✅ I have transferred (Activate now)",
            "zh_CN" to "✅ 我已转账 (立即激活)", "zh_TW" to "✅ 我已轉帳 (立即啟用)",
            "ja" to "✅ 送金完了 (今すぐ有効化)", "ko" to "✅ 이체 완료 (즉시 활성화)"
        ),
        "btn_save_pay_qr" to mapOf(
            "th" to "💾 บันทึก QR ลงเครื่องเพื่อสแกนจ่าย", "en" to "💾 Save QR image to gallery",
            "zh_CN" to "💾 保存二维码到相册", "zh_TW" to "💾 儲存QR碼至相簿",
            "ja" to "💾 QR画像を保存", "ko" to "💾 QR 이미지 저장"
        ),

        // General
        "close" to mapOf(
            "th" to "ปิด", "en" to "Close", "zh_CN" to "关闭", "zh_TW" to "關閉",
            "ja" to "閉じる", "ko" to "닫기", "es" to "Cerrar", "fr" to "Fermer", "de" to "Schließen"
        ),
        "confirm" to mapOf(
            "th" to "ยืนยัน", "en" to "Confirm", "zh_CN" to "确认", "zh_TW" to "確認",
            "ja" to "確認", "ko" to "확인", "es" to "Confirmar", "fr" to "Confirmer"
        )
    )

    fun getString(key: String, vararg args: Any): String {
        val currentCode = effectiveLanguageCode()
        val entry = translations[key]
        val raw = entry?.get(currentCode)
            ?: entry?.get("en")
            ?: entry?.get("th")
            ?: key

        return if (args.isNotEmpty()) {
            try {
                String.format(raw, *args)
            } catch (e: Exception) {
                raw
            }
        } else {
            raw
        }
    }
}

/**
 * Composable helper that observes the current language reactively
 */
@Composable
fun localizedString(key: String, vararg args: Any): String {
    val lang by LocalizationManager.currentLanguage.collectAsState()
    return LocalizationManager.getString(key, *args)
}

@Composable
fun localizedText(th: String, en: String): String {
    val lang by LocalizationManager.currentLanguage.collectAsState()
    return if (LocalizationManager.effectiveLanguageCode(lang.code) == "th") th else en
}

fun localizedNow(th: String, en: String): String =
    if (LocalizationManager.effectiveLanguageCode() == "th") th else en
