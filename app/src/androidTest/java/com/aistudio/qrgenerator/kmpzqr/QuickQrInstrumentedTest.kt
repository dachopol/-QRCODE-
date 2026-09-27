package com.aistudio.qrgenerator.kmpzqr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuickQrInstrumentedTest {
  @Test
  fun useAppContext() {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
  }

  @Test
  fun customCenterLogoQrDecodesToOriginalContent() {
    val content = "QRBUSINESS_CENTER_LOGO_E2E_20260927"
    val logo = Bitmap.createBitmap(300, 120, Bitmap.Config.ARGB_8888).apply {
      eraseColor(Color.rgb(32, 96, 180))
    }

    val qr = QrCodeUtil.generateQrBitmap(
      content = content,
      centerLogo = logo
    )

    assertNotNull(qr)
    assertEquals(content, QrScannerUtil.decodeBitmap(qr!!))
  }
}
