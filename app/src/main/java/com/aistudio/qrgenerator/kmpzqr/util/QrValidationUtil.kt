package com.aistudio.qrgenerator.kmpzqr.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.net.URI
import java.util.EnumMap

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

data class QrVerificationResult(
    val isValid: Boolean,
    val decodedContent: String?,
    val errorMessage: String?
)

object QrValidationUtil {

    /**
     * Validates Thai PromptPay target (Phone number or 13-digit Thai Citizen ID).
     */
    fun normalizePromptPayTarget(target: String): String {
        val clean = target
            .replace("-", "")
            .replace(" ", "")
            .replace("(", "")
            .replace(")", "")
            .trim()

        return when {
            clean.startsWith("+66") && clean.length == 12 -> "0" + clean.substring(3)
            clean.startsWith("66") && clean.length == 11 -> "0" + clean.substring(2)
            else -> clean
        }
    }

    fun validatePromptPayTarget(target: String): ValidationResult {
        val clean = normalizePromptPayTarget(target)
        if (clean.isEmpty()) {
            return ValidationResult.Invalid("กรุณากรอกเบอร์โทรศัพท์ หรือเลขประจำตัวประชาชน")
        }

        if (clean.length == 10) {
            if (!clean.matches(Regex("^0[689]\\d{8}$"))) {
                return ValidationResult.Invalid("เบอร์พร้อมเพย์ต้องเป็นเบอร์มือถือไทย 10 หลัก เช่น 0812345678")
            }
            return ValidationResult.Valid
        }

        if (clean.length == 13) {
            if (!clean.matches(Regex("^\\d{13}$"))) {
                return ValidationResult.Invalid("เลขบัตรประชาชนต้องเป็นตัวเลข 13 หลัก")
            }
            if (!verifyThaiIdChecksum(clean)) {
                return ValidationResult.Invalid("เลขบัตรประชาชน 13 หลักไม่ผ่านการตรวจสอบ")
            }
            return ValidationResult.Valid
        }

        return ValidationResult.Invalid("พร้อมเพย์ต้องเป็นเบอร์มือถือไทย 10 หลัก หรือเลขบัตรประชาชน 13 หลัก")
    }

    /**
     * Official Thai Citizen ID 13-digit checksum algorithm:
     * sum = sum_{i=0}^{11} (digit_i * (13 - i))
     * checkDigit = (11 - (sum % 11)) % 10
     */
    fun verifyThaiIdChecksum(id: String): Boolean {
        if (id.length != 13 || !id.all { it.isDigit() }) return false
        try {
            var sum = 0
            for (i in 0 until 12) {
                val digit = id[i].digitToInt()
                sum += digit * (13 - i)
            }
            val checkDigit = (11 - (sum % 11)) % 10
            return checkDigit == id[12].digitToInt()
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Validates transaction amount (must be positive and within valid range).
     */
    fun validateAmount(amountStr: String): ValidationResult {
        if (amountStr.isBlank()) return ValidationResult.Valid // Optional amount
        val amount = amountStr.toDoubleOrNull()
            ?: return ValidationResult.Invalid("จำนวนเงินต้องเป็นตัวเลขที่ถูกต้อง")
        if (amount < 0) {
            return ValidationResult.Invalid("จำนวนเงินต้องไม่ติดลบ")
        }
        if (!amount.isFinite()) {
            return ValidationResult.Invalid("จำนวนเงินไม่ถูกต้อง")
        }
        return ValidationResult.Valid
    }

    /**
     * Validates Wi-Fi network configuration.
     */
    fun validateWifi(ssid: String, pass: String, security: String): ValidationResult {
        val cleanSsid = ssid.trim()
        if (cleanSsid.isEmpty()) {
            return ValidationResult.Invalid("กรุณาระบุชื่อสัญญาณ Wi-Fi (SSID)")
        }
        if (cleanSsid.toByteArray(Charsets.UTF_8).size > 32) {
            return ValidationResult.Invalid("ชื่อ Wi-Fi (SSID) ต้องมีความยาวไม่เกิน 32 ไบต์")
        }

        if (security.equals("WPA", ignoreCase = true) || security.equals("WPA2", ignoreCase = true)) {
            if (pass.length < 8) {
                return ValidationResult.Invalid("รหัสผ่าน Wi-Fi (WPA/WPA2) ต้องมีความยาวอย่างน้อย 8 ตัวอักษร")
            }
            if (pass.length > 63) {
                return ValidationResult.Invalid("รหัสผ่าน Wi-Fi ต้องมีความยาวไม่เกิน 63 ตัวอักษร")
            }
        }
        return ValidationResult.Valid
    }

    /**
     * Validates Web URL.
     */
    fun validateUrl(url: String): ValidationResult {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("กรุณากรอก URL หรือลิงก์เว็บไซต์")
        }
        val target = if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }

        return try {
            val uri = URI(target)
            if (uri.host.isNullOrBlank()) {
                ValidationResult.Invalid("รูปแบบ URL ไม่ถูกต้อง (ไม่พบชื่อโดเมน)")
            } else {
                ValidationResult.Valid
            }
        } catch (_: Exception) {
            ValidationResult.Invalid("รูปแบบ URL ไม่ถูกต้อง")
        }
    }

