package com.aistudio.qrgenerator.kmpzqr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCenterImageScanTest {

    @Test
    fun customCenterImageQr_decodesOriginalContent() {
        val content = "https://example.com/quickqr-center-image-qa"
        val logo = Bitmap.createBitmap(160, 96, Bitmap.Config.ARGB_8888)
        Canvas(logo).apply {
            drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 118, 110)
            }
            drawRoundRect(8f, 8f, 152f, 88f, 16f, 16f, paint)
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 34f
            paint.isFakeBoldText = true
            drawText("QA", 80f, 60f, paint)
        }

        val qr = QrCodeUtil.generateQrBitmap(
            content = content,
            size = 512,
            centerLogo = logo
        )
        assertNotNull(qr)

        val bitmap = requireNotNull(qr)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )

        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val decoded = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)))

        assertEquals(content, decoded.text)
    }
}
