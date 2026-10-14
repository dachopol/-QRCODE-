package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PromptPayParseValidationTest {
    @Test
    fun parsePromptPay_acceptsValidPhoneAndId() {
        val phonePayload = PromptPayGenerator.generatePayload("0812345678", 10.0)
        val idPayload = PromptPayGenerator.generatePayload("1101700203450", 20.0)

        assertNotNull(PromptPayGenerator.parsePromptPay(phonePayload))
        assertNotNull(PromptPayGenerator.parsePromptPay(idPayload))
    }

    @Test
    fun parsePromptPay_acceptsMissingAmountAsUnspecified() {
        val payload = PromptPayGenerator.generatePayload("0812345678", null)

        assertEquals(null, PromptPayGenerator.parsePromptPay(payload)?.second)
    }

    @Test
    fun parsePromptPay_rejectsInvalidPhoneEvenWithValidCrc() {
        val invalidPhonePayload = PromptPayGenerator.generatePayload("0012345678", 10.0)

        assertEquals(null, PromptPayGenerator.parsePromptPay(invalidPhonePayload))
    }

    @Test
    fun parsePromptPay_rejectsInvalidThaiIdChecksumEvenWithValidCrc() {
        val invalidIdPayload = PromptPayGenerator.generatePayload("1111111111111", 10.0)

        assertEquals(null, PromptPayGenerator.parsePromptPay(invalidIdPayload))
    }

    @Test
    fun parsePromptPay_rejectsInvalidAmountTagEvenWithValidCrc() {
        listOf("0", "-1", "NaN", "Infinity", "not-a-number").forEach { invalidAmount ->
            val payload = payloadWithAmountTag(invalidAmount)
            assertEquals(
                "amount=$invalidAmount",
                null,
                PromptPayGenerator.parsePromptPay(payload)
            )
        }
    }

    private fun payloadWithAmountTag(amountText: String): String {
        require(amountText.length <= 99)
        val base = PromptPayGenerator.generatePayload("0812345678", null)
        val bodyWithoutCrc = base.substringBeforeLast("6304")
        val countryTag = "5802TH"
        val countryIndex = bodyWithoutCrc.indexOf(countryTag)
        require(countryIndex >= 0)

        val amountTag = "54" + amountText.length.toString().padStart(2, '0') + amountText
        val bodyWithAmount = buildString {
            append(bodyWithoutCrc.substring(0, countryIndex))
            append(amountTag)
            append(bodyWithoutCrc.substring(countryIndex))
        }
        val beforeCrc = bodyWithAmount + "6304"
        return beforeCrc + PromptPayGenerator.crc16Ccitt(beforeCrc)
    }
}
