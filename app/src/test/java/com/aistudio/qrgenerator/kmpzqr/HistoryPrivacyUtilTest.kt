package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.HistoryPrivacyUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryPrivacyUtilTest {
    @Test
    fun wifiHistorySubtitle_neverIncludesPlaintextPassword() {
        val secret = "secret123"
        val english = HistoryPrivacyUtil.wifiHistorySubtitle(hasPassword = true, languageCode = "en")
        val thai = HistoryPrivacyUtil.wifiHistorySubtitle(hasPassword = true, languageCode = "th")

        assertFalse(english.contains(secret))
        assertFalse(thai.contains(secret))
        assertTrue(english.contains("hidden", ignoreCase = true))
        assertTrue(thai.contains("ซ่อน"))
    }

    @Test
    fun wifiHistorySubtitle_handlesOpenNetworkWithoutClaimingPassword() {
        val english = HistoryPrivacyUtil.wifiHistorySubtitle(hasPassword = false, languageCode = "en")
        val thai = HistoryPrivacyUtil.wifiHistorySubtitle(hasPassword = false, languageCode = "th")

        assertTrue(english.contains("No password"))
        assertTrue(thai.contains("ไม่มีรหัสผ่าน"))
    }
}
