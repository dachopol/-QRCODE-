package com.example.util

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.ParsedQrResult
import com.example.model.ParsedQrType
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

object QrScannerUtil {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(com.google.zxing.BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }

    /**
     * Decodes QR text from a Bitmap (e.g. chosen from gallery).
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decodeWithState(binaryBitmap)
            result.text
        } catch (_: Exception) {
            null
        }
    }

    /**
     * CameraX ImageAnalysis analyzer to detect QR in real-time.
     */
    class QrCodeImageAnalyzer(
        private val onQrCodeScanned: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private var lastScanTime = 0L

        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            // Throttle to avoid excessive CPU usage
            if (currentTime - lastScanTime < 400) {
                imageProxy.close()
                return
            }

            try {
                val planes = imageProxy.planes
                if (planes.isNotEmpty()) {
                    val buffer = planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)

                    val width = imageProxy.width
                    val height = imageProxy.height

                    val source = PlanarYUVLuminanceSource(
                        data,
                        width,
                        height,
                        0,
                        0,
                        width,
                        height,
                        false
                    )
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val result = MultiFormatReader().decodeWithState(binaryBitmap)
                    if (result != null && result.text.isNotBlank()) {
                        lastScanTime = currentTime
                        onQrCodeScanned(result.text)
                    }
                }
            } catch (_: Exception) {
                // No QR found in frame
            } finally {
                imageProxy.close()
            }
        }
    }

    /**
     * Smart parser to analyze raw text and classify it into PromptPay, Wi-Fi, URL, vCard, etc.
     */
    fun parseQrContent(raw: String): ParsedQrResult {
        val trimmed = raw.trim()

        // 1. PromptPay EMVCo check
        if (trimmed.contains("000201") && trimmed.contains("A000000677010111")) {
            val parsed = PromptPayGenerator.parsePromptPay(trimmed)
            if (parsed != null) {
                val target = parsed.first
                val amount = parsed.second
                val amountText = if (amount != null) "จำนวนเงิน: ฿${String.format("%,.2f", amount)}" else "ไม่ระบุยอดเงิน"
                return ParsedQrResult(
                    rawText = raw,
                    type = ParsedQrType.PROMPTPAY,
                    title = "พร้อมเพย์ (Thai QR Payment)",
                    subtitle = "บัญชีรับเงิน: $target | $amountText",
                    promptPayId = target,
                    amount = amount
                )
            }
        }

        // 2. Wi-Fi QR check
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            var ssid = ""
            var pass = ""
            var sec = "WPA"

            val parts = trimmed.substring(5).split(";")
            for (part in parts) {
                if (part.startsWith("S:")) ssid = part.substring(2)
                if (part.startsWith("P:")) pass = part.substring(2)
                if (part.startsWith("T:")) sec = part.substring(2)
            }

            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.WIFI,
                title = "เครือข่าย Wi-Fi",
                subtitle = "ชื่อ: $ssid (ความปลอดภัย: $sec)",
                wifiSsid = ssid,
                wifiPass = pass,
                wifiSecurity = sec
            )
        }

        // 3. vCard / Contact
        if (trimmed.contains("BEGIN:VCARD", ignoreCase = true)) {
            var fn = ""
            var tel = ""
            var email = ""
            var org = ""

            trimmed.lines().forEach { line ->
                val l = line.trim()
                if (l.startsWith("FN:", ignoreCase = true)) fn = l.substring(3).trim()
                if (l.startsWith("TEL", ignoreCase = true)) {
                    val colonIdx = l.indexOf(':')
                    if (colonIdx != -1) tel = l.substring(colonIdx + 1).trim()
                }
                if (l.startsWith("EMAIL", ignoreCase = true)) {
                    val colonIdx = l.indexOf(':')
                    if (colonIdx != -1) email = l.substring(colonIdx + 1).trim()
                }
                if (l.startsWith("ORG:", ignoreCase = true)) org = l.substring(4).trim()
            }

            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.VCARD,
                title = "นามบัตรดิจิทัล (Contact Card)",
                subtitle = listOfNotNull(fn.ifBlank { null }, org.ifBlank { null }, tel.ifBlank { null }).joinToString(" • "),
                contactName = fn,
                contactPhone = tel,
                contactEmail = email,
                contactOrg = org
            )
        }

        // 4. URL
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true)
        ) {
            val url = if (trimmed.startsWith("www.", ignoreCase = true)) "https://$trimmed" else trimmed
            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.URL,
                title = "ลิงก์เว็บไซต์ / ร้านค้า",
                subtitle = url,
                url = url
            )
        }

        // 5. Phone dial
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4)
            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.PHONE,
                title = "เบอร์โทรศัพท์",
                subtitle = phone,
                contactPhone = phone
            )
        }

        // 6. Generic text
        return ParsedQrResult(
            rawText = raw,
            type = ParsedQrType.TEXT,
            title = "ข้อความทั่วไป",
            subtitle = if (trimmed.length > 50) trimmed.take(50) + "..." else trimmed
        )
    }
}
