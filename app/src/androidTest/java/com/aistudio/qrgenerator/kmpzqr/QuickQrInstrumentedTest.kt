package com.aistudio.qrgenerator.kmpzqr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil
import com.aistudio.qrgenerator.kmpzqr.util.ValidationResult
import com.aistudio.qrgenerator.kmpzqr.util.localizedNow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
  @Test
  fun generatedQrVerification_passesOnlyForExactPayload() {
    val actualContent = "QRBUSINESS_EXACT_PAYLOAD"
    val logo = Bitmap.createBitmap(220, 90, Bitmap.Config.ARGB_8888).apply {
      eraseColor(Color.rgb(20, 100, 180))
    }
    val qr = QrCodeUtil.generateQrBitmap(
      content = actualContent,
      centerLogo = logo
    )

    assertNotNull(qr)

    val exact = QrValidationUtil.verifyGeneratedQrBitmap(
      bitmap = qr!!,
      expectedContent = actualContent
    )
    assertTrue(exact.isValid)
    assertEquals(actualContent, exact.decodedContent)

    val mismatch = QrValidationUtil.verifyGeneratedQrBitmap(
      bitmap = qr,
      expectedContent = "QRBUSINESS_DIFFERENT_PAYLOAD"
    )
    assertFalse(mismatch.isValid)
    assertEquals(actualContent, mismatch.decodedContent)
  }

  @Test
  fun promptPayValidationMessage_followsSelectedLanguage() {
    val invalid = QrValidationUtil.validatePromptPayTarget("0895469") as ValidationResult.Invalid
    val originalLanguage = LocalizationManager.currentLanguage.value.code
    try {
      LocalizationManager.setLanguageByCode("en")
      assertEquals(invalid.reasonEn, localizedNow(invalid.reasonTh, invalid.reasonEn))

      LocalizationManager.setLanguageByCode("th")
      assertEquals(invalid.reasonTh, localizedNow(invalid.reasonTh, invalid.reasonEn))
    } finally {
      LocalizationManager.setLanguageByCode(originalLanguage)
    }
  }

}
