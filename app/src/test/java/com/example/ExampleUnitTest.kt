package com.example

import com.example.util.PromptPayGenerator
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
}
