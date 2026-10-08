package com.aistudio.qrgenerator.kmpzqr

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aistudio.qrgenerator.kmpzqr.data.AppDatabase
import com.aistudio.qrgenerator.kmpzqr.data.MerchantProfileEntity
import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity
import com.aistudio.qrgenerator.kmpzqr.model.CardColorTheme
import com.aistudio.qrgenerator.kmpzqr.model.DigitalBusinessCard
import com.aistudio.qrgenerator.kmpzqr.model.GeoPoint
import com.aistudio.qrgenerator.kmpzqr.model.ParsedQrResult
import com.aistudio.qrgenerator.kmpzqr.model.StoreLinkModel
import com.aistudio.qrgenerator.kmpzqr.model.StorePlatform
import com.aistudio.qrgenerator.kmpzqr.model.WifiSecurity
import com.aistudio.qrgenerator.kmpzqr.util.CurrentLocationProvider
import com.aistudio.qrgenerator.kmpzqr.util.ImageExporter
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import com.aistudio.qrgenerator.kmpzqr.util.HistoryPromptPayResolver
import com.aistudio.qrgenerator.kmpzqr.util.HistoryPrivacyUtil
import com.aistudio.qrgenerator.kmpzqr.util.LocationError
import com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil
import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrGenerationRequestGuard
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil
import com.aistudio.qrgenerator.kmpzqr.util.ValidationResult
import com.aistudio.qrgenerator.kmpzqr.util.localizedNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    private companion object {
        // UI query batch only; this does not cap or delete stored history.
        const val HISTORY_PAGE_SIZE = 100
    }

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.qrDao()
    private val centerLogoFile = File(application.filesDir, "qr_center_logo.png")
    private val qrGenerationRequestGuard = QrGenerationRequestGuard()
    private var qrGenerationJob: Job? = null

    // Active bottom navigation tab: 0=Generate, 1=Card Studio, 2=Scanner, 3=History
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Sub-tab under Generator: 0=PromptPay, 1=Wi-Fi, 2=Store Links, 3=Text, 4=Location
    private val _generatorCategory = MutableStateFlow(0)
    val generatorCategory: StateFlow<Int> = _generatorCategory.asStateFlow()

    // PromptPay State
    private val _promptPayTarget = MutableStateFlow("")
    val promptPayTarget: StateFlow<String> = _promptPayTarget.asStateFlow()

    private val _promptPayAmount = MutableStateFlow("")
    val promptPayAmount: StateFlow<String> = _promptPayAmount.asStateFlow()

    private val _promptPayShopName = MutableStateFlow("")
    val promptPayShopName: StateFlow<String> = _promptPayShopName.asStateFlow()

    // Wi-Fi State
    private val _wifiSsid = MutableStateFlow("")
    val wifiSsid: StateFlow<String> = _wifiSsid.asStateFlow()

    private val _wifiPassword = MutableStateFlow("")
    val wifiPassword: StateFlow<String> = _wifiPassword.asStateFlow()

    private val _wifiSecurity = MutableStateFlow(WifiSecurity.WPA)
    val wifiSecurity: StateFlow<WifiSecurity> = _wifiSecurity.asStateFlow()

    private val _wifiHidden = MutableStateFlow(false)
    val wifiHidden: StateFlow<Boolean> = _wifiHidden.asStateFlow()

    // Store Link State
    private val _storePlatform = MutableStateFlow(StorePlatform.LINE_OA)
    val storePlatform: StateFlow<StorePlatform> = _storePlatform.asStateFlow()

    private val _storeValue = MutableStateFlow("")
    val storeValue: StateFlow<String> = _storeValue.asStateFlow()

    // Text State
    private val _rawText = MutableStateFlow("")
    val rawText: StateFlow<String> = _rawText.asStateFlow()

    // Current location state (foreground, user-requested only)
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // Digital Business Card State
    private val _businessCard = MutableStateFlow(DigitalBusinessCard())
    val businessCard: StateFlow<DigitalBusinessCard> = _businessCard.asStateFlow()

    // Active Preview Modal
    private val _activePreview = MutableStateFlow<ActiveQrPreview?>(null)
    val activePreview: StateFlow<ActiveQrPreview?> = _activePreview.asStateFlow()

    // Scan Result Modal
    private val _activeScanResult = MutableStateFlow<ParsedQrResult?>(null)
    val activeScanResult: StateFlow<ParsedQrResult?> = _activeScanResult.asStateFlow()

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

    // Optional center logo state
    private val _includeCenterLogo = MutableStateFlow(true)
    val includeCenterLogo: StateFlow<Boolean> = _includeCenterLogo.asStateFlow()

    private val _customCenterLogo = MutableStateFlow<Bitmap?>(null)
    val customCenterLogo: StateFlow<Bitmap?> = _customCenterLogo.asStateFlow()

    fun setIncludeCenterLogo(enabled: Boolean) {
        cancelPendingQrGeneration()
        _includeCenterLogo.value = enabled
    }

    fun setCustomCenterLogo(uri: Uri?) {
        cancelPendingQrGeneration()
        if (uri == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = decodeCenterLogo(uri)
            val persisted = bitmap?.let { selected ->
                try {
                    centerLogoFile.outputStream().use { stream ->
                        selected.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                } catch (_: Exception) {
                    false
                }
            } ?: false

            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    _customCenterLogo.value = bitmap
                    _includeCenterLogo.value = true
                    Toast.makeText(
                        getApplication(),
                        if (persisted) {
                            localizedNow(
                                "ใช้รูป/ตราร้านตรงกลาง QR แล้ว",
                                "Custom center image/logo saved"
                            )
                        } else {
                            localizedNow(
                                "ใช้รูปกลาง QR แล้ว แต่ยังบันทึกถาวรไม่ได้",
                                "Center image selected, but could not be saved persistently"
                            )
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        getApplication(),
                        localizedNow("ไม่สามารถอ่านรูปที่เลือกได้", "Unable to read the selected image"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun clearCustomCenterLogo() {
        _customCenterLogo.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (centerLogoFile.exists()) centerLogoFile.delete()
            } catch (_: Exception) { }
        }
    }

    private fun decodeCenterLogo(uri: Uri): Bitmap? {
        val resolver = getApplication<Application>().contentResolver
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) {
                sample *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    

    // History is stored without a retention cap. The UI loads a bounded window and expands
    // only after an explicit user action so a large local history does not become one unbounded query.
    private val _historyLimit = MutableStateFlow(HISTORY_PAGE_SIZE)

    val historyItems: StateFlow<List<QrItemEntity>> = _historyLimit
        .flatMapLatest { limit -> dao.getAllQrItems(limit) }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val historyTotalCount: StateFlow<Int> = dao.getQrItemCount()
        .catch { emit(0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    fun loadMoreHistory() {
        if (historyItems.value.size >= historyTotalCount.value) return
        _historyLimit.value = _historyLimit.value + HISTORY_PAGE_SIZE
    }

    init {
        // Load an optional custom QR center image from app-private storage.
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (centerLogoFile.exists()) {
                    BitmapFactory.decodeFile(centerLogoFile.absolutePath)?.let { savedLogo ->
                        _customCenterLogo.value = savedLogo
                    }
                }
            } catch (_: Exception) { }

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
        cancelPendingQrGeneration()
        _currentTab.value = index
    }

    fun setGeneratorCategory(index: Int) {
        cancelPendingQrGeneration()
        _generatorCategory.value = index
    }

    fun setPromptPayTarget(target: String) {
        cancelPendingQrGeneration()
        _promptPayTarget.value = target
    }

    fun setPromptPayAmount(amount: String) {
        cancelPendingQrGeneration()
        _promptPayAmount.value = amount
    }

    fun setPromptPayShopName(name: String) {
        cancelPendingQrGeneration()
        _promptPayShopName.value = name
    }

    fun setWifiSsid(ssid: String) {
        cancelPendingQrGeneration()
        _wifiSsid.value = ssid
    }

    fun setWifiPassword(pass: String) {
        cancelPendingQrGeneration()
        _wifiPassword.value = pass
    }

    fun setWifiSecurity(security: WifiSecurity) {
        cancelPendingQrGeneration()
        _wifiSecurity.value = security
    }

    fun setWifiHidden(hidden: Boolean) {
        cancelPendingQrGeneration()
        _wifiHidden.value = hidden
    }

    fun setStorePlatform(platform: StorePlatform) {
        cancelPendingQrGeneration()
        _storePlatform.value = platform
    }

    fun setStoreValue(value: String) {
        cancelPendingQrGeneration()
        _storeValue.value = value
    }

    fun setRawText(text: String) {
        cancelPendingQrGeneration()
        _rawText.value = text
    }

    fun setLocationError(message: String?) {
        _locationError.value = message
    }

    fun fetchCurrentLocation() {
        if (_isLoadingLocation.value) return
        _isLoadingLocation.value = true
        _locationError.value = null
        CurrentLocationProvider.requestCurrentLocation(
            context = getApplication(),
            onResult = { location ->
                val point = location?.let { GeoPoint(it.latitude, it.longitude) }
                if (point != null && point.isValid()) {
                    _currentLocation.value = point
                    _locationError.value = null
                } else {
                    _locationError.value = localizedNow(
                        "ไม่พบพิกัดปัจจุบันจากอุปกรณ์",
                        "The device did not return a current location"
                    )
                }
                _isLoadingLocation.value = false
            },
            onError = { error ->
                _isLoadingLocation.value = false
                _locationError.value = when (error) {
                    LocationError.PERMISSION_REQUIRED -> localizedNow(
                        "ต้องอนุญาตสิทธิ์ตำแหน่งก่อน",
                        "Location permission is required"
                    )
                    LocationError.SERVICES_DISABLED -> localizedNow(
                        "กรุณาเปิดบริการตำแหน่งของเครื่อง",
                        "Turn on the device location service"
                    )
                    LocationError.UNAVAILABLE -> localizedNow(
                        "ไม่สามารถอ่านพิกัดปัจจุบันได้",
                        "Current location is unavailable"
                    )
                }
            }
        )
    }

    fun updateBusinessCard(card: DigitalBusinessCard) {
        cancelPendingQrGeneration()
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
                Toast.makeText(getApplication(), localizedNow("บันทึกโปรไฟล์นามบัตรแล้ว", "Business card profile saved"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openPromptPayHistoryItem(item: QrItemEntity) {
        cancelPendingQrGeneration()
        val resolved = HistoryPromptPayResolver.resolve(item)
        if (resolved == null) {
            Toast.makeText(
                getApplication(),
                localizedNow("ข้อมูลพร้อมเพย์ในประวัติไม่ถูกต้อง", "Saved PromptPay data is invalid"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val amountStr = if (resolved.amount != null && resolved.amount > 0) {
            "฿${String.format("%,.2f", resolved.amount)}"
        } else {
            localizedNow("ไม่ระบุยอดเงิน", "Amount not specified")
        }
        val title = localizedNow("พร้อมเพย์จากประวัติ", "PromptPay history")
        val subtitle = localizedNow(
            "เบอร์/เลขบัตร: ${resolved.target} ($amountStr)",
            "PromptPay ID: ${resolved.target} ($amountStr)"
        )

        executeQrAction(
            content = resolved.payload,
            qrType = "PROMPTPAY",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                val standeeBitmap = QrCodeUtil.createPromptPayStandeeBitmap(
                    qrBitmap = qrBitmap,
                    title = "THAI QR PAYMENT",
                    targetId = resolved.target,
                    amount = resolved.amount,
                    merchantName = "",
                    backgroundColor = lightColor
                )
                ActiveQrPreview(
                    title = title,
                    subtitle = subtitle,
                    rawContent = resolved.payload,
                    qrBitmap = qrBitmap,
                    standeeBitmap = standeeBitmap,
                    targetId = resolved.target,
                    amount = resolved.amount,
                    type = "PROMPTPAY"
                )
            }
        )
    }

    fun closePreview() {
        cancelPendingQrGeneration()
        _activePreview.value = null
    }

    fun closeScanResult() {
        _activeScanResult.value = null
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
        cancelPendingQrGeneration()
        _qrForegroundColor.value = color
    }

    fun setQrBackgroundColor(color: Color) {
        cancelPendingQrGeneration()
        _qrBackgroundColor.value = color
    }

    /**
     * Executes a QR action directly.
     * Monetization is not included in this build.
     */
    private fun executeQrAction(
        content: String,
        qrType: String,
        darkColor: Int,
        lightColor: Int,
        buildPreview: (Bitmap) -> ActiveQrPreview,
        buildHistoryItem: ((ActiveQrPreview) -> QrItemEntity?)? = null
    ) {
        if (QrValidationUtil.checkColorContrast(darkColor, lightColor) is ValidationResult.Invalid) {
            Toast.makeText(
                getApplication(),
                localizedNow(
                    "สี QR กับพื้นหลังตัดกันไม่พอ กรุณาเลือกสีที่ต่างกันชัดเจน",
                    "QR foreground and background need more contrast"
                ),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val includeCenterLogo = _includeCenterLogo.value
        val customCenterLogo = _customCenterLogo.value
        val requestToken = qrGenerationRequestGuard.begin()

        qrGenerationJob?.cancel()
        qrGenerationJob = viewModelScope.launch(Dispatchers.Default) {
            val centerLogo = if (!includeCenterLogo) {
                null
            } else {
                customCenterLogo ?: QrCodeUtil.createDefaultCenterLogo(qrType)
            }

            if (!qrGenerationRequestGuard.isCurrent(requestToken)) return@launch

            val qrBitmap = QrCodeUtil.generateQrBitmap(
                content = content,
                size = QrCodeUtil.DEFAULT_SIZE,
                darkColor = darkColor,
                lightColor = lightColor,
                centerLogo = centerLogo
            )
            if (qrBitmap == null) {
                showQrGenerationError(
                    requestToken,
                    "ไม่สามารถสร้าง QR ได้",
                    "Could not generate the QR code"
                )
                return@launch
            }

            if (!qrGenerationRequestGuard.isCurrent(requestToken)) return@launch

            val verification = QrValidationUtil.verifyGeneratedQrBitmap(qrBitmap, content)
            if (!verification.isValid || verification.decodedContent != content) {
                showQrGenerationError(
                    requestToken,
                    "QR ที่สร้างอ่านกลับไม่ตรงกับข้อมูลต้นฉบับ กรุณาเปลี่ยนสีหรือโลโก้",
                    "The generated QR did not decode back to the original content. Try different colors or logo."
                )
                return@launch
            }

            if (!qrGenerationRequestGuard.isCurrent(requestToken)) return@launch

            val preview = try {
                buildPreview(qrBitmap)
            } catch (_: Exception) {
                showQrGenerationError(
                    requestToken,
                    "ไม่สามารถสร้างตัวอย่าง QR ได้",
                    "Could not build the QR preview"
                )
                return@launch
            }

            if (!qrGenerationRequestGuard.isCurrent(requestToken)) return@launch

            val historyItem = buildHistoryItem?.invoke(preview)
            val accepted = withContext(Dispatchers.Main) {
                if (qrGenerationRequestGuard.isCurrent(requestToken)) {
                    _activePreview.value = preview
                    true
                } else {
                    false
                }
            }

            if (accepted && historyItem != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    dao.insertQrItem(historyItem)
                }
            }
        }
    }

    private fun cancelPendingQrGeneration() {
        qrGenerationRequestGuard.invalidate()
        qrGenerationJob?.cancel()
        qrGenerationJob = null
    }

    private suspend fun showQrGenerationError(
        requestToken: Long,
        thMessage: String,
        enMessage: String
    ) {
        if (!qrGenerationRequestGuard.isCurrent(requestToken)) return
        withContext(Dispatchers.Main) {
            if (qrGenerationRequestGuard.isCurrent(requestToken)) {
                Toast.makeText(
                    getApplication(),
                    localizedNow(thMessage, enMessage),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Generate PromptPay QR
     */
    fun generatePromptPay() {
        cancelPendingQrGeneration()
        val target = _promptPayTarget.value.trim()
        if (target.isBlank()) {
            Toast.makeText(
                getApplication(),
                localizedNow("กรุณาระบุเบอร์โทรหรือเลขบัตรประชาชน", "Enter a PromptPay phone number or ID"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        when (val validation = QrValidationUtil.validatePromptPayTarget(target)) {
            is ValidationResult.Invalid -> {
                Toast.makeText(getApplication(), localizedNow(validation.reasonTh, validation.reasonEn), Toast.LENGTH_SHORT).show()
                return
            }
            ValidationResult.Valid -> Unit
        }

        val amountInput = _promptPayAmount.value
        when (val amountValidation = QrValidationUtil.validateAmount(amountInput)) {
            is ValidationResult.Invalid -> {
                Toast.makeText(
                    getApplication(),
                    localizedNow(amountValidation.reasonTh, amountValidation.reasonEn),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            ValidationResult.Valid -> Unit
        }

        val amount = amountInput.toDoubleOrNull()
        val payload = PromptPayGenerator.generatePayload(target, amount)
        val merchantName = _promptPayShopName.value
        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val amountStr = if (amount != null && amount > 0) {
            "฿${String.format("%,.2f", amount)}"
        } else {
            localizedNow("ไม่ระบุยอดเงิน", "Amount not specified")
        }
        val subtitle = localizedNow(
            "เบอร์/เลขบัตร: $target ($amountStr)",
            "PromptPay ID: $target ($amountStr)"
        )
        val previewTitle = localizedNow("พร้อมเพย์", "PromptPay")
        val historyTitle = localizedNow("พร้อมเพย์ $amountStr", "PromptPay $amountStr")

        executeQrAction(
            content = payload,
            qrType = "PROMPTPAY",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                val standeeBitmap = QrCodeUtil.createPromptPayStandeeBitmap(
                    qrBitmap = qrBitmap,
                    title = "THAI QR PAYMENT",
                    targetId = target,
                    amount = amount,
                    merchantName = merchantName,
                    backgroundColor = lightColor
                )
                ActiveQrPreview(
                    title = previewTitle,
                    subtitle = subtitle,
                    rawContent = payload,
                    qrBitmap = qrBitmap,
                    standeeBitmap = standeeBitmap,
                    targetId = target,
                    amount = amount,
                    type = "PROMPTPAY"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "PROMPTPAY",
                    title = historyTitle,
                    subtitle = subtitle,
                    rawContent = payload,
                    targetId = target,
                    amount = amount,
                    isScan = false
                )
            }
        )
    }

    /**
     * Generate Wi-Fi QR
     */
    fun generateWifi() {
        cancelPendingQrGeneration()
        val ssid = _wifiSsid.value.trim()
        val password = _wifiPassword.value
        val security = _wifiSecurity.value.code
        val hidden = _wifiHidden.value

        if (ssid.isBlank()) {
            Toast.makeText(
                getApplication(),
                localizedNow("กรุณาระบุชื่อ Wi-Fi (SSID)", "Enter a Wi-Fi network name (SSID)"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (QrValidationUtil.validateWifi(ssid, password, security) is ValidationResult.Invalid) {
            Toast.makeText(
                getApplication(),
                localizedNow("ข้อมูล Wi-Fi ไม่ถูกต้อง", "Invalid Wi-Fi configuration"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val payload = QrCodeUtil.buildWifiPayload(
            ssid = ssid,
            pass = password,
            security = security,
            isHidden = hidden
        )
        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val previewSubtitle = localizedNow(
            "รหัสผ่าน: ${password.ifBlank { "(ไม่มี)" }}",
            "Password: ${password.ifBlank { "(none)" }}"
        )
        val historySubtitle = HistoryPrivacyUtil.wifiHistorySubtitle(
            password.isNotBlank(),
            LocalizationManager.effectiveLanguageCode()
        )

        executeQrAction(
            content = payload,
            qrType = "WIFI",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                ActiveQrPreview(
                    title = "Wi-Fi: $ssid",
                    subtitle = previewSubtitle,
                    rawContent = payload,
                    qrBitmap = qrBitmap,
                    type = "WIFI"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "WIFI",
                    title = "Wi-Fi: $ssid",
                    subtitle = historySubtitle,
                    rawContent = payload,
                    isScan = false
                )
            }
        )
    }

    /**
     * Generate Store Link QR
     */
    fun generateStoreLink() {
        cancelPendingQrGeneration()
        val platform = _storePlatform.value
        val storeValue = _storeValue.value
        val model = StoreLinkModel(platform, storeValue)
        val fullUrl = model.fullUrl

        if (storeValue.isBlank()) {
            Toast.makeText(
                getApplication(),
                localizedNow("กรุณากรอกลิงก์หรือไอดีร้านค้า", "Enter a store link or ID"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (QrValidationUtil.validateUrl(fullUrl) is ValidationResult.Invalid) {
            Toast.makeText(
                getApplication(),
                localizedNow("ลิงก์ร้านค้าไม่ถูกต้อง", "Invalid store link"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val title = localizedNow(
            "ลิงก์ร้าน ${platform.title}",
            "Store link: ${platform.title}"
        )

        executeQrAction(
            content = fullUrl,
            qrType = "STORE",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                ActiveQrPreview(
                    title = title,
                    subtitle = fullUrl,
                    rawContent = fullUrl,
                    qrBitmap = qrBitmap,
                    type = "STORE_LINK"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "STORE_LINK",
                    title = title,
                    subtitle = fullUrl,
                    rawContent = fullUrl,
                    isScan = false
                )
            }
        )
    }

    /**
     * Generate a location QR from the real foreground device location.
     */
    fun generateLocation() {
        cancelPendingQrGeneration()
        val point = _currentLocation.value
        if (point == null || !point.isValid()) {
            Toast.makeText(
                getApplication(),
                localizedNow("ยังไม่มีพิกัดปัจจุบัน", "No current location is available"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val payload = LocationQrUtil.buildGeoPayload(point)
        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val subtitle = LocationQrUtil.formatPoint(point)
        val title = localizedNow("พิกัดปัจจุบัน", "Current location")

        executeQrAction(
            content = payload,
            qrType = "LOCATION",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                ActiveQrPreview(
                    title = title,
                    subtitle = subtitle,
                    rawContent = payload,
                    qrBitmap = qrBitmap,
                    type = "LOCATION"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "LOCATION",
                    title = title,
                    subtitle = subtitle,
                    rawContent = payload,
                    isScan = false
                )
            }
        )
    }

    /**
     * Generate Text QR
     */
    fun generateText() {
        cancelPendingQrGeneration()
        val text = _rawText.value.trim()
        if (text.isBlank()) {
            Toast.makeText(
                getApplication(),
                localizedNow("กรุณากรอกข้อความ", "Enter text"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val darkColor = _qrForegroundColor.value.toArgb()
        val lightColor = _qrBackgroundColor.value.toArgb()
        val previewTitle = localizedNow("ข้อความ QR", "Text QR")
        val historyTitle = localizedNow("ข้อความ", "Text")
        val previewSubtitle = if (text.length > 40) text.take(40) + "..." else text

        executeQrAction(
            content = text,
            qrType = "TEXT",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                ActiveQrPreview(
                    title = previewTitle,
                    subtitle = previewSubtitle,
                    rawContent = text,
                    qrBitmap = qrBitmap,
                    type = "TEXT"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "TEXT",
                    title = historyTitle,
                    subtitle = text.take(40),
                    rawContent = text,
                    isScan = false
                )
            }
        )
    }

    /**
     * Generate Digital Business Card vCard & PromptPay QR
     */
    fun generateBusinessCardPreview() {
        cancelPendingQrGeneration()
        val card = _businessCard.value
        if (QrValidationUtil.validateBusinessCard(
                card.fullName.ifBlank { card.businessName },
                card.phoneNumber
            ) is ValidationResult.Invalid
        ) {
            Toast.makeText(
                getApplication(),
                localizedNow("กรุณากรอกชื่อและเบอร์โทรศัพท์บนบัตร", "Enter a name and phone number for the card"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

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
        val darkColor = card.cardTheme.primaryColorHex.toInt()
        val lightColor = Color.White.toArgb()
        val name = card.businessName.ifBlank { card.fullName }
        val previewTitle = localizedNow(
            "นามบัตรดิจิทัล: $name",
            "Digital business card: $name"
        )
        val historyTitle = localizedNow(
            "นามบัตร: $name",
            "Business card: $name"
        )
        val subtitle = "${card.profession} • ${card.phoneNumber}"

        executeQrAction(
            content = vcard,
            qrType = "VCARD",
            darkColor = darkColor,
            lightColor = lightColor,
            buildPreview = { qrBitmap ->
                ActiveQrPreview(
                    title = previewTitle,
                    subtitle = subtitle,
                    rawContent = vcard,
                    qrBitmap = qrBitmap,
                    type = "VCARD"
                )
            },
            buildHistoryItem = {
                QrItemEntity(
                    type = "VCARD",
                    title = historyTitle,
                    subtitle = "${card.profession} | ${card.phoneNumber}",
                    rawContent = vcard,
                    isScan = false
                )
            }
        )
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
                            Toast.makeText(getApplication(), localizedNow("ไม่พบ QR ในรูปภาพที่เลือก", "No QR code found in the selected image"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), localizedNow("เกิดข้อผิดพลาดในการอ่านรูปภาพ", "Could not read the image"), Toast.LENGTH_SHORT).show()
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
                localizedNow("บันทึกรูปภาพแล้ว", "Image saved"),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(getApplication(), localizedNow("ไม่สามารถบันทึกรูปภาพได้", "Could not save the image"), Toast.LENGTH_SHORT).show()
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
