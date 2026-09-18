package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtil {

    /**
     * Generates a QR Code Bitmap from text with customizable colors.
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 800,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE,
        margin: Int = 2
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, margin)
            }

            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Builds a Wi-Fi QR payload string (standard WIFI URI format).
     */
    fun buildWifiPayload(
        ssid: String,
        pass: String,
        security: String = "WPA",
        isHidden: Boolean = false
    ): String {
        val escape = { s: String ->
            s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:")
                .replace("\"", "\\\"")
        }
        val safeSsid = escape(ssid)
        val safePass = escape(pass)
        val sec = if (security.equals("nopass", ignoreCase = true)) "nopass" else security
        return "WIFI:S:$safeSsid;T:$sec;P:$safePass;H:$isHidden;;"
    }

    /**
     * Builds a vCard 3.0 contact payload string with RFC escaping.
     */
    fun buildVCardPayload(
        fullName: String,
        org: String,
        title: String,
        phone: String,
        email: String,
        url: String,
        note: String
    ): String {
        val escapeVCard = { s: String ->
            s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "")
        }
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:${escapeVCard(fullName)}")
            if (org.isNotBlank()) appendLine("ORG:${escapeVCard(org)}")
            if (title.isNotBlank()) appendLine("TITLE:${escapeVCard(title)}")
            if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL:${phone.trim()}")
            if (email.isNotBlank()) appendLine("EMAIL:${email.trim()}")
            if (url.isNotBlank()) appendLine("URL:${url.trim()}")
            if (note.isNotBlank()) appendLine("NOTE:${escapeVCard(note)}")
            append("END:VCARD")
        }
    }

    /**
     * Creates a stylized PromptPay payment standee bitmap ready for sharing or printing.
     */
    fun createPromptPayStandeeBitmap(
        qrBitmap: Bitmap,
        title: String,
        targetId: String,
        amount: Double?,
        merchantName: String,
        backgroundColor: Int = Color.WHITE,
        headerColor: Int = Color.parseColor("#0B2853")
    ): Bitmap {
        val width = 1000
        val height = 1380
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Background: clean crisp card with user-selected background color
        canvas.drawColor(backgroundColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top Header Banner (Uses headerColor)
        paint.color = headerColor
        canvas.drawRect(0f, 0f, width.toFloat(), 240f, paint)

        // Thai QR Payment Header Text
        paint.color = Color.WHITE
        paint.textSize = 54f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("THAI QR PAYMENT", width / 2f, 105f, paint)

        paint.textSize = 34f
        paint.isFakeBoldText = false
        canvas.drawText("พร้อมเพย์ (PromptPay)", width / 2f, 175f, paint)

        // Check if background is dark or light to set text color
        val isDarkBg = androidx.core.graphics.ColorUtils.calculateLuminance(backgroundColor) < 0.5

        // Merchant Name or App Title
        paint.color = if (isDarkBg) Color.WHITE else Color.parseColor("#1E293B")
        paint.textSize = 42f
        paint.isFakeBoldText = true
        canvas.drawText(
            if (merchantName.isNotBlank()) merchantName else "สแกนเพื่อจ่ายเงิน",
            width / 2f,
            330f,
            paint
        )

        // QR Code Box with soft border
        val qrBoxSize = 640f
        val qrLeft = (width - qrBoxSize) / 2f
        val qrTop = 380f
        val qrRect = RectF(qrLeft, qrTop, qrLeft + qrBoxSize, qrTop + qrBoxSize)

        paint.color = if (isDarkBg) Color.parseColor("#27272A") else Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(qrRect, 28f, 28f, paint)

        // Draw soft outline around QR Box
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = if (isDarkBg) Color.parseColor("#52525B") else Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(qrRect, 28f, 28f, paint)
        paint.style = Paint.Style.FILL

        // Draw QR
        val qrInnerPadding = 30
        val destRect = Rect(
            (qrLeft + qrInnerPadding).toInt(),
            (qrTop + qrInnerPadding).toInt(),
            (qrLeft + qrBoxSize - qrInnerPadding).toInt(),
            (qrTop + qrBoxSize - qrInnerPadding).toInt()
        )
        canvas.drawBitmap(qrBitmap, null, destRect, null)

        // Target Info (Phone or ID)
        val infoY = qrTop + qrBoxSize + 80f
        paint.color = if (isDarkBg) Color.parseColor("#E2E8F0") else Color.parseColor("#334155")
        paint.textSize = 34f
        paint.isFakeBoldText = false
        val displayTarget = if (targetId.length == 10 && targetId.startsWith("0")) {
            "${targetId.substring(0, 3)}-${targetId.substring(3, 6)}-${targetId.substring(6)}"
        } else if (targetId.length == 13) {
            "${targetId.substring(0, 1)}-${targetId.substring(1, 5)}-${targetId.substring(5, 10)}-${targetId.substring(10, 12)}-${targetId.substring(12)}"
        } else {
            targetId
        }
        canvas.drawText("บัญชีพร้อมเพย์: $displayTarget", width / 2f, infoY, paint)

        // Amount if specified
        if (amount != null && amount > 0) {
            paint.color = Color.parseColor("#059669")
            paint.textSize = 58f
            paint.isFakeBoldText = true
            val amountFormatted = String.format("฿ %,.2f", amount)
            canvas.drawText(amountFormatted, width / 2f, infoY + 80f, paint)
        } else {
            paint.color = if (isDarkBg) Color.parseColor("#A1A1AA") else Color.parseColor("#64748B")
            paint.textSize = 32f
            canvas.drawText("ไม่ระบุยอดเงิน (ผู้โอนกรอกจำนวนเงินเอง)", width / 2f, infoY + 70f, paint)
        }

        // Bottom Footer
        paint.color = if (isDarkBg) Color.parseColor("#71717A") else Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        canvas.drawText("รองรับโมบายแบงก์กิ้งทุกธนาคารในไทย", width / 2f, height - 60f, paint)

        return result
    }
}
