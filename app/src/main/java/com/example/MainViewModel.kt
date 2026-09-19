package com.example

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.admob.AdMobManager
import com.example.data.WalletManager
import com.example.data.AppDatabase
import com.example.data.MerchantProfileEntity
import com.example.data.QrItemEntity
import com.example.model.CardColorTheme
import com.example.model.DigitalBusinessCard
import com.example.model.ParsedQrResult
import com.example.model.PromptPayModel
import com.example.model.StoreLinkModel
import com.example.model.StorePlatform
import com.example.model.WifiModel
import com.example.model.WifiSecurity
import com.example.util.ImageExporter
import com.example.util.PromptPayGenerator
import com.example.util.QrCodeUtil
import com.example.util.QrScannerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ActiveQrPreview(
    val title: String,
    val subtitle: String,
    val rawContent: String,
    val qrBitmap: Bitmap,
    val standeeBitmap: Bitmap? = null,
    val targetId: String? = null,
    val amount: Double? = null,
    val type: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.qrDao()

    // Active bottom navigation tab: 0=Generate, 1=Card Studio, 2=Scanner, 3=History
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Sub-tab under Generator: 0=PromptPay, 1=Wi-Fi, 2=Store Links, 3=Text
    private val _generatorCategory = MutableStateFlow(0)
    val generatorCategory: StateFlow<Int> = _generatorCategory.asStateFlow()

    // PromptPay State
    private val _promptPayTarget = MutableStateFlow("0812345678")
    val promptPayTarget: StateFlow<String> = _promptPayTarget.asStateFlow()

    private val _promptPayAmount = MutableStateFlow("")
    val promptPayAmount: StateFlow<String> = _promptPayAmount.asStateFlow()

    private val _promptPayShopName = MutableStateFlow("ร้านค้า / ช่าง / ฟรีแลนซ์")
    val promptPayShopName: StateFlow<String> = _promptPayShopName.asStateFlow()

    // Wi-Fi State
    private val _wifiSsid = MutableStateFlow("Shop-Free-WiFi")
    val wifiSsid: StateFlow<String> = _wifiSsid.asStateFlow()

    private val _wifiPassword = MutableStateFlow("12345678")
    val wifiPassword: StateFlow<String> = _wifiPassword.asStateFlow()

    private val _wifiSecurity = MutableStateFlow(WifiSecurity.WPA)
    val wifiSecurity: StateFlow<WifiSecurity> = _wifiSecurity.asStateFlow()

    private val _wifiHidden = MutableStateFlow(false)
    val wifiHidden: StateFlow<Boolean> = _wifiHidden.asStateFlow()

    // Store Link State
    private val _storePlatform = MutableStateFlow(StorePlatform.LINE_OA)
    val storePlatform: StateFlow<StorePlatform> = _storePlatform.asStateFlow()

    private val _storeValue = MutableStateFlow("@myshop")
    val storeValue: StateFlow<String> = _storeValue.asStateFlow()

    // Text State
    private val _rawText = MutableStateFlow("")
    val rawText: StateFlow<String> = _rawText.asStateFlow()

    // Digital Business Card State
    private val _businessCard = MutableStateFlow(
        DigitalBusinessCard(
            fullName = "สมชาย รับเหมาบริการ",
            businessName = "สมชาย การช่าง & ซ่อมบำรุง",
            profession = "ช่างรับเหมา / ไฟฟ้า-ประปา",
            phoneNumber = "0812345678",
            promptPayId = "0812345678",
            lineId = "@somchai_service",
            facebook = "สมชายการช่างรับเหมา",
            email = "somchai.service@gmail.com",
            services = "รับเหมาต่อเติมบ้าน ระบบไฟฟ้า ประปา แอร์ ซ่อมแซมด่วน 24 ชม.",
            cardTheme = CardColorTheme.NAVY_BLUE
        )
    )
    val businessCard: StateFlow<DigitalBusinessCard> = _businessCard.asStateFlow()

    // Active Preview Modal
    private val _activePreview = MutableStateFlow<ActiveQrPreview?>(null)
    val activePreview: StateFlow<ActiveQrPreview?> = _activePreview.asStateFlow()

    // Scan Result Modal
    private val _activeScanResult = MutableStateFlow<ParsedQrResult?>(null)
    val activeScanResult: StateFlow<ParsedQrResult?> = _activeScanResult.asStateFlow()

    // TopUp and Subscription Dialog
    private val _showTopUpDialog = MutableStateFlow(false)
    val showTopUpDialog: StateFlow<Boolean> = _showTopUpDialog.asStateFlow()

    // Ad Stats Dialog
    private val _showAdStats = MutableStateFlow(false)
    val showAdStats: StateFlow<Boolean> = _showAdStats.asStateFlow()

    // Support and Bug Report Dialog
    private val _showSupportSheet = MutableStateFlow(false)
    val showSupportSheet: StateFlow<Boolean> = _showSupportSheet.asStateFlow()

    // Language and Currency Dialog
    private val _showLanguageAndCurrencyDialog = MutableStateFlow(false)
    val showLanguageAndCurrencyDialog: StateFlow<Boolean> = _showLanguageAndCurrencyDialog.asStateFlow()

    private val _languageCurrencyInitialTab = MutableStateFlow(0)
    val languageCurrencyInitialTab: StateFlow<Int> = _languageCurrencyInitialTab.asStateFlow()

    // QR Code Custom Color State
    private val _qrForegroundColor = MutableStateFlow(Color.Black)
    val qrForegroundColor: StateFlow<Color> = _qrForegroundColor.asStateFlow()

    private val _qrBackgroundColor = MutableStateFlow(Color.White)
    val qrBackgroundColor: StateFlow<Color> = _qrBackgroundColor.asStateFlow()

    // GEN_QR Spec Center Logo State (logo=20%=102px pad=12)
    private val _includeCenterLogo = MutableStateFlow(true)
    val includeCenterLogo: StateFlow<Boolean> = _includeCenterLogo.asStateFlow()

    fun setIncludeCenterLogo(enabled: Boolean) {
        _includeCenterLogo.value = enabled
    }

    // History flows from Room - Lazily loaded on demand when user opens History tab
    val historyItems: StateFlow<List<QrItemEntity>> = dao.getAllQrItems()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    init {
        // Initialize Wallet and Quota Manager
        WalletManager.initialize(application)

        // Asynchronously load stored profile on IO thread to avoid main thread startup latency
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.getMerchantProfile().catch { }.collect { saved ->
                    if (saved != null) {
                        val theme = try {
                            CardColorTheme.valueOf(saved.cardTheme)
                        } catch (_: Exception) {
                            CardColorTheme.NAVY_BLUE
                        }
                        _businessCard.value = DigitalBusinessCard(
                            fullName = saved.fullName,
                            businessName = saved.businessName,
                            profession = saved.profession,
                            phoneNumber = saved.phoneNumber,
                            promptPayId = saved.promptPayId,
                            lineId = saved.lineId,
                            facebook = saved.facebook,
                            email = saved.email,
                            services = saved.services,
                            cardTheme = theme
                        )
                        if (saved.promptPayId.isNotBlank()) {
                            _promptPayTarget.value = saved.promptPayId
                        }
                        if (saved.businessName.isNotBlank()) {
                            _promptPayShopName.value = saved.businessName
                        }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setGeneratorCategory(index: Int) {
        _generatorCategory.value = index
    }

    fun setPromptPayTarget(target: String) {
        _promptPayTarget.value = target
    }

    fun setPromptPayAmount(amount: String) {
        _promptPayAmount.value = amount
    }

    fun setPromptPayShopName(name: String) {
        _promptPayShopName.value = name
    }

    fun setWifiSsid(ssid: String) {
        _wifiSsid.value = ssid
    }

    fun setWifiPassword(pass: String) {
        _wifiPassword.value = pass
    }

    fun setWifiSecurity(security: WifiSecurity) {
        _wifiSecurity.value = security
    }

    fun setWifiHidden(hidden: Boolean) {
        _wifiHidden.value = hidden
    }

    fun setStorePlatform(platform: StorePlatform) {
        _storePlatform.value = platform
    }

    fun setStoreValue(value: String) {
        _storeValue.value = value
    }

    fun setRawText(text: String) {
        _rawText.value = text
    }

    fun updateBusinessCard(card: DigitalBusinessCard) {
        _businessCard.value = card
    }

    fun saveBusinessCardProfile() {
        val card = _businessCard.value
        viewModelScope.launch(Dispatchers.IO) {
            dao.saveMerchantProfile(
                MerchantProfileEntity(
                    id = 1,
                    fullName = card.fullName,
                    businessName = card.businessName,
                    profession = card.profession,
                    phoneNumber = card.phoneNumber,
                    promptPayId = card.promptPayId,
                    lineId = card.lineId,
                    facebook = card.facebook,
                    email = card.email,
                    services = card.services,
                    cardTheme = card.cardTheme.name
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "บันทึกโปรไฟล์นามบัตรเรียบร้อย", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun closePreview() {
        _activePreview.value = null
    }

    fun closeScanResult() {
        _activeScanResult.value = null
    }

    fun openTopUpDialog() {
        _showTopUpDialog.value = true
    }

    fun closeTopUpDialog() {
        _showTopUpDialog.value = false
    }

    fun openAdStats() {
        _showAdStats.value = true
    }

    fun closeAdStats() {
        _showAdStats.value = false
    }

    fun openSupportSheet() {
        _showSupportSheet.value = true
    }

    fun closeSupportSheet() {
        _showSupportSheet.value = false
    }

    fun openLanguageAndCurrencyDialog(initialTab: Int = 0) {
        _languageCurrencyInitialTab.value = initialTab
        _showLanguageAndCurrencyDialog.value = true
    }

    fun closeLanguageAndCurrencyDialog() {
        _showLanguageAndCurrencyDialog.value = false
    }

    fun setQrForegroundColor(color: Color) {
        _qrForegroundColor.value = color
    }

    fun setQrBackgroundColor(color: Color) {
        _qrBackgroundColor.value = color
    }

    /**
     * Policy for Free vs VIP users:
     * - VIP users: Instant generation without any ads.
     * - Free users: "ใช้ฟรีต้องดูโฆษณา 30 วิ แล้วกดข้ามได้" -> shows 30s ad, user watches 30s and can skip to generate.
     * - If quota is exhausted: Watching 30s ad grants the free usage!
     */
    private fun executeWithFreeOrVipPolicy(
        actionName: String,
        onExecute: () -> Unit
    ) {
        val state = WalletManager.walletState.value
        if (state.isUnlimitedVip) {
            onExecute()
            return
        }

        // Free tier user: must watch 30-second ad and can then skip
        com.example.admob.AdMobManager.show30sFreeAd(triggerReason = actionName) {
            if (!WalletManager.canUse()) {
                // Reward 1 free use for completing the 30-second ad
                WalletManager.addFreeUse(1)
            }
            onExecute()
        }
    }

    /**
     * Generate PromptPay QR
     */
    fun generatePromptPay() {
        val target = _promptPayTarget.value.trim()
        if (target.isBlank()) {
            Toast.makeText(getApplication(), "กรุณาระบุเบอร์โทรหรือเลขบัตรประชาชน", Toast.LENGTH_SHORT).show()
            return
        }

        executeWithFreeOrVipPolicy("สร้าง QR พร้อมเพย์") {
            val amount = _promptPayAmount.value.toDoubleOrNull()
            val payload = PromptPayGenerator.generatePayload(target, amount)
            val centerLogo = if (_includeCenterLogo.value) QrCodeUtil.createDefaultCenterLogo("PROMPTPAY") else null
            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = payload,
                size = QrCodeUtil.SPEC_SIZE,
                darkColor = _qrForegroundColor.value.toArgb(),
                lightColor = _qrBackgroundColor.value.toArgb(),
                centerLogo = centerLogo
            ) ?: return@executeWithFreeOrVipPolicy
            val standeeBitmap = QrCodeUtil.createPromptPayStandeeBitmap(
                qrBitmap = qrBitmap,
                title = "THAI QR PAYMENT",
                targetId = target,
                amount = amount,
                merchantName = _promptPayShopName.value,
                backgroundColor = _qrBackgroundColor.value.toArgb()
            )

            // Consume quota
            WalletManager.consumeUsage()

            val amountStr = if (amount != null && amount > 0) "฿${String.format("%,.2f", amount)}" else "ไม่ระบุยอดเงิน"
            val subtitle = "เบอร์/เลขบัตร: $target ($amountStr)"

            _activePreview.value = ActiveQrPreview(
                title = "พร้อมเพย์ (PromptPay)",
                subtitle = subtitle,
                rawContent = payload,
                qrBitmap = qrBitmap,
                standeeBitmap = standeeBitmap,
                targetId = target,
                amount = amount,
                type = "PROMPTPAY"
            )

            // Save to History in Room
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "PROMPTPAY",
                        title = "พร้อมเพย์ $amountStr",
                        subtitle = subtitle,
                        rawContent = payload,
                        targetId = target,
                        amount = amount,
                        isScan = false
                    )
                )
            }
        }
    }

    /**
     * Generate Wi-Fi QR
     */
    fun generateWifi() {
        val ssid = _wifiSsid.value.trim()
        if (ssid.isBlank()) {
            Toast.makeText(getApplication(), "กรุณาระบุชื่อ Wi-Fi (SSID)", Toast.LENGTH_SHORT).show()
            return
        }

        executeWithFreeOrVipPolicy("สร้าง QR Wi-Fi") {
            val payload = QrCodeUtil.buildWifiPayload(
                ssid = ssid,
                pass = _wifiPassword.value,
                security = _wifiSecurity.value.code,
                isHidden = _wifiHidden.value
            )
            val centerLogo = if (_includeCenterLogo.value) QrCodeUtil.createDefaultCenterLogo("WIFI") else null
            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = payload,
                size = QrCodeUtil.SPEC_SIZE,
                darkColor = _qrForegroundColor.value.toArgb(),
                lightColor = _qrBackgroundColor.value.toArgb(),
                centerLogo = centerLogo
            ) ?: return@executeWithFreeOrVipPolicy

            WalletManager.consumeUsage()

            _activePreview.value = ActiveQrPreview(
                title = "Wi-Fi: $ssid",
                subtitle = "รหัสผ่าน: ${_wifiPassword.value.ifBlank { "(ไม่มี)" }}",
                rawContent = payload,
                qrBitmap = qrBitmap,
                type = "WIFI"
            )

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "WIFI",
                        title = "Wi-Fi: $ssid",
                        subtitle = "รหัสผ่าน: ${_wifiPassword.value}",
                        rawContent = payload,
                        isScan = false
                    )
                )
            }
        }
    }

    /**
     * Generate Store Link QR
     */
    fun generateStoreLink() {
        val model = StoreLinkModel(_storePlatform.value, _storeValue.value)
        val fullUrl = model.fullUrl
        if (_storeValue.value.isBlank()) {
            Toast.makeText(getApplication(), "กรุณากรอกลิงก์หรือไอดีร้านค้า", Toast.LENGTH_SHORT).show()
            return
        }

        executeWithFreeOrVipPolicy("สร้าง QR ลิงก์ร้านค้า") {
            val centerLogo = if (_includeCenterLogo.value) QrCodeUtil.createDefaultCenterLogo("STORE") else null
            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = fullUrl,
                size = QrCodeUtil.SPEC_SIZE,
                darkColor = _qrForegroundColor.value.toArgb(),
                lightColor = _qrBackgroundColor.value.toArgb(),
                centerLogo = centerLogo
            ) ?: return@executeWithFreeOrVipPolicy

            WalletManager.consumeUsage()

            _activePreview.value = ActiveQrPreview(
                title = "ลิงก์ร้าน ${_storePlatform.value.title}",
                subtitle = fullUrl,
                rawContent = fullUrl,
                qrBitmap = qrBitmap,
                type = "STORE_LINK"
            )

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "STORE_LINK",
                        title = "ลิงก์ร้าน ${_storePlatform.value.title}",
                        subtitle = fullUrl,
                        rawContent = fullUrl,
                        isScan = false
                    )
                )
            }
        }
    }

    /**
     * Generate Text QR
     */
    fun generateText() {
        val text = _rawText.value.trim()
        if (text.isBlank()) {
            Toast.makeText(getApplication(), "กรุณากรอกข้อความ", Toast.LENGTH_SHORT).show()
            return
        }

        executeWithFreeOrVipPolicy("สร้าง QR ข้อความ") {
            val centerLogo = if (_includeCenterLogo.value) QrCodeUtil.createDefaultCenterLogo("TEXT") else null
            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = text,
                size = QrCodeUtil.SPEC_SIZE,
                darkColor = _qrForegroundColor.value.toArgb(),
                lightColor = _qrBackgroundColor.value.toArgb(),
                centerLogo = centerLogo
            ) ?: return@executeWithFreeOrVipPolicy

            WalletManager.consumeUsage()

            _activePreview.value = ActiveQrPreview(
                title = "ข้อความ QR",
                subtitle = if (text.length > 40) text.take(40) + "..." else text,
                rawContent = text,
                qrBitmap = qrBitmap,
                type = "TEXT"
            )

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "TEXT",
                        title = "ข้อความ",
                        subtitle = text.take(40),
                        rawContent = text,
                        isScan = false
                    )
                )
            }
        }
    }

    /**
     * Generate Digital Business Card vCard & PromptPay QR
     */
    fun generateBusinessCardPreview() {
        val card = _businessCard.value

        executeWithFreeOrVipPolicy("สร้าง QR นามบัตรดิจิทัล") {
            val noteContent = buildString {
                if (card.profession.isNotBlank()) append("บริการ: ${card.profession}\n")
                if (card.promptPayId.isNotBlank()) append("พร้อมเพย์: ${card.promptPayId}\n")
                if (card.lineId.isNotBlank()) append("LINE: ${card.lineId}\n")
                if (card.services.isNotBlank()) append("รายละเอียด: ${card.services}")
            }
            val vcard = QrCodeUtil.buildVCardPayload(
                fullName = card.fullName,
                org = card.businessName,
                title = card.profession,
                phone = card.phoneNumber,
                email = card.email,
                url = if (card.facebook.isNotBlank()) "https://facebook.com/${card.facebook}" else "",
                note = noteContent
            )

            val centerLogo = if (_includeCenterLogo.value) QrCodeUtil.createDefaultCenterLogo("VCARD") else null
            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = vcard,
                size = QrCodeUtil.SPEC_SIZE,
                darkColor = card.cardTheme.primaryColorHex.toInt(),
                centerLogo = centerLogo
            ) ?: return@executeWithFreeOrVipPolicy

            WalletManager.consumeUsage()

            _activePreview.value = ActiveQrPreview(
                title = "นามบัตรดิจิทัล: ${card.businessName.ifBlank { card.fullName }}",
                subtitle = "${card.profession} • ${card.phoneNumber}",
                rawContent = vcard,
                qrBitmap = qrBitmap,
                type = "VCARD"
            )

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "VCARD",
                        title = "นามบัตร: ${card.businessName.ifBlank { card.fullName }}",
                        subtitle = "${card.profession} | ${card.phoneNumber}",
                        rawContent = vcard,
                        isScan = false
                    )
                )
            }
        }
    }

    /**
     * Handles Scanned QR string (from Camera or Gallery)
     * Direct result without blocking ads!
     */
    fun onQrScanned(raw: String) {
        val parsed = QrScannerUtil.parseQrContent(raw)

        // Save scan to Room
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertQrItem(
                QrItemEntity(
                    type = parsed.type.name,
                    title = parsed.title,
                    subtitle = parsed.subtitle,
                    rawContent = raw,
                    targetId = parsed.promptPayId,
                    amount = parsed.amount,
                    isScan = true
                )
            )
        }

        // Instant result presentation - no ads blocking!
        _activeScanResult.value = parsed
    }

    /**
     * Process image picked from gallery
     */
    fun scanImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val decodedText = QrScannerUtil.decodeBitmap(bitmap)
                    withContext(Dispatchers.Main) {
                        if (decodedText != null) {
                            onQrScanned(decodedText)
                        } else {
                            Toast.makeText(getApplication(), "ไม่พบคิวอาร์โค้ดในรูปภาพที่เลือก", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "เกิดข้อผิดพลาดในการอ่านรูปภาพ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * User action to Save QR or Standee Bitmap.
     * Direct fast saving without intrusive blocking ads!
     */
    fun requestSaveBitmap(bitmap: Bitmap, title: String) {
        val uri = ImageExporter.saveBitmapToGallery(getApplication(), bitmap, title)
        if (uri != null) {
            Toast.makeText(
                getApplication(),
                "บันทึกรูปภาพลงอัลบั้มสำเร็จ (Pictures/QR_PromptPay)",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(getApplication(), "ไม่สามารถบันทึกรูปภาพได้", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Delete item from History
     */
    fun deleteHistoryItem(item: QrItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteQrItem(item)
        }
    }

    /**
     * Clear all scans or generated items
     */
    fun clearHistory(isScan: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearHistory(isScan)
        }
    }
}
