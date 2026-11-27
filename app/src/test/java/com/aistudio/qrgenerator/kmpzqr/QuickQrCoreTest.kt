package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.model.ParsedQrType
import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil
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
        val target = "1101700203450"
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
        val payload = com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil.buildWifiPayload(ssid, password)
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
            ORG:QuickQR Business
            TEL;TYPE=CELL:0812345678
            EMAIL:hello@example.com
            END:VCARD
            """.trimIndent()
        )

        assertEquals(ParsedQrType.VCARD, parsed.type)
        assertEquals("Example Shop", parsed.contactName)
        assertEquals("QuickQR Business", parsed.contactOrg)
        assertEquals("0812345678", parsed.contactPhone)
        assertEquals("hello@example.com", parsed.contactEmail)
    }

    @Test
    fun promptPayPlus66_isAcceptedAndNormalizesToThaiLocalNumber() {
        val input = "+66812345678"
        assertEquals(com.aistudio.qrgenerator.kmpzqr.util.ValidationResult.Valid, com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil.validatePromptPayTarget(input))
        val payload = PromptPayGenerator.generatePayload(input, 150.0)
        val parsed = PromptPayGenerator.parsePromptPay(payload)
        assertNotNull(parsed)
        assertEquals("0812345678", parsed?.first)
        assertEquals(150.0, parsed?.second ?: 0.0, 0.001)
    }

    @Test
    fun promptPayShortPhone_isRejectedWithSpecificReason() {
        val result = com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil.validatePromptPayTarget("0895469")
        assertTrue(result is com.aistudio.qrgenerator.kmpzqr.util.ValidationResult.Invalid)
        val invalid = result as com.aistudio.qrgenerator.kmpzqr.util.ValidationResult.Invalid
        assertTrue(invalid.reasonTh.contains("10 หลัก"))
        assertTrue(invalid.reasonEn.contains("10-digit"))
    }

    @Test
    fun locationQr_roundTripUsesRealCoordinateFormat() {
        val point = com.aistudio.qrgenerator.kmpzqr.model.GeoPoint(14.056637, 99.805625)
        val payload = com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.buildGeoPayload(point)
        val parsed = com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeo(payload)

        assertEquals(point.latitude, parsed.latitude, 0.0000001)
        assertEquals(point.longitude, parsed.longitude, 0.0000001)
        assertEquals(ParsedQrType.GEO, QrScannerUtil.parseQrContent(payload).type)
    }

    @Test
    fun locationQr_rejectsPlaceholderAndOutOfRangeCoordinates() {
        assertFalse(com.aistudio.qrgenerator.kmpzqr.model.GeoPoint(0.0, 0.0).isValid())
        assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull("geo:0,0"))
        assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull("geo:91,100"))
        assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull("geo:13,181"))
    }

    @Test
    fun mapsUrl_readsExplicitLocationQueryParameters() {
        val url = "https://www.google.com/maps/search/?api=1&query=13.7563%2C100.5018"
        val point = com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(url)
        assertNotNull(point)
        assertEquals(13.7563, point!!.latitude, 0.0000001)
        assertEquals(100.5018, point.longitude, 0.0000001)
        assertEquals(ParsedQrType.GEO, QrScannerUtil.parseQrContent(url).type)
    }

    @Test
    fun mapsUrl_doesNotUseUnrelatedQueryOrRouteOriginAsLocation() {
        val url = "https://www.google.com/maps?campaign=13.7563%2C100.5018&origin=14.0%2C101.0"
        assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(url))
        assertEquals(ParsedQrType.URL, QrScannerUtil.parseQrContent(url).type)
    }

    @Test
    fun mapsDirections_usesDestinationCoordinatesNotOrigin() {
        val url = "https://www.google.com/maps/dir/?api=1&origin=14.0%2C101.0&destination=13.7563%2C100.5018"
        val point = com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(url)
        assertNotNull(point)
        assertEquals(13.7563, point!!.latitude, 0.0000001)
        assertEquals(100.5018, point.longitude, 0.0000001)
    }

    @Test
    fun mapsUrl_rejectsNonWebSchemesEvenWithGoogleHost() {
        val raw = "customscheme://www.google.com/maps/@13.7563,100.5018"
        assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(raw))
    }

    @Test
    fun luminancePlane_respectsBufferPositionAndStridesAcrossLayouts() {
        val random = kotlin.random.Random(20261127)
        for (width in 1..16) for (height in 1..12) {
            for (pixelStride in 1..4) for (padding in 0..3) {
                val span = (width - 1) * pixelStride + 1
                val rowStride = span + padding
                val prefix = random.nextInt(0, 8)
                val bytes = ByteArray(prefix + (height - 1) * rowStride + span) {
                    random.nextInt().toByte()
                }
                val source = java.nio.ByteBuffer.wrap(bytes)
                source.position(prefix)
                val expected = ByteArray(width * height) { index ->
                    bytes[prefix + (index / width) * rowStride + (index % width) * pixelStride]
                }
                val actual = QrScannerUtil.copyLuminancePlane(
                    source, width, height, rowStride, pixelStride
                )
                org.junit.Assert.assertArrayEquals(expected, actual)
                assertEquals(prefix, source.position())
                assertEquals(bytes.size, source.limit())
            }
        }
    }

    @Test
    fun luminancePlane_rejectsTruncationInvalidStrideAndExcessiveDimensions() {
        val invalid = listOf(
            intArrayOf(2, 2, 2, 1), intArrayOf(2, 2, 1, 1),
            intArrayOf(0, 1, 1, 1), intArrayOf(1, -1, 1, 1),
            intArrayOf(1, 1, 1, 0),
            intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
            intArrayOf(4097, 4096, 4097, 1),
            intArrayOf(1, 2, Int.MAX_VALUE, 1)
        )
        for (layout in invalid) {
            val error = runCatching {
                QrScannerUtil.copyLuminancePlane(
                    java.nio.ByteBuffer.allocate(3),
                    layout[0], layout[1], layout[2], layout[3]
                )
            }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun mapsUrl_abstainsOnConflictingOrInvalidExplicitCoordinates() {
        val base = "https://www.google.com/maps"
        for (suffix in listOf(
            "?q=13.7563,100.5018&query=14,101",
            "?q=13.7563,100.5018&q=invalid",
            "/dir/?destination=13.7563,100.5018&daddr=14,101",
            "/dir/?center=14,101&origin=14,101",
            "/dir/@14,101",
            "/dir/?destination=91,101"
        )) {
            val url = base + suffix
            assertEquals(null, com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(url))
            assertEquals(ParsedQrType.URL, QrScannerUtil.parseQrContent(url).type)
        }
    }

    @Test
    fun mapsUrl_usesUniqueDestinationAndAllowsIdenticalDuplicateCoordinates() {
        val base = "https://www.google.com/maps"
        for (suffix in listOf(
            "?q=13.7563,100.5018&q=13.7563,100.5018",
            "/dir/?q=14,101&destination=13.7563,100.5018"
        )) {
            val point = com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil.parseGeoOrNull(base + suffix)
            assertEquals(com.aistudio.qrgenerator.kmpzqr.model.GeoPoint(13.7563, 100.5018), point)
        }
    }

    @Test
    fun qrPayloadVerification_requiresExactDecodedContent() {
        val exact = QrValidationUtil.verifyDecodedContent("expected", "expected")
        assertTrue(exact.isValid)
        assertEquals("expected", exact.decodedContent)

        val mismatch = QrValidationUtil.verifyDecodedContent("different", "expected")
        assertFalse(mismatch.isValid)
        assertEquals("different", mismatch.decodedContent)
        assertTrue(mismatch.errorMessage?.contains("ไม่ตรง") == true)

        val unreadable = QrValidationUtil.verifyDecodedContent(null, "expected")
        assertFalse(unreadable.isValid)
        assertEquals(null, unreadable.decodedContent)
    }
}
