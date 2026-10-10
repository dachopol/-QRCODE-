package com.aistudio.qrgenerator.kmpzqr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.GalleryBitmapDecoder
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryBitmapDecoderInstrumentedTest {
    @Test
    fun largeGalleryImage_isSampledAndStillDecodesQr() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val payload = "QUICKQR_GALLERY_LARGE_IMAGE_REGRESSION"
        val qr = requireNotNull(
            QrCodeUtil.generateQrBitmap(
                content = payload,
                size = 1200,
                centerLogo = null
            )
        )

        val large = Bitmap.createBitmap(2560, 2560, Bitmap.Config.ARGB_8888)
        Canvas(large).apply {
            drawColor(Color.WHITE)
            drawBitmap(qr, 680f, 680f, null)
        }

        val file = File(context.cacheDir, "gallery-large-regression.png")
        FileOutputStream(file).use { stream ->
            assertTrue(large.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        large.recycle()
        qr.recycle()

        val decodedBitmap = GalleryBitmapDecoder.decode(
            resolver = context.contentResolver,
            uri = Uri.fromFile(file)
        )

        assertNotNull(decodedBitmap)
        decodedBitmap!!
        assertTrue(decodedBitmap.width <= GalleryBitmapDecoder.MAX_DECODE_DIMENSION)
        assertTrue(decodedBitmap.height <= GalleryBitmapDecoder.MAX_DECODE_DIMENSION)
        assertEquals(payload, QrScannerUtil.decodeBitmap(decodedBitmap))

        decodedBitmap.recycle()
        file.delete()
    }
}
