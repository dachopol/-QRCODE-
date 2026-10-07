package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PromptPayParseValidationTest {
    @Test
    fun parsePromptPay_acceptsValidPhoneAndId() {
        val phonePayload = PromptPayGenerator.generatePayload("0812345678", 10.0)
        val idPayload = PromptPayGenerator.generatePayload("1101700203451", 20.0)

        assertNotNull(PromptPayGenerator.parsePromptPay(phonePayload))
        assertNotNull(PromptPayGenerator.parsePromptPay(idPayload))
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
}
