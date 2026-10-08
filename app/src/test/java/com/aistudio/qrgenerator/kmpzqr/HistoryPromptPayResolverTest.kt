package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity
import com.aistudio.qrgenerator.kmpzqr.util.HistoryPromptPayResolver
import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryPromptPayResolverTest {
    @Test
    fun savedRawPayloadWinsOverStaleDuplicatedMetadata() {
        val payload = PromptPayGenerator.generatePayload("0812345678", 123.45)
        val item = QrItemEntity(
            type = "PROMPTPAY",
            title = "stale title",
            subtitle = "stale subtitle",
            rawContent = payload,
            targetId = "0899999999",
            amount = 999.0
        )

        val resolved = HistoryPromptPayResolver.resolve(item)

        assertEquals(payload, resolved?.payload)
        assertEquals("0812345678", resolved?.target)
        assertEquals(123.45, resolved?.amount ?: 0.0, 0.001)
    }

    @Test
    fun invalidSavedPayloadFailsClosed() {
        val item = QrItemEntity(
            type = "PROMPTPAY",
            title = "PromptPay",
            subtitle = "invalid",
            rawContent = "not-a-valid-promptpay-payload",
            targetId = "0812345678",
            amount = 50.0
        )

        assertNull(HistoryPromptPayResolver.resolve(item))
    }
}
