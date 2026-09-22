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

    private fun createReader(): MultiFormatReader =
        MultiFormatReader().apply {
            val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                put(DecodeHintType.POSSIBLE_FORMATS, listOf(com.google.zxing.BarcodeFormat.QR_CODE))
                put(DecodeHintType.TRY_HARDER, true)
                put(DecodeHintType.CHARACTER_SET, "UTF-8")
            }
            setHints(hints)
        }

    /**
     * Decodes QR text from a Bitmap selected from the gallery.
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            createReader().decodeWithState(binaryBitmap).text
        } catch (_: Exception) {
            null
        }
    }

    /**
     * CameraX analyzer that handles Y-plane row stride and sensor rotation
     * before passing luminance data to ZXing.
     */
    class QrCodeImageAnalyzer(
        private val onQrCodeScanned: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private var lastAnalysisTime = 0L
        private var lastDecodedValue: String? = null
        private var lastDecodedAt = 0L

        override fun analyze(imageProxy: ImageProxy) {
            val now = System.currentTimeMillis()

            // Limit decode work while still feeling immediate on camera preview.
            if (now - lastAnalysisTime < 250L) {
                imageProxy.close()
                return
            }
            lastAnalysisTime = now

            try {
                val plane = imageProxy.planes.firstOrNull() ?: return
                val width = imageProxy.width
                val height = imageProxy.height
                val yPlane = copyLuminancePlane(
                    buffer = plane.buffer,
                    width = width,
                    height = height,
                    rowStride = plane.rowStride,
                    pixelStride = plane.pixelStride
                )

                val rotated = rotateLuminance(
                    data = yPlane,
                    width = width,
                    height = height,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees
                )

                val source = PlanarYUVLuminanceSource(
                    rotated.data,
                    rotated.width,
                    rotated.height,
                    0,
                    0,
                    rotated.width,
                    rotated.height,
                    false
                )
                val result = createReader().decodeWithState(
                    BinaryBitmap(HybridBinarizer(source))
                )

                val value = result.text
                if (value.isNotBlank()) {
                    // Avoid opening the same result repeatedly while the camera
                    // remains pointed at one QR code.
                    val duplicateTooSoon =
                        value == lastDecodedValue && now - lastDecodedAt < 1500L
                    if (!duplicateTooSoon) {
                        lastDecodedValue = value
                        lastDecodedAt = now
                        onQrCodeScanned(value)
                    }
                }
            } catch (_: Exception) {
                // No readable QR in this frame.
            } finally {
                imageProxy.close()
            }
        }
    }

    private data class RotatedLuminance(
        val data: ByteArray,
        val width: Int,
        val height: Int
    )

    private fun copyLuminancePlane(
        buffer: java.nio.ByteBuffer,
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int
    ): ByteArray {
        val src = buffer.duplicate()
        val output = ByteArray(width * height)

        for (row in 0 until height) {
            val rowOffset = row * rowStride
            for (col in 0 until width) {
                val sourceIndex = rowOffset + col * pixelStride
                if (sourceIndex < src.limit()) {
                    output[row * width + col] = src.get(sourceIndex)
                }
            }
        }
        return output
    }

    private fun rotateLuminance(
        data: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int
    ): RotatedLuminance {
        val normalized = ((rotationDegrees % 360) + 360) % 360
        if (normalized == 0) {
            return RotatedLuminance(data, width, height)
        }

        val output = ByteArray(data.size)

        return when (normalized) {
            90 -> {
                for (row in 0 until height) {
                    for (col in 0 until width) {
                        val newRow = col
                        val newCol = height - 1 - row
                        output[newRow * height + newCol] = data[row * width + col]
                    }
                }
                RotatedLuminance(output, height, width)
            }
            180 -> {
                for (i in data.indices) {
                    output[data.lastIndex - i] = data[i]
                }
                RotatedLuminance(output, width, height)
            }
            270 -> {
                for (row in 0 until height) {
                    for (col in 0 until width) {
                        val newRow = width - 1 - col
                        val newCol = row
                        output[newRow * height + newCol] = data[row * width + col]
                    }
                }
                RotatedLuminance(output, height, width)
            }
            else -> RotatedLuminance(data, width, height)
        }
    }

    /**
     * Classifies a decoded QR payload into PromptPay, Wi-Fi, URL, vCard or text.
     */
    fun parseQrContent(raw: String): ParsedQrResult {
        val trimmed = raw.trim()

        if (trimmed.contains("000201") && trimmed.contains("A000000677010111")) {
            val parsed = PromptPayGenerator.parsePromptPay(trimmed)
            if (parsed != null) {
                val target = parsed.first
                val amount = parsed.second
                val amountText = if (amount != null) {
                    localizedNow(
                        "จำนวนเงิน: ฿${String.format("%,.2f", amount)}",
                        "Amount: ฿${String.format("%,.2f", amount)}"
                    )
                } else {
                    localizedNow("ไม่ระบุยอดเงิน", "Amount not specified")
                }
                return ParsedQrResult(
                    rawText = raw,
                    type = ParsedQrType.PROMPTPAY,
                    title = localizedNow("พร้อมเพย์", "PromptPay"),
                    subtitle = localizedNow(
                        "บัญชีรับเงิน: $target | $amountText",
                        "Payee: $target | $amountText"
                    ),
                    promptPayId = target,
                    amount = amount
                )
            }
        }

        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            var ssid = ""
            var pass = ""
            var sec = "WPA"

            trimmed.substring(5).split(";").forEach { part ->
                if (part.startsWith("S:")) ssid = part.substring(2)
                if (part.startsWith("P:")) pass = part.substring(2)
                if (part.startsWith("T:")) sec = part.substring(2)
            }

            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.WIFI,
                title = localizedNow("เครือข่าย Wi-Fi", "Wi-Fi network"),
                subtitle = localizedNow(
                    "ชื่อ: $ssid • ความปลอดภัย: $sec",
                    "SSID: $ssid • Security: $sec"
                ),
                wifiSsid = ssid,
                wifiPass = pass,
                wifiSecurity = sec
            )
        }

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
                title = localizedNow("นามบัตรดิจิทัล", "Contact card"),
                subtitle = listOfNotNull(
                    fn.ifBlank { null },
                    org.ifBlank { null },
                    tel.ifBlank { null }
                ).joinToString(" • "),
                contactName = fn,
                contactPhone = tel,
                contactEmail = email,
                contactOrg = org
            )
        }

        if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true)
        ) {
            val url =
                if (trimmed.startsWith("www.", ignoreCase = true)) "https://$trimmed"
                else trimmed
            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.URL,
                title = localizedNow("ลิงก์เว็บไซต์ / ร้านค้า", "Website / store link"),
                subtitle = url,
                url = url
            )
        }

        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4)
            return ParsedQrResult(
                rawText = raw,
                type = ParsedQrType.PHONE,
                title = localizedNow("เบอร์โทรศัพท์", "Phone number"),
                subtitle = phone,
                contactPhone = phone
            )
        }

        return ParsedQrResult(
            rawText = raw,
            type = ParsedQrType.TEXT,
            title = localizedNow("ข้อความทั่วไป", "Text"),
            subtitle = if (trimmed.length > 50) trimmed.take(50) + "..." else trimmed
        )
    }
}
