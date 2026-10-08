package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.model.ParsedQrType
import com.aistudio.qrgenerator.kmpzqr.util.QrCodeUtil
import com.aistudio.qrgenerator.kmpzqr.util.QrScannerUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class VCardRoundTripTest {
    @Test
    fun generatedVCard_roundTripPreservesEscapedText() {
        val fullName = "ร้าน,หลัก;สาขา\\เหนือ\nชั้น 2"
        val organization = "QuickQR, Business;TH\\Ops"
        val phone = "0812345678"
        val email = "hello@example.com"

        val payload = QrCodeUtil.buildVCardPayload(
            fullName = fullName,
            org = organization,
            title = "",
            phone = phone,
            email = email,
            url = "",
            note = ""
        )
        val parsed = QrScannerUtil.parseQrContent(payload)

        assertEquals(ParsedQrType.VCARD, parsed.type)
        assertEquals(fullName, parsed.contactName)
        assertEquals(organization, parsed.contactOrg)
        assertEquals(phone, parsed.contactPhone)
        assertEquals(email, parsed.contactEmail)
    }

    @Test
    fun unknownVCardEscape_isPreservedLiterally() {
        val payload = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Alpha\\xBeta
            END:VCARD
        """.trimIndent()

        val parsed = QrScannerUtil.parseQrContent(payload)

        assertEquals("Alpha\\xBeta", parsed.contactName)
    }
}
