package com.example

import com.example.model.ParsedQrType
import com.example.util.PromptPayGenerator
import com.example.util.QrScannerUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickQrCoreTest {

    @Test
    fun promptPayPhone_roundTripPreservesTargetAndAmount() {
        val payload = PromptPayGenerator.generatePayload("0812345678", 500.0)

        assertTrue(PromptPayGenerator.verifyCrc(payload))

        val parsed = PromptPayGenerator.parsePromptPay(payload)
        assertNotNull(parsed)
        assertEquals("0812345678", parsed?.first)
        assertEquals(500.0, parsed?.second ?: 0.0, 0.001)
    }

    @Test
    fun promptPayId_roundTripWithoutAmount() {
        val target = "1101700203451"
        val payload = PromptPayGenerator.generatePayload(target, null)

        assertTrue(PromptPayGenerator.verifyCrc(payload))

        val parsed = PromptPayGenerator.parsePromptPay(payload)
        assertNotNull(parsed)
        assertEquals(target, parsed?.first)
        assertEquals(null, parsed?.second)
    }

    @Test
    fun tamperedPayload_failsCrcValidation() {
        val payload = PromptPayGenerator.generatePayload("0812345678", 100.0)
        val tampered = payload.replace("100.00", "900.00")

        assertFalse(PromptPayGenerator.verifyCrc(tampered))
        assertEquals(null, PromptPayGenerator.parsePromptPay(tampered))
    }
    @Test
    fun scannerPromptPay_usesVerifiedPayloadAndExtractsFields() {
        val payload = PromptPayGenerator.generatePayload("0812345678", 250.0)

        val parsed = QrScannerUtil.parseQrContent(payload)

        assertEquals(ParsedQrType.PROMPTPAY, parsed.type)
        assertEquals("0812345678", parsed.promptPayId)
        assertEquals(250.0, parsed.amount ?: 0.0, 0.001)
    }

    @Test
    fun scannerWifi_extractsNetworkFields() {
        val parsed = QrScannerUtil.parseQrContent(
            "WIFI:T:WPA;S:MyShop;P:secret123;;"
        )

        assertEquals(ParsedQrType.WIFI, parsed.type)
        assertEquals("MyShop", parsed.wifiSsid)
        assertEquals("secret123", parsed.wifiPass)
        assertEquals("WPA", parsed.wifiSecurity)
    }

    @Test
    fun scannerWwwLink_normalizesToHttpsUrl() {
        val parsed = QrScannerUtil.parseQrContent("www.example.com/shop")

        assertEquals(ParsedQrType.URL, parsed.type)
        assertEquals("https://www.example.com/shop", parsed.url)
    }

    @Test
    fun scannerWifi_readsEscapedFieldsWithoutCreatingExtraFields() {
        val parsed = QrScannerUtil.parseQrContent(
            """WIFI:S:Shop\;T\:nopass;T:WPA;P:p\;ass\:word\,\"\\;;"""
        )
        assertEquals(ParsedQrType.WIFI, parsed.type)
        assertEquals("Shop;T:nopass", parsed.wifiSsid)
        assertEquals("WPA", parsed.wifiSecurity)
        assertEquals("p;ass:word,\"\\", parsed.wifiPass)
    }

    @Test
    fun scannerWifi_roundTripPreservesSpecialCharactersAndThai() {
        val ssid = "ร้าน;\\Wi-Fi,:\""
        val password = "p\\;:a,\"ss"
        val payload = com.example.util.QrCodeUtil.buildWifiPayload(ssid, password)
        val parsed = QrScannerUtil.parseQrContent(payload)
        assertEquals(ssid, parsed.wifiSsid)
        assertEquals(password, parsed.wifiPass)
        assertEquals("WPA", parsed.wifiSecurity)
    }

    @Test
    fun scannerWifi_handlesEscapedBackslashBeforeDelimiterAndOpenNetwork() {
        val parsed = QrScannerUtil.parseQrContent("""WIFI:S:Shop\\;T:nopass;P:;;""")
        assertEquals("Shop\\", parsed.wifiSsid)
        assertEquals("nopass", parsed.wifiSecurity)
        assertEquals("", parsed.wifiPass)
    }

    @Test
    fun scannerVCard_extractsContactFields() {
        val parsed = QrScannerUtil.parseQrContent(
            """
            BEGIN:VCARD
            VERSION:3.0
            FN:Example Shop
            ORG:QRprom
            TEL;TYPE=CELL:0812345678
            EMAIL:hello@example.com
            END:VCARD
            """.trimIndent()
        )

        assertEquals(ParsedQrType.VCARD, parsed.type)
        assertEquals("Example Shop", parsed.contactName)
        assertEquals("QRprom", parsed.contactOrg)
        assertEquals("0812345678", parsed.contactPhone)
        assertEquals("hello@example.com", parsed.contactEmail)
    }

    @Test
    fun promptPayPlus66_isAcceptedAndNormalizesToThaiLocalNumber() {
        val input = "+66812345678"
        assertEquals(com.example.util.ValidationResult.Valid, com.example.util.QrValidationUtil.validatePromptPayTarget(input))
        val payload = PromptPayGenerator.generatePayload(input, 150.0)
        val parsed = PromptPayGenerator.parsePromptPay(payload)
        assertNotNull(parsed)
        assertEquals("0812345678", parsed?.first)
        assertEquals(150.0, parsed?.second ?: 0.0, 0.001)
    }

    @Test
    fun promptPayShortPhone_isRejectedWithSpecificReason() {
        val result = com.example.util.QrValidationUtil.validatePromptPayTarget("0895469")
        assertTrue(result is com.example.util.ValidationResult.Invalid)
        val reason = (result as com.example.util.ValidationResult.Invalid).reason
        assertTrue(reason.contains("10 หลัก"))
    }

    @Test
    fun locationQr_roundTripUsesRealCoordinateFormat() {
        val point = com.example.model.GeoPoint(14.056637, 99.805625)
        val payload = com.example.util.LocationQrUtil.buildGeoPayload(point)
        val parsed = com.example.util.LocationQrUtil.parseGeo(payload)

        assertEquals(point.latitude, parsed.latitude, 0.0000001)
        assertEquals(point.longitude, parsed.longitude, 0.0000001)
        assertEquals(ParsedQrType.GEO, QrScannerUtil.parseQrContent(payload).type)
    }

    @Test
    fun locationQr_rejectsPlaceholderAndOutOfRangeCoordinates() {
        assertFalse(com.example.model.GeoPoint(0.0, 0.0).isValid())
        assertEquals(null, com.example.util.LocationQrUtil.parseGeoOrNull("geo:0,0"))
        assertEquals(null, com.example.util.LocationQrUtil.parseGeoOrNull("geo:91,100"))
        assertEquals(null, com.example.util.LocationQrUtil.parseGeoOrNull("geo:13,181"))
    }

}
