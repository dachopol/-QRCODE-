package com.example

import com.example.model.ParsedQrType
import com.example.util.PromptPayGenerator
import com.example.util.QrScannerUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

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

}