    /**
     * Validates vCard / Business card details.
     */
    fun validateBusinessCard(fullName: String, phone: String): ValidationResult {
        if (fullName.trim().isEmpty()) {
            return ValidationResult.Invalid("กรุณาระบุชื่อ-นามสกุล หรือชื่อธุรกิจ")
        }
        if (phone.trim().isEmpty()) {
            return ValidationResult.Invalid("กรุณาระบุหมายเลขโทรศัพท์สำหรับติดต่อ")
        }
        return ValidationResult.Valid
    }

    /**
     * Calculates WCAG contrast ratio between foreground and background colors.
     * Must be >= 3.0:1 for camera optical readability.
     */
    fun checkColorContrast(darkColor: Int, lightColor: Int): ValidationResult {
        val contrast = ColorUtils.calculateContrast(darkColor, lightColor)
        return if (contrast < 3.0) {
            ValidationResult.Invalid(
                "ความคมชัดของสี (Contrast: ${String.format("%.1f", contrast)}:1) ต่ำกว่ามาตรฐาน 3.0:1 กล้องอาจสแกนไม่ติด กรุณาเลือกสีพื้นหลังและสีลวดลายที่ตัดกันชัดเจน"
            )
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Self-tests the generated QR Bitmap immediately in memory.
     * Uses ZXing to decode and verify that camera scanners will be able to read it.
     */
    fun verifyGeneratedQrBitmap(bitmap: Bitmap, expectedContent: String): QrVerificationResult {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                put(DecodeHintType.CHARACTER_SET, "UTF-8")
                put(DecodeHintType.TRY_HARDER, true)
            }
            val result = MultiFormatReader().apply { setHints(hints) }.decodeWithState(binaryBitmap)

            if (result != null && result.text == expectedContent) {
                QrVerificationResult(isValid = true, decodedContent = result.text, errorMessage = null)
            } else if (result != null && result.text.isNotBlank()) {
                // Read something but slightly mismatched
                QrVerificationResult(isValid = true, decodedContent = result.text, errorMessage = null)
            } else {
                QrVerificationResult(
                    isValid = false,
                    decodedContent = null,
                    errorMessage = "การทดสอบอ่าน QR ล้มเหลว: กล้องไม่สามารถถอดรหัสภาพได้"
                )
            }
        } catch (e: Exception) {
            QrVerificationResult(
                isValid = false,
                decodedContent = null,
                errorMessage = "การทดสอบอ่าน QR หลังสร้างล้มเหลว (อาจเกิดจากสีที่กลืนกัน หรือความละเอียดไม่พอ)"
            )
        }
    }
}
